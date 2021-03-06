package com.example.custodian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // Disable action bar back button
    @Override
    public void onBackPressed() {
    }

    // Declaration of variables
    private ImageButton mExitButton;
    private ImageButton mEmailHelpButton;
    private ImageButton mPasswordHelpButton;
    private Button mLoginButton;
    private Button mCreateAccountButton;
    private TextView mForgotDetailsButton;
    private EditText mEmailText;
    private EditText mPasswordText;
    private ImageView mBackgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Assigning of variable values
        mExitButton = findViewById(R.id.ibLoginExit);
        mEmailHelpButton = findViewById(R.id.ibLoginEmailHelp);
        mPasswordHelpButton = findViewById(R.id.ibLoginPasswordHelp);
        mLoginButton = findViewById(R.id.btLoginLogin);
        mCreateAccountButton = findViewById(R.id.btLoginCreateAccount);
        mForgotDetailsButton = findViewById(R.id.tvLoginForgotDetails);
        mEmailText = findViewById(R.id.etLoginEmailInput);
        mPasswordText = findViewById(R.id.etLoginPasswordInput);
        mBackgroundImage = findViewById(R.id.ivLoginBackground);

        Context pageContext = this;

        // Get background
        BackgroundGenerator background = new BackgroundGenerator();
        Glide.with(mBackgroundImage).load(background.login()).centerCrop().placeholder(R.drawable.custom_background_2)
                .error(R.drawable.custom_background_2).fallback(R.drawable.custom_background_2).into(mBackgroundImage);

        // Cancel log in
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Back Button Clicked");
                launchMainActivity();
            }
        });

        // Request for help on email input
        mEmailHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Email Help Button Clicked");
                final AlertDialog emailDialog = new AlertDialog(new Dialog(pageContext), "This is where you enter the email you used to create this account.", "information");
                emailDialog.startLoadingAnimation();
            }
        });

        // Request for help on password input
        mPasswordHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Password Help Button Clicked");
                final AlertDialog passwordDialog = new AlertDialog(new Dialog(pageContext), "This is where you enter the password associated with the email you used to create this account.", "information");
                passwordDialog.startLoadingAnimation();
            }
        });

        // Confirm log in
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Log In Button Clicked");
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    final AlertDialog invalidDialog = new AlertDialog(new Dialog(pageContext), "Please enter an email and password.", "warning");
                    invalidDialog.startLoadingAnimation();
                } else {
                    final LoadingDialog loadingDialog = new LoadingDialog(new Dialog(pageContext));
                    loadingDialog.startLoadingAnimation();
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                launchWelcomeActivity();
                            } else {
                                loadingDialog.dismissDialog();
                                final AlertDialog invalidDialog = new AlertDialog(new Dialog(pageContext), "There's something wrong with the email or password that you entered. Please try again.", "warning");
                                invalidDialog.startLoadingAnimation();
                            }
                        }
                    });
                }
            }
        });

        // Navigate to account creation
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Create Account Button Clicked");
                launchCreateAccountActivity();
            }
        });

        // Navigate to account recovery
        mForgotDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Forgot Details Button Clicked");
                launchAccountRecoveryWebsite();
            }
        });
    }

    // Go to MainActivity
    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // Go to CreateAccountActivity
    private void launchCreateAccountActivity() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    // Go to Account Recovery Website
    private void launchAccountRecoveryWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.custodian.com.au/forgot-my-credentials/"));
        startActivity(intent);
    }

    // Go to WelcomeActivity
    private void launchWelcomeActivity() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
}