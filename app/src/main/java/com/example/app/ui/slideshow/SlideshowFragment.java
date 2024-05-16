package com.example.app.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.app.R;

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        final ImageView userImage = root.findViewById(R.id.user_image);
        final TextView userName = root.findViewById(R.id.user_name);
        final TextView userPhone = root.findViewById(R.id.user_phone);

        slideshowViewModel.getUserName().observe(getViewLifecycleOwner(), userName::setText);
        slideshowViewModel.getUserPhone().observe(getViewLifecycleOwner(), userPhone::setText);
        slideshowViewModel.getUserImage().observe(getViewLifecycleOwner(), imageResId -> userImage.setImageResource(imageResId));

        return root;
    }
}
