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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.delivery_food_app.Adapter.AllFoodAdapter;
import com.example.delivery_food_app.Adapter.PopularFoodAdapter;
import com.example.delivery_food_app.Model.AllFood;
import com.example.delivery_food_app.Model.FiveStarFood;
import com.example.delivery_food_app.Model.Popular;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.navigation.NavigationView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class HomeActivity extends AppCompatActivity {

    RecyclerView food_recycler, all_food;
    PopularFoodAdapter popularFoodAdapter;
    AllFoodAdapter allFoodAdapter;

    DrawerLayout drawer_layout_sidebar;
    BottomAppBar bottomAppBar2;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;

    TextView UserName, dis_user_name, dis_user_email;
    CircleImageView profile_img, profile_img_sidebar;

    String userId;

    Menu optionsMenu;

    public List<FiveStarFood> product;
    public List<FiveStarFood> allFoods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarColor();
        setContentView(R.layout.activity_home);

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        UserName = findViewById(R.id.UserName);
        profile_img = findViewById(R.id.profile_img);

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.sidebar_left_header);
        profile_img_sidebar = (CircleImageView)headerView.findViewById(R.id.profile_img_sidebar);
        dis_user_name = (TextView) headerView.findViewById(R.id.dis_user_name);
        dis_user_email = (TextView)headerView.findViewById(R.id.dis_user_email);

        product = new ArrayList<>();

        getFiveStarFood();

        allFoods = new ArrayList<>();

        getAllFood();

//        getDynamicMenuCategory(allFoods);

        // sidebar

        drawer_layout_sidebar = findViewById(R.id.drawer_layout_sidebar);
        bottomAppBar2 = findViewById(R.id.bottomAppBar2);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout_sidebar, bottomAppBar2, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout_sidebar.addDrawerListener(toggle);
        toggle.syncState();

        // set logged user name in header

        DocumentReference documentReference = fStore.collection("users_info").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshot.exists()) {
                        UserName.setText(documentSnapshot.getString("UserName"));
                        dis_user_name.setText(documentSnapshot.getString("UserName"));
                        dis_user_email.setText(documentSnapshot.getString("Email"));

                    } else {
                        Log.d("tag", "Document nie istnieje");
                    }
                }
            }
        });

        // set img profile home

        StorageReference profileRef = storageReference.child("users_img/"+fAuth.getCurrentUser().getUid()+"/default.png");

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile_img);
                Picasso.get().load(uri).into(profile_img_sidebar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("tag", "Ustawiony został awatar domyślny");
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.drawer_sidebar, menu);
//        //  store the menu to var when creating options menu
//        optionsMenu = menu;
//        return false;
//    }
//
//    private void getDynamicMenuCategory(List<FiveStarFood> foods) {
//        MenuItem item = optionsMenu.findItem(R.id.category_menu);
//        optionsMenu.addSubMenu((CharSequence) item);
//
//        for (int i = 0; i < foods.size(); i++) {
//            Log.d("MENUCr", String.valueOf(foods));
//        }
//    }

    private void getFiveStarFood() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("products");

        Query query = reference.orderByChild("ProductFiveStar").equalTo("tak");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                product.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FiveStarFood pro = snapshot.getValue(FiveStarFood.class);

                    product.add(pro);
                }

                food_recycler = findViewById(R.id.food_recycler);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this, RecyclerView.HORIZONTAL, false);
                food_recycler.setLayoutManager(layoutManager);

                popularFoodAdapter = new PopularFoodAdapter(HomeActivity.this, product);
                food_recycler.setAdapter(popularFoodAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllFood() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://deliveryfoodapp-62868-default-rtdb.europe-west1.firebasedatabase.app/").getReference("products");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allFoods.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FiveStarFood pro = snapshot.getValue(FiveStarFood.class);

                    allFoods.add(pro);
                }

                all_food = findViewById(R.id.asia_food);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this, RecyclerView.VERTICAL, false);
                all_food.setLayoutManager(layoutManager);

                allFoodAdapter = new AllFoodAdapter(HomeActivity.this, allFoods);
                all_food.setAdapter(allFoodAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout_sidebar.isDrawerOpen(GravityCompat.START)) {
            drawer_layout_sidebar.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    public void toProfile() {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        finish();
    }

    public void toFavourite() {
        startActivity(new Intent(getApplicationContext(), FavoActivity.class));
        finish();
    }

    public void floatingActionButton(View view) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.account:
                toProfile();
                return true;
            case R.id.heart:
                toFavourite();
                return true;
            case R.id.nav_profile:
                toProfile();
                return true;
            case R.id.nav_favourite:
                toFavourite();
                return true;
            case R.id.nav_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}