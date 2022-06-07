package com.bignerdranch.android.aiomamerchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;

import java.security.Policy;
import java.util.ArrayList;

public class RewardActivity extends AppCompatActivity {

    RecyclerView mRewardsList;

    DatabaseReference mRewardsDatabaseRef;

    ArrayList<CreatedRewards> list;

    TextView createRewardMessage;

    ImageView homeNav, profileNav;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");


        //Nav bar
        homeNav = (ImageView) findViewById(R.id.home_nav);
        homeNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RewardActivity.this, HomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        profileNav = (ImageView) findViewById(R.id.profile_nav);
        profileNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(RewardActivity.this, ProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent2);
            }
        });

        Button createReward = (Button) findViewById(R.id.create_reward_btn);
        createReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RewardActivity.this, CreateRewardActivity.class);
                startActivity(intent);
            }
        });

        mRewardsList = (RecyclerView) findViewById(R.id.rewards_list);
        mRewardsList.setHasFixedSize(true);
        mRewardsList.setLayoutManager(new LinearLayoutManager(this));

        //Set recyclerview layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mRewardsList.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView

        mRewardsDatabaseRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Rewards");

        createRewardMessage = (TextView) findViewById(R.id.create_reward_message);
        createRewardMessage.setVisibility(View.INVISIBLE);




    }

    @Override
    protected void onStart() {
        super.onStart();

        mRewardsDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    createRewardMessage.setVisibility(View.INVISIBLE);

                    mRewardsDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                list = new ArrayList<>();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    list.add(dataSnapshot.getValue(CreatedRewards.class));
                                }
                                RewardAdapterClass rewardAdapterClass = new RewardAdapterClass(list);
                                mRewardsList.setAdapter(rewardAdapterClass);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            Toast.makeText(RewardActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    createRewardMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }





    public void onBackPressed(){
        //no activity to go back

    }

}