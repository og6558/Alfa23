package com.example.alfa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TextActivity extends AppCompatActivity {
    private EditText editText;
    private Button saveButton, loadButton;
    private TextView displayText;
    private DatabaseReference databaseReference;
    private DatabaseReference historyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        editText = findViewById(R.id.editText);
        saveButton = findViewById(R.id.saveButton);
        loadButton = findViewById(R.id.loadButton);
        displayText = findViewById(R.id.displayText);

        databaseReference = FirebaseDatabase.getInstance().getReference("textData");
        historyReference = FirebaseDatabase.getInstance().getReference("textHistory");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFirebase();
                loadButton.setVisibility(View.VISIBLE);
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLatestFromFirebase();
            }
        });
    }

    private void saveToFirebase() {
        String text = editText.getText().toString().trim();
        if (!text.isEmpty()) {

            databaseReference.push().setValue(text);


            historyReference.push().setValue(text);

            editText.getText().clear();
        }
    }

    private void loadLatestFromFirebase() {

        databaseReference.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String text = snapshot.getValue(String.class);
                    if (text != null) {
                        displayText.append(text + "\n");
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                displayText.setText("Failed to load data");
            }
        });
    }

    public void next(View view) {
        startActivity(new Intent(this, TimeActivity.class));
    }
}