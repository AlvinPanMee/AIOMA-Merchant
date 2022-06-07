package com.bignerdranch.android.aiomamerchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class ProfileActivity extends AppCompatActivity {

    ImageView mMerchantPFP;
    TextView mMerchantName;
    TextView mEmail;
    TextView mType;

    DatabaseReference mMerchantUsersRef;

    String uid;

    ImageView homeNav, rewardNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        //Navbar
        homeNav = (ImageView) findViewById(R.id.home_nav);
        homeNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        rewardNav = (ImageView) findViewById(R.id.rewards_nav);
        rewardNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RewardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });

        ImageView mHomeBtn = (ImageView) findViewById(R.id.home_nav);
        mHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(ProfileActivity.this, HomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(homeIntent);
            }
        });


        //Set user information
        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);
        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mEmail = (TextView) findViewById(R.id.email);
        mType = (TextView) findViewById(R.id.type);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mMerchantUsersRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(uid);

        //display merchantPFP, merchantName & email from Firebase
        mMerchantUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String merchantPFPUrl = snapshot.child("merchantPFPUrl").getValue(String.class);
                String merchantName = snapshot.child("merchantName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String type = snapshot.child("type").getValue(String.class);


                Glide.with(mMerchantPFP).load(merchantPFPUrl).into(mMerchantPFP);
                mMerchantName.setText(merchantName);
                mEmail.setText(email);
                mType.setText(type);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        Button editProfileBtn = findViewById(R.id.edit_merchant_btn);
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditMerchantActivity.class);
                startActivity(intent);
            }
        });

        Button changePasswordBtn = findViewById(R.id.change_password_btn);
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        Button signOutBtn = findViewById(R.id.sign_out_btn);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });


    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),StartActivity.class));
        finish();
    }
}