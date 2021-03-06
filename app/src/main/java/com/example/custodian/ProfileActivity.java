package com.example.custodian;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Document;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    // Disable action bar back button
    @Override
    public void onBackPressed() {
    }

    // Declaration of variables
    private ImageButton mSettings;
    private ImageButton mHomeButton;
    private ImageButton mHistoryButton;
    private ImageButton mNewPostButton;
    private ImageButton mRewardsButton;
    private ImageButton mProfileButton;
    private ImageView mBackgroundImage;
    private ImageView mProfileIcon;
    private TextView mUsername;
    private TextView mResponses;
    private TextView mPoints;
    private TextView mPosts;
    private TextView mGender;
    private TextView mOrigin;
    private TextView mNoMessages;
    private RecyclerView mFeedList;

    private String category = "profile";
    private Boolean openSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Assigning of variable values
        mSettings = findViewById(R.id.ibProfileEditAccount);
        mHomeButton = findViewById(R.id.ibNavigationHome);
        mHistoryButton = findViewById(R.id.ibNavigationHistory);
        mNewPostButton = findViewById(R.id.ibNavigationNewPost);
        mRewardsButton = findViewById(R.id.ibNavigationRewards);
        mProfileButton = findViewById(R.id.ibNavigationProfile);
        mBackgroundImage = findViewById(R.id.ivProfileBackground);
        mProfileIcon = findViewById(R.id.ivProfileIcon);
        mUsername = findViewById(R.id.tvProfileUsername);
        mResponses = findViewById(R.id.tvProfileResponses);
        mPoints = findViewById(R.id.tvProfilePoints);
        mPosts = findViewById(R.id.tvProfilePosts);
        mGender = findViewById(R.id.tvProfileGender);
        mOrigin = findViewById(R.id.tvProfileOrigin);
        mNoMessages = findViewById(R.id.tvProfileNoMessages);
        mFeedList = findViewById(R.id.rlProfileFeed);

        mNoMessages.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        openSettings = intent.getBooleanExtra("OPEN_SETTINGS", false);

        if (openSettings) {
            final UserSettingsDialog userSettingsDialog = new UserSettingsDialog(this, new Dialog(this));
            userSettingsDialog.startLoadingAnimation();
        }

        FirebaseFirestore.getInstance().collection("entries").whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                Integer postsAmount = snapshotList.size();
                mPosts.setText(String.valueOf(postsAmount));
            }
        });

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mUsername.setText(documentSnapshot.getString("username"));
                Float allTimePoints = documentSnapshot.getLong("alltimepoints").floatValue();
                String pointsString = "";
                if (allTimePoints >= 1000) {
                    allTimePoints = allTimePoints/1000;
                    pointsString = String.format("%.1f" + "k", allTimePoints);
                } else {
                    pointsString = String.valueOf(allTimePoints.intValue());
                }
                mPoints.setText(pointsString);
                String getGender = documentSnapshot.getString("gender");
                String getOrigin = documentSnapshot.getString("origin");
                String genderOutput = "";
                String originOutput = "";
                switch (getGender) {
                    case "Male":
                        genderOutput = "Male";
                        break;
                    case "Female":
                        genderOutput = "Female";
                        break;
                    case "Other":
                        genderOutput = "Other Gender";
                        break;
                    case "Did Not Disclose":
                        genderOutput = "Gender Undisclosed";
                        break;
                }
                switch (getOrigin) {
                    case "True":
                        originOutput = "Indigenous / Torres Strait";
                        break;
                    case "False":
                        originOutput = "Non-Indigenous";
                        break;
                    case "Did Not Disclose":
                        originOutput = "Origin Undisclosed";
                        break;
                }
                mGender.setText(genderOutput);
                mOrigin.setText(originOutput);
            }
        });

        FirebaseFirestore.getInstance().collection("entries").whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                ArrayList<Boolean> analysedHistory = new ArrayList<Boolean>();
                for (DocumentSnapshot document: snapshotList) {
                    analysedHistory.add(document.getBoolean("analysed"));
                }
                if (analysedHistory.size() < 1) {
                    mResponses.setText("---");
                } else {
                    Integer analyseCount = 0;
                    for (Boolean value: analysedHistory) {
                        if (value) {
                            analyseCount += 1;
                        }
                    }
                    mResponses.setText(String.valueOf(analyseCount));
                }
            }
        });

        FirebaseStorage.getInstance().getReference().child("profileicons/").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar).fallback(R.drawable.avatar).into(mProfileIcon);
            }
        });

        NavigationBar navigationBar = new NavigationBar();
        navigationBar.create(mHomeButton, mHistoryButton, mNewPostButton, mRewardsButton, mProfileButton);
        navigationBar.getLocation(category);
        navigationBar.adjustToPage();

        // Making the Feed
        Context pageContext = this;
        FirebaseFirestore.getInstance().collection("messages").orderBy("date", Query.Direction.DESCENDING).whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                ArrayList<String> identifierData = new ArrayList<String>();
                ArrayList<Date> timeData = new ArrayList<Date>();
                ArrayList<String> titleData = new ArrayList<String>();
                ArrayList<String> idData = new ArrayList<String>();
                ArrayList<String> authorData = new ArrayList<String>();
                ArrayList<String> contentData = new ArrayList<String>();
                ArrayList<Boolean> readData = new ArrayList<Boolean>();
                for (DocumentSnapshot snapshot: snapshotList) {
                    identifierData.add(snapshot.getString("identifier"));
                    Date value = snapshot.getTimestamp("date").toDate();
                    timeData.add(value);
                    titleData.add(snapshot.getString("title"));
                    idData.add(snapshot.getString("user"));
                    authorData.add(snapshot.getString("author"));
                    contentData.add(snapshot.getString("content"));
                    readData.add(snapshot.getBoolean("read"));
                }
                FeedListAdapter listAdapter = new FeedListAdapter(pageContext, identifierData, timeData, titleData, idData, authorData, contentData, readData, FirebaseAuth.getInstance().getCurrentUser().getUid(), new Dialog(pageContext));
                mFeedList.setAdapter(listAdapter);
                mFeedList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                if (snapshotList.size() == 0) {
                    mNoMessages.setVisibility(View.VISIBLE);
                }
            }
        });

        // Get background
        BackgroundGenerator background = new BackgroundGenerator();
        Glide.with(mBackgroundImage).load(background.login()).centerCrop().placeholder(R.drawable.custom_background_2)
                .error(R.drawable.custom_background_2).fallback(R.drawable.custom_background_2).into(mBackgroundImage);

        // Edit account
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final UserSettingsDialog userSettingsDialog = new UserSettingsDialog(pageContext, new Dialog(pageContext));
                userSettingsDialog.startLoadingAnimation();
            }
        });

        // Go to Home
        mHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchXActivity("home");
            }
        });

        // Go to History
        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchXActivity("history");
            }
        });

        // Create a New Post
        mNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchXActivity("new-post");
            }
        });

        // Go to Rewards
        mRewardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchXActivity("rewards");
            }
        });

        // Go to Profile
        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchXActivity("profile");
            }
        });
    }

    // Go to XActivity
    public void launchXActivity(String activity) {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        Intent historyIntent = new Intent(this, HistoryActivity.class);
        Intent newPostIntent = new Intent(this, NewPostActivity.class);
        Intent rewardsIntent = new Intent(this, RewardsActivity.class);
        Intent profileIntent = new Intent(this, ProfileActivity.class);

        switch (activity) {
            case "home":
                System.out.println("Section clicked: Home");
                startActivity(homeIntent);
                break;
            case "history":
                System.out.println("Section clicked: History");
                startActivity(historyIntent);
                break;
            case "new-post":
                System.out.println("Section clicked: New Post");
                startActivity(newPostIntent);
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                break;
            case "rewards":
                System.out.println("Section clicked: Rewards");
                startActivity(rewardsIntent);
                break;
            case "profile":
                System.out.println("Section clicked: Profile");
                startActivity(profileIntent);
                break;
        }
    }
}