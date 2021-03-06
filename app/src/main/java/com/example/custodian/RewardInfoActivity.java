package com.example.custodian;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RewardInfoActivity extends AppCompatActivity {

    // Disable action bar back button
    @Override
    public void onBackPressed() {
    }

    // Declaration of variables
    private ImageView mBackground;
    private TextView mTitle;
    private TextView mDescription;
    private Button mConfirmButton;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_info);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Assigning of variable values
        mBackground = findViewById(R.id.ivRewardInfoImage);
        mTitle = findViewById(R.id.tvRewardInfoTitle);
        mDescription = findViewById(R.id.tvRewardInfoDescription);
        mConfirmButton = findViewById(R.id.btRewardInfoConfirm);
        mCancelButton = findViewById(R.id.btRewardInfoCancel);

        // Get and set data
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        Integer cost = intent.getIntExtra("cost", 0);
        System.out.println(id);

        FirebaseFirestore.getInstance().document("rewards/"+id).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                mTitle.setText(value.getString("title"));
                mDescription.setText(value.getString("description"));
                Glide.with(mBackground).load(value.getString("image")).centerCrop().placeholder(R.drawable.custom_background_2)
                        .error(R.drawable.custom_background_2).fallback(R.drawable.custom_background_2).into(mBackground);
                mConfirmButton.setText("- " + value.getLong("cost").intValue() + "pts");
            }
        });

        Context pageContext = this;
        FirebaseFirestore.getInstance().document("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Integer currentPoints = documentSnapshot.getLong("currentpoints").intValue();
                mConfirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentPoints < cost) {
                            final com.example.custodian.AlertDialog notEnoughPointsDialog = new com.example.custodian.AlertDialog(new Dialog(pageContext), "You don't have enough points to redeem this reward. Accumulate more by making posts!", "warning");
                            notEnoughPointsDialog.startLoadingAnimation();
                        } else {
                            showWarningDialog(getCurrentFocus(), currentPoints, cost);
                        }
                    }
                });
            }
        });

        // Cancel reward redemption
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchRewardsActivity();
            }
        });
    }

    // Warn user that any entered data will be wiped
    public void showWarningDialog(View v, Integer currentPoints, Integer cost) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are you sure?");
        alert.setMessage("If you proceed, points will be immediately deducted from your account and your one-time use code will be created. It is " +
                "recommended that you continue when you are close to redemption.");
        alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseFirestore.getInstance().document("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).update("currentpoints", currentPoints - cost);
                launchRedemptionCodeActivity();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.create().show();
    }

    // Go to RedemptionCodeActivity
    private void launchRedemptionCodeActivity() {
        Intent intent = new Intent(this, RedemptionCodeActivity.class);
        startActivity(intent);
    }

    // Go to RewardsActivity
    private void launchRewardsActivity() {
        Intent intent = new Intent(this, RewardsActivity.class);
        startActivity(intent);
    }
}