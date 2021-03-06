package com.example.socialmedia.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.socialmedia.Models.User;
import com.example.socialmedia.R;
import com.example.socialmedia.UserClient.UserClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ViewProfileFragment extends Fragment {


    private static final String TAG = "ViewProfileFragment";


    private String userId;
    private User user , mCurrUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private DatabaseReference Follow;
    private TextView User_Fullname, User_bio, Username, User_FollowersCount , User_FollowingCount;
    private CircleImageView User_image;
    private Button mBtnFollow;
    private Context context;
    private int FollowerCount, FollowingCount;
    Boolean isFollowing = false;


    public ViewProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Follow=FirebaseDatabase.getInstance().getReference("Follow");

        mCurrUser=((UserClient)(getActivity().getApplicationContext())).getUser();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context=container.getContext();
        View view=inflater.inflate(R.layout.fragment_view_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
      //  Follow = FirebaseDatabase.getInstance().getReference().child ("Follow");


        //Initializing

        User_Fullname= view.findViewById(R.id.ViewProfile_fragment_fullname);
        User_bio=view.findViewById(R.id.userProfile_fragment_bio);
        Username= view.findViewById(R.id.ViewProfile_fragment_username);
        User_image= view.findViewById(R.id.ViewProfile_fragment_image);
        mBtnFollow= view.findViewById(R.id.btnFollow);
        User_FollowersCount = view.findViewById(R.id.ViewPost_noFollowers);
        User_FollowingCount= view.findViewById(R.id.ViewPost_noFollowing);

        mBtnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Clicked");

                if(isFollowing)
                    User_Unfollow();
                else
                    User_Follow();

            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        userId = bundle.getString(FragmentActionListener.KEY_SELECTED_USERID);
        getUser(userId);
    }

    private void getUser(String userId) {


        mUserRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    user=dataSnapshot.getValue(User.class);
                    Log.d(TAG, "onDataChange: Curr User is " + user.getFullname());
                    setLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void User_Unfollow() {

        Follow.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Follow.child(mCurrUser.getUserid()).child("Following").child(user.getUserid()).removeValue();
                Follow.child(user.getUserid()).child("Followers").child(mCurrUser.getUserid()).removeValue();
                isFollowing=false;
                ButtonChange();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void User_Follow(){

        Follow.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Follow.child(mCurrUser.getUserid()).child("Following").child(user.getUserid()).setValue(true);
                Follow.child(user.getUserid()).child("Followers").child(mCurrUser.getUserid()).setValue(true);
                isFollowing=true;
                ButtonChange();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void setLayout() {

        isFollowing();
        User_Fullname.setText(user.getFullname());
        Username.setText(user.getUsername());
        if(!(user.getUserbio().equals("")))
            User_bio.setText(user.getUserbio());
        else
            User_bio.setText(getResources().getString(R.string.Default_bio));
        if(!user.getProfileImageUri().equals("defaultpic"))
            Picasso.get().load(user.getProfileImageUri()).into(User_image);
        getFollowCount();

    }


    private void getFollowCount(){


        Follow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FollowerCount= (int) dataSnapshot.child(user.getUserid()).child("Followers").getChildrenCount();
                FollowingCount= (int) dataSnapshot.child(user.getUserid()).child("Following").getChildrenCount();
                User_FollowersCount.setText(Integer.toString(FollowerCount));
                User_FollowingCount.setText(Integer.toString(FollowingCount));
                Log.d(TAG, "onDataChange: Followers"+FollowerCount + "  Following"+FollowingCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void isFollowing()
    {
        Follow.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isFollowing = dataSnapshot.child(mCurrUser.getUserid()).child("Following").hasChild(user.getUserid());
                ButtonChange();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ButtonChange() {

        if(isFollowing)
        {
        Drawable myDrawable = context.getResources().getDrawable(R.drawable.btnfollow);
        mBtnFollow.setBackground(myDrawable);
        mBtnFollow.setText("Following");
        mBtnFollow.setTextColor(Color.BLACK);
        }
        else
            {
            Drawable myDrawable = context.getResources().getDrawable(R.drawable.button1);
            mBtnFollow.setBackground(myDrawable);
            mBtnFollow.setText("Follow");
            mBtnFollow.setTextColor(Color.WHITE);}

    }

}
