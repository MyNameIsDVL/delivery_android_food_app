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
import androidx.cardview.widget.CardView;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity {

    Intent data;

    StorageReference storageReference;

    ImageView imageViewFood;
    TextView product_title, product_category, product_description, product_price;

    CardView plus, minus, addToFavourite;
    EditText editTextNumber;

    Integer currentValue;
    Integer currentValueAfterOp;
    Float currentPrice;
    BigDecimal operation;
    Float currentPriceSession;

    FloatingActionButton floatingActionButton;
    Button button_add_to_card;

    DatabaseReference reference;
    DatabaseReference referenceFavourite;
    String userId;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarColor();
        setContentView(R.layout.activity_product_details);

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        imageViewFood = findViewById(R.id.imageViewFood);
        product_title = findViewById(R.id.product_title);
        product_category = findViewById(R.id.product_category);
        product_description = findViewById(R.id.product_description);
        product_price = findViewById(R.id.product_price);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        button_add_to_card = findViewById(R.id.button_add_to_card);
        addToFavourite = findViewById(R.id.addToFavourite);

        editTextNumber = findViewById(R.id.editTextNumber);
        plus = findViewById(R.id.plus);
        minus = findViewById(R.id.minus);

        data = getIntent();

        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("product_images/"+data.getStringExtra("foodId")+"/default.png");

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imageViewFood);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get().load(R.mipmap.ic_launcher_round).into(imageViewFood);
                Log.d("tag", "Ustawiony został obrazek domyślny");
            }
        });

        product_title.setText(data.getStringExtra("productName"));
        product_category.setText(data.getStringExtra("productCategory"));
        product_description.setText(data.getStringExtra("productDescription"));
        product_price.setText(data.getStringExtra("productPrice"));

        currentPrice = Float.parseFloat(product_price.getText().toString());
        // clickListeners

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentValue = Integer.parseInt(editTextNumber.getText().toString());
                editTextNumber.setText(String.valueOf(currentValue + 1));
                currentValueAfterOp = Integer.parseInt(editTextNumber.getText().toString());
                operation = round(currentValueAfterOp * currentPrice, 2);
                product_price.setText(operation.toString());
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentValue = Integer.parseInt(editTextNumber.getText().toString());
                if (currentValue > 1) {
                    editTextNumber.setText(String.valueOf(currentValue - 1));
                    currentValueAfterOp = Integer.parseInt(editTextNumber.getText().toString());
                    currentPriceSession = Float.parseFloat(product_price.getText().toString());
                    operation = round(currentPriceSession - currentPrice, 2);
                    product_price.setText(operation.toString());
                } else
                {
                    editTextNumber.setText(String.valueOf(currentValue));
                    product_price.setText(currentPrice.toString());
                }
            }
        });

        button_add_to_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // for users order operations
                reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("basket_added");
                DatabaseReference newChildRef = reference.push();
                // store data

                String key = newChildRef.getKey();

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("IdUser", userId);
                hashMap.put("IdKey", key);
                hashMap.put("ProductId", data.getStringExtra("foodId"));
                hashMap.put("ProductQuantity", editTextNumber.getText().toString());
                hashMap.put("ProductName", product_title.getText().toString());
                hashMap.put("ProductCategory", product_category.getText().toString());
                hashMap.put("ProductDescription", product_description.getText().toString());
                hashMap.put("ProductPrice", product_price.getText().toString());

                reference.child(key).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProductDetailsActivity.this, "Pomyślnie dodano produkt do koszyka", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Pomyślnie dodano produkt do koszyka dla użytkownika: "+ userId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductDetailsActivity.this, "Nie udało się dodać produktu do koszyka. Sprawdź połączenie z internetem lub spróbuj ponownie później", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Błąd "+ userId);
                    }
                });
            }
        });

        addToFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // for users order operations
                referenceFavourite = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("favourite_added");
                DatabaseReference newChildRef = referenceFavourite.push();
                // store data

                String key = newChildRef.getKey();

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("IdUser", userId);
                hashMap.put("IdKey", key);
                hashMap.put("ProductId", data.getStringExtra("foodId"));
                hashMap.put("ProductName", product_title.getText().toString());
                hashMap.put("ProductCategory", product_category.getText().toString());
                hashMap.put("ProductDescription", product_description.getText().toString());
                hashMap.put("ProductPrice", data.getStringExtra("productPrice"));

                referenceFavourite.child(key).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProductDetailsActivity.this, "Pomyślnie dodano produkt do ulubionych", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Pomyślnie dodano produkt do ulubionych dla użytkownika: "+ userId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductDetailsActivity.this, "Nie udało się dodać produktu do ulubionych. Sprawdź połączenie z internetem lub spróbuj ponownie później", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Błąd "+ userId);
                    }
                });
            }
        });
    }

    public void floatingActionButton(View view) {
        startActivity(new Intent(getApplicationContext(), BasketActivity.class));
        finish();
    }

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
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