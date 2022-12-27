package com.mszgajewski.chatappjavaandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://chatappjavaandroid-default-rtdb.europe-west1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText name = findViewById(R.id.register_name);
        final EditText phone = findViewById(R.id.register_phone);
        final EditText email = findViewById(R.id.register_email);
        final AppCompatButton registerBtn = findViewById(R.id.registerBtn);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("ładowanie...");

        if (!MemoryData.getData(this).isEmpty()){
            Intent intent = new Intent(Register.this, MainActivity.class);
            intent.putExtra("phone", MemoryData.getData(this));
            intent.putExtra("name", MemoryData.getName(this));
            intent.putExtra("email", "");
            startActivity(intent);
            finish();
        }

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();
                final String nameTxt = name.getText().toString();
                final String phoneTxt = phone.getText().toString();
                final String emailTxt = email.getText().toString();

                if (nameTxt.isEmpty() || phoneTxt.isEmpty() || emailTxt.isEmpty()){
                    Toast.makeText(Register.this, "Wszystkie pola wymagane!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            progressDialog.dismiss();

                            if (snapshot.child("users").hasChild(phoneTxt)){
                                Toast.makeText(Register.this, "Numer telefonu już istnieje w bazie", Toast.LENGTH_SHORT).show();
                            } else {
                                databaseReference.child("users").child(phoneTxt).child("email").setValue(emailTxt);
                                databaseReference.child("users").child(phoneTxt).child("name").setValue(nameTxt);
                                databaseReference.child("users").child(phoneTxt).child("profile_pic").setValue("");

                                MemoryData.saveData(phoneTxt, Register.this);
                                MemoryData.saveName(nameTxt,Register.this);

                                Toast.makeText(Register.this, "Rejestracja udana", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Register.this, MainActivity.class);
                                intent.putExtra("phone", phoneTxt);
                                intent.putExtra("name", nameTxt);
                                intent.putExtra("email",emailTxt);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();

                        }
                    });

                }
            }
        });
    }
}