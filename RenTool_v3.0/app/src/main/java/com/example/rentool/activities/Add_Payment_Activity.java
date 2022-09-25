package com.example.rentool.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.rentool.R;
import com.example.rentool.domain.Tool;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class Add_Payment_Activity extends AppCompatActivity {
    private TextInputLayout cardNumber, cardHolderName, cardDate, cardCVV ;
    private TextInputEditText cardDateEditText, cardNumEditText, cardHolderEditName, CVVEditText ;
    private ImageView backBtn, homeBtn ;
    private Button checkOut ;
    private Tool tool ;
    private String toolId ;
    private SwitchCompat rememberCard ;
    private FirebaseAuth mAuth ;
    private static String sub ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        mAuth = FirebaseAuth.getInstance() ;

        cardNumber = findViewById(R.id.cardNumber) ;
        cardHolderName = findViewById(R.id.cardHolderName) ;
        cardDate = findViewById(R.id.cardDate) ;
        cardCVV = findViewById(R.id.cardCVV) ;
        cardDateEditText = findViewById(R.id.cardDateEditText) ;
        cardNumEditText = findViewById(R.id.cardNumEditText) ;
        cardHolderEditName = findViewById(R.id.cardHolderEditName) ;
        CVVEditText = findViewById(R.id.CVVEditText) ;
        rememberCard = findViewById(R.id.rememberCard) ;
        checkOut = findViewById(R.id.checkOut) ;
        backBtn = findViewById(R.id.backBtn) ;
        homeBtn = findViewById(R.id.homeBtn) ;

        final Object obj = getIntent().getSerializableExtra("toolDetails");
        toolId = getIntent().getStringExtra("toolId") ;
        if (obj instanceof Tool && obj != null) {
            tool = (Tool) obj ;
        }

        sub = getIntent().getStringExtra("subscribe") ;

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Add_Payment_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sub != null) {

                }
                else {
                    Intent intent = new Intent(Add_Payment_Activity.this, Address_Activity.class) ;
                    intent.putExtra("toolDetails", tool) ;
                    intent.putExtra("toolId", toolId) ;
                    startActivity(intent);
                    finish();
                }
            }
        });

        SharedPreferences cardSharedPref = getSharedPreferences("cardInfo", Context.MODE_PRIVATE) ;
        String saveCardSwitch = cardSharedPref.getString("rememberCard", "") ;
        String userId = cardSharedPref.getString("userId", "") ;
        if (saveCardSwitch.equals("true")) {
            if (userId.equals(mAuth.getCurrentUser().getUid())) {
                cardNumber.getEditText().setText(cardSharedPref.getString("cardNumber", ""));
                cardHolderName.getEditText().setText(cardSharedPref.getString("cardHolderName", ""));
                cardDate.getEditText().setText(cardSharedPref.getString("cardDate", ""));
                cardCVV.getEditText().setText(cardSharedPref.getString("cardCVV", ""));
                rememberCard.setChecked(true);
            }
        }
        else {
            rememberCard.setChecked(false);
        }

        cardNumEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                int count = 0 ;
                int inputlength = cardNumEditText.getText().toString().length();

                if (count <= inputlength && inputlength == 4 || inputlength == 9 || inputlength == 14) {
                    cardNumEditText.setText(cardNumEditText.getText().toString() + " ");
                    int pos = cardNumEditText.getText().length();
                    cardNumEditText.setSelection(pos);
                }
                else if (count >= inputlength && (inputlength == 4 || inputlength == 9 || inputlength == 14)) {
                    cardNumEditText.setText(cardNumEditText.getText().toString()
                            .substring(0, cardNumEditText.getText().toString().length() - 1));

                    int pos = cardNumEditText.getText().length();
                    cardNumEditText.setSelection(pos);
                }
                count = cardNumEditText.getText().toString().length();
            }
        });

        cardDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                int count = 0 ;
                int inputlength = cardDateEditText.getText().toString().length();

                if (count <= inputlength && inputlength == 2) {
                    cardDateEditText.setText(cardDateEditText.getText().toString() + "/");
                    int pos = cardDateEditText.getText().length() ;
                    cardDateEditText.setSelection(pos);
                }
                else if (count >= inputlength && inputlength == 2) {
                    cardDateEditText.setText(cardDateEditText.getText().toString()
                            .substring(0, cardDateEditText.getText().toString().length() - 1));
                    int pos = cardDateEditText.getText().length() ;
                    cardDateEditText.setSelection(pos);
                }
                count = cardDateEditText.getText().toString().length() ;
            }
        });



        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cardnum = cardNumber.getEditText().getText().toString() ;
                String nameOnCard = cardHolderName.getEditText().getText().toString() ;
                String dateOnCard = cardDate.getEditText().getText().toString() ;
                String CVV = cardCVV.getEditText().getText().toString() ;

                if (!validateName() || !validateCardNumber() || !validateCardCVV() || !validateDate()) {
                    return;
                }
                else {
                    if (rememberCard.isChecked()) {
                        SharedPreferences cardSharedPref = getSharedPreferences("cardInfo", Context.MODE_PRIVATE) ;
                        SharedPreferences.Editor cardEditor = cardSharedPref.edit() ;
                        cardEditor.putString("cardNumber", cardnum) ;
                        cardEditor.putString("cardHolderName", nameOnCard) ;
                        cardEditor.putString("cardDate", dateOnCard) ;
                        cardEditor.putString("cardCVV", CVV) ;
                        cardEditor.putString("rememberCard", "true") ;
                        cardEditor.putString("userId", mAuth.getCurrentUser().getUid()) ;

                        cardEditor.commit() ;
                        rememberCard.setChecked(true);
                    }
                    else {
                        SharedPreferences cardSharedPref = getSharedPreferences("cardInfo", Context.MODE_PRIVATE) ;
                        SharedPreferences.Editor cardEditor = cardSharedPref.edit() ;
                        cardEditor.putString("rememberCard", "false") ;
                        cardEditor.commit() ;
                        rememberCard.setChecked(false);
                    }

                }
                Intent intent = new Intent(Add_Payment_Activity.this, Payment_Activity.class) ;
                intent.putExtra("toolDetails", tool) ;
                intent.putExtra("toolId", tool.getToolId()) ;
                startActivity(intent);
                finish();
            }
        });



    }

    private boolean validateName() {
        String val = cardHolderName.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            cardHolderName.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(nameVal)) {
            cardHolderName.setError("Name is not valid") ;
            return false ;
        }

        else {
            cardHolderName.setError(null) ;
            cardHolderName.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateCardNumber() {
        String val = cardNumber.getEditText().getText().toString() ;
        if (val.isEmpty()) {
            cardNumber.setError("Field cannot be empty") ;
            return false ;
        }

        else {
            cardNumber.setError(null) ;
            cardNumber.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateCardCVV() {
        String val = cardCVV.getEditText().getText().toString() ;
        if (val.isEmpty()) {
            cardCVV.setError("Field cannot be empty") ;
            return false ;
        }
        else {
            cardCVV.setError(null) ;
            cardCVV.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateDate() {

        if (cardDate.getEditText().getText().toString().isEmpty()) {
            cardDate.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!cardDate.getEditText().getText().toString().isEmpty()) {

            Calendar todayCal = Calendar.getInstance() ;
            int currentYear = todayCal.get(Calendar.YEAR) ;
            String[] val = cardDate.getEditText().getText().toString().split("/") ;
            int cardMonth = Integer.parseInt(val[0]) ;
            int cardYear = Integer.parseInt(val[1]) ;

            if (currentYear > cardYear + 2000) {
                cardDate.setError("Card date is not correct") ;
                return false ;
            }

            else if (cardMonth > 12 || cardMonth < 0) {
                cardDate.setError("Month is incorrect") ;
                return false ;
            }
        }
        cardDate.setError(null) ;
        cardDate.setErrorEnabled(false);
        return true ;
    }

    @Override
    public void onBackPressed() {
        if (sub != null) {

        }
        else {
            Intent intent = new Intent(Add_Payment_Activity.this, Address_Activity.class) ;
            intent.putExtra("toolDetails", tool) ;
            intent.putExtra("toolId", toolId) ;
            startActivity(intent);
            finish();
        }
    }

}