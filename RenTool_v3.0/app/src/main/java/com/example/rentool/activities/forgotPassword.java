package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentool.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPassword extends AppCompatActivity {

    private TextInputLayout emailTxt ;
    private Button resetPass,  goLogin ;
    private ImageView logo_img ;
    private TextView logo_text, logo_desc ;
    private FirebaseAuth mAuth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance() ;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailTxt = findViewById(R.id.emailTxt);
        resetPass = findViewById(R.id.resetPass) ;
        goLogin = findViewById(R.id.goLogin);
        logo_img = findViewById(R.id.logo_img) ;
        logo_text = findViewById(R.id.logo_text);
        logo_desc = findViewById(R.id.logo_desc) ;





        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(forgotPassword.this, Login.class);

                Pair[] pairs = new Pair[6];

                pairs[0] = new Pair<View, String>(logo_img, "logo_img");
                pairs[1] = new Pair<View, String>(logo_text, "logo_text");
                pairs[2] = new Pair<View, String>(logo_desc, "logo_desc");
                pairs[3] = new Pair<View, String> (emailTxt, "emailTxt") ;
                pairs[4] = new Pair<View, String>(resetPass, "resetPass");
                pairs[5] = new Pair<View, String>(goLogin, "goLogin");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(forgotPassword.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailTxt.getEditText().getText().toString().trim() ;

                if (!validateEmail()) {
                    return;
                }

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(forgotPassword.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(forgotPassword.this, "Something went wrong!, Try again!", Toast.LENGTH_LONG).show();
                        }
                    }
                }) ;
            }
        });

    }

//    private void resetPassword() {
//
//
//    }

    private boolean validateEmail() {
        String val = emailTxt.getEditText().getText().toString() ;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+" ;

        if (val.isEmpty()) {
            emailTxt.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(emailPattern)) {
            emailTxt.setError("Invalid email address");
            return false ;
        }

        else {
            emailTxt.setError(null) ;
            emailTxt.setErrorEnabled(false);
            return true ;
        }
    }

}