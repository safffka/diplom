package com.example.app.ui.slideshow;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app.R;

public class SlideshowViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mUserName;
    private final MutableLiveData<String> mUserPhone;
    private final MutableLiveData<Integer> mUserImage; // Use Integer for resource ID

    public SlideshowViewModel(Application application) {
        super(application);
        Context context = getApplication().getApplicationContext();

        mUserName = new MutableLiveData<>();
        mUserPhone = new MutableLiveData<>();
        mUserImage = new MutableLiveData<>();

        mUserName.setValue(context.getString(R.string.nav_header_title));
        mUserPhone.setValue(context.getString(R.string.nav_header_subtitle));
        mUserImage.setValue(R.mipmap.ic_launcher_round); // Reference to the mipmap resource
    }

    public LiveData<String> getUserName() {
        return mUserName;
    }

    public LiveData<String> getUserPhone() {
        return mUserPhone;
    }

    public LiveData<Integer> getUserImage() {
        return mUserImage;
    }
}
