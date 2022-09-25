package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentool.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private TextInputLayout nameTxt, mailTxt, passTxt, confirmPass, phoneTxt, date;
    private TextInputEditText datePickerField ;
    private Button regBtn, loginBtn;
    private ImageView logo_img ;
    private TextView logo_text, logo_desc ;
    private FirebaseFirestore mStore ;
    Dialog dialog ;
    String genders[] = {"male", "female"} ;
    AutoCompleteTextView autoCompleteTxt ;
    ArrayAdapter<String> adapterGenders ;
    private DatePickerDialog.OnDateSetListener mDateSetListener ;
    private FirebaseAuth mAuth;
    private static int userYear, birthYear ;
    private static String birthDate ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance() ;

        mailTxt = findViewById(R.id.emailTxt);
        passTxt = findViewById(R.id.passwordTxt);
        phoneTxt = findViewById(R.id.phoneNumberTxt);
        confirmPass = findViewById(R.id.confirmPasswordTxt);
        regBtn = findViewById(R.id.regBtn);
        loginBtn = findViewById(R.id.loginBtn) ;
        nameTxt = findViewById(R.id.nameTxt) ;
        logo_text = findViewById(R.id.logo_text) ;
        logo_desc = findViewById(R.id.logo_desc) ;
        logo_img = findViewById(R.id.logo_img) ;
        date = findViewById(R.id.date) ;
        autoCompleteTxt = findViewById(R.id.auto_complete_txt) ;
        datePickerField = findViewById(R.id.datePickerField) ;

        adapterGenders = new ArrayAdapter<String>(this, R.layout.auto_complete_list, genders) ;
        autoCompleteTxt.setAdapter(adapterGenders);


        dialog = new Dialog(Register.this) ;
        dialog.setContentView(R.layout.activity_register_message);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation ;

        Button goLogin = dialog.findViewById(R.id.goLogin) ;

        datePickerField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance() ;
                int year = cal.get(Calendar.YEAR) ;
                int month = cal.get(Calendar.MONTH) ;
                int day = cal.get(Calendar.DAY_OF_MONTH) ;


                DatePickerDialog dateDialog = new DatePickerDialog(
                        Register.this, R.style.datePickerStyle, mDateSetListener,
                        year, month, day) ;
                dateDialog.getDatePicker().setMaxDate(new Date().getTime());
                dateDialog.getDatePicker().setMinDate(1900);
                dateDialog.getWindow();
                dateDialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1 ;
                userYear = year ;
                validateDate() ;
                birthDate = day + "/" + month + "/" + year ;
                birthYear = year ;
                date.getEditText().setText(birthDate);
            }
        } ;

        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, Login.class);
                Toast.makeText(Register.this, "Login", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);

                Pair[] pairs = new Pair[12] ;

                pairs[0] = new Pair<View, String> (mailTxt, "mailTxt") ;
                pairs[1] = new Pair<View, String> (passTxt, "passTxt") ;
                pairs[2] = new Pair<View, String> (phoneTxt, "phoneTxt") ;
                pairs[3] = new Pair<View, String> (confirmPass, "confirmPass") ;
                pairs[4] = new Pair<View, String> (regBtn, "regBtn") ;
                pairs[5] = new Pair<View, String> (loginBtn, "loginBtn") ;
                pairs[6] = new Pair<View, String> (nameTxt, "nameTxt") ;
                pairs[7] = new Pair<View, String> (logo_text, "logo_text") ;
                pairs[8] = new Pair<View, String> (logo_desc, "logo_desc") ;
                pairs[9] = new Pair<View, String> (logo_img, "logo_img") ;
                pairs[10] = new Pair<View, String> (date, "date") ;
                pairs[11] = new Pair<View, String> (autoCompleteTxt, "autoCompleteTxt") ;

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Register.this, pairs) ;
                startActivity(intent, options.toBundle()) ;
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateEmail() | !validateName() | !validatePass() | !validateConfPass() | !validatePhone() | !validateDate() | !validateGender()) {
                    return ;
                }

                else {
                    isUser() ;
                }

            }
        });



    }


    private void isUser() {

        String name = nameTxt.getEditText().getText().toString() ;
        String email = mailTxt.getEditText().getText().toString().toLowerCase() ;
        String pass = passTxt.getEditText().getText().toString() ;
        String phone = phoneTxt.getEditText().getText().toString() ;
        String gender = autoCompleteTxt.getText().toString() ;

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    String id = task.getResult().getUser().getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("email", email);
                    user.put("password", pass);
                    user.put("phoneNum", phone);
                    user.put("yearOfBirth", birthYear);
                    user.put("gender", gender) ;
                    user.put("admin", false) ;
                    user.put("id", task.getResult().getUser().getUid());
                    user.put("img_url", null) ;
                    user.put("address", null) ;
                    user.put("profession", null) ;
                    user.put("location", null) ;

                    // Add a new document with a generated ID
                    mStore.collection("Users").document(id).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", id);
                            mStore.collection("Users").document(id).set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dialog.show();
                                }
                            });
                        }
                    });
                }
                else {
                    try {
                        throw task.getException() ;
                    }
                    catch (FirebaseAuthUserCollisionException existEmail) {
                        Log.d("TAG", "onComplete: exist_email");
                        Toast.makeText(Register.this, "Email already Exist", Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e) {
                        Log.d("TAG", "onComplete: " + e.getMessage());
                    }
                }
            }
        }) ;
    }

    private boolean validateName() {

        String val = nameTxt.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            nameTxt.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(nameVal)) {
            nameTxt.setError("Name is not valid") ;
            return false ;
        }

        else {
            nameTxt.setError(null) ;
            nameTxt.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateDate() {
        Calendar todayCal = Calendar.getInstance() ;
        int currentYear = todayCal.get(Calendar.YEAR) ;
        if (currentYear - userYear < 18) {
            date.setError("You must be over 18") ;
            return false ;
        }
        else if (date.getEditText().getText().toString().isEmpty()) {
            date.setError("Field cannot be empty") ;
            return false ;
        }
        else {
            date.setError(null) ;
            date.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateEmail() {
        String val = mailTxt.getEditText().getText().toString() ;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.+[a-zA-Z]+" ;

        if (val.isEmpty()) {
            mailTxt.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(emailPattern)) {
            mailTxt.setError("Invalid email address");
            return false ;
        }

        else {
            mailTxt.setError(null) ;
            mailTxt.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validatePass() {

        String val = passTxt.getEditText().getText().toString() ;
        String passVal = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9*]{8,15}" ;

        if (val.isEmpty()) {
            passTxt.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(passVal)) {
            passTxt.setError("Password is too weak") ;
            return false ;
        }

        else {
            passTxt.setError(null) ;
            return true ;
        }
    }

    private boolean validateConfPass() {

        String val = confirmPass.getEditText().getText().toString() ;
        String pass = passTxt.getEditText().getText().toString() ;
        String passVal = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9*]{8,15}" ;

        if (val.isEmpty()) {
            confirmPass.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(passVal)) {
            confirmPass.setError("Password is too weak") ;
            return false ;
        }

        else if (!pass.equals(val)) {
            confirmPass.setError("Password doesn't match");
            return false ;
        }

        else {
            confirmPass.setError(null) ;
            confirmPass.setErrorEnabled(false) ;
            return true ;
        }
    }

    private boolean validatePhone() {

        String val = phoneTxt.getEditText().getText().toString() ;
        String number = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$" ;

        if (val.isEmpty()) {
            phoneTxt.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(number)) {
            phoneTxt.setError("Phone is not valid");
            return false ;
        }

        else {
            phoneTxt.setError(null) ;
            return true ;
        }
    }

    private boolean validateGender() {
        String val = autoCompleteTxt.getText().toString() ;

        if (val.isEmpty()) {
            autoCompleteTxt.setError("Cannot be empty") ;
            return false ;
        }
        else {
            autoCompleteTxt.setError(null) ;
            return true ;
        }
    }

}