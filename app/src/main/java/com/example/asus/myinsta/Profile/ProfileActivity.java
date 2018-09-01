package com.example.asus.myinsta.Profile;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.asus.myinsta.R;
import com.example.asus.myinsta.Utils.ViewCommentsFragment;
import com.example.asus.myinsta.Utils.ViewPostFragment;
import com.example.asus.myinsta.Utils.ViewProfileFragment;
import com.example.asus.myinsta.models.Photo;

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener, ViewProfileFragment.OnGridImageSelectedListener{

    private static final int ACTIVITY_NUM = 4;
    private Context mContext = ProfileActivity.this;
    private ImageView profilePhoto;

    private static final int NUM_GRID_COLUMNS = 3;

    private ProgressBar mProgressBar;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toast.makeText(ProfileActivity.this, "Made it to ProfileActivity", Toast.LENGTH_SHORT);

//        mProgressBar = (ProgressBar)findViewById(R.id.profileProgressBar);
//        mProgressBar.setVisibility(View.GONE);

        init();

//        setupBottomNavigationView();
//        setupToolbar();
//        setupActivityWidgets();
//        setProfileImage();
//
//        tempGridSetup();
    }

    private void init(){
        Intent intent = getIntent();

        if(intent.hasExtra(getString(R.string.calling_activity))){
            if(intent.hasExtra(getString(R.string.intent_user))){
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra(getString(R.string.intent_user)));
                fragment.setArguments(args);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.view_profile_fragment));
                transaction.commit();
            }else{
                Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else{
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

//    private void tempGridSetup(){
//        ArrayList<String> imgURLs = new ArrayList<>();
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/08/sei_25132831-96a7.jpg?quality=80&strip=all&zoom=1&resize=644%2C428");
//        imgURLs.add("https://cdn.images.express.co.uk/img/dynamic/67/590x/Messi-1004000.jpg?r=1534376150905");
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/08/sei_25140552-9a6b.jpg?quality=80&strip=all&zoom=1&resize=540%2C360");
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/08/sei_25138905-74f5.jpg?quality=80&strip=all&zoom=1&resize=540%2C350");
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/08/gettyimages-962797074-84af.jpg?quality=80&strip=all&zoom=1&resize=644%2C429");
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/08/gettyimages-963313868-efc8.jpg?quality=80&strip=all&zoom=1&resize=540%2C360");
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/08/gettyimages-1009976184.jpg?quality=80&strip=all&zoom=1&resize=644%2C429");
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/07/gettyimages-949616628.jpg?quality=80&strip=all&zoom=1&resize=540%2C359");
//        imgURLs.add("https://metrouk2.files.wordpress.com/2018/08/sei_25501091-3e72.jpg?quality=80&strip=all&zoom=1&resize=540%2C341");
//
//        setupImageGrid(imgURLs);
//    }
//
//    private void setupImageGrid(ArrayList<String> imgURLs){
//        GridView gridView = (GridView)findViewById(R.id.gridView);
//
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
//        gridView.setColumnWidth(imageWidth);
//
//        GridImageAdapter imageAdapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
//        gridView.setAdapter(imageAdapter);
//    }
//
//    private void setProfileImage(){
//        String imgURL = "https://themify.me/demo/themes/pinshop/files/2012/12/man-in-suit2.jpg";
//        UniversalImageLoader.setImage(imgURL, profilePhoto, mProgressBar, "");
//    }
//
//    private void setupActivityWidgets(){
//        mProgressBar = (ProgressBar)findViewById(R.id.profileProgressBar);
//        mProgressBar.setVisibility(View.GONE);
//        profilePhoto = (ImageView)findViewById(R.id.profileImage);
//    }
//
//    private void setupToolbar(){
//        Toolbar toolbar = (Toolbar)findViewById(R.id.profileToolbar);
//        setSupportActionBar(toolbar);
//
//        ImageView profileMenu = (ImageView)findViewById(R.id.profileMenu);
//        profileMenu.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
//                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void setupBottomNavigationView(){
//        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
//        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
//        BottomNavigationViewHelper.enableNavigation(ProfileActivity.this, bottomNavigationViewEx);
//
//        Menu menu = bottomNavigationViewEx.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }
}
