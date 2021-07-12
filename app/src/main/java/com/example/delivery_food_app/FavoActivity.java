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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.delivery_food_app.Adapter.BasketAdapter;
import com.example.delivery_food_app.Adapter.FavouriteAdapter;
import com.example.delivery_food_app.Model.Basket;
import com.example.delivery_food_app.Model.Favourite;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FavoActivity extends AppCompatActivity {

    FirebaseAuth fAuth;
    String userId;

    RecyclerView favourite_content;
    FavouriteAdapter favouriteAdapter;

    private List<Favourite> productFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarColor();
        setContentView(R.layout.activity_favo);

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        productFavourite = new ArrayList<>();

        getFavouriteProducts();
    }

    private void getFavouriteProducts() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("favourite_added");

        Query query = reference.orderByChild("IdUser").equalTo(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productFavourite.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Favourite fav = snapshot.getValue(Favourite.class);

                    productFavourite.add(fav);

//                    gifbasket = findViewById(R.id.gifbasket);
//
//                    if (pro == null) {
//                        gifbasket.setVisibility(View.VISIBLE);
//                    } else {
//                        gifbasket.setVisibility(View.GONE);
//                        productBusket.add(pro);
//                    }
                }

                favourite_content = findViewById(R.id.favourite_content);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FavoActivity.this, RecyclerView.VERTICAL, false);
                favourite_content.setLayoutManager(layoutManager);

                favouriteAdapter = new FavouriteAdapter(FavoActivity.this, productFavourite);
                favourite_content.setAdapter(favouriteAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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