package com.example.asus.myinsta.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.myinsta.Home.HomeActivity;
import com.example.asus.myinsta.R;
import com.example.asus.myinsta.models.Comment;
import com.example.asus.myinsta.models.Like;
import com.example.asus.myinsta.models.Photo;
import com.example.asus.myinsta.models.User;
import com.example.asus.myinsta.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    public interface OnCommentThreadSelectedListener{
        void onCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    private Photo mPhoto;
    private int mActivityNumber = 0;

    private Boolean mLikedByCurrentUser;
    private static final String TAG = "ViewPostFragment";

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mDateStamp, mLikes, mComments;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage, mComment;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private String photoUsername = "";
    private String photoUrl = "";
    private UserAccountSettings mUserAccountSettings;
    private User mCurrentUser;

    private GestureDetector gestureDetector;
    private Heart mHeart;
    private StringBuilder mUsers;
    private String mLikesString = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        mPostImage = (SquareImageView)view.findViewById(R.id.post_image);
        bottomNavigationView = (BottomNavigationViewEx)view.findViewById(R.id.bottomNavViewBar);

        mBackArrow = (ImageView)view.findViewById(R.id.backArrow);
        mEllipses = (ImageView)view.findViewById(R.id.ivEllipses);
        mBackLabel = (TextView)view.findViewById(R.id.tvBackLabel);
        mUsername = (TextView)view.findViewById(R.id.username);
        mDateStamp = (TextView)view.findViewById(R.id.image_time_posted);
        mHeartRed = (ImageView)view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView)view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView)view.findViewById(R.id.profile_photo);
        mLikes = (TextView)view.findViewById(R.id.image_likes);
        mComment = (ImageView)view.findViewById(R.id.speech_bubble);
        mCaption = (TextView)view.findViewById(R.id.image_caption);
        mComments = (TextView)view.findViewById(R.id.image_comments_link);

        mHeart = new Heart(mHeartWhite, mHeartRed);
        gestureDetector = new GestureDetector(getActivity(), new GestureListener());

        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener)getActivity();
        }catch (ClassCastException e){}
    }

    private void init(){
        try{
            //mPhoto = getPhotoFormBundle();
            UniversalImageLoader.setImage(getPhotoFormBundle().getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();

            String photo_id = getPhotoFormBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Photo newPhoto = new Photo();

                        Map<String, Object> objectMap = (HashMap<String, Object>)singleSnapshot.getValue();

                        newPhoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Comment> commentList = new ArrayList<>();
                        for(DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            commentList.add(comment);
                        }

                        newPhoto.setComments(commentList);

                        mPhoto = newPhoto;

                        getCurrentUser();
                        getPhotoDetails();
                        //getLikesString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }catch (NullPointerException e){

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isAdded()){
            init();
        }
    }

    public void getLikesString(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference.child(getString(R.string.dbname_user))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(ds.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                mUsers.append(ds.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");

                            if(mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser = true;
                            }else{
                                mLikedByCurrentUser = false;
                            }
                            int length = splitUsers.length;
                            if(length == 1){
                                mLikesString = "Liked by " + splitUsers[0];
                            }else if(length == 2){
                                mLikesString = "Liked by " + splitUsers[1] + " and " + splitUsers[0];
                            }else if(length == 3){
                                mLikesString = "Liked by " + splitUsers[splitUsers.length-1] + ", " + splitUsers[splitUsers.length-2]
                                                + " and 1 other";
                            }else{
                                mLikesString = "Liked by " + splitUsers[splitUsers.length-1] + ", " + splitUsers[splitUsers.length-2]
                                        + " and " + (splitUsers.length-2) + " others";
                            }
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    mLikesString = "Be the first to like this photo";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mCurrentUser = ds.getValue(User.class);
                }
                getPhotoDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String keyID = ds.getKey();
                        //user has already liked the photo
                        if(mLikedByCurrentUser &&
                                ds.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID).removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID).removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }
                        //the user has not liked the photo yet
                        else if(!mLikedByCurrentUser){
                            addNewLike();
                            break;
                        }

                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    }

    private void addNewLike(){
        String newLikeID = myRef.push().getKey();

        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID).setValue(like);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID).setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    public ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }

    private Photo getPhotoFormBundle(){
        Bundle bundle = this.getArguments();

        if(bundle != null){
            return bundle.getParcelable(getString(R.string.photo));
        }

        return null;
    }

    private int getActivityNumFromBundle(){
        Bundle bundle = this.getArguments();

        if(bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        }

        return 0;
    }

    private void setupBottomNavigationView(){
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

    private void setupWidgets(){
        String timeStampDiff = getTimeStampDifference();
        if(!timeStampDiff.equals("0")){
            mDateStamp.setText(timeStampDiff + " DAYS AGO");
        }else{
            mDateStamp.setText("TODAY");
        }

        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_phote(), mProfileImage, null, "");
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());

        if(mPhoto.getComments().size() > 0){
            mComments.setText("View all " + mPhoto.getComments().size() + " comments");
        }else{
            mComments.setText("");
        }

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        if(mLikedByCurrentUser){
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });
        }else{
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);

            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });
        }

    }

    private String getTimeStampDifference(){
        String difference = "";
        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+1"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp = null;
        final String photoTimeStamp = mPhoto.getDate_created();
        try{
            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round((today.getTime()-timeStamp.getTime())/1000/60/60/24));
        }catch (ParseException e){
            difference = "0";
        }


        return difference;
    }

    //Firebase...................................................................

    private void setupFirebaseAuth(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //  Toast.makeText(getActivity(), "Something happened", Toast.LENGTH_SHORT);
                }else{
                    //  Toast.makeText(getActivity(), "Else exuceuted", Toast.LENGTH_SHORT);
                }
            }
        };

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

    private void getPhotoDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mUserAccountSettings = ds.getValue(UserAccountSettings.class);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
