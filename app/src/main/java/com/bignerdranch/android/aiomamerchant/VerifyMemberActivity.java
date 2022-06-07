package com.bignerdranch.android.aiomamerchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class VerifyMemberActivity extends AppCompatActivity {

    EditText mInputPoint;
    Button mGrantPointsBtn;

    Integer points;

    String userID;
    String merchantID;
    Integer currentPoints;

    String transactionID;
    String merchantName;
    String date;

    DatabaseReference customerUserPointsRef;
    DatabaseReference merchantUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_member);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        Intent verifiedMemberIntent = getIntent();
        userID = verifiedMemberIntent.getStringExtra("userID");

        merchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        transactionID = UUID.randomUUID().toString();

        customerUserPointsRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(userID)
                .child("Memberships")
                .child(merchantID)
                .child("membershipPoints");

        customerUserPointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                currentPoints = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        merchantUserRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("merchantName");

        merchantUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                merchantName = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        Date currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy, HH:mm:ss");
        date = dateFormat.format(currentTime);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mInputPoint = (EditText) findViewById(R.id.input_points);

        mGrantPointsBtn = (Button) findViewById(R.id.grant_points_to_user);
        mGrantPointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pointsStr = mInputPoint.getText().toString().trim();

                if(TextUtils.isEmpty(pointsStr)){
                    mInputPoint.setError("Please enter points to grant customer");
                    return;
                }


                PendingTransaction pendingTransaction = new PendingTransaction(transactionID, merchantID, merchantName,
                        pointsStr);

                FirebaseDatabase.getInstance().getReference("customerUsers")
                        .child(userID)
                        .child("Pending Transaction")
                        .child(transactionID)
                        .setValue(pendingTransaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(VerifyMemberActivity.this,
                                pointsStr + " points granted", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(VerifyMemberActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}