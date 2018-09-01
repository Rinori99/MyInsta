package com.example.asus.myinsta.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.DialogTitle;
import android.util.Log;
import android.widget.Toast;

import com.example.asus.myinsta.Home.HomeActivity;
import com.example.asus.myinsta.Profile.AccountSettingsActivity;
import com.example.asus.myinsta.Profile.ProfileActivity;
import com.example.asus.myinsta.R;
import com.example.asus.myinsta.models.Photo;
import com.example.asus.myinsta.models.User;
import com.example.asus.myinsta.models.UserAccountSettings;
import com.example.asus.myinsta.models.UserSettings;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private FirebaseAuth mAuth;
    private String userID;
    private Context mContext;
    private static final String TAG = "FirebaseMethods";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        myRef = mFirebaseDatabase.getReference();
        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

//    public boolean checkIfUsernameAlreadyExists(String username, DataSnapshot dataSnapshot){
//        User user = new User();
//
//        for(DataSnapshot ds : dataSnapshot.child(userID).getChildren()){
//            user.setUsername(ds.getValue(User.class).getUsername());
//
//            if(StringManipulation.extendUsername(user.getUsername()).equals(username)){
//                return true;
//            }
//        }
//        return false;
//    }


    public void registerNewEmail ( final String email, final String username, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:failed");
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
                            //updateUI(user);
                        } else if (task.isSuccessful()) {
                            sendVerificationEmail();

                            userID = mAuth.getCurrentUser().getUid();
                        }

                        // ...
                    }
                });
    }

    public void sendVerificationEmail () {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    } else {
                        Toast.makeText(mContext, "Couldn't send email verification", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void addNewUser (String email, String username, String description, String website, String profile_photo){
        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));

        myRef.child(mContext.getString(R.string.dbname_user))
                .child(userID)
                .setValue(user);

        UserAccountSettings accountSettings = new UserAccountSettings(
                description,
                username,
                (long) 0,
                (long) 0,
                (long) 0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID
        );

        myRef.child(mContext.getString(R.string.user_account_settings))
                .child(userID)
                .setValue(accountSettings);

    }

    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (ds.getKey().equals(mContext.getString(R.string.user_account_settings))) {

                try {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );

                    settings.setProfile_phote(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_phote()
                    );

                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );

                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );

                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );
                } catch (NullPointerException ex) {

                }
            }

            if (ds.getKey().equals(mContext.getString(R.string.dbname_user))) {
                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );

                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );

                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number()
                );

                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
            }
        }

        return new UserSettings(user, settings);
    }

    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){
        if(displayName != null){
            myRef.child(mContext.getString(R.string.user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if(website != null){
            myRef.child(mContext.getString(R.string.user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if(description != null){
            myRef.child(mContext.getString(R.string.user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if(phoneNumber!=0){
            myRef.child(mContext.getString(R.string.dbname_user))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }
    }


    public void updateUsername (String username){
        myRef.child(mContext.getString(R.string.dbname_user))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    public void updateEmail(String email){
        myRef.child(mContext.getString(R.string.dbname_user))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds : dataSnapshot
        .child(mContext.getString(R.string.dbname_user_photos))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()){
            count++;
        }
        return count;
    }

    public void uploadNewPhoto(String photoType, final String caption, final int count, String imgUrl, Bitmap bm){
        FilePaths filePaths = new FilePaths();

        //case 1: new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count+1));

            //convert img url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 70);

            final UploadTask uploadTask = storageReference.putBytes(bytes);


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(mContext, "photo uploaded succesfully", Toast.LENGTH_SHORT).show();
                    Uri firebaseURL = null;
                    /////////////////////////////////////////////////////////////////////////////////////////////////

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                task.getException();
                            }
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                // insert into user_settings_node
                                addPhotoToDatabase(caption, task.getResult().toString());
                            }
                        }
                    });

                    /////////////////////////////////////////////////////////////////////////////////////////////////////
                    // add the photo to the nodes


                    // navigate to the main feed
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                }
            });
        }
        //case 2: new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_phote))){



            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert img url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 70);

            final UploadTask uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(mContext, "photo uploaded succesfully", Toast.LENGTH_SHORT).show();
                    Uri firebaseURL = null;

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                task.getException();
                            }
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                // insert into user_settings_node
                                setupProfilePhoto(task.getResult().toString());
                            }
                        }
                    });

                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile))
                    );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                }
            });
        }
    }

    private void setupProfilePhoto(String url){
        myRef.child(mContext.getString(R.string.user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_phote)).setValue(url);
    }

    private void addPhotoToDatabase(String caption, String imgUrl){
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        String tags = StringManipulation.getTags(caption);
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(imgUrl);
        photo.setTags(tags);
        photo.setUser_id(mAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //inserting

        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    private String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+1"));
        return sdf.format(new Date());
    }
}
