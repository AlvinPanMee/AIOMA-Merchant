package com.bignerdranch.android.aiomamerchant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class EditRewardActivity extends AppCompatActivity {

    //TextView & ImageView
    TextView mMerchantName;
    ImageView mMerchantPFP;

    TextView mRewardTitle;
    ImageView mRewardIcon;
    TextView mPointsRequired;
    TextView mRewardDesc;
    TextView mRewardPolicy;

    Button mSaveChangeBtn;

    ProgressBar progressBar;


    //String & Integer
    String merchantName;
    String merchantPFPUrl;

    String rewardID;
    String rewardTitle;
    String rewardIconUrl;
    String newRewardIconUrl;
    Uri rewardIconUri;

    Integer pointRequired;
    String rewardDesc;
    String rewardPolicy;


    DatabaseReference merchantRef;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reward);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");


        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);

        merchantRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        merchantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                merchantName = snapshot.child("merchantName").getValue(String.class);
                merchantPFPUrl = snapshot.child("merchantPFPUrl").getValue(String.class);

                mMerchantName.setText(merchantName);
                Glide.with(mMerchantPFP.getContext()).load(merchantPFPUrl).into(mMerchantPFP);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //get reward title based on clicked reward from RewardAdapterClass
        Intent singleRewardIntent = getIntent();
        rewardID = singleRewardIntent.getStringExtra("rewardID");

        mRewardTitle = (EditText) findViewById(R.id.input_reward_title);
        mRewardIcon = (ImageView) findViewById(R.id.reward_icon);
        mPointsRequired = (EditText) findViewById(R.id.input_points_required);
        mRewardDesc = (EditText) findViewById(R.id.input_reward_desc);
        mRewardPolicy = (EditText) findViewById(R.id.input_reward_policy);

        merchantRef.child("Rewards").child(rewardID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                rewardTitle = snapshot.child("rewardTitle").getValue(String.class);
                rewardIconUrl = snapshot.child("rewardIconUrl").getValue(String.class);
                pointRequired = snapshot.child("pointsRequired").getValue(int.class);
                rewardDesc = snapshot.child("rewardDesc").getValue(String.class);
                rewardPolicy = snapshot.child("rewardPolicy").getValue(String.class);

                mRewardTitle.setText(rewardTitle);
                Glide.with(mRewardIcon.getContext()).load(rewardIconUrl).into(mRewardIcon);
                mPointsRequired.setText(String.valueOf(pointRequired));
                mRewardDesc.setText(rewardDesc);
                mRewardPolicy.setText(rewardPolicy);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        mRewardIcon = findViewById(R.id.reward_icon);
        mRewardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 3);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 3 && resultCode == RESULT_OK && data != null){
            rewardIconUri = data.getData();
            mRewardIcon.setImageURI(rewardIconUri);
            getRewardIconUrl(rewardIconUri);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSaveChangeBtn = (Button) findViewById(R.id.save_change_btn);
        mSaveChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String newRewardTitle = mRewardTitle.getText().toString().trim();
                final Integer newPointsRequired = Integer.parseInt(mPointsRequired.getText().toString());
                final String newRewardDesc = mRewardDesc.getText().toString().trim();
                final String newRewardPolicy = mRewardPolicy.getText().toString().trim();

                if (newRewardTitle.equals(rewardTitle) && newPointsRequired.equals(pointRequired)
                        && newRewardDesc.equals(rewardDesc) && newRewardPolicy.equals(rewardPolicy)
                        && newRewardIconUrl == null) {

                    Toast.makeText(EditRewardActivity.this, "No field has been changed",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if(TextUtils.isEmpty(newRewardTitle)){
                    Toast.makeText(EditRewardActivity.this, "Reward Title is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(String.valueOf(newPointsRequired))){
                    Toast.makeText(EditRewardActivity.this, "Points Required is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(newRewardDesc)){
                    Toast.makeText(EditRewardActivity.this, "Reward Description is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(newRewardPolicy)){
                    Toast.makeText(EditRewardActivity.this, "Reward Policy is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(newRewardIconUrl == null){
                    //Do nothing

                } else if (!newRewardIconUrl.equals(rewardIconUrl)){
                    merchantRef.child("Rewards").child(rewardID).child("rewardIconUrl")
                            .setValue(newRewardIconUrl);
                }


                if(!newRewardTitle.equals(rewardTitle)){
                    merchantRef.child("Rewards").child(rewardID).child("rewardTitle")
                            .setValue(newRewardTitle);
                }

                if(!newPointsRequired.equals(pointRequired)){
                    merchantRef.child("Rewards").child(rewardID).child("pointsRequired")
                            .setValue(newPointsRequired);
                }


                if (!newRewardDesc.equals(rewardDesc)){
                    merchantRef.child("Rewards").child(rewardID).child("rewardDesc")
                            .setValue(newRewardDesc);
                }



                if (!newRewardPolicy.equals(rewardPolicy)){
                    merchantRef.child("Rewards").child(rewardID).child("rewardPolicy")
                            .setValue(newRewardPolicy);

                }

                Toast.makeText(EditRewardActivity.this, "Reward has been edited",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(EditRewardActivity.this, RewardActivity.class);
                startActivity(intent);

            }
        });

    }

    protected void getRewardIconUrl(Uri uri) {
        StorageReference storageRef = storageReference.child(merchantName + " - " + rewardID +
                "_reward_icon." + getFileExtension(rewardIconUri));
        storageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newRewardIconUrl = uri.toString();

                        progressBar.setVisibility(View.INVISIBLE);

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
                Toast.makeText(EditRewardActivity.this, "Uploading Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater ();
        inflater.inflate(R.menu.activity_edit_reward, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.delete_reward:

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditRewardActivity.this);
                LayoutInflater layoutInflater = EditRewardActivity.this.getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.dialog_delete_reward, null);
                dialogBuilder.setView(dialogView);


                ImageView reward_icon = dialogView.findViewById(R.id.reward_icon);
                Glide.with(reward_icon.getRootView()).load(rewardIconUrl).
                        into(reward_icon);

                TextView add_membership_message = dialogView.findViewById(R.id.delete_reward_message);
                add_membership_message.setText(Html.fromHtml("Are you sure you want to delete "
                        + "<b>" + rewardTitle + "</b>" + " reward/voucher?"));

                dialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditRewardActivity.this, rewardTitle + " has been deleted",
                                Toast.LENGTH_LONG).show();

                        merchantRef.child("Rewards").child(rewardID).removeValue();
                        Intent intent = new Intent(EditRewardActivity.this, RewardActivity.class);
                        startActivity(intent);
                    }
                });

                dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(EditRewardActivity.this, RewardActivity.class);
        startActivity(intent);

    }
}