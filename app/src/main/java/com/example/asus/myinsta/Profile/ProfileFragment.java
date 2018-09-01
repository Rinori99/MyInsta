package com.example.asus.myinsta.Profile;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.asus.myinsta.R;
import com.example.asus.myinsta.Utils.BottomNavigationViewHelper;
import com.example.asus.myinsta.Utils.FirebaseMethods;
import com.example.asus.myinsta.Utils.GridImageAdapter;
import com.example.asus.myinsta.Utils.StringManipulation;
import com.example.asus.myinsta.Utils.UniversalImageLoader;
import com.example.asus.myinsta.models.Comment;
import com.example.asus.myinsta.models.Like;
import com.example.asus.myinsta.models.Photo;
import com.example.asus.myinsta.models.User;
import com.example.asus.myinsta.models.UserAccountSettings;
import com.example.asus.myinsta.models.UserSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    @Override
    public void onAttach(Context context) {
        try{
            mOnGridImageSelectedListener = (OnGridImageSelectedListener)getActivity();
        }catch(ClassCastException ex){}
        super.onAttach(context);
    }

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;

    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mDisplayName = (TextView)view.findViewById(R.id.center_fullname);
        mUsername = (TextView)view.findViewById(R.id.topUsername);
        mWebsite = (TextView)view.findViewById(R.id.website);
        mDescription = (TextView)view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView)view.findViewById(R.id.profileImage);
        mPosts = (TextView)view.findViewById(R.id.tvPosts);
        mFollowers = (TextView)view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView)view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar)view.findViewById(R.id.profileProgressBar);
        gridView = (GridView)view.findViewById(R.id.gridView);
        profileMenu = (ImageView)view.findViewById(R.id.profileMenu);
        bottomNavigationView = (BottomNavigationViewEx)view.findViewById(R.id.bottomNavViewBar);

        toolbar = (Toolbar)view.findViewById(R.id.profileToolbar);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());
        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        setupGridView();

        TextView editProfile = (TextView)view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    private void setupGridView(){
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>)ds.getValue();
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());


                    ArrayList<Comment> comments = new ArrayList<>();
                    for(DataSnapshot d: ds.child(getString(R.string.field_comments)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(d.getValue(Comment.class).getUser_id());
                        comment.setComment(d.getValue(Comment.class).getComment());
                        comment.setDate_created(d.getValue(Comment.class).getDate_created());
                        comments.add(comment);
                    }

                    photo.setComments(comments);


                    List<Like> likesList = new ArrayList<>();
                    for(DataSnapshot d: ds.child(getString(R.string.field_likes)).getChildren()){
                        Like like = new Like();
                        like.setUser_id(d.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    photo.setLikes(likesList);
                    photos.add(photo);
                }

                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<>();
                for(int i = 0; i < photos.size(); i++){
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter gridImageAdapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                                                    "", imgUrls);
                gridView.setAdapter(gridImageAdapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupBottomNavigationView(){
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void setupToolbar(){
        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(settings.getProfile_phote(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(Long.toString(settings.getPosts()));
        mFollowing.setText(Long.toString(settings.getFollowing()));
        mFollowers.setText(Long.toString(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);
        mUsername.setText(StringManipulation.condenseUsername(settings.getUsername()));
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
