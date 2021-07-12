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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Date;
import java.util.HashMap;

public class SummaryActivity extends AppCompatActivity {

    RadioGroup allmethods;
    RadioButton radioButton;
    CardView cardFormPayMethods, cardFormDeliveryData, cardFormGetPlace;
    EditText phoneNumber, deliveryAddress, phoneNumber_delivery_pay_on, deliveryAddress_delivery_pay_on;

    Intent data;

    TextView to_pay_price;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userId;
    DatabaseReference reference;

    RadioButton radio_payU, radio_payPal, radio_payPrzelewy24;
    Button payOnlineWithMethods, payInDeliveryHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarColor();
        setContentView(R.layout.activity_summary);

        to_pay_price = findViewById(R.id.to_pay_price);

        phoneNumber = findViewById(R.id.phoneNumber);
        deliveryAddress = findViewById(R.id.deliveryAddress);
        phoneNumber_delivery_pay_on = findViewById(R.id.phoneNumber_delivery_pay_on);
        deliveryAddress_delivery_pay_on = findViewById(R.id.deliveryAddress_delivery_pay_on);

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();

        data = getIntent();

        to_pay_price.setText(data.getStringExtra("totalPrice"));

        // delivery home, set data to EditTexts

        DocumentReference documentReference = fStore.collection("users_info").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshot.exists()) {
                        phoneNumber.setText(documentSnapshot.getString("Phone"));
                        deliveryAddress.setText(documentSnapshot.getString("Location"));
                        phoneNumber_delivery_pay_on.setText(documentSnapshot.getString("Phone"));
                        deliveryAddress_delivery_pay_on.setText(documentSnapshot.getString("Location"));
                    } else {
                        Log.d("tag", "Document nie istnieje");
                    }
                }
            }
        });

        radio_payU = findViewById(R.id.radio_payU);
        radio_payPal = findViewById(R.id.radio_payPal);
        radio_payPrzelewy24 = findViewById(R.id.radio_payPrzelewy24);
        payOnlineWithMethods = findViewById(R.id.payOnlineWithMethods);
        payInDeliveryHome = findViewById(R.id.payInDeliveryHome);

        // pay online

        payOnlineWithMethods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radio_payPal.isChecked()) {
                    Intent intent = new Intent(SummaryActivity.this, PaypalPaymentActivity.class);
                    intent.putExtra("orderedUserId", userId);
                    intent.putExtra("totalPrice", data.getStringExtra("totalPrice"));
                    intent.putExtra("phone", phoneNumber_delivery_pay_on.getText().toString());
                    intent.putExtra("deliveryAddress", deliveryAddress_delivery_pay_on.getText().toString());
                    startActivity(intent);
                }
            }
        });

        // pay on delivery

        payInDeliveryHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // order to home
                reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("orders_pay_on_delivery");

                DatabaseReference newChildRef = reference.push();
                // store data

                String key = newChildRef.getKey();

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("ClientId", userId);
                hashMap.put("KeyId", key);
                hashMap.put("Phone", phoneNumber.getText().toString());
                hashMap.put("Location", deliveryAddress.getText().toString());
                hashMap.put("PaymentResult", "brak");
                hashMap.put("PaymentMethod", "kartą przy odbiorze");
                hashMap.put("OrderDate", new Date().toString());

                reference.child(key).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(SummaryActivity.this, "Zamówienie zostało przyjęte. Dziękujemy.", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Pomyślnie dodano zamówienie dla "+ userId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SummaryActivity.this, "Nie udało się złożyć zamówienia. Prosimy spróbować ponownie później.", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Błąd "+ userId);
                    }
                });

                // remove all items from basket

                DatabaseReference reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("basket_added");

                Query query = reference.orderByChild("IdUser").equalTo(userId);

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    public void checkBtn(View view) {
        cardFormPayMethods = findViewById(R.id.cardFormPayMethods);
        cardFormDeliveryData = findViewById(R.id.cardFormDeliveryData);
        cardFormGetPlace = findViewById(R.id.cardFormGetPlace);

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_payOnline:
                if (checked)
                    cardFormPayMethods.setVisibility(View.VISIBLE);
                    cardFormDeliveryData.setVisibility(View.GONE);
                    cardFormGetPlace.setVisibility(View.GONE);
                    break;
            case R.id.radio_payCardWhenDelivery:
                if (checked)
                    cardFormPayMethods.setVisibility(View.GONE);
                    cardFormDeliveryData.setVisibility(View.VISIBLE);
                    cardFormGetPlace.setVisibility(View.GONE);
                    break;
            case R.id.radio_payOnPlace:
                if (checked)
                    cardFormPayMethods.setVisibility(View.GONE);
                    cardFormDeliveryData.setVisibility(View.GONE);
                    cardFormGetPlace.setVisibility(View.VISIBLE);
                    break;
        }
    }

    public void cardBack(View view) {
        startActivity(new Intent(getApplicationContext(), BasketActivity.class));
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