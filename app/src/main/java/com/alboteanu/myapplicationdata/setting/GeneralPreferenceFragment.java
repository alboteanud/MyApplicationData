package com.alboteanu.myapplicationdata.setting;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alboteanu.myapplicationdata.R;

public class GeneralPreferenceFragment extends PreferenceFragment {
    Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener;
    boolean isPrefsInitialised = false;
    OnTitleChangeListener mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);
        sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, @NonNull Object value) {
                String stringValue = value.toString();
                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                } else {
                    // For all otherText preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                    if(preference.getKey().equals(getString(R.string.display_title_text_key))){
                        if(!isPrefsInitialised){
                            isPrefsInitialised = true;
                            Log.d("tag GenFrag", "isPrefsInitialised " + isPrefsInitialised);
                            if (mCallback==null)
                                mCallback = (OnTitleChangeListener) getActivity();
                        }else if(mCallback != null)
                            mCallback.onTitleChanged();
                    }
                    Log.d("tag GenFrag", "mCalback= " + (mCallback != null ? mCallback.toString() : null));
                }
                return true;
            }
        };

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.display_title_text_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.custom_message_text_key)));
    }

    public GeneralPreferenceFragment() {
    }

    private void bindPreferenceSummaryToValue(@NonNull Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }



    // Container Activity must implement this interface
    public interface OnTitleChangeListener {
        public void onTitleChanged();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnTitleChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnTitleChangeListener");
        }
    }

}
