package com.example.android.petz.data;

import android.provider.BaseColumns;

/**
 * Created by Paviliondm4 on 3/21/2017.
 */

/*VIDEO CREATE THE CONTRACT */

/*Just a class for providing constants, that´s why is a final class*/

public final class PetContract {
    //1. Outer class named PetContract

    //2.Inner class name PetEntry for each table in the db.
    public static abstract class PetEntry implements BaseColumns{

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
