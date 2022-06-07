package com.bignerdranch.android.aiomamerchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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

import org.jetbrains.annotations.NotNull;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText mCurrentPassword;
    EditText mNewPassword;
    EditText mConfirmPassword;

    String merchantID;
    String email;
    String currentPassword;
    String newPassword;
    String newConfirmPassword;

    DatabaseReference mMerchantUsersRef;

    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        merchantID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mMerchantUsersRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID);

        mMerchantUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                email = snapshot.child("email").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        //Get from xml layout
        mCurrentPassword = (EditText) findViewById(R.id.input_current_password);
        mNewPassword = (EditText) findViewById(R.id.input_new_password);
        mConfirmPassword = (EditText) findViewById(R.id.input_confirm_password);

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);


    }


    @Override
    protected void onStart() {
        super.onStart();

        Button changePasswordBtn = findViewById(R.id.change_password_btn);
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(ChangePasswordActivity.this);

                mProgressBar.setVisibility(View.VISIBLE);

                currentPassword = mCurrentPassword.getText().toString().trim();
                newPassword = mNewPassword.getText().toString().trim();
                newConfirmPassword = mConfirmPassword.getText().toString().trim();


                if(TextUtils.isEmpty(currentPassword) && TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(newConfirmPassword)){
                    Toast.makeText(ChangePasswordActivity.this,
                            "All fields are required", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }


                if (TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Current password is required", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if (currentPassword.length() < 6) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "New password is required", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if (newPassword.length() < 6) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if (TextUtils.isEmpty(newConfirmPassword)) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Confirm password is required", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if(!newPassword.equals(newConfirmPassword)){
                    Toast.makeText(ChangePasswordActivity.this,
                            "Confirm password does not match the new password", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

                    assert user != null;
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            mProgressBar.setVisibility(View.INVISIBLE);

                            user.updatePassword(newPassword);

                            Toast.makeText(ChangePasswordActivity.this, "Password Changed Successfully",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ChangePasswordActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(ChangePasswordActivity.this, "Incorrect Password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }
}