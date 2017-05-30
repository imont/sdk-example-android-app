/*
 * Copyright 2017 IMONT Technologies Limited
 */
package io.imont.android.sdkdemo.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeSeriesDBHelper extends SQLiteOpenHelper {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);

    final SQLiteDatabase db;

    public TimeSeriesDBHelper(final Context context) {
        super(context, null, null, 1);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.compileStatement("CREATE TABLE data (time timestamp, value decimal);").execute();
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

    }

    public void addData(final Date date, final Double value) {
        db.execSQL("INSERT INTO data VALUES (?, ?)", new Object[] {DATE_FORMAT.format(date), value});
    }

    public Cursor fetch(final String groupBy) {
        return db.rawQuery("SELECT strftime(?, time) as tm, AVG(value) FROM data GROUP BY tm ORDER BY time", new String[] { groupBy });
    }
}
