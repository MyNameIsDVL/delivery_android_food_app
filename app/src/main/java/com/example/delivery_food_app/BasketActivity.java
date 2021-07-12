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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delivery_food_app.Adapter.BasketAdapter;
import com.example.delivery_food_app.Adapter.PopularFoodAdapter;
import com.example.delivery_food_app.Model.Basket;
import com.example.delivery_food_app.Model.FiveStarFood;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BasketActivity extends AppCompatActivity {

    FirebaseAuth fAuth;
    String userId;

    BasketAdapter basketAdapter;
    RecyclerView basket_content;

    private List<Basket> productBusket;

    RelativeLayout gifbasket;
    Button button_order_pay;

    Basket basket_check_empty_box;

    TextView to_pay_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarColor();
        setContentView(R.layout.activity_basket);

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        button_order_pay = findViewById(R.id.button_order_pay);
        to_pay_price = findViewById(R.id.to_pay_price);

        productBusket = new ArrayList<>();

        getBasketProducts();

        button_order_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (basket_check_empty_box != null) {

                    Intent intent = new Intent(BasketActivity.this, SummaryActivity.class);
                    intent.putExtra("orderedUserId", userId);
                    intent.putExtra("totalPrice", to_pay_price.getText().toString());
                    startActivity(intent);

                    Log.d("Basket", "Koszyk jest wypełniony");
                } else {
                    Toast.makeText(BasketActivity.this, "Twój koszyk jest pusty.", Toast.LENGTH_SHORT).show();
                    Log.d("Basket", "Koszyk jest pusty");
                }
            }
        });
    }

    private void getBasketProducts() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("basket_added");

        Query query = reference.orderByChild("IdUser").equalTo(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productBusket.clear();

                Float total = 0f;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Basket pro = snapshot.getValue(Basket.class);

                    Float pro_price = Float.valueOf(pro.getProductPrice());
                    total = total + pro_price;
                    to_pay_price.setText(total.toString());

                    gifbasket = findViewById(R.id.gifbasket);

                    if (pro == null) {
                        gifbasket.setVisibility(View.VISIBLE);
                    } else {
                        basket_check_empty_box = pro;
                        gifbasket.setVisibility(View.GONE);
                        productBusket.add(pro);
                    }
                }

                basket_content = findViewById(R.id.basket_content);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BasketActivity.this, RecyclerView.VERTICAL, false);
                basket_content.setLayoutManager(layoutManager);

                basketAdapter = new BasketAdapter(BasketActivity.this, productBusket);
                basket_content.setAdapter(basketAdapter);
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