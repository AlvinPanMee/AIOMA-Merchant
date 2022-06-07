package com.bignerdranch.android.aiomamerchant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.regex.Pattern;

public class CreateRewardActivity extends AppCompatActivity implements View.OnClickListener {

    TextView mMerchantName;
    ImageView mMerchantPFP;

    String MerchantID;
    String merchantName;

    EditText mRewardTitle, mPointsRequired, mRewardDesc, mRewardPolicy;

    private ImageView mRewardIcon;

    private StorageReference storageReference;
    private Uri rewardIconUri;
    public String rewardIconURL;

    Button mCreateRewardBtn;

    private ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    ScrollView mScrollView;

    String rewardTitle;
    String rewardID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reward);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);

        MerchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rewardID = UUID.randomUUID().toString();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mMerchantRef = databaseReference.child("merchantUsers");


        mMerchantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                merchantName = snapshot.child(MerchantID).child("merchantName").getValue(String.class);
                mMerchantName.setText(merchantName);

                String merchantPFPUrl = snapshot.child(MerchantID).child("merchantPFPUrl").getValue(String.class);
                Glide.with(mMerchantPFP).load(merchantPFPUrl).into(mMerchantPFP);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        mScrollView = (ScrollView) findViewById(R.id.create_reward_form_sv);

        mRewardTitle = (EditText) findViewById(R.id.input_reward_title);
        mPointsRequired = (EditText) findViewById(R.id.input_points_required);
        mRewardDesc = (EditText) findViewById(R.id.input_reward_desc);
        mRewardPolicy = (EditText) findViewById(R.id.input_policy);

        mCreateRewardBtn = (Button) findViewById(R.id.create_reward_btn);


        storageReference = FirebaseStorage.getInstance().getReference();

        mRewardIcon = findViewById(R.id.add_reward_icon);
        mRewardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 2);

            }
        });

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        findViewById(R.id.create_reward_btn).setOnClickListener(this);

    }

    public void createReward(){
        rewardTitle = mRewardTitle.getText().toString().trim();
        final Integer pointsRequired = Integer.parseInt(mPointsRequired.getText().toString());
        final String rewardDesc = mRewardDesc.getText().toString().trim();
        final String rewardPolicy = mRewardPolicy.getText().toString().trim();
        final String rewardIconUrl = "rewardIconURL";

        if (rewardIconUri == null){
            Toast.makeText(CreateRewardActivity.this,"Please insert reward picture",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(rewardTitle)){
            mRewardTitle.setError("Reward title is required");
            Toast.makeText(CreateRewardActivity.this,
                    "Error " + mRewardTitle.getError(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(pointsRequired == null) {
            mPointsRequired.setError("Please enter minimum points to redeem this reward");
            Toast.makeText(CreateRewardActivity.this,
                    "Error " + mPointsRequired.getError(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(rewardDesc)){
            mRewardDesc.setError("Please enter reward reward description");
            Toast.makeText(CreateRewardActivity.this,
                    "Error " + mRewardDesc.getError(),
                    Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(rewardPolicy)){
            mRewardPolicy.setError("Please enter reward reward policy / terms and conditions");
            Toast.makeText(CreateRewardActivity.this,
                    "Error " + mRewardPolicy.getError(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        else {
            uploadToFirebase(rewardIconUri);

            DatabaseReference add_reward_ref = FirebaseDatabase.getInstance()
                    .getReference("merchantUsers")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Rewards")
                    .child(rewardID);

            //construct in Rewards class
            Rewards reward = new Rewards(rewardID, rewardTitle, pointsRequired, rewardDesc, rewardPolicy, rewardIconUrl);

            //Add/set value in database
            add_reward_ref.setValue(reward);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            rewardIconUri = data.getData();
            mRewardIcon.setImageURI(rewardIconUri);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_reward_btn:
                createReward();
                mRewardTitle.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mPointsRequired.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mRewardPolicy.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
                break;
        }
    }

    protected void uploadToFirebase(Uri uri) {
        StorageReference fileRef = storageReference.child(merchantName + " - " + rewardID +
                "_reward_icon." + getFileExtension(rewardIconUri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        rewardIconURL = uri.toString();

                        FirebaseDatabase.getInstance().getReference("merchantUsers")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("Rewards")
                                .child(rewardID)
                                .child("rewardIconUrl")
                                .setValue(rewardIconURL);

                        startActivity(new Intent(CreateRewardActivity.this, RewardActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(CreateRewardActivity.this, "Uploading Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

}