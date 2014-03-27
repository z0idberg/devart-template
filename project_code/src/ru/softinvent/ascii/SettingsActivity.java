package ru.softinvent.ascii;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;

/**
 * Preferences activity.
 *
 * @author A. Knyazhev, "SoftInvent" (http://softinvent.ru).
 */
public class SettingsActivity extends Activity {
    private static final int MIN_CHANGE_RATE = 3;
    private static final int MAX_CHANGE_RATE = 300;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
        private NumberPickerPreference changeRatePref;
        private EditTextPreference charsPref;
        private MultiSelectListPreference sourcesPref;

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            sourcesPref = (MultiSelectListPreference) findPreference(getString(R.string.key_sources));
            getLoaderManager().initLoader(0, null, this);

            changeRatePref = (NumberPickerPreference) findPreference(getString(R.string.key_change_rate));
            changeRatePref.setMinValue(MIN_CHANGE_RATE);
            changeRatePref.setMaxValue(MAX_CHANGE_RATE);

            charsPref = (EditTextPreference) findPreference(getString(R.string.key_chars));

            updateChangeRateSummary();
            updateCharsSummary();

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.key_chars))) {
                if (charsPref.getText().isEmpty()) {
                    charsPref.setText("*");
                }
                updateCharsSummary();
            } else if (key.equals(getString(R.string.key_change_rate))) {
                updateChangeRateSummary();
            }
        }

        private void updateChangeRateSummary() {
            changeRatePref.setSummary(getString(R.string.pref_photo_change_rate_summary, changeRatePref.getValue()));
        }

        private void updateCharsSummary() {
            charsPref.setSummary(charsPref.getText());
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = new String[] {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.BUCKET_ID,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
            };
            String sort = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " COLLATE LOCALIZED ASC";
            String selection = MediaStore.Images.ImageColumns.MIME_TYPE + " LIKE 'image/%'"
                    + ") GROUP BY (" + MediaStore.Images.ImageColumns.BUCKET_ID;

            return new CursorLoader(getActivity(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sort);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor c) {
            if (c != null) {
                if (c.getCount() == 0) {
                    sourcesPref.setEnabled(false);
                } else {
                    int bucketIdCol = c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
                    int bucketNameCol = c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
                    String[] entries = new String[c.getCount()];
                    String[] entryValues = new String[c.getCount()];

                    int k = 0;
                    c.moveToFirst();
                    do {
                        entryValues[k] = String.valueOf(c.getInt(bucketIdCol));
                        entries[k] = c.getString(bucketNameCol);
                        k++;
                    } while (c.moveToNext());

                    sourcesPref.setEntries(entries);
                    sourcesPref.setEntryValues(entryValues);
                }
            } else {
                sourcesPref.setEnabled(false);
            }
       }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }
}