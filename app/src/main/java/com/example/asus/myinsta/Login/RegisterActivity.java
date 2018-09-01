package com.example.asus.myinsta.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.myinsta.Profile.ProfileActivity;
import com.example.asus.myinsta.R;
import com.example.asus.myinsta.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private Context mContext;
    private String email, username, password;
    private EditText mEmail, mPassword, mUsername;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String append = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: started");
        mContext = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);

        initWidgets();

        //Setting up ============================================================
        setupFirebaseAuth();
        //==========================================================================

        init();
    }

    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    finish();


                }else{
//                    Intent intent = new Intent(mContext, LoginActivity.class);
//                    startActivity(intent);
//                    finish();
                }
            }
        };
    }

    private void checkIfUsernameExists(final String username) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user))
                .orderByChild(mContext.getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleShot : dataSnapshot.getChildren()) {
                    if (singleShot.exists()) {
                        append = myRef.push().getKey().substring(3,10);
                    }
                }

                //first check: make sure username is not already in use
                String mUsername = "";
                mUsername = username + append;

                firebaseMethods.addNewUser(email, mUsername, "", "", "");
                Toast.makeText(mContext, "Sginup successful, sending verification email", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initWidgets(){
        mEmail = (EditText)findViewById(R.id.input_email);
        mUsername = (EditText)findViewById(R.id.input_username);
        mPassword = (EditText)findViewById(R.id.input_password);
        loadingPleaseWait = (TextView)findViewById(R.id.loadingPleaseWait);
        btnRegister = (Button)findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        loadingPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                if(checkInput(email, username, password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewEmail(email, username, password);
                }
            }
        });
    }

    private boolean checkInput(String email, String username, String password){
        if(email.equals("") || username.equals("") || password.equals("")){
            Toast.makeText(mContext, "None of the fields can be left blank!", Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }


    //Firebase ------------------------------------------------------------------------
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

//    private void checkCurrentUser(FirebaseUser user){
//        if(user == null){
//            Intent intent = new Intent(mContext, LoginActivity.class);
//            startActivity(intent);
//        }
//    }

//    private void updateUI(FirebaseUser user){
//        checkCurrentUser(user);
//        if(user != null){
//            Log.d(TAG, "updateUI: signed in: " + user.getUid());
//        }else{
//            Log.d(TAG, "updateUI: Signed out");
//        }
//    }

    private boolean isStringNull(String s){
        return s == "";
    }
}
