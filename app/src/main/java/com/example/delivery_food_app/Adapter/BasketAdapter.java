/*
 * Copyright (c) 2021 | Mateusz Hus.
 * The content of this application, including (but not limited to) all written material, images, photos, and code, are protected under international copyright and trademark laws. You may not copy, reproduce, modify, republish, transmit or distribute any material from this application.
 *
 * Author: Mateusz Hus
 *
 */

package com.example.delivery_food_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delivery_food_app.BasketActivity;
import com.example.delivery_food_app.Model.Basket;
import com.example.delivery_food_app.Model.FiveStarFood;
import com.example.delivery_food_app.ProductDetailsActivity;
import com.example.delivery_food_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketViewHolder> {
    Context context;
    List<Basket> basketFood;

    StorageReference storageReference;

    FirebaseAuth fAuth;
    String userId;

    public BasketAdapter(Context context, List<Basket> basketFood) {
        this.context = context;
        this.basketFood = basketFood;
    }

    @NonNull
    @Override
    public BasketAdapter.BasketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.basket_food_view_item, parent, false);
        return new BasketAdapter.BasketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BasketAdapter.BasketViewHolder holder, int position) {

        Basket food = basketFood.get(position);
        holder.name.setText(food.getProductName());
        holder.price.setText(food.getProductPrice());
        holder.category.setText(food.getProductCategory());

        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("product_images/"+food.getProductId()+"/default.png");

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get().load(R.mipmap.ic_launcher_round).into(holder.image);
                Log.d("tag", "Ustawiony został obrazek domyślny");
            }
        });

        holder.itemView.findViewById(R.id.deleteItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fAuth = FirebaseAuth.getInstance();
                userId = fAuth.getCurrentUser().getUid();

                DatabaseReference reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("basket_added");

                Query query = reference.child(food.getIdKey());

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

    @Override
    public int getItemCount() {
        return basketFood.size();
    }

    public static final class BasketViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name, price, category;
        CardView deleteItem;

        public BasketViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.foodImg);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            category = itemView.findViewById(R.id.category);
            deleteItem = itemView.findViewById(R.id.deleteItem);
        }
    }
}
