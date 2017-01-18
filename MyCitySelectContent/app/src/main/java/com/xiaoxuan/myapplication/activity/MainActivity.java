package com.xiaoxuan.myapplication.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.xiaoxuan.myapplication.R;
import com.xiaoxuan.myapplication.adapter.ListAdapter;
import com.xiaoxuan.myapplication.adapter.ResultListAdapter;
import com.xiaoxuan.myapplication.model.City;
import com.xiaoxuan.myapplication.model.db.DatabaseHelper;
import com.xiaoxuan.myapplication.util.CityTools;
import com.xiaoxuan.myapplication.view.MyLetterListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by xiaoxuan on 2017/1/17 0017.
 */

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.edit_search)
    EditText editSearch;
    @Bind(R.id.list_view)
    ListView personList;
    @Bind(R.id.search_result)
    ListView resultList;
    @Bind(R.id.tv_noresult)
    TextView tv_noresult;
    @Bind(R.id.MyLetterListView01)
    MyLetterListView letterListView;

    private ListAdapter adapter;
    private ResultListAdapter resultListAdapter;
    private HashMap<String, Integer> alphaIndexer = new HashMap<>();// 存放存在的汉语拼音首字母和与之对应的列表位置
    private List<City> allCity_lists = new ArrayList<>(); // 所有城市列表
    private List<City> city_lists = new ArrayList<>();// 城市列表
    private List<City> city_hot = new ArrayList<>();
    private List<City> city_result = new ArrayList<>();
    private List<String> city_history = new ArrayList<>();

    private LocationClient mLocationClient;

    private String currentCity; // 用于保存定位到的城市
    private int locateProcess = 1; // 记录当前定位的状态 正在定位-定位成功-定位失败

    private DatabaseHelper helper;

    private final static int PERMISSIONS_REQUEST_FINE_LOCATE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {

        helper = new DatabaseHelper(this);

        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(bdLocationListener);

        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
        editSearch.addTextChangedListener(watcher);

        resultListAdapter = new ResultListAdapter(this, city_result);
        resultList.setAdapter(resultListAdapter);
        resultList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        city_result.get(position).getName(), Toast.LENGTH_SHORT)
                        .show();
                CityTools.insertCity(helper, city_result.get(position).getName());
            }
        });

        city_hot = CityTools.initHotCity(city_hot);
        city_history = CityTools.initHisCity(helper);
        city_lists = CityTools.getCityList(this);
        allCity_lists = CityTools.initCity(city_lists);

        adapter = new ListAdapter(this, allCity_lists, city_hot, city_history, alphaIndexer, locateProcess, currentCity, mLocationClient);
        personList.setAdapter(adapter);

        personList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position >= 4) {

                    Toast.makeText(getApplicationContext(),
                            allCity_lists.get(position).getName(),
                            Toast.LENGTH_SHORT).show();
                    CityTools.insertCity(helper, allCity_lists.get(position).getName());//插入最近访问城市
                }
            }
        });

        InitLocation();//开启定位
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    /**
     * 开启定位
     */
    private void InitLocation() {
        //开启定位
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll");// 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);//必须填此类型，不然获取城市地址为空
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    /**
     * 实现实位回调监听
     */
    private BDLocationListener bdLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (167 == location.getLocType()) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_FINE_LOCATE);
            }
            if (location == null) {
                return;
            }
            if (location.getCity() == null) {
                locateProcess = 3; // 定位失败
                adapter = new ListAdapter(MainActivity.this, allCity_lists, city_hot, city_history, alphaIndexer, locateProcess, currentCity, mLocationClient);
                personList.setAdapter(adapter);
                return;
            }

            currentCity = location.getCity().substring(0, location.getCity().length() - 1);
            locateProcess = 2; // 定位成功
            adapter = new ListAdapter(MainActivity.this, allCity_lists, city_hot, city_history, alphaIndexer, locateProcess, currentCity, mLocationClient);
            personList.setAdapter(adapter);
        }
    };

    /**
     * 侧边栏拼音
     */
    private class LetterListViewListener implements
            MyLetterListView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                personList.setSelection(position);
            }
        }
    }

    /**
     *  搜索监听引擎
     */
    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString() == null || "".equals(charSequence.toString())) {
                personList.setVisibility(View.VISIBLE);
                resultList.setVisibility(View.GONE);
                tv_noresult.setVisibility(View.GONE);
            } else {
                city_result.clear();
                personList.setVisibility(View.GONE);
                city_result = CityTools.getResultCityList(MainActivity.this, charSequence.toString());
                if (city_result.size() <= 0) {
                    tv_noresult.setVisibility(View.VISIBLE);
                    resultList.setVisibility(View.GONE);
                } else {
                    tv_noresult.setVisibility(View.GONE);
                    resultList.setVisibility(View.VISIBLE);
                    resultListAdapter = new ResultListAdapter(MainActivity.this, city_result);
                    resultList.setAdapter(resultListAdapter);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

}
