package com.bitp3453.newbie;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Timmy Ho on 5/12/2017.
 */

public class NewbieDB extends SQLiteOpenHelper {
    public static final String dbName = "newbie.db";

    public static final String userTblName = "user";
    public static final String colUserId = "user_id";
    public static final String colUserName = "user_name";
    public static final String colUserEmail = "user_email";
    public static final String colUserPassword= "user_password";
    public static final String colUserMatricNo= "user_matricNo";

    public static final String classTblName = "class";
    public static final String colClassId= "class_id";
    public static final String colClassStart= "class_start";
    public static final String colClassEnd= "class_end";
    public static final String colClassDay= "class_day";
    public static final String colClassType= "class_type";
    public static final String colClassLocation= "class_location";
    public static final String colClassRoom= "class_room";
    public static final String colClassColor= "class_color";
    public static final String colClassRemindHour= "class_remind";

    public static final String subTblName = "subject";
    public static final String colSubId = "sub_id";
    public static final String colSubName = "sub_name";
    public static final String colSubLecturer = "sub_lecturer";
    public static final String colSubColor= "sub_color";

    public static final String eventTblName = "event";
    public static final String colEventId = "event_id";
    public static final String colEventTitle = "event_title";
    public static final String colEventDesc = "event_description";
    public static final String colEventDate = "event_date";
    public static final String colEventStart= "event_start";
    public static final String colEventEnd = "event_end";
    public static final String colEventLocation= "event_location";
    public static final String colEventCategory= "event_category";
    public static final String colEventHost= "event_host";
    public static final String colEventRemindHour= "event_remind";
    public static final String colEventColor= "event_color";


    public NewbieDB(Context context) {
        super(context, dbName, null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+
                userTblName+ "("+
                colUserName+" VARCHAR, "+
                colUserEmail+" VARCHAR, "+
                colUserPassword+" VARCHAR, "+
                colUserMatricNo+" VARCHAR);");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+
                classTblName+ "("+
                colClassId+"  INT, "+
                colClassType+" VARCHAR, "+
                colClassLocation+" VARCHAR, "+
                colClassRoom+" VARCHAR, "+
                colClassDay+" VARCHAR, " +
                colClassStart+" TIME, " +
                colClassEnd+" TIME, "+
                colClassColor+" VARCHAR, "+
                colClassRemindHour+" VARCHAR, "+
                colSubId+" INT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+
                subTblName+ "("+
                colSubId+"  INT, "+
                colSubName+" VARCHAR, "+
                colSubLecturer+" VARCHAR, "+
                colSubColor+" VARCHAR);");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+
                eventTblName+ "("+
                colEventId+"  INT, "+
                colEventTitle+" VARCHAR, "+
                colEventDesc+" VARCHAR, "+
                colEventDate+" VARCHAR, "+
                colEventStart+" TIME, "+
                colEventEnd+" TIME, "+
                colEventLocation+" VARCHAR, "+
                colEventCategory+" VARCHAR, "+
                colEventHost+" VARCHAR, "+
                colEventRemindHour+" VARCHAR, "+
                colEventColor+" VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+userTblName);
        db.execSQL("DROP TABLE IF EXISTS "+classTblName);
        db.execSQL("DROP TABLE IF EXISTS "+subTblName);
        db.execSQL("DROP TABLE IF EXISTS "+eventTblName);
        onCreate(db);
    }

    public boolean fnExecuteSql(String strSql, Context appContext){
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL(strSql);
        }
        catch (Exception e){
            Log.d("unable to run query", "error!");
            return false;
        }
        return true;
    }

    public Cursor getDataById(String tblName, String tblId, int id){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from "+tblName+" where "+tblId+"= "+id, null);
    }

    public int fnTotalRow(String tblName){
        int intRow;
        SQLiteDatabase db = this.getReadableDatabase();
        intRow = (int) DatabaseUtils.queryNumEntries(db, tblName);

        return intRow;
    }
}
