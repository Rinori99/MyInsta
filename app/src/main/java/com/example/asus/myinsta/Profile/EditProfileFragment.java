package com.example.asus.myinsta.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.myinsta.R;
import com.example.asus.myinsta.Share.ShareActivity;
import com.example.asus.myinsta.Utils.FirebaseMethods;
import com.example.asus.myinsta.Utils.UniversalImageLoader;
import com.example.asus.myinsta.dialogs.ConfirmPasswordDialog;
import com.example.asus.myinsta.models.User;
import com.example.asus.myinsta.models.UserAccountSettings;
import com.example.asus.myinsta.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    private FirebaseMethods mFirebaseMethods;

    private String userID;

    private UserSettings mUserSettings;
    private static final String TAG = "EditProfileFragment";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText)view.findViewById(R.id.display_name);
        mUsername = (EditText)view.findViewById(R.id.username);
        mWebsite = (EditText)view.findViewById(R.id.website);
        mDescription = (EditText)view.findViewById(R.id.description);
        mEmail = (EditText)view.findViewById(R.id.email);
        mPhoneNumber = (EditText)view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = (TextView)view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        setupFirebaseAuth();

        //setProfileImage();

        ImageView backArrow = (ImageView)view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        ImageView checkMark = (ImageView)view.findViewById(R.id.saveChanges);
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfileSettings();
            }
        });

        return view;
    }

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password" + password);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), password);

        mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: User reauthenticated");

                    mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if(task.isSuccessful()){
                                try{
                                    if(task.getResult().getSignInMethods().size() == 1){
                                        Log.d(TAG, "onComplete: That email is already in use");
                                        Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Log.d(TAG, "onComplete: That email is available");

                                        //updating email, since it is available
                                        mAuth.getCurrentUser().updateEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getActivity(), "email updated", Toast.LENGTH_SHORT).show();
                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());

                                                    mFirebaseMethods.sendVerificationEmail();
                                                }
                                            }
                                        });
                                    }
                                }catch (NullPointerException ex){}

                            }
                        }
                    });


                }else{
                    Log.d(TAG, "onComplete: re-authentication failed");
                }
            }
        });
    }

//    private void setProfileImage(){
//        String imageURL = "https://themify.me/demo/themes/pinshop/files/2012/12/man-in-suit2.jpg";
//        UniversalImageLoader.setImage(imageURL, mProfilePhoto, null, "");
//    }

    private void checkIfUsernameExists(final String username) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //add username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved username", Toast.LENGTH_SHORT);
                }

                for (DataSnapshot singleShot : dataSnapshot.getChildren()) {
                    if (singleShot.exists()) {
                        Toast.makeText(getActivity(), "That username already exists", Toast.LENGTH_SHORT);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveProfileSettings(){
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());

        //if user changed username
        if(!mUserSettings.getUser().getUsername().equals(username)) {
            checkIfUsernameExists(username);
        }

        //if user changed email
        if(!mUserSettings.getUser().getEmail().equals(email)){
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);
        }

        //display name changed
        if(!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName)){
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }

        if(!mUserSettings.getUserAccountSettings().getWebsite().equals(website)){
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0);
        }

        if(!mUserSettings.getUserAccountSettings().getDescription().equals(description)){
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0);
        }

        if(mUserSettings.getUser().getPhone_number() != phoneNumber){
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);
        }
    }

    private void setProfileWidgets(UserSettings userSettings){
        mUserSettings = userSettings;
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(settings.getProfile_phote(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mUsername.setText(settings.getUsername());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(Long.toString(userSettings.getUser().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

    }


    //Firebase stuff ------------------------------------------------------

    private void setupFirebaseAuth(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();

        mAuthListener = (FirebaseAuth.AuthStateListener) (firebaseAuth) ->{
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if(user != null){

            }else{

            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //retrieve user data form database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
                //retrieve the image for the user

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
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
}