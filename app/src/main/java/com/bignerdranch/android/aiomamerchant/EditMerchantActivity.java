package com.bignerdranch.android.aiomamerchant;

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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.util.regex.Pattern;

public class EditMerchantActivity extends AppCompatActivity {

    private static final String TAG = "PasswordCheck";

    ImageView mMerchantPFP;
    EditText mMerchantName;
    EditText mEmail;
    EditText mType;
    CheckBox mFreeMembershipCB;

    Integer checked;

    ProgressBar mProgressBar;

    DatabaseReference mMerchantUsersRef;

    Button mSaveChangeBtn;
    Button mDisableSaveChangeBtn;

    String merchantName;
    String email;
    String merchantID;
    String merchantPFPUrl;
    String newMerchantPFPUrl;
    Uri merchantPFPUri;
    String type;

    private StorageReference storageReference;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_merchant);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        //Get from xml layout
        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);
        mMerchantName = (EditText) findViewById(R.id.input_merchant_name);
        mEmail = (EditText) findViewById(R.id.input_email);
        mType = (EditText) findViewById(R.id.input_type);

        merchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mMerchantUsersRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID);


        mMerchantUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                merchantName = snapshot.child("merchantName").getValue(String.class);
                email = snapshot.child("email").getValue(String.class);
                merchantPFPUrl = snapshot.child("merchantPFPUrl").getValue(String.class);
                type = snapshot.child("type").getValue(String.class);

                mMerchantName.setText(merchantName);
                mEmail.setText(email);
                Glide.with(mMerchantPFP).load(merchantPFPUrl).into(mMerchantPFP);
                mType.setText(type);


                return;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //Tap profile pic to open gallery
        mMerchantPFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 3);
            }
        });

        mFreeMembershipCB = findViewById(R.id.checkbox_free);
        mMerchantUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.hasChild("FreeMembership")){
                    mFreeMembershipCB.setChecked(true);
                    checked = 1;
                } else {
                    checked = 0;
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mDisableSaveChangeBtn = (Button) findViewById(R.id.disable_save_change_btn);
        mDisableSaveChangeBtn.setVisibility(View.INVISIBLE);

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 3 && resultCode == RESULT_OK && data != null){
            merchantPFPUri = data.getData();
            getPFPUrl(merchantPFPUri);
            mMerchantPFP.setImageURI(merchantPFPUri);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSaveChangeBtn = (Button) findViewById(R.id.save_change_btn);
        mSaveChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);

                final String newUsername =mMerchantName.getText().toString().trim();
                final String newEmail = mEmail.getText().toString().trim();
                final String newType = mType.getText().toString().trim();

                //username
                if (TextUtils.isEmpty(newUsername)) {
                    mMerchantName.setError("Merchant name is required");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!newUsername.equals(merchantName)){
                    mMerchantUsersRef.child("merchantName").setValue(newUsername);
                }

                //business type
                if(TextUtils.isEmpty(newType)){
                    mType.setError("Business type is required");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if(!newType.equals(type)){
                    mMerchantUsersRef.child("type").setValue(newType);
                }

                //profile pic
                if (newMerchantPFPUrl == null){
                    //Hide progress bar, do nothing and continue
                    mProgressBar.setVisibility(View.INVISIBLE);

                } else if (!newMerchantPFPUrl.equals(merchantPFPUrl)){
                    mMerchantUsersRef.child("merchantPFPUrl").setValue(newMerchantPFPUrl);
                }

                //email
                if (TextUtils.isEmpty(newEmail)) {
                    mEmail.setError("Email is required");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!emailPattern.matcher(newEmail).matches()) {
                    mEmail.setError("Invalid Email Address");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!newEmail.equals(email)){

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditMerchantActivity.this);
                    LayoutInflater layoutInflater = EditMerchantActivity.this.getLayoutInflater();
                    View dialogView = layoutInflater.inflate(R.layout.dialog_confirm_password, null);
                    dialogBuilder.setView(dialogView);


                    ProgressBar dialogProgressBar = dialogView.findViewById(R.id.dialog_progress_bar);
                    dialogProgressBar.setVisibility(View.INVISIBLE);

                    TextView error_message = dialogView.findViewById(R.id.error_message);
                    error_message.setVisibility(View.INVISIBLE);

                    EditText mPassword = dialogView.findViewById(R.id.input_password);

                    dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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

                    dialogBuilder.setView(dialogView);
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogProgressBar.setVisibility(View.VISIBLE);

                            final String password = mPassword.getText().toString().trim();

                            if (TextUtils.isEmpty(password)) {
                                dialogProgressBar.setVisibility(View.INVISIBLE);
                                mPassword.setError("Password is required");


                            } else if (password.length() < 6) {
                                dialogProgressBar.setVisibility(View.INVISIBLE);
                                mPassword.setError("Password should be at least 6 characters");
                            } else {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                                assert user != null;
                                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        Log.d(TAG, "User re-authenticated.");
                                        dialogProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialogProgressBar.setVisibility(View.INVISIBLE);

                                            user.updateEmail(newEmail).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                    error_message.setText("Error: "+ e.getMessage());
                                                    error_message.setVisibility(View.VISIBLE);
                                                    return;
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    mMerchantUsersRef.child("email").setValue(newEmail);

                                                    Intent intent = new Intent(EditMerchantActivity.this, ProfileActivity.class);
                                                    startActivity(intent);
                                                }
                                            });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        dialogProgressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(v.getContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }
                        }
                    });
                    return;

                }

                //previously unchecked, but now checked
                if (checked == 0 && mFreeMembershipCB.isChecked()){
                    FirebaseDatabase.getInstance().getReference("merchantUsers")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("FreeMembership")
                            .setValue(true);

                //previously checked, but now unchecked
                } else if (checked == 1 && !mFreeMembershipCB.isChecked() ){
                    FirebaseDatabase.getInstance().getReference("merchantUsers")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("FreeMembership")
                            .removeValue();
                }

                //if nothing changed
                if (newUsername.equals(merchantName) && newEmail.equals(email)
                        && newMerchantPFPUrl == null && newType.equals(type)
                        && (checked == 1 && mFreeMembershipCB.isChecked()
                        || checked == 0 && !mFreeMembershipCB.isChecked()) ) {

                    Toast.makeText(EditMerchantActivity.this, "No field has been changed",
                            Toast.LENGTH_SHORT).show();
                    return;

                }

                //if successful
                Intent intent = new Intent(EditMerchantActivity.this, ProfileActivity.class);
                startActivity(intent);

            }
        });


    }

    public final Pattern emailPattern = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    );

    protected void getPFPUrl(Uri uri) {
        StorageReference storageRef = storageReference.child(merchantID + "_logo."
                + getFileExtension(merchantPFPUri));
        storageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newMerchantPFPUrl = uri.toString();

                        mProgressBar.setVisibility(View.INVISIBLE);
                        mDisableSaveChangeBtn.setVisibility(View.INVISIBLE);

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                mDisableSaveChangeBtn.setVisibility(View.VISIBLE);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(EditMerchantActivity.this, "Uploading Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    public void onBackPressed() {

        Intent intent = new Intent(EditMerchantActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}