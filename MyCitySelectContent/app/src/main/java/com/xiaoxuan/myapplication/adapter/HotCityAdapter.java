package com.xiaoxuan.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaoxuan.myapplication.R;
import com.xiaoxuan.myapplication.model.City;

import java.util.List;

/**
 * 热门城市
 * Created by xiaoxuan on 2017/1/15 0015.
 */

public class HotCityAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<City> hotCitys;

    public HotCityAdapter(Context context, List<City> hotCitys) {
        this.context = context;
        this.hotCitys = hotCitys;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return hotCitys.size();
    }

    @Override
    public Object getItem(int position) {
        return hotCitys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_city, null);
        TextView city = (TextView) convertView.findViewById(R.id.city);
        city.setText(hotCitys.get(position).getName());
        return convertView;
    }
}
