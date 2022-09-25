package com.example.rentool.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rentool.R;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Login extends AppCompatActivity {

    private static final String FILE_EMAIL = "remember";

    private TextInputLayout emailTxt, passTxt;
    private Button signupBtn, signinBtn, forgotpassBtn;
    private ImageView logo_img;
    private TextView logo_text, logo_desc;
    private CheckBox rememberMe;
    private TextInputEditText emailEditText, passwordEditText;

    private FirebaseAuth mAuth;
    FirebaseFirestore mStore ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance() ;

        // Variables from Login fields
        emailTxt = findViewById(R.id.emailTxt);
        passTxt = findViewById(R.id.passTxt);
        signupBtn = findViewById(R.id.signupBtn);
        signinBtn = findViewById(R.id.signinBtn);
        forgotpassBtn = findViewById(R.id.forgotpassBtn);
        logo_img = findViewById(R.id.logo_img);
        logo_text = findViewById(R.id.logo_text);
        logo_desc = findViewById(R.id.logo_desc);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMe = findViewById(R.id.rememberMe);


        SharedPreferences loginSharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE) ;
        String remCheckBox = loginSharedPref.getString("rememberMeChecked", "") ;

        if (remCheckBox.equals("true")) {
            String remUserEmail = loginSharedPref.getString("email", "") ;
            String remPassword = loginSharedPref.getString("password", "") ;
            emailTxt.getEditText().setText(remUserEmail);
            passTxt.getEditText().setText(remPassword);
            rememberMe.setChecked(true);
        }
        else {
            rememberMe.setChecked(false);
        }


        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);

                Pair[] pairs = new Pair[8];

                pairs[0] = new Pair<View, String>(logo_img, "logo_img");
                pairs[1] = new Pair<View, String>(logo_text, "logo_text");
                pairs[2] = new Pair<View, String>(logo_desc, "logo_desc");
                pairs[3] = new Pair<View, String>(emailTxt, "emailTxt");
                pairs[4] = new Pair<View, String>(passTxt, "passTxt");
                pairs[5] = new Pair<View, String>(signinBtn, "signinBtn");
                pairs[6] = new Pair<View, String>(signupBtn, "signupBtn");
                pairs[7] = new Pair<View, String>(forgotpassBtn, "forgotpassBtn");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });

        forgotpassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, forgotPassword.class);

                Pair[] pairs = new Pair[8];

                pairs[0] = new Pair<View, String>(logo_img, "logo_img");
                pairs[1] = new Pair<View, String>(logo_text, "logo_text");
                pairs[2] = new Pair<View, String>(logo_desc, "logo_desc");
                pairs[3] = new Pair<View, String>(emailTxt, "emailTxt");
                pairs[4] = new Pair<View, String>(passTxt, "passTxt");
                pairs[5] = new Pair<View, String>(signinBtn, "signinBtn");
                pairs[6] = new Pair<View, String>(signupBtn, "signupBtn");
                pairs[7] = new Pair<View, String>(forgotpassBtn, "forgotpassBtn");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get Data from login Screen
                String userEmail = emailTxt.getEditText().getText().toString().trim();
                String userPass = passTxt.getEditText().getText().toString().trim();

                    if (!validateEmail() | !validatePass()) {
                        return;
                    }
                    else {
                        isUser();
                    }

            }
        });

    }


        private void isUser (){

            String userEmail = emailTxt.getEditText().getText().toString().trim();
            String userPass = passTxt.getEditText().getText().toString().trim();

            mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mStore.collection("Users").document(task.getResult().getUser().getUid())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            User admin = task.getResult().toObject(User.class) ;
                                            if (admin.isAdmin() == true) {
                                                if (rememberMe.isChecked()) {
                                                    SharedPreferences loginSharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE) ;
                                                    SharedPreferences.Editor loginEditor = loginSharedPref.edit() ;
                                                    loginEditor.putString("email", userEmail) ;
                                                    loginEditor.putString("password", userPass) ;
                                                    loginEditor.putString("rememberMeChecked", "true") ;
                                                    loginEditor.commit() ;
                                                }
                                                else {
                                                    SharedPreferences loginSharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE) ;
                                                    SharedPreferences.Editor loginEditor = loginSharedPref.edit() ;
                                                    loginEditor.putString("rememberMeChecked", "false") ;
                                                    loginEditor.commit() ;
                                                }
                                                Intent intent = new Intent(Login.this, Admin_Home_Activity.class);
                                                startActivity(intent);
                                            }
                                            else {
                                                if (rememberMe.isChecked()) {
                                                    SharedPreferences loginSharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE) ;
                                                    SharedPreferences.Editor loginEditor = loginSharedPref.edit() ;
                                                    loginEditor.putString("email", userEmail) ;
                                                    loginEditor.putString("password", userPass) ;
                                                    loginEditor.putString("rememberMeChecked", "true") ;
                                                    loginEditor.commit() ;
                                                }
                                                else {
                                                    SharedPreferences loginSharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE) ;
                                                    SharedPreferences.Editor loginEditor = loginSharedPref.edit() ;
                                                    loginEditor.putString("rememberMeChecked", "false") ;
                                                    loginEditor.commit() ;
                                                }

                                                Map<String, Object> onlineMap = new HashMap<>() ;
                                                Date currentDate = new Date() ;
                                                onlineMap.put("time", currentDate.getTime()) ;
                                                onlineMap.put("status", "online") ;
                                                mStore.collection("OnlineUsers").document(mAuth.getCurrentUser().getUid())
                                                        .set(onlineMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Log.d("TAG", "User is online") ;
                                                            }
                                                        }) ;

                                                Intent intent = new Intent(Login.this, Main_Activity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                }) ;
//                        if (rememberMe.isChecked()) {
//                            SharedPreferences loginSharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE) ;
//                            SharedPreferences.Editor loginEditor = loginSharedPref.edit() ;
//                            loginEditor.putString("email", userEmail) ;
//                            loginEditor.putString("password", userPass) ;
//                            loginEditor.putString("rememberMeChecked", "true") ;
//                            loginEditor.commit() ;
//                        }
//                        else {
//                            SharedPreferences loginSharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE) ;
//                            SharedPreferences.Editor loginEditor = loginSharedPref.edit() ;
//                            loginEditor.putString("rememberMeChecked", "false") ;
//                            loginEditor.commit() ;
//                        }
//                        Intent intent = new Intent(Login.this, Main_Activity.class);
//                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(Login.this, "Failed to login! check your credentials.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private boolean validateEmail () {
            String val = emailTxt.getEditText().getText().toString();
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if (val.isEmpty()) {
                emailTxt.setError("Field cannot be empty");
                return false;
            } else if (!val.matches(emailPattern)) {
                emailTxt.setError("Invalid email address");
                return false;
            } else {
                emailTxt.setError(null);
                emailTxt.setErrorEnabled(false);
                return true;
            }
        }

        private boolean validatePass () {

            String val = passTxt.getEditText().getText().toString();

            if (val.isEmpty()) {
                passTxt.setError("Field cannot be empty");
                return false;
            } else {
                passTxt.setError(null);
                return true;
            }
        }


}

