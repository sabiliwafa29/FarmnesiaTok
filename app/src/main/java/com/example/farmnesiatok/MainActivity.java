package com.example.farmnesiatok;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.farmnesiatok.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reference = FirebaseDatabase.getInstance().getReference("Sensor");

        View bleButton = findViewById(R.id.rectangle_ble);
        bleButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Ble_Connectivity.class)));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String humidity = String.valueOf(dataSnapshot.child("kelembabanUdara").getValue());
                    String suhu = String.valueOf(dataSnapshot.child("suhuCelcius").getValue());
                    binding.hmdt.setText(humidity);
                    binding.Ctemperature.setText(suhu);
                } else {
                    Toast.makeText(MainActivity.this, "Data Doesn't Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}