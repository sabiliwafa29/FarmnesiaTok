package com.example.farmnesiatok;

import static androidx.core.content.ContextCompat.startActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class choice extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_choice, container, false);

        View choiceButton = rootView.findViewById(R.id.back_choice);
        choiceButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), Ble_Connect.class)));

        View choice2Button = rootView.findViewById(R.id.rectangle_choice2);
        choice2Button.setOnClickListener(v -> startActivity(new Intent(getActivity(), DataInformation.class)));

        View scanButton = rootView.findViewById(R.id.rectangle_choice);
        scanButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), ScanOcr.class)));

        return rootView;
    }
}
