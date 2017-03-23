package com.example.android.petz.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.petz.data.PetContract.PetEntry;

/**
 * Created by Paviliondm4 on 3/21/2017.
 */

//1. Create a class that extends from SQLiteOpenHelper
public class PetDbHelper extends SQLiteOpenHelper {

    //2.Create constants for database name and database version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Shelter.db" ;

    //3.Create a constructor
    public PetDbHelper (Context context){
        super(context,DATABASE_NAME , null , DATABASE_VERSION ) ;
    }

    //4.Implement onCreate() method
     public void onCreate(SQLiteDatabase db){

         //6. Create table statement using contract constants.
         // Create a String that contains the SQL statement to create the pets table
         String SQL_CREATE_ENTRIES =  "CREATE TABLE " + PetEntry.TABLE_NAME + " ("
                 + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                 + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                 + PetEntry.COLUMN_PET_BREED + " TEXT, "
                 + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                 + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

         db.execSQL(SQL_CREATE_ENTRIES) ;
     }

    //5. Implement onUpgrade() method
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here..
    }
}
