package com.xiaoxuan.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.xiaoxuan.myapplication.R;
import com.xiaoxuan.myapplication.model.City;
import com.xiaoxuan.myapplication.model.db.DatabaseHelper;
import com.xiaoxuan.myapplication.util.CityTools;

import java.util.List;
import java.util.Map;

/**
 * 全部城市
 * Created by xiaoxuan on 2017/1/18 0018.
 */

public class ListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<City> list;
    private List<City> hotList;
    private List<String> hisCity;
    final int VIEW_TYPE = 5;
    private Map<String, Integer> alphaIndexer;
    private String[] sections;
    private int locateProcess;
    private String currentCity;
    private LocationClient mLocationClient;

    public ListAdapter(Context context, List<City> list, List<City> hotList, List<String> hisCity,
                       Map<String, Integer> alphaIndexer, int locateProcess, String currentCity, LocationClient mLocationClient) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
        this.hotList = hotList;
        this.hisCity = hisCity;
        this.alphaIndexer = alphaIndexer;
        this.locateProcess = locateProcess;
        this.currentCity = currentCity;
        this.mLocationClient = mLocationClient;
        sections = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            // 当前汉语拼音首字母
            String currentStr = CityTools.getAlpha(list.get(i).getPinyi());
            // 上一个汉语拼音首字母，如果不存在为" "
            String previewStr = (i - 1) >= 0 ? CityTools.getAlpha(list.get(i - 1)
                    .getPinyi()) : " ";
            if (!previewStr.equals(currentStr)) {
                String name = CityTools.getAlpha(list.get(i).getPinyi());
                alphaIndexer.put(name, i);
                sections[i] = name;
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE;
    }

    @Override
    public int getItemViewType(int position) {
        return position < 4 ? position : 4;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final TextView city;
        int viewType = getItemViewType(position);
        if (viewType == 0) { // 定位
            convertView = inflater.inflate(R.layout.first_list_item, null);
            TextView locateHint = (TextView) convertView
                    .findViewById(R.id.locateHint);
            ProgressBar pbLocate = (ProgressBar) convertView.findViewById(R.id.pbLocate);
            city = (TextView) convertView.findViewById(R.id.lng_city);
            city.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (locateProcess == 2) {
                        Toast.makeText(context,
                                city.getText().toString(),
                                Toast.LENGTH_SHORT).show();
                    } else if (locateProcess == 3) {
                        locateProcess = 1;
                        currentCity = "";
                        if (mLocationClient != null && !mLocationClient.isStarted()) {
                            mLocationClient.start();
                            mLocationClient.requestLocation();
                        }
                    }
                }
            });
            if (locateProcess == 1) { // 正在定位
                locateHint.setText("正在定位");
                city.setVisibility(View.GONE);
                pbLocate.setVisibility(View.VISIBLE);//显示进度条
            } else if (locateProcess == 2) { // 定位成功
                locateHint.setText("当前定位城市");
                city.setVisibility(View.VISIBLE);
                city.setText(currentCity);
                if (mLocationClient != null && mLocationClient.isStarted()){
                    mLocationClient.stop();
                }
                pbLocate.setVisibility(View.GONE);//隐藏进度条
            } else if (locateProcess == 3) {
                locateHint.setText("未定位到城市,请选择");
                city.setVisibility(View.VISIBLE);
                city.setText("重新选择");
                pbLocate.setVisibility(View.GONE);
            }
        } else if (viewType == 1) { // 最近访问城市
            convertView = inflater.inflate(R.layout.recent_city, null);
            GridView rencentCity = (GridView) convertView
                    .findViewById(R.id.recent_city);
            rencentCity
                    .setAdapter(new HisCityAdapter(context, this.hisCity));
            rencentCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Toast.makeText(context,
                            hisCity.get(position), Toast.LENGTH_SHORT)
                            .show();

                }
            });
            TextView recentHint = (TextView) convertView
                    .findViewById(R.id.recentHint);
            recentHint.setText("最近访问的城市");
        } else if (viewType == 2) { //热门城市
            convertView = inflater.inflate(R.layout.recent_city, null);
            GridView hotCity = (GridView) convertView
                    .findViewById(R.id.recent_city);
            hotCity.setAdapter(new HotCityAdapter(context, this.hotList));
            hotCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Toast.makeText(context,
                            hotList.get(position).getName(),
                            Toast.LENGTH_SHORT).show();
                    DatabaseHelper helper = new DatabaseHelper(context);
                    CityTools.insertCity(helper, hotList.get(position).getName());
                }
            });
            TextView hotHint = (TextView) convertView
                    .findViewById(R.id.recentHint);
            hotHint.setText("热门城市");
        } else if (viewType == 3) { //全部城市
            convertView = inflater.inflate(R.layout.total_item, null);
        } else {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.alpha = (TextView) convertView
                        .findViewById(R.id.alpha);
                holder.name = (TextView) convertView
                        .findViewById(R.id.name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position >= 1) {
                holder.name.setText(list.get(position).getName());
                String currentStr = CityTools.getAlpha(list.get(position).getPinyi());
                String previewStr = (position - 1) >= 0 ? CityTools.getAlpha(list
                        .get(position - 1).getPinyi()) : " ";
                if (!previewStr.equals(currentStr)) {
                    holder.alpha.setVisibility(View.VISIBLE);
                    holder.alpha.setText(currentStr);
                } else {
                    holder.alpha.setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }


    private class ViewHolder {
        TextView alpha; // 首字母标题
        TextView name; // 城市名字
    }


}
