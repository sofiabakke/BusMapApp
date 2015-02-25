package no.application.sofia.busmapapp.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sofia on 25.02.15.
 * This is the database helper class making it possible to save a stop in the database
 */
public class StopsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stopsManager"; //Database name
    private static final String TABLE_STOPS = "stops"; //Table name where stops are saved
    private static final String COLUMN_ID = "id"; //Autoincrement id of table
    private static final String COLUMN_ENTRYID = "entryId"; //ID from company's server
    private static final String COLUMN_NAME = "name"; //Name of stop
    private static final String COLUMN_LAT = "lat"; //latitude of the stop
    private static final String COLUMN_LNG = "lng"; //longitude of the stop
    private static final String COLUMN_FAVORITE = "favorite";

    public StopsDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_STOPS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ENTRYID + " INTEGER, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_LAT + " DOUBLE, "
                + COLUMN_LNG + " DOUBLE, "
                + COLUMN_FAVORITE + " INTEGER)";
        db.execSQL(sql);
    }

    //The table is dropped if the database is updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOPS);
        onCreate(db);
    }

    private ContentValues getValuesFromStop(Stop stop){
        ContentValues values = new ContentValues();
        values.put(COLUMN_ENTRYID, stop.getEntryId());
        values.put(COLUMN_NAME, stop.getName());
        values.put(COLUMN_LAT, stop.getLat());
        values.put(COLUMN_LNG, stop.getLng());
        values.put(COLUMN_FAVORITE, stop.getFavorite());
        return values;
    }

    public void addStop(Stop stop){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getValuesFromStop(stop);
        db.insert(TABLE_STOPS, null, values);
        db.close();
    }

    public void deleteStop(Stop stop){
        int id = stop.getId();
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOPS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateStop(Stop stop){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getValuesFromStop(stop);
        db.update(TABLE_STOPS, values, COLUMN_ID + "=?", new String[]{String.valueOf(stop.getId())});
        db.close();
    }

    public List<Stop> getAllStops(){
        List<Stop> stopList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STOPS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()){
            do{
                Stop stop = new Stop();
                stop.setId(cursor.getInt(0));
                stop.setEntryId(cursor.getInt(1));
                stop.setName(cursor.getString(2));
                stop.setLat(cursor.getDouble(3));
                stop.setLng(cursor.getDouble(4));
                stop.setFavorite(cursor.getInt(5));
                stopList.add(stop);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return stopList;
    }

    public List<Stop> getFavoriteStops(){
        List<Stop> stopList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STOPS + " WHERE " + COLUMN_FAVORITE + "=TRUE";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()){
            do {
                Stop stop = new Stop();
                stop.setId(cursor.getInt(0));
                stop.setEntryId(cursor.getInt(1));
                stop.setName(cursor.getString(2));
                stop.setLat(cursor.getDouble(3));
                stop.setLng(cursor.getDouble(4));
                stop.setFavorite(cursor.getInt(5));
                stopList.add(stop);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        return stopList;
    }
}
