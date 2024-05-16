package com.example.app.ui.gallery;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GalleryViewModel extends ViewModel {

    private MutableLiveData<List<HistoryItem>> historyList = new MutableLiveData<>();

    private static final String HISTORY_URL = "http://192.168.1.86:8000/user-history";
    private static final String TAG = "GalleryViewModel";

    public LiveData<List<HistoryItem>> getHistoryList() {
        return historyList;
    }

    public void fetchHistory(String userLogin, String userPhone) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            HttpUrl.Builder urlBuilder = HttpUrl.parse(HISTORY_URL).newBuilder();
            urlBuilder.addQueryParameter("user_login", userLogin);
            urlBuilder.addQueryParameter("user_phone", userPhone);
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray historyArray = jsonResponse.getJSONArray("history");
                    List<HistoryItem> historyItems = new ArrayList<>();
                    for (int i = 0; i < historyArray.length(); i++) {
                        JSONArray itemArray = historyArray.getJSONArray(i);
                        HistoryItem item = new HistoryItem(
                                itemArray.getInt(0),
                                itemArray.getString(1),
                                itemArray.getString(2),
                                itemArray.getString(3),
                                itemArray.getString(4)
                        );
                        historyItems.add(item);
                    }
                    historyList.postValue(historyItems);
                } else {
                    Log.e(TAG, "Failed to fetch history: " + response.message());
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Failed to fetch history", e);
            }
        }).start();
    }
}

class HistoryItem {
    private int id;
    private String userLogin;
    private String userPhone;
    private String filename;
    private String plateNumber;

    public HistoryItem(int id, String userLogin, String userPhone, String filename, String plateNumber) {
        this.id = id;
        this.userLogin = userLogin;
        this.userPhone = userPhone;
        this.filename = filename;
        this.plateNumber = plateNumber;
    }

    public int getId() {
        return id;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getFilename() {
        return filename;
    }

    public String getPlateNumber() {
        return plateNumber;
    }
}
