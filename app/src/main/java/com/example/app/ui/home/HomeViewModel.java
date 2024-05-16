package com.example.app.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> plateNumber = new MutableLiveData<>();

    private static final String UPLOAD_URL = "http://192.168.1.86:8000/upload-video/";


    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Для загрузки нуобходимо видео хорошего качества в формате .mp4");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getPlateNumber() {
        return plateNumber;
    }

    public void uploadVideo(File videoFile, String userLogin, String userPhone) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody videoBody = RequestBody.create(videoFile, MediaType.parse("video/mp4"));
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", videoFile.getName(), videoBody)
                    .addFormDataPart("user_login", userLogin)
                    .addFormDataPart("user_phone", userPhone)
                    .build();

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    // Assuming the response is in JSON format
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String plate = jsonResponse.getString("first_plate");
                    plateNumber.postValue(plate);
                } else {
                    // Handle the error
                    plateNumber.postValue("Upload failed: " + response.message());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                plateNumber.postValue("Upload failed: " + e.getMessage());
            }
        }).start();
    }
}
