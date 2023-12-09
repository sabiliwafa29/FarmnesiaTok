package com.example.farmnesiatok;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmnesiatok.databinding.ActivityDataInformationBinding;
import com.example.farmnesiatok.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataInformation extends AppCompatActivity {
    ActivityDataInformationBinding binding;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reference = FirebaseDatabase.getInstance().getReference("Domba");

        View backButton = findViewById(R.id.back_data);
        backButton.setOnClickListener(v -> startActivity(new Intent(DataInformation.this, choice.class)));

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String goat1 = String.valueOf(dataSnapshot.child("BeratDomba").getValue());
                    String goat2 = String.valueOf(dataSnapshot.child("beratbadan").getValue());
                    String goat3 = String.valueOf(dataSnapshot.child("beratbadan").getValue());
                    String id1 = String.valueOf(dataSnapshot.child("id").getValue());
                    binding.goatData1.setText(goat1);
                    binding.dombaId.setText(id1);
//                    binding.goatData2.setText(goat2);
//                    binding.goatData3.setText(goat3);
                } else {
                    Toast.makeText(DataInformation.this, "Data Doesn't Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
