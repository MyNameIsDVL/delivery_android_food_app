/*
 * Copyright (c) 2021 | Mateusz Hus.
 * The content of this application, including (but not limited to) all written material, images, photos, and code, are protected under international copyright and trademark laws. You may not copy, reproduce, modify, republish, transmit or distribute any material from this application.
 *
 * Author: Mateusz Hus
 *
 */

package com.example.delivery_food_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = "TAG";

    CardView resedComponent;
    Button btnResendCode;

    FirebaseUser user;
    FirebaseAuth fAuth;

    FirebaseFirestore fStore;
    String userId;
    StorageReference storageReference;
    DatabaseReference reference;

    TextView dis_username, dis_email, dis_phoneNumber, dis_location;
    EditText up_UserName, up_Email, up_phoneNumber, up_location;
    ImageView setImageProfile, profile_img;
    Button update_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarColor();
        setContentView(R.layout.activity_profile);

        resedComponent = (CardView) findViewById(R.id.resedComponent);
        btnResendCode = (Button) findViewById(R.id.btnResendCode);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        if (!user.isEmailVerified()) {
            resedComponent.setVisibility(View.VISIBLE);

            btnResendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send verif to user email

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(view.getContext(), "Link weryfikacyjny został wysłany na Twojego maila", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Błąd podczas wysyłania linku weryfikacyjnego. Spróbuj ponownie póżniej");
                        }
                    });
                }
            });
        }

        // dla wyświetlanych danych profilu
        dis_username = (TextView)findViewById(R.id.dis_username);
        dis_email = (TextView)findViewById(R.id.dis_email);
        dis_phoneNumber = (TextView)findViewById(R.id.dis_phoneNumber);
        dis_location = (TextView)findViewById(R.id.dis_location);

        // EditText - update dla profilu
        up_UserName = (EditText)findViewById(R.id.up_UserName);
        up_Email = (EditText)findViewById(R.id.up_Email);
        up_phoneNumber = (EditText)findViewById(R.id.up_phoneNumber);
        up_location = (EditText)findViewById(R.id.up_location);

        // Button - do zapisania edycji profilu
        update_profile = (Button)findViewById(R.id.update_profile);

        // obrazki
        setImageProfile = (ImageView)findViewById(R.id.setImageProfile);
        profile_img = (ImageView)findViewById(R.id.profile_img);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = fAuth.getCurrentUser().getUid();

        StorageReference profileRef = storageReference.child("users_img/"+userId+"/default.png");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile_img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("tag", "Ustawiony został awatar domyślny");
            }
        });

        // set profile data

        DocumentReference documentReference = fStore.collection("users_info").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshot.exists()) {
                        dis_username.setText(documentSnapshot.getString("UserName"));
                        dis_email.setText(documentSnapshot.getString("Email"));
                        dis_phoneNumber.setText(documentSnapshot.getString("Phone"));
                        dis_location.setText(documentSnapshot.getString("Location"));

                        up_UserName.setText(dis_username.getText());
                        up_Email.setText(dis_email.getText());
                        up_phoneNumber.setText(dis_phoneNumber.getText());
                        up_location.setText(dis_location.getText());

                    } else {
                        Log.d("tag", "Document nie istnieje");
                    }
                }
            }
        });

        setImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery, 1000);
            }
        });

        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (up_UserName.getText().toString().isEmpty() || up_Email.getText().toString().isEmpty() || up_phoneNumber.getText().toString().isEmpty()
                || up_location.getText().toString().isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Wszystkie pola są wymagane. Prosimy je wypełnić", Toast.LENGTH_SHORT).show();
                    return;
                }

                String em = up_Email.getText().toString();

                user.updateEmail(em).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DocumentReference documentReference = fStore.collection("users_info").document(user.getUid());
                        Map<String, Object> editProfile = new HashMap<>();
                        editProfile.put("Email", em);
                        editProfile.put("Location", up_location.getText().toString());
                        editProfile.put("Phone", up_phoneNumber.getText().toString());
                        editProfile.put("UserName", up_UserName.getText().toString());
                        documentReference.update(editProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ProfileActivity.this, "Pomyślnie zaktualizowano profil", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Nie udało się zaktualizować profilu. Spróbuj ponownie póżniej", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // for users order operations
                        reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(user.getUid());

                        // store data
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("Id", user.getUid());
                        hashMap.put("UserName", up_UserName.getText().toString());
                        hashMap.put("ImageURL", "default");

                        reference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("TAG", "Poprawnie zaktualizowano RDB UserName");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "Błąd podczas aktualizacji RDB UserName" + e.getMessage());
                            }
                        });

                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Nie udało się zaktualizować maila. Prosimy spróbować ponownie póżniej" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                Toast.makeText(ProfileActivity.this, "To może trochę potrwać", Toast.LENGTH_SHORT).show();
                uploadImageDB(imageUri);
            }
        }
    }

    private void uploadImageDB(Uri imageUri) {
        StorageReference fileRef = storageReference.child("users_img/"+fAuth.getCurrentUser().getUid()+"/default.png");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profile_img);
                    }
                });
                Toast.makeText(ProfileActivity.this, "Zapisano zdjęcie profilowe", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Nie udało się zapisać zdjęcia profilowego", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cardBack(View view) {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}