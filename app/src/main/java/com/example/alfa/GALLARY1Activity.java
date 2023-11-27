package com.example.alfa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Random;


public class GALLARY1Activity extends AppCompatActivity {

    ImageView iV;

    Uri imageUri;
    Button btn2;
    StorageReference storageReference;
    public static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallary1);
        iV = findViewById(R.id.iV);
        btn2 = findViewById(R.id.btn2);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);

        }


    }

    private void uploadImage() {
        Random random = new Random();
        int randomNumber = random.nextInt(1000) + 1;
        String fileName = "file" + randomNumber;
        storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);
        storageReference.putFile(imageUri).
                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       iV.setImageURI(null);
                        Toast.makeText(GALLARY1Activity.this, "It Uploaded",Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(GALLARY1Activity.this, "Faild to Upload",Toast.LENGTH_SHORT).show();

                    }
                });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK ){
            imageUri = data.getData();
            uploadImage();

        }
    }


    public void btn1(View view) {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
        btn2.setVisibility(View.VISIBLE);
    }

    public void show(View view) {
        storageReference.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
            iV.setImageBitmap(bitmap);

        }).addOnFailureListener(e -> {

            Toast.makeText(this, "Image download failed", Toast.LENGTH_SHORT).show();
        });

    }

    public void go(View view) {

        startActivity(new Intent(this, TextActivity.class));
    }
}