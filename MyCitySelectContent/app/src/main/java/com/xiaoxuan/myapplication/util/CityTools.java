package com.xiaoxuan.myapplication.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xiaoxuan.myapplication.model.City;
import com.xiaoxuan.myapplication.model.db.DBHelper;
import com.xiaoxuan.myapplication.model.db.DatabaseHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by xiaoxuan on 2017/1/8 0008.
 */

public class CityTools {

    /**
     * 初始化城市
     */
    public static List<City> initCity(List<City> city_lists) {
        List<City> allCities = new ArrayList<>();
        City city = new City("定位", "0"); // 当前定位城市
        allCities.add(city);
        city = new City("最近", "1"); // 最近访问的城市
        allCities.add(city);
        city = new City("热门", "2"); // 热门城市
        allCities.add(city);
        city = new City("全部", "3"); // 全部城市
        allCities.add(city);
        allCities.addAll(city_lists);
        return allCities;
    }

    /**
     * 插入最近访问城市
     */
    public static void insertCity(DatabaseHelper helper, String name) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from recentcity where name = '"
                + name + "'", null);
        if (cursor.getCount() > 0) { //
            db.delete("recentcity", "name = ?", new String[]{name});
        }
        db.execSQL("insert into recentcity(name, date) values('" + name + "', "
                + System.currentTimeMillis() + ")");
        db.close();
    }

    /**
     * 热门城市
     */
    public static List<City> initHotCity(List<City> hotCities) {
        City city = new City("上海", "2");
        hotCities.add(city);
        city = new City("北京", "2");
        hotCities.add(city);
        city = new City("广州", "2");
        hotCities.add(city);
        city = new City("深圳", "2");
        hotCities.add(city);
        city = new City("武汉", "2");
        hotCities.add(city);
        city = new City("天津", "2");
        hotCities.add(city);
        city = new City("西安", "2");
        hotCities.add(city);
        city = new City("南京", "2");
        hotCities.add(city);
        city = new City("杭州", "2");
        hotCities.add(city);
        city = new City("成都", "2");
        hotCities.add(city);
        city = new City("重庆", "2");
        hotCities.add(city);
        return hotCities;
    }

    public static List<String> initHisCity(DatabaseHelper helper) {
        List<String> city_history = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from recentcity order by date desc limit 0, 3", null);
        while (cursor.moveToNext()) {
            city_history.add(cursor.getString(1));
        }
        cursor.close();
        db.close();
        return city_history;
    }

    /**
     * 获取城市列表
     */
    @SuppressWarnings("unchecked")
    public static List<City> getCityList(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        ArrayList<City> list = new ArrayList<City>();
        try {
            dbHelper.createDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select * from city", null);
            City city;
            while (cursor.moveToNext()) {
                city = new City(cursor.getString(1), cursor.getString(2));
                list.add(city);
            }
            cursor.close();
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(list, comparator);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<City> getResultCityList(Context context,String keyword) {
        List<City> city_result = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(context);
        try {
            dbHelper.createDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery(
                    "select * from city where name like \"%" + keyword
                            + "%\" or pinyin like \"%" + keyword + "%\"", null);
            City city;
            Log.e("info", "length = " + cursor.getCount());
            while (cursor.moveToNext()) {
                city = new City(cursor.getString(1), cursor.getString(2));
                city_result.add(city);
            }
            cursor.close();
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(city_result, comparator);
        return city_result;
    }

    /**
     * a-z排序
     */
    @SuppressWarnings("rawtypes")
    protected static Comparator comparator = new Comparator<City>() {
        @Override
        public int compare(City lhs, City rhs) {
            String a = lhs.getPinyi().substring(0, 1);
            String b = rhs.getPinyi().substring(0, 1);
            int flag = a.compareTo(b);
            if (flag == 0) {
                return a.compareTo(b);
            } else {
                return flag;
            }
        }
    };

    /**
     * 获得汉语拼音首字母
     */
    public static String getAlpha(String str) {
        if (str == null) {
            return "#";
        }
        if (str.trim().length() == 0) {
            return "#";
        }
        char c = str.trim().substring(0, 1).charAt(0);
        // 正则表达式，判断首字母是否是英文字母
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c + "").matches()) {
            return (c + "").toUpperCase();
        } else if (str.equals("0")) {
            return "定位";
        } else if (str.equals("1")) {
            return "最近";
        } else if (str.equals("2")) {
            return "热门";
        } else if (str.equals("3")) {
            return "全部";
        } else {
            return "#";
        }
    }

}
