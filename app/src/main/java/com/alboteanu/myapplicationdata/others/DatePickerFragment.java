package com.alboteanu.myapplicationdata.others;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.DatePicker;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
//        String dateString = Utils.calendarToString(calendar);
//        ((CheckBox) getActivity().findViewById(R.id.checkBox6Luni)).setChecked(false);  //implica stregere datei TextView10 - DATA
//        ((TextView)getActivity().findViewById(R.id.return_date_textView)).setText(dateString);
//        ((BaseDetailsActivity)getActivity()).calendar = calendar;
        OnDateSelectedListener mCallback = (OnDateSelectedListener) getActivity();
        mCallback.onDateSelected(calendar);
    }


    // Container Activity must implement this interface
    public interface OnDateSelectedListener {
        void onDateSelected(Calendar calendar);
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
