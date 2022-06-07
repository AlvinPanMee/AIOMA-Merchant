package com.bignerdranch.android.aiomamerchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ConfirmRewardRedemptionActivity extends HomeActivity {

    TextView mRewardTitle, mUsername, mMerchantName, mRewardDesc, mDate;

    Button mScanAgainBtn, mConfirmBtn;

    String userID, username, rewardID, merchantName;

    String rewardRedeemedTransactionID, date, rewardTitle, rewardDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_reward_redemption);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5fabc8")));

        Intent intent = getIntent();
        userID = intent.getStringExtra("user_ID");
        username = intent.getStringExtra("username");
        rewardID = intent.getStringExtra("reward_ID");
        merchantName = intent.getStringExtra("merchant_Name");

        mUsername = (TextView) findViewById(R.id.username);
        mUsername.setText(username);

        mRewardTitle = (TextView) findViewById(R.id.reward_title);

        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantName.setText(merchantName);;

        mRewardDesc = (TextView) findViewById(R.id.reward_desc);

        DatabaseReference redeemedUsersRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Rewards")
                .child(rewardID)
                .child("redeemedUsers");

        redeemedUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.hasChild(userID)){
                    Toast.makeText(ConfirmRewardRedemptionActivity.this,
                            "User has already redeemed this reward/voucher",
                            Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(ConfirmRewardRedemptionActivity.this, HomeActivity.class);
                    startActivity(intent1);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        DatabaseReference rewardRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Rewards")
                .child(rewardID);

        rewardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                rewardTitle = snapshot.child("rewardTitle").getValue(String.class);
                rewardDesc = snapshot.child("rewardDesc").getValue(String.class);

                mRewardTitle.setText(rewardTitle);
                mRewardDesc.setText(rewardDesc);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        rewardRedeemedTransactionID = UUID.randomUUID().toString();

        mDate = (TextView) findViewById(R.id.date);
        Date currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy, HH:mm:ss");
        mDate.setText(dateFormat.format(currentTime));
        date = dateFormat.format(currentTime);

        mScanAgainBtn = (Button) findViewById(R.id.scan_again_btn);
        mScanAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRScanner();
            }
        });

        mConfirmBtn = (Button) findViewById(R.id.confirm_btn);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference mAddRedeemedUserRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Rewards")
                        .child(rewardID)
                        .child("redeemedUsers")
                        .child(userID);

                mAddRedeemedUserRef.child("onDate").setValue(dateFormat.format(currentTime));
                mAddRedeemedUserRef.child("userID").setValue(userID);

                Transaction transaction = new Transaction(rewardRedeemedTransactionID,
                        merchantID, merchantName, "", date, rewardTitle + " used at");

                FirebaseDatabase.getInstance().getReference("customerUsers")
                        .child(userID)
                        .child("Transactions")
                        .child(rewardRedeemedTransactionID)
                        .setValue(transaction);


                Toast.makeText(ConfirmRewardRedemptionActivity.this,
                        "User has successfully redeemed reward"
                        , Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ConfirmRewardRedemptionActivity.this, RewardActivity.class);
                startActivity(intent);
            }
        });

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ConfirmRewardRedemptionActivity.this, RewardActivity.class);
        startActivity(intent);
    }
}