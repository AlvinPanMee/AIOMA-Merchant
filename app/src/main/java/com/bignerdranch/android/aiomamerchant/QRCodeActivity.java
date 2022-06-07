package com.bignerdranch.android.aiomamerchant;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeActivity extends AppCompatActivity {

    ImageView QRCodeImageView;
    Button mChangePointsBtn;

    String membershipPoints;

    String transactionPasscode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);


        QRCodeImageView = (ImageView) findViewById(R.id.QR_code);

        Intent intent = getIntent();
        transactionPasscode = intent.getStringExtra("transactionPasscode");
        String merchantID = intent.getStringExtra("merchantID");
        String merchantName = intent.getStringExtra("merchantName");
            actionBar.setTitle(merchantName);

        membershipPoints = intent.getStringExtra("membershipPoints");


        //Initialise multi format writer
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //Initialise bit matrix
            BitMatrix bitMatrix = multiFormatWriter.encode(
                    "Grant Membership Points" + "\n" +
                            "Merchant ID: " + merchantID + "\n" +
                            "Merchant: " + merchantName + "\n" +
                            "Points: " + membershipPoints + "\n" +
                            "Transaction Passcode: " + transactionPasscode
                    , BarcodeFormat.QR_CODE, 1000, 1000);

            //Initialise barcode encoder
            BarcodeEncoder encoder = new BarcodeEncoder();

            //Initialise bitmap
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            //Set bitmap on imageView
            QRCodeImageView.setImageBitmap(bitmap);


        } catch (
                WriterException e) {
            e.printStackTrace();
        }

        mChangePointsBtn = (Button) findViewById(R.id.change_points);
        mChangePointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QRCodeActivity.this, HomeActivity.class);
                intent.putExtra("pointsEntered", membershipPoints);
                startActivity(intent);

                FirebaseDatabase.getInstance().getReference("Transaction Passcode")
                        .child(transactionPasscode)
                        .removeValue();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(QRCodeActivity.this, HomeActivity.class);
        startActivity(intent);

        FirebaseDatabase.getInstance().getReference("Transaction Passcode")
                .child(transactionPasscode)
                .removeValue();
    }
}