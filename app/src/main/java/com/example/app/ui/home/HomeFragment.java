package com.example.app.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.app.databinding.FragmentHomeBinding;

import java.io.File;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private static final int REQUEST_VIDEO_PICK = 1;
    private static final int REQUEST_PERMISSION_READ_MEDIA_VIDEO = 2;
    private Uri selectedVideoUri;

    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        final TextView plateNumberTextView = binding.textPlateNumber;
        Button selectVideoButton = binding.buttonSelectVideo;

        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        homeViewModel.getPlateNumber().observe(getViewLifecycleOwner(), plateNumber -> {
            plateNumberTextView.setText("License Plate Number: " + plateNumber);
        });

        selectVideoButton.setOnClickListener(v -> {
            Log.d(TAG, "Select Video Button Clicked");
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting READ_MEDIA_VIDEO permission");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_PERMISSION_READ_MEDIA_VIDEO);
            } else {
                Log.d(TAG, "READ_MEDIA_VIDEO permission already granted");
                selectVideo();
            }
        });

        return root;
    }

    private void selectVideo() {
        Log.d(TAG, "Launching video picker");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(TAG, "Video selected successfully");
            selectedVideoUri = data.getData();
            // Convert URI to File (You might need to handle permissions)
            String filePath = getPathFromUri(selectedVideoUri);
            if (filePath != null) {
                File videoFile = new File(filePath);
                Log.d(TAG, "Video file path: " + filePath);
                String userLogin = "example_user";
                String userPhone = "1234567890";
                homeViewModel.uploadVideo(videoFile, userLogin, userPhone);
            } else {
                Log.e(TAG, "Failed to get video file path");
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        Log.d(TAG, "Getting path from URI: " + uri);
        String filePath = null;
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult called with requestCode: " + requestCode);
        if (requestCode == REQUEST_PERMISSION_READ_MEDIA_VIDEO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ_MEDIA_VIDEO permission granted");
                selectVideo();
            } else {
                Log.d(TAG, "READ_MEDIA_VIDEO permission denied");
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
