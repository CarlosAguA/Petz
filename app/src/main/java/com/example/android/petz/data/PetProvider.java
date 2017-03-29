package com.example.android.petz.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.UserDictionary;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.R.attr.id;
import static android.R.attr.value;

/**
 * Created by Paviliondm4 on 3/26/2017.
 */

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    /* Global variable of PetDbHelper */
    private PetDbHelper mDbHelper ;

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS , PETS);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*  Get database object */
        SQLiteDatabase database = mDbHelper.getReadableDatabase() ;

        Cursor cursor ;

        /* Help us find out what kind of input uri is passing to us */
        int match =  sUriMatcher.match(uri) ;

        switch (match){
            case PETS:
                // use the query() method to retrieve at least one column of data.
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                // TODO: Perform database query on pets table
               cursor = database.query(
                        PetContract.PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break ;
            case PET_ID:
                // SQLITE statement: SELECT id, name FROM pets WHERE _id=5 ;
                selection = PetContract.PetEntry._ID + "=?" ;
                /*Projection :{ "_id" , "name" } */
                //Just using id and name for query given by the programmer
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))} ;
                cursor = database.query(
                        PetContract.PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown: " + uri);
        }
        return cursor ;
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Hint: You might want to write a method in the PetContract for determining
       // whether a gender value is valid or not, using the gender constants declared there.
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        String breed = values.getAsString(PetContract.PetEntry.COLUMN_PET_BREED);
        if (breed == null) {
            throw new IllegalArgumentException("Pet requires a breed");
        }
        int weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight <= 0 || weight > 100 ) {
            throw new IllegalArgumentException("Invalid weight for pet");
        }

        // Insert the new pet with the given values
        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

}
