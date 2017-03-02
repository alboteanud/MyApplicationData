package com.alboteanu.myapplicationdata.others;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private static final String STORED_CALENDAR = "my_calendar";
    SharedPreferences sharedPref;
    Calendar calendar;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current return_date_millis as the default return_date_millis in the picker
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        long storedCal = sharedPref.getLong(STORED_CALENDAR, System.currentTimeMillis());
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(storedCal);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the return_date_millis chosen by the user
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(STORED_CALENDAR, calendar.getTimeInMillis());
        editor.apply();

        OnDateSelectedListener mCallback = (OnDateSelectedListener) getActivity();
        mCallback.onDateSelected(calendar.getTimeInMillis());
    }


    // Container Activity must implement this interface
    public interface OnDateSelectedListener {
        void onDateSelected(long date);
    }

/*    @Override
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
    }*/


}
