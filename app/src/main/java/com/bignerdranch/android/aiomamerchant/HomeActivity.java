package com.bignerdranch.android.aiomamerchant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    TextView mMerchantName;
    ImageView mMerchantPFP;

    String MerchantID;

    EditText mInputPoints;
    Button mGenerateQRCode;
    ImageView mQRCode;

    ImageView rewardNav, profileNav;

    String merchantName, merchantPFP, merchantID;

    String pointsEntered;


    ImageView mQRScannerBtn;

    //QR scan result
    String QRUserID;
    String QRUsername;
    String QRRewardID;
    String QRRewardTitle;
    String QRMerchantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        //Nav bar
        rewardNav = (ImageView) findViewById(R.id.rewards_nav);
        rewardNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RewardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });

        profileNav = (ImageView) findViewById(R.id.profile_nav);
        profileNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(HomeActivity.this, ProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent2);
            }
        });



        //Display merchant info
        MerchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mMerchantRef = databaseReference.child("merchantUsers");




        mMerchantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                merchantName = snapshot.child(MerchantID).child("merchantName").getValue(String.class);
                mMerchantName.setText(merchantName);

                merchantPFP = snapshot.child(MerchantID).child("merchantPFPUrl").getValue(String.class);
                Glide.with(mMerchantPFP.getContext()).load(merchantPFP).into(mMerchantPFP);


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        mMerchantPFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent addMembershipIntent = new Intent(HomeActivity.this, AddMembershipQRCodeActivity.class);
                addMembershipIntent.putExtra("merchantName", merchantName);
                addMembershipIntent.putExtra("merchantID", MerchantID);
                addMembershipIntent.putExtra("merchantPFPUrl", merchantPFP);
                startActivity(addMembershipIntent);

            }
        });

        Intent intent = getIntent();
        pointsEntered = intent.getStringExtra("pointsEntered");

        mInputPoints = (EditText) findViewById(R.id.input_points);
        if (pointsEntered != null) {
            mInputPoints.setText(pointsEntered);
        }

        mQRCode = (ImageView) findViewById(R.id.QR_code);

        mGenerateQRCode = findViewById(R.id.generate_QR_code);
        mGenerateQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String transactionPasscode = UUID.randomUUID().toString();

                FirebaseDatabase.getInstance().getReference("Transaction Passcode")
                        .child(transactionPasscode)
                        .setValue("Pending");

                //Get input value from editText
                String inputPoints = mInputPoints.getText().toString();

                if (TextUtils.isEmpty(inputPoints)) {
                    mInputPoints.setError("Please enter points");

                } else {
                    mMerchantRef.child(MerchantID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                            //Get merchant info
                            String merchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String merchantName = snapshot.child("merchantName").getValue().toString();

                            Intent intent = new Intent(getApplicationContext(), QRCodeActivity.class);
                            intent.putExtra("transactionPasscode", transactionPasscode);
                            intent.putExtra("merchantID", merchantID);
                            intent.putExtra("merchantName", merchantName);
                            intent.putExtra("membershipPoints", inputPoints);
                            startActivity(intent);

                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            Toast.makeText(HomeActivity.this, "Error: " + error
                                    , Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });


        mQRScannerBtn = findViewById(R.id.scan_QR_code_btn);
        mQRScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRScanner();
            }
        });

        merchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

        public void QRScanner(){

            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(CaptureAct.class);
            integrator.setOrientationLocked(false);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scanning QR Code");
            integrator.initiateScan();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (result != null) {
                if (result.getContents() != null) {

                    if(result.getContents().startsWith("User ID:")){
                        Intent intent = new Intent(getApplicationContext(), ConfirmRewardRedemptionActivity.class);
                        String[] tokens = result.getContents().split("\n");

                        for(int i = 0; i < tokens.length; i++){
                            System.out.println(" "+tokens[i]);

                            if(tokens[i].startsWith("User ID:")) {
                                QRUserID = tokens[i].substring(9);
                                intent.putExtra("user_ID", QRUserID);

                            } else if(tokens[i].startsWith("Username:")) {
                                QRUsername = tokens[i].substring(10);
                                intent.putExtra("username", QRUsername);

                            } else if(tokens[i].startsWith("Reward:")) {
                                QRRewardTitle = tokens[i].substring(8);
                                intent.putExtra("reward_Title", QRRewardTitle);

                            } else if (tokens[i].startsWith("Merchant:")){
                                QRMerchantName = tokens[i].substring(10);
                                intent.putExtra("merchant_Name", QRMerchantName);

                            } else if(tokens[i].startsWith("Reward ID:")) {
                                QRRewardID = tokens[i].substring(11);
                                intent.putExtra("reward_ID", QRRewardID);

                                DatabaseReference rewardListRef = FirebaseDatabase.getInstance()
                                        .getReference("merchantUsers")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("Rewards");

                                rewardListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild(QRRewardID)){
                                            startActivity(intent);

                                        } else{
                                            Toast.makeText(HomeActivity.this,
                                                    "Reward/Voucher Not Found",
                                                    Toast.LENGTH_LONG).show();
                                            Intent intent1 = new Intent(HomeActivity.this, HomeActivity.class);
                                            startActivity(intent1);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });


                            }

                        }

                    } else if(result.getContents().startsWith("Verify Membership")){
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        String[] tokens = result.getContents().split("\n");

                        for(int i = 0; i < tokens.length; i++){
                            System.out.println(" "+tokens[i]);

                            if(tokens[i].startsWith("User ID:")) {
                                QRUserID = tokens[i].substring(9);
                                intent.putExtra("user_ID", QRUserID);

                                DatabaseReference userMembershipsRef = FirebaseDatabase.getInstance()
                                        .getReference("customerUsers")
                                        .child(QRUserID)
                                        .child("Memberships");

                                userMembershipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild(merchantID)){

                                            Intent verifiedMemberIntent = new Intent(HomeActivity.this, VerifyMemberActivity.class);
                                            verifiedMemberIntent.putExtra("userID", QRUserID);
                                            startActivity(verifiedMemberIntent);


                                        } else {
                                            Toast.makeText(HomeActivity.this, "This user is not a member", Toast.LENGTH_LONG).show();

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });

                            }

                        }

                    } else {
                        Toast.makeText(HomeActivity.this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "No Result", Toast.LENGTH_SHORT).show();
                }

            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }



    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),StartActivity.class));
        finish();
    }

    public void generateQRCode(){

    }

    public void onBackPressed(){
        //no activity to go back

    }

}

