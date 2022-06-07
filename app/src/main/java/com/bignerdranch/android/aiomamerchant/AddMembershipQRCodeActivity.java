package com.bignerdranch.android.aiomamerchant;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class AddMembershipQRCodeActivity extends AppCompatActivity {

    ImageView QRCodeImageView;

    ImageView mMerchantPFP;
    TextView mMerchantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_membership_qrcode);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        getSupportActionBar().setElevation(0);




        Intent intent = getIntent();
        String merchantID = intent.getStringExtra("merchantID");
        String merchantName = intent.getStringExtra("merchantName");
        String merchantPFPUrl = intent.getStringExtra("merchantPFPUrl");

        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);
        Glide.with(mMerchantPFP).load(merchantPFPUrl).into(mMerchantPFP);

        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantName.setText(merchantName);

        QRCodeImageView = (ImageView) findViewById(R.id.QR_code);

        //Initialise multi format writer
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //Initialise bit matrix
            BitMatrix bitMatrix = multiFormatWriter.encode(
                    "Merchant Name: " + merchantName + "\n" +
                            "Merchant ID: " + merchantID + "\n" +
                            "merchantPFPUrl:" + merchantPFPUrl
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
    }
}