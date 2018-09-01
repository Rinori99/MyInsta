package com.example.asus.myinsta.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.myinsta.Home.HomeActivity;
import com.example.asus.myinsta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Context mContext;
    private ProgressBar mProgressbar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started");

        mAuth = FirebaseAuth.getInstance();

        mProgressbar = (ProgressBar)findViewById(R.id.loginRequestLoadingProgressbar);

        mPleaseWait = (TextView)findViewById(R.id.pleaseWait);
        mEmail = (EditText)findViewById(R.id.input_email);
        mPassword = (EditText)findViewById(R.id.input_password);
        mContext = LoginActivity.this;


        mPleaseWait.setVisibility(View.GONE);
        mProgressbar.setVisibility(View.GONE);

        init();
    }

    private boolean isStringNull(String s){
        return s == null || s.isEmpty();
    }

    //-----------------------------------------------
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    private void checkCurrentUser(FirebaseUser user){
        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void updateUI(FirebaseUser user){
        checkCurrentUser(user);
        if(user != null){
            Log.d(TAG, "updateUI: signed in: " + user.getUid());
        }else{
            Log.d(TAG, "updateUI: Signed out");
        }
    }

    private void init(){
        Button btnLogin = (Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if(isStringNull(email) || isStringNull(password)){
                    Toast.makeText(mContext, "You must fill all the fields", Toast.LENGTH_SHORT);
                }else{
                    mProgressbar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (task.isSuccessful()) {
                                        try{
                                            //boolean verifiedOnce = false;
                                            //DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//                                            Query query = ref.child(mContext.getString(R.string.dbname_user))
//                                                    .orderByChild(mContext.getString(R.string.field_verified_once))
//                                                    .equalTo(true);
                                            if(user.isEmailVerified()){
                                                Intent intent = new Intent(mContext, HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                mProgressbar.setVisibility(View.GONE);
                                                mPleaseWait.setVisibility(View.GONE);
                                                mAuth.signOut();
                                                Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }catch (NullPointerException ex){
                                            Toast.makeText(mContext, "You haven't verified your account yet", Toast.LENGTH_SHORT);
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    //if signed in navigate to home screen


                }
            }
        });

        TextView linkSignup = (TextView)findViewById(R.id.link_signup);
        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
