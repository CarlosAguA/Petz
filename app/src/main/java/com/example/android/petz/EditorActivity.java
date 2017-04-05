package com.example.android.petz;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.UserDictionary;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.petz.data.PetContract;
import com.example.android.petz.data.PetContract.PetEntry;
import com.example.android.petz.data.PetDbHelper;

import java.util.Set;

import static android.R.attr.name;
import static android.R.attr.switchMinWidth;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    /* Loader ID for PET_LOADER */
    private static final int PET_LOADER = 0;

    //Uri for passing the uri within the onCreateLoader.
    private Uri currentPetUri;

    //Booblean for tracking if values on Edit fields have changed.
    private boolean mPetHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        if (currentPetUri != null) {
            // Since the editor shows all pet attributes, define a projection that contains
            // all columns from the pet table
            String[] projection = {
                    PetEntry._ID,
                    PetEntry.COLUMN_PET_NAME,
                    PetEntry.COLUMN_PET_BREED,
                    PetEntry.COLUMN_PET_GENDER,
                    PetEntry.COLUMN_PET_WEIGHT};

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(this,   // Parent activity context
                    currentPetUri,         // Query the content URI for the current pet
                    projection,             // Columns to include in the resulting Cursor
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);                  // Default sort order
        }
        return null ;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
           int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // Extract out the value from the Cursor for the given column index
            String petName = cursor.getString(nameColumnIndex);
            String petBreed = cursor.getString(breedColumnIndex);
            int petGender = cursor.getInt(genderColumnIndex);
            int petWeight = cursor.getInt(weightColumnIndex);

            //Set the retrieved info from the pets table in the editTexts
            mNameEditText.setText(petName);
            mBreedEditText.setText(petBreed);
            mWeightEditText.setText(String.valueOf(petWeight));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (petGender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
            mGenderSpinner.setSelection(petGender);

        }

    }

    @Override
    public void onLoaderReset(Loader loader) {

        //Clear editor fields if the loader resets.
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0); // Select "Unknown" gender

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        //Register the edit Fields with the mTouchListener
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
        
        // You can be pretty confident that the intent will not be null here.
        Intent intent = getIntent();
        currentPetUri = intent.getData();

        if (currentPetUri == null) {

            setTitle(getString(R.string.editor_activity_title_new_pet));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
                    // (It doesn't make sense to delete a pet that hasn't been created yet.)
                            invalidateOptionsMenu();

        } else {

            setTitle(R.string.editor_activity_title_edit_pet);
            Log.i(LOG_TAG, "Test" + currentPetUri.toString() ) ;

            }

        /*Initialize the loader  */
        getLoaderManager().initLoader(PET_LOADER, null, this);



        }
    
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });

    }

    private void savePet(){

        /**
         * 1. Get all the data from the EditText fields
         * 2. Save them in a ContentValues object
         * 3. Insert the ContentValues object into the pets table.
         */
        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        String petGender = mGenderSpinner.getSelectedItem().toString();
        int gender ;
        if (petGender.equals(getString(R.string.gender_male))){
            gender = PetEntry.GENDER_MALE;
        }else if (petGender.equals(getString(R.string.gender_female))){
            gender = PetEntry.GENDER_FEMALE ;
        }else {
            gender = PetEntry.GENDER_UNKNOWN ;
        }
       // Log.i(LOG_TAG, "Editor TAG: " + mWeightEditText.getText().toString()) ;
        String petWeight = mWeightEditText.getText().toString().trim();

        //Before content values object
        if( currentPetUri == null && TextUtils.isEmpty(petName) && TextUtils.isEmpty(petBreed)
                && TextUtils.isEmpty(petWeight) && petGender.equals(getString(R.string.gender_unknown)) ) {
            //Finish the activity if all fields are empty.
            //finish();
            return ;
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, petName );
        values.put(PetEntry.COLUMN_PET_BREED, petBreed );
        values.put(PetEntry.COLUMN_PET_GENDER, gender );

        //before placing the weight into the content value object
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int weight = 0;
        if (!TextUtils.isEmpty(petWeight)) {
            weight = Integer.parseInt(petWeight);
        }

        //Convert the string to int  for weight
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight );

        // Condition Add a pet
        if (currentPetUri == null) {

            // Insert a new pet into the provider, returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        // Condition edit a pet
        }else{
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify
            int rowsAffected = getContentResolver().update(
                    currentPetUri, // the user pet content URI
                    values,               // the columns to update
                    null,
                    null ) ;

            //Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // TODO: Implement this method

        // Deletes the pet that match the selection criteria
        // Defines a variable to contain the number of rows deleted

       int mRowsDeleted = getContentResolver().delete(
                currentPetUri,   // the user dictionary content URI
                null,            // the column to select on
                null             // the value to compare to
        );

        // Show a toast message depending on whether or not the delete was successful
        if (mRowsDeleted == 0 ){
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, R.string.editor_delete_pet_failed,
                    Toast.LENGTH_SHORT ).show();
        }else{
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, R.string.editor_delete_pet_successful,
                    Toast.LENGTH_SHORT ).show();
        }

        //Close the activity after deleting
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save data
                savePet();
                //Exit Activity
                finish() ;
                return true;
            // Respond to a click on the "Delete" menu option
           /* This case is only accesible if the Activity is on edit mode but not on add mode */
            case R.id.action_delete:
                //add strng for pet deleted
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
