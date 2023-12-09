package com.example.farmnesiatok;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.farmnesiatok.databinding.ActivityScanOcrBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.HashMap;

public class ScanOcr extends AppCompatActivity {

    private Button inputImage;
    private Button scanImage;
    private ShapeableImageView imageV;
    private EditText editT;
    private static final String TAG = "MAIN_TAG";
    private Uri imageUri = null;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    private String[] cameraPermission;
    private String[] storagePermission;
    private ProgressDialog progressDialog;
    private TextRecognizer textRecognizer;

    ActivityScanOcrBinding binding;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ocr);
        binding = ActivityScanOcrBinding.inflate(getLayoutInflater());

        inputImage = findViewById(R.id.inputimage);
        scanImage = findViewById(R.id.scanImage);
        imageV = findViewById(R.id.imageV);
        editT = findViewById(R.id.scannedT);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        reference = FirebaseDatabase.getInstance().getReference("Domba").child("beratBadan");

        View backButton = findViewById(R.id.back_ocr);
        backButton.setOnClickListener(v -> startActivity(new Intent(ScanOcr.this, choice.class)));

        inputImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputImageDialog();
            }
        });

        scanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null){
                    Toast.makeText(ScanOcr.this, "Pick Image First", Toast.LENGTH_SHORT).show();
                } else {

                    scanImagetoText();
                }
            }
        });
    }

    private void scanImagetoText() {
        Log.d(TAG, "scanImagetoText: ");
        progressDialog.setMessage("Preparing image");
        progressDialog.show();

        try {
            InputImage inputimage = InputImage.fromFilePath(this, imageUri);

            progressDialog.setMessage("Scanning Image");

            Task<Text> textTaskReslut = textRecognizer.process(inputimage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            progressDialog.dismiss();
                            String scannedText = text.getText();
                            Log.d(TAG, "onSuccess: scannedText: "+scannedText);

                            updateData(scannedText);
                            editT.setText(scannedText);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.e(TAG, "onFailure: ", e);
                            Toast.makeText(ScanOcr.this, "Failed scanning text due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e){
            progressDialog.dismiss();
            Log.e(TAG, "scanImagetoText: ", e);
            Toast.makeText(ScanOcr.this, "Failed preparing image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, inputImage);
        popupMenu.getMenu().add(Menu.NONE, 1,1, "CAMERA");
        popupMenu.getMenu().add(Menu.NONE, 2,2, "GALLERY");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==1){
                    Log.d(TAG, "onMenuItemClick: Camera Clicked");
                    if (checkCameraPermission()){
                        pickImageCamera();
                    } else {
                        requestCameraPermission();
                    }
                } else if (id == 2){
                    Log.d(TAG, "onMenuItemClick: Gallery Clicked");
                    if (checkStoragePermission()){
                        pickImageGallery();
                    } else {
                        requestStoragePermission();
                    }
                }
                return false;
            }
        });
    }

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK){
                        Intent data = o.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri"+imageUri);
                        imageV.setImageURI(imageUri);

                    } else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(ScanOcr.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Sample");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>(){
                @Override
                public void onActivityResult(ActivityResult result){
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: imageUri"+imageUri);
                        imageV.setImageURI(imageUri);
                    } else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(ScanOcr.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }

    );

    private void updateData(String scannedText){
        HashMap beratDomba = new HashMap();
        beratDomba.put("BeratDomba", scannedText);

        reference = FirebaseDatabase.getInstance().getReference("Domba");
        reference.updateChildren(beratDomba).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    binding.scannedT.setText(scannedText);
                } else {
                    Toast.makeText(ScanOcr.this,"Failed to Update",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean cameraResult = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return cameraResult && storageResult;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        pickImageCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ScanOcr.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted){
                        pickImageGallery();
                    } else {
                        Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }
}