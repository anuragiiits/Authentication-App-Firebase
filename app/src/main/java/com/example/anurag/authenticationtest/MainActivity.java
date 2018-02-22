package com.example.anurag.authenticationtest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout, btnEmail, btnRefresh;
    private TextView textView;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    final private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"onCreate");                          //debugging purpose

        auth = FirebaseAuth.getInstance();                    //initialize firebase auth
        btnLogout = (Button) findViewById(R.id.logout);
        btnEmail = (Button) findViewById(R.id.emailBtn);
        btnRefresh = (Button) findViewById(R.id.refreshBtn);

        authListener = new FirebaseAuth.AuthStateListener() {           //keep check on the change of auth state
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));     //if user is logged out for any reason
                    finishAffinity();                                   //clear browser stack to prohibit the page after logging out
                }
            }
        };

        FirebaseUser user = auth.getCurrentUser();                      //get current active user object
        if(user.isEmailVerified()){
            email_verified();                                           //function to implement if email is verified
        }
        else {
            email_not_verified();                                       //function to implement if email is not verified
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();                                               //sign out the user
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {        //to resend the email if email is not received due to any network issue
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(MainActivity.this, "Email sent",
                                            Toast.LENGTH_SHORT).show();                         //display quick toast
                                }
                            }
                        });
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity();                              //button to refresh the activity page
            }
        });
    }

    private void refreshActivity(){                              //function to refresh the activity with authentication details
        FirebaseAuth.getInstance().getCurrentUser()
                .reload()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(user.isEmailVerified()){
                            email_verified();
                        }
                        else {
                            email_not_verified();
                        }
                    }
                });
    }

    private void email_verified(){                              //function to invoke if email is verified
        btnEmail.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        textView = (TextView) findViewById(R.id.textView);
        String user_name =  user.getDisplayName();
        textView.setText("Hello "+user_name);
    }

    private void email_not_verified(){                           //function to invoke if email is not verified
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("Verify your email to continue");
    }
    //sign out method
    public void signOut() {
        auth.signOut();
    }                       //sign out the user

    @Override
    public void onBackPressed() {
        finishAffinity();                                           //stop from going to login page on pressing back button if user is not logged out
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        refreshActivity();
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
