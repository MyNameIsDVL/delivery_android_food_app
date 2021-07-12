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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.delivery_food_app.Model.AllFood;
import com.example.delivery_food_app.Model.FiveStarFood;
import com.example.delivery_food_app.ProductDetailsActivity;
import com.example.delivery_food_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AllFoodAdapter extends RecyclerView.Adapter<AllFoodAdapter.AllFoodViewHolder> {
    Context context;
    List<FiveStarFood> allFood;

    StorageReference storageReference;

    public AllFoodAdapter(Context context, List<FiveStarFood> allFood) {
        this.context = context;
        this.allFood = allFood;
    }

    @NonNull
    @Override
    public AllFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_food_view_item, parent, false);
        return new AllFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllFoodViewHolder holder, int position) {
        FiveStarFood food = allFood.get(position);
        holder.name.setText(food.getProductName());
        holder.price.setText(food.getProductPrice());
        holder.rating.setText("5.0");

        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("product_images/"+food.getId()+"/default.png");

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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra("foodId", food.getId());
                intent.putExtra("productName", food.getProductName());
                intent.putExtra("productDescription", food.getProductDescription());
                intent.putExtra("productCategory", food.getProductCategory());
                intent.putExtra("productPrice", food.getProductPrice());
                intent.putExtra("productFiveStar", food.getProductFiveStar());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allFood.size();
    }

    public static final class AllFoodViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name, price, rating;

        public AllFoodViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.foodImg);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}
