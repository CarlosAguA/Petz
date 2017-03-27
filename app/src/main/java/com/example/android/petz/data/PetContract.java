package com.example.android.petz.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Paviliondm4 on 3/21/2017.
 */

/*VIDEO CREATE THE CONTRACT */

/*Just a class for providing constants, that´s why is a final class*/

public final class PetContract {
    //1. Outer class named PetContract

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PetContract() {}

    /* Scheme */
    public static final String CONTENT_AUTHORITY = "com.example.android.petz";
    /* Content authority */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /* Path table name */
    public static final String PATH_PETS = "pets";

    //2.Inner class name PetEntry for each table in the db.
    public static abstract class PetEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        //3. String constants for the table name and each of the headings.
        public static final String TABLE_NAME = "pets";
        public static final String COLUMN_PET_NAME = "name" ;
        public static final String COLUMN_PET_BREED = "breed" ;
        public static final String COLUMN_PET_GENDER = "gender" ;
        public static final String COLUMN_PET_WEIGHT = "weight" ;
        public static final String _ID = BaseColumns._ID ;

        //4. Create constants for gender and use them where needed.
        public static final int GENDER_UNKNOWN = 0 ;
        public static final int GENDER_MALE = 1 ;
        public static final int GENDER_FEMALE = 2 ;
    }



}
