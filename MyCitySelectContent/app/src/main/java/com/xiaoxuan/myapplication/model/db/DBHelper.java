package com.xiaoxuan.myapplication.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by xiaoxuan on 2017/1/4 0004.
 */

public class DBHelper extends SQLiteOpenHelper {

    private final Context context;
    private static final int DB_VERSION = 3;
    private static String DB_PATH = "/data/data/com.xiaoxuan.myapplication/databases/";
    private static final String DB_NAME = "meituan_cities.db";
    private static final String ASSETS_NAME = "meituan_cities.db";

    private SQLiteDatabase database = null;


    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
        this.context = context;
    }

    public DBHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public DBHelper(Context context, String name) {
        this(context, name, DB_VERSION);
    }

    public DBHelper(Context context) {
        this(context, DB_PATH + DB_NAME);
    }

    /**
     * 创建数据库
     */
    public void createDataBase() throws IOException{
        boolean dbExist = checkDataBase();
        //数据库存在
        if (dbExist != false) {
            //do nothing - database already exist
        } else {
            //创建数据库
            try {
                File dir = new File(DB_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(DB_PATH + DB_NAME);
                if (file.exists()) {
                    file.delete();
                }
                SQLiteDatabase.openOrCreateDatabase(file, null);
                copyDataBase();
            } catch (IOException e) {
                Log.e("createDataBaseError", e.getMessage());
                throw new Error("数据库创建失败");
            }
        }

    }

    /**
     * 检查数据库是否有效
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDb = null;
        String path = DB_PATH + DB_NAME;
        try {
            checkDb = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            Log.e("SQLiteException", e.getMessage());
        }finally {
            if (checkDb != null) {
                checkDb.close();
                return true;
            }
            return false;
        }
//        File dbFile = new File(DB_PATH + DB_NAME);
//        Log.i("dbFile", dbFile + "   "+ dbFile.exists());
//        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        InputStream inputStream =  context.getAssets().open(ASSETS_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream outputStream = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer,0,length);
        }
        outputStream.flush();//冲走，往文件中写东西时系统把数据暂存到缓存区，调用flush时把数据传到通道的另一边(类似马桶冲便的原理，flush就是把便便冲到化粪池的)
        inputStream.close();
        outputStream.close();
        Log.i("copyDataBase","execute copyDataBase()");
    }

    /**
     * 必须是两个或者两个以上的线程才需要同步
     * 必须要保证多个线程使用的是同一个锁，才可以实现多个线程被同步
     */
    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    /**
     * 该函数是在第一次创建的时候执行， 实际上是第一次得到SQLiteDatabase对象的时候才会调用这个方法
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    /**
     * 数据库表结构有变化时采用
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
