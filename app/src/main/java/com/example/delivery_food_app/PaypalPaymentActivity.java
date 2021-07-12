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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delivery_food_app.ConfigPayments.PaypalConfigID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

public class PaypalPaymentActivity extends AppCompatActivity {

    Button payByPaypal;

    Intent data;

    TextView to_pay_price;

    DatabaseReference reference;

    private int PAYPAL_REQ_CODE = 12;
    private static PayPalConfiguration payPalConfiguration = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PaypalConfigID.PAYPAL_CLIENT_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal_payment);

        data = getIntent();

        payByPaypal = findViewById(R.id.payByPaypal);
        to_pay_price = findViewById(R.id.to_pay_price);

        to_pay_price.setText(data.getStringExtra("totalPrice"));

        Intent intent = new Intent(PaypalPaymentActivity.this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfiguration);
        startService(intent);

        payByPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPaypalPayment(data);
            }
        });

    }

    private void getPaypalPayment(Intent data) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(data.getStringExtra("totalPrice")), "PLN", "Twoje id: " + data.getStringExtra("orderedUserId"), PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(PaypalPaymentActivity.this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfiguration);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PAYPAL_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYPAL_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // for users order operations
                reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("orders_payed");

                DatabaseReference newChildRef = reference.push();
                // store data

                String key = newChildRef.getKey();

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("ClientId", data.getStringExtra("orderedUserId"));
                hashMap.put("KeyId", key);
                hashMap.put("Phone", data.getStringExtra("Phone"));
                hashMap.put("Location", data.getStringExtra("Location"));
                hashMap.put("PaymentResult", "opłacone");
                hashMap.put("PaymentMethod", "paypal");
                hashMap.put("PaymentDate", new Date().toString());

                reference.child(key).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("TAG", "Pomyślnie dodano zamówienie dla "+ data.getStringExtra("orderedUserId"));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Błąd "+ data.getStringExtra("orderedUserId"));
                    }
                });

                // remove all items from basket

                DatabaseReference reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("basket_added");

                Query query = reference.orderByChild("IdUser").equalTo(data.getStringExtra("orderedUserId"));

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

                Toast.makeText(this, "Płatność przebiegła pomyślnie. Dziękujemy i zapraszamy ponownie", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Płatność została anulowana", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    public void cardBack(View view) {
        startActivity(new Intent(getApplicationContext(), BasketActivity.class));
        finish();
    }
}