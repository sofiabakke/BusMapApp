package no.application.sofia.busmapapp.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sofia on 05.03.15.
 * The database helper controlling saving of lines to the database.
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

    /**
     * Adding lines to the database one by one
     * @param line the current line to add
     */
    public void addLine(Line line){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LINEID, line.getLineId());
        values.put(COLUMN_NAME, line.getName());
        values.put(COLUMN_TRANSPORTATION, line.getTransportation());
        db.insert(TABLE_LINES, null, values);
        db.close();
    }

    /**
     * Used to see how lon the database is.
     * If there is no elements in the database, all lines from the server is added.
     * @return the database length
     */
    public long dbLength(){
        SQLiteDatabase db = getWritableDatabase();
        long length = DatabaseUtils.queryNumEntries(db, TABLE_LINES);
        db.close();
        return length;
    }

    /**
     * Each line has a unique number which needs to be found from the name the user searches for.
     * For instance, no line has a letter in it. For instance bus line 80E, has the ID 2080
     * @param lineNumber the line input from the user
     * @return the line from the database with all its attributes.
     */
    public Line getLineByLineNumber(String lineNumber){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LINES, null, COLUMN_NAME + "=?", new String[]{lineNumber}, null, null, null);
        Log.d("Cursor length", "Columns: "+ cursor.getColumnCount() + " Rows: " + cursor.getCount());
        Line line = new Line();
        if (cursor.moveToFirst()) {
            line.setId(cursor.getInt(0));
            line.setLineId(cursor.getInt(1));
            line.setName(cursor.getString(2));
            line.setTransportation(cursor.getInt(3));
        }
        db.close();

        Log.d("Line", line.getId() + ", " + line.getLineId() + ", " + line.getName() + ", " + line.getTransportation());
        return line;
    }

}
