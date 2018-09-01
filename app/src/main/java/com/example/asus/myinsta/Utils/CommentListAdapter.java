package com.example.asus.myinsta.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.asus.myinsta.R;
import com.example.asus.myinsta.models.Comment;
import com.example.asus.myinsta.models.UserAccountSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder{
        TextView comment, username, timeStamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = (TextView)convertView.findViewById(R.id.comment);
            holder.username = (TextView)convertView.findViewById(R.id.comment_username);
            holder.timeStamp = (TextView)convertView.findViewById(R.id.comment_time_posted);
            holder.reply = (TextView)convertView.findViewById(R.id.comment_reply);
            holder.like = (ImageView)convertView.findViewById(R.id.comment_like);
            holder.likes = (TextView)convertView.findViewById(R.id.comment_likes);

            holder.profileImage = (CircleImageView)convertView.findViewById(R.id.comment_profile_image);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        //setting the comment
        holder.comment.setText(getItem(position).getComment());

        //setting the time difference
        String timeStamp = getTimeStampDifference(getItem(position));

        if(!timeStamp.equals("0")){
            holder.timeStamp.setText(timeStamp + "d");
        }else{
            holder.timeStamp.setText("Today");
        }

        //setting the username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    holder.username.setText(ds.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(ds.getValue(UserAccountSettings.class).getProfile_phote(), holder.profileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        try{
            if(position == 0){
                holder.like.setVisibility(View.GONE);
                holder.likes.setVisibility(View.GONE);
                holder.reply.setVisibility(View.GONE);
            }
        }catch (NullPointerException e){}


        return convertView;
    }

    private String getTimeStampDifference(Comment comment){
        String difference = "";
        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+1"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp = null;
        final String photoTimeStamp = comment.getDate_created();
        try{
            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round((today.getTime()-timeStamp.getTime())/1000/60/60/24));
        }catch (ParseException e){
            difference = "0";
        }


        return difference;
    }
}
