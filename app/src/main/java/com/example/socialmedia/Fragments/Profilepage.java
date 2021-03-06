package com.example.socialmedia.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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


public class Profilepage extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeLayout;
    private static final String TAG = "Profile Page Fragment";
    private User mCurruser;
    private TextView mUsername,mFullname,mBio,mFollowersCount,mFollowingCount;
    private CircleImageView mProfile_image;
    private Button mEditBtn;
    private int FollowerCount;
    private int FollowingCount;
    private DatabaseReference Follow;

    public Profilepage() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Follow= FirebaseDatabase.getInstance().getReference("Follow");
        mCurruser=((UserClient)(getActivity().getApplicationContext())).getUser();
        Log.d(TAG, "onCreate: mCurrUser bio is " + mCurruser.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: .");
        View view=inflater.inflate(R.layout.fragment_profilepage, container, false);

        //Initializing Profile Details
        mFullname= view.findViewById(R.id.user_fullname);
        mUsername= view.findViewById(R.id.username);
        mBio= view.findViewById(R.id.user_bio);
        mProfile_image = view.findViewById(R.id.profile_pic);
        mEditBtn=view.findViewById(R.id.btnEditProfile);
        mFollowersCount=view.findViewById(R.id.noFollowers);
        mFollowingCount=view.findViewById(R.id.noFollowing);

        // Refresh
        swipeLayout = view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoEditProfile();
            }
        });

        //Set the layout
        setLayout();

        // Inflate the layout for this fragment
        return view ;
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(false);
        mCurruser=((UserClient)(getActivity().getApplicationContext())).getUser();
        Log.d(TAG, "onRefresh: bio is "+ mCurruser.getUserbio());
        setLayout();
    }

    private void gotoEditProfile() {
        Log.d(TAG, "inflating Editing Profile " );
        EditProfile fragment = new EditProfile();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.profile_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void setLayout() {

        Log.d(TAG, "setLayout: CALLED");
        mFullname.setText(mCurruser.getFullname());
        mUsername.setText(mCurruser.getUsername());
        if(!(mCurruser.getUserbio().equals("")))
            mBio.setText(mCurruser.getUserbio());
        else
            mBio.setText(getResources().getString(R.string.Default_bio));
        if(!mCurruser.getProfileImageUri().equals("defaultpic"))
            Picasso.get().load(mCurruser.getProfileImageUri()).into(mProfile_image);
        getFollowCount();

    }

    private void getFollowCount(){


        Follow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FollowerCount= (int) dataSnapshot.child(mCurruser.getUserid()).child("Followers").getChildrenCount();
                FollowingCount= (int) dataSnapshot.child(mCurruser.getUserid()).child("Following").getChildrenCount();
                mFollowersCount.setText(Integer.toString(FollowerCount));
                mFollowingCount.setText(Integer.toString(FollowingCount));
                Log.d(TAG, "onDataChange: Followers"+FollowerCount + "  Following"+FollowingCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
