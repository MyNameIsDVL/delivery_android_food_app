/*
 * Copyright (c) 2021 | Mateusz Hus.
 * The content of this application, including (but not limited to) all written material, images, photos, and code, are protected under international copyright and trademark laws. You may not copy, reproduce, modify, republish, transmit or distribute any material from this application.
 *
 * Author: Mateusz Hus
 *
 */

package com.example.delivery_food_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText username, phoneNumber, email, deliveryAddress, password;
    Button register_btn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userId;

    Context context;

    DatabaseReference reference;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarColor();
        setContentView(R.layout.activity_register);

        // get controls
        username = (EditText)findViewById(R.id.username);
        phoneNumber = (EditText)findViewById(R.id.phoneNumber);
        email = (EditText)findViewById(R.id.email);
        deliveryAddress = (EditText)findViewById(R.id.deliveryAddress);
        password = (EditText)findViewById(R.id.password);
        register_btn = (Button)findViewById(R.id.register_btn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        storageReference = FirebaseStorage.getInstance().getReference();

        if (fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usern = username.getText().toString().trim();
                String phone = phoneNumber.getText().toString().trim();
                String emailadress = email.getText().toString().trim();
                String delivery= deliveryAddress.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (TextUtils.isEmpty(emailadress)) {
                    email.setError("Email jest wymagany");
                    return;
                }

                if (TextUtils.isEmpty(pass)) {
                    password.setError("Hasło jest wymagane");
                    return;
                }

                if (pass.length() < 8) {
                    password.setError("Hasło musi zawierać 8 znaków");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(emailadress, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // send verif to user email

                            FirebaseUser userLink = fAuth.getCurrentUser();
                            userLink.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(RegisterActivity.this, "Link weryfikacyjny został wysłany na Twojego maila", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Błąd podczas wysyłania linku weryfikacyjnego. Spróbuj ponownie póżniej");
                                }
                            });

                            Toast.makeText(RegisterActivity.this, "Witaj w gronie zarejestrowanych użytkowników", Toast.LENGTH_SHORT).show();
                            userId = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users_info").document(userId);

                            // store data

                            Map<String, Object> user = new HashMap<>();
                            user.put("UserName", usern);
                            user.put("Phone", phone);
                            user.put("Email", emailadress);
                            user.put("Location", delivery);
                            user.put("Id", userId);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Pomyślnie utworzono profil dla "+ userId);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Błąd podczas tworzenia profilu "+ e.getMessage());
                                }
                            });

                            // for users order operations
                            reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(userId);

                            // store data

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("Id", userId);
                            hashMap.put("UserName", usern);
                            hashMap.put("ImageURL", "default");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "Pomyślnie utworzono profil dla chatu "+ userId);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Błąd "+ userId);
                                }
                            });

                            // send default img profile

                            StorageReference fileRef = storageReference.child("users_img/"+userId+"/default.png");
                            Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.mipmap.ic_default_foreground);

                            fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.d(TAG, "Dodano domyślne zdjęcie profilowe "+ userId);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Nie udało się dodać domyślnego zdjęcia profilowego "+ userId);
                                }
                            });

                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            FirebaseAuth.getInstance().signOut();
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "Nieprawidłowe dane", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public void toLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}