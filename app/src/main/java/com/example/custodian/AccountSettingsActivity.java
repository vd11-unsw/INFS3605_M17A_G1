package com.example.custodian;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AccountSettingsActivity extends AppCompatActivity {

    // Disable action bar back button
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
    }
}