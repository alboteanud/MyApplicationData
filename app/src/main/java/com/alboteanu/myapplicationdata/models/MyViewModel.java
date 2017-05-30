package com.alboteanu.myapplicationdata.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.HashMap;

/**
 * Created by alboteanu on 30.05.2017.
 */

public class MyViewModel extends ViewModel {

    private final MutableLiveData<HashMap<String, Contact>> selected = new MutableLiveData<>();

    public LiveData<HashMap<String, Contact>> getSelected() {
        return selected;
    }

    public void setSelected(HashMap<String, Contact> newSelected) {
        selected.setValue(newSelected);
    }


}
