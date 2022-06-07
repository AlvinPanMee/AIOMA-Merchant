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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mMerchantName, mEmail, mPassword, mConfirmPassword;

    CheckBox mFreeMembershipCB;

    EditText mInput_type;

    TextView mMerchantType;
    String merchantType;

    ImageView mMerchantPFP;

    StorageReference storageReference;
    Uri merchantPFPUri;
    String merchantPFPURL;

    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    ScrollView mScrollView;

    Integer selectedNum;
    String merchantTypeStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        mScrollView = findViewById(R.id.signUp_form_sv);

        mMerchantPFP = findViewById(R.id.add_merchant_logo);

        mMerchantName = findViewById(R.id.input_merchant_name);
        mEmail = findViewById(R.id.input_email);

        mFreeMembershipCB = findViewById(R.id.checkbox_free);
        mFreeMembershipCB.setChecked(true);

        selectedNum = -1;

        mMerchantType = (TextView) findViewById(R.id.select_type);
        mMerchantType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] businessTypes = {"Food & Beverage", "Clothing", "Grocery", "Others"};

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
                LayoutInflater layoutInflater = SignUpActivity.this.getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.dialog_merchant_type_picker, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("Select Business Type");

                mInput_type = dialogView.findViewById(R.id.input_type);
                mInput_type.setVisibility(View.INVISIBLE);
                mInput_type.setText(merchantType);

                dialogBuilder.setSingleChoiceItems(businessTypes, selectedNum, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectedNum = which;

                        merchantType = businessTypes[which];

                        if (selectedNum == 3){
                            mInput_type.setVisibility(View.VISIBLE);

                        } else {
                            mInput_type.setVisibility(View.INVISIBLE);
                        }



                    }
                });
                dialogBuilder.setNegativeButton("back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(merchantType.equals("Others")) {
                            merchantType = mInput_type.getText().toString().trim();

                            if (TextUtils.isEmpty(merchantType)){
                                mInput_type.setError("Business Type is required");

                            } else {
                                mMerchantType.setText(merchantType);
                                dialog.dismiss();
                            }


                        } else {
                            mMerchantType.setText(merchantType);
                            dialog.dismiss();
                        }

                    }
                });
            }
        });


        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);

        storageReference = FirebaseStorage.getInstance().getReference();

        mMerchantPFP = findViewById(R.id.add_merchant_logo);
        mMerchantPFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 2);

            }
        });

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        findViewById(R.id.sign_up_btn).setOnClickListener(this);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            merchantPFPUri = data.getData();
            mMerchantPFP.setImageURI(merchantPFPUri);
        }
    }


    @Override
    protected void onStart(){
        super.onStart();

        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
        }
    }


    //add information to database
    private void registerUser() {
        final String merchantName = mMerchantName.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmPassword = mConfirmPassword.getText().toString().trim();
        final String merchantPFPUrl = "merchantPFPURL";

    //Input Constraint
        if(merchantPFPUri == null && TextUtils.isEmpty(merchantName) && TextUtils.isEmpty(email) && merchantType == null
                && TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(SignUpActivity.this,"All fields are required",Toast.LENGTH_SHORT).show();
            return;
        }

        if (merchantPFPUri == null){
            Toast.makeText(SignUpActivity.this,"Please insert merchant logo",Toast.LENGTH_SHORT).show();
            return;
        }

        //username
        if (TextUtils.isEmpty(merchantName)) {
            mMerchantName.setError("Merchant name is required");
            return;
        }

        //email
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Merchant Email is required");
            return;
        }
        if (!emailPattern.matcher(email).matches()) {
            mEmail.setError("Invalid Email Address");
            return;
        }

        //merchant type
        if(merchantType == null){
            mMerchantType.setError("Please select a business type");
            return;
        }

        //password
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignUpActivity.this,
                    "Password is required", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(SignUpActivity.this,
                    "Password should have at least 6 characters", Toast.LENGTH_LONG).show();
            return;
        }

        //confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(SignUpActivity.this,
                    "Confirm Password is required", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this,
                    "Confirm Password does not match password", Toast.LENGTH_LONG).show();
            return;
        }

        //If constraints are okay, add to firebase database
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful() ) {
                        Toast.makeText(SignUpActivity.this,
                                "Error " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        uploadToFirebase(merchantPFPUri);

                        String merchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        Merchant merchantUser = new Merchant(merchantID, merchantName, email, merchantType, merchantPFPUrl);


                        FirebaseDatabase.getInstance().getReference("merchantUsers")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(merchantUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this,
                                            "initializing", Toast.LENGTH_SHORT).show();

                                } if(!task.isSuccessful()){
                                    Toast.makeText(SignUpActivity.this,
                                            "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });



    }



    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.sign_up_btn:
                registerUser();
                mMerchantName.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mEmail.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mConfirmPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
                break;
        }
    }

    public final Pattern emailPattern = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-zA-Z0-9]+\\.+[a-z]+"
    );

    protected void uploadToFirebase(Uri uri) {
        StorageReference fileRef = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()
                + "_logo." + getFileExtension(merchantPFPUri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        merchantPFPURL = uri.toString();

                        FirebaseDatabase.getInstance().getReference("merchantUsers")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("merchantPFPUrl")
                                .setValue(merchantPFPURL);

                        Toast.makeText(SignUpActivity.this,
                                "Merchant Registered Successfully", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(getApplicationContext(), HomeActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));


                        if(mFreeMembershipCB.isChecked()){
                            FirebaseDatabase.getInstance().getReference("merchantUsers")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("FreeMembership")
                                    .setValue(true);
                        }
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
                Toast.makeText(SignUpActivity.this, "Uploading Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


}