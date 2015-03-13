package no.application.sofia.busmapapp.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sofia on 05.03.15.
 */
public class LineDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "lineManager"; //Database name
    private static final String TABLE_LINES = "lines"; //Table name where stops are saved
    private static final String COLUMN_ID = "id"; //Autoincrement id of table
    private static final String COLUMN_LINEID = "LineID"; //ID from company's server
    private static final String COLUMN_NAME = "name"; //Name of stop
    private static final String COLUMN_TRANSPORTATION = "transportation"; //The type of vehicle



    public LineDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_LINES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_LINEID + " INTEGER, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_TRANSPORTATION + " INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINES);
        onCreate(db);
    }

    //Adding lines to the database
    public void addLine(Line line){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LINEID, line.getLineId());
        values.put(COLUMN_NAME, line.getName());
        values.put(COLUMN_TRANSPORTATION, line.getTransportation());
        db.insert(TABLE_LINES, null, values);
        db.close();
    }

    public List<Line> getAllLines(){
        List<Line> lineList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_LINES;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()){
            do {
                Line line = new Line();
                line.setId(cursor.getInt(0));
                line.setLineId(cursor.getInt(1));
                line.setName(cursor.getString(2));
                line.setTransportation(cursor.getInt(3));
                lineList.add(line);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return lineList;
    }

    public long dbLength(){
        SQLiteDatabase db = getWritableDatabase();
        long length = DatabaseUtils.queryNumEntries(db, TABLE_LINES);
        db.close();
        return length;
    }

    public Line getLineByName(String name){
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_LINES + " WHERE " + COLUMN_NAME + " = " + name;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Line line = new Line();
        if (cursor.moveToFirst()) {
            line.setId(cursor.getInt(0));
            line.setLineId(cursor.getInt(1));
            line.setName(cursor.getString(2));
            line.setTransportation(cursor.getInt(3));
        }

        db.close();
        return line;
    }

}
