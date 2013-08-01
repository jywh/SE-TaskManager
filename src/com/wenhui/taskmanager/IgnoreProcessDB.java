package com.wenhui.taskmanager;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IgnoreProcessDB {

	public static final String DATABASE_NAME = "ignore_process.db";
	public static final int DATABASE_VERSION = 1;

	public static final String DATABASE_TABLE = "tasks_killer";
	public static final String IGNORE_PACKAGENAME = "_ignore_packagenames";

	private DbHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private static class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + DATABASE_TABLE + " ("
					+ "_id INTEGER PRIMARY KEY autoincrement,"
					+ IGNORE_PACKAGENAME + " TEXT" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);

		}
	}
	
	public IgnoreProcessDB(Context context){
		mDbHelper = new DbHelper(context);
		mDb = mDbHelper.getWritableDatabase();
	}
	
	public void close(){
		mDb.close();
	}
	
	public long insertIgnorePkgname(String pkgname){
		ContentValues values = new ContentValues();
		values.put(IGNORE_PACKAGENAME, pkgname);
		return mDb.insert(DATABASE_TABLE, null, values);
	}
	
	public boolean clearIgnorePkgname(String pkgname){
		String[] where =new String[]{pkgname};
		return  mDb.delete(DATABASE_TABLE, IGNORE_PACKAGENAME + " LIKE ?", where)>0;
	
	}
	
	public boolean clearAllIgnorePkg(){
		return mDb.delete(DATABASE_TABLE, null, null)>0;
	}
	public ArrayList<String> getIgnoredPackage(){
		Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{IGNORE_PACKAGENAME}, null, null, null, null, null);
		ArrayList<String> ignoredPkg= new ArrayList<String>();
		if(cursor.moveToFirst()){
			while(!cursor.isAfterLast()){
				String pkgname = cursor.getString(cursor.getColumnIndexOrThrow(IGNORE_PACKAGENAME));
				ignoredPkg.add(pkgname);
				cursor.moveToNext();
			}
		}
		cursor.close();
		return ignoredPkg;
	}
}
