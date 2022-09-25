package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rentool.R;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add_Address_Activity extends AppCompatActivity {
    private TextInputLayout mName, mCity, mStreet, mCode, mNumber, mCountry;
    private Button mAddAddressbtn;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private ImageView backBtn, homeBtn ;
    private Tool tool ;
    private String toolId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_add_address);

        final Object obj = getIntent().getSerializableExtra("toolDetails");
        toolId = getIntent().getStringExtra("toolId") ;
        if (obj instanceof Tool && obj != null) {
            tool = (Tool) obj ;
        }

        mName=findViewById(R.id.ad_name);
        mCity=findViewById(R.id.ad_city);
        mCountry = findViewById(R.id.ad_country) ;
        mStreet=findViewById(R.id.ad_street);
        mCode=findViewById(R.id.ad_postal);
        mNumber=findViewById(R.id.ad_phone);
        mAddAddressbtn=findViewById(R.id.ad_add_address);
        mStore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        backBtn = findViewById(R.id.backBtn) ;
        homeBtn = findViewById(R.id.homeBtn) ;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Add_Address_Activity.this, Address_Activity.class) ;
                intent.putExtra("toolDetails", tool) ;
                intent.putExtra("toolId", toolId) ;
                startActivity(intent);
                finish();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Add_Address_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });

        mAddAddressbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!validateName() | !validateCity() | !validateCountry() | !validatePhone() | !validateStreet() | !validateCode()) {
                    return ;
                }

                else {
                    String name = mName.getEditText().getText().toString();
                    String city = mCity.getEditText().getText().toString();
                    String country = mCountry.getEditText().getText().toString() ;
                    String street = mStreet.getEditText().getText().toString();
                    String code = mCode.getEditText().getText().toString();
                    String number = mNumber.getEditText().getText().toString();

                    String final_address = "" ;
                    final_address =name + ", " + country + ", " + city + ", " + street + ", " + code ;

                    Map<String, String> mAddress = new HashMap<>() ;
                    mAddress.put("address", final_address) ;
                    mAddress.put("city", city) ;
                    mAddress.put("name", name) ;
                    mAddress.put("phoneNumber", number) ;

                    mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                            .collection("Address").add(mAddress).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Add_Address_Activity.this, "Address added", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Add_Address_Activity.this, Address_Activity.class) ;
                                        startActivity(intent);
                                    }
                                }
                            }) ;
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Add_Address_Activity.this, Address_Activity.class) ;
        Bundle toolArr = new Bundle() ;
        intent.putExtra("toolDetails", tool) ;
        intent.putExtra("toolId", toolId) ;
        startActivity(intent);
        finish();
    }

    private boolean validateName() {

        String val = mName.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            mName.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(nameVal)) {
            mName.setError("Name is not valid") ;
            return false ;
        }

        else {
            mName.setError(null) ;
            return true ;
        }
    }

    private boolean validateCity() {

        String val = mCity.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            mCity.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(nameVal)) {
            mCity.setError("City is not valid") ;
            return false ;
        }

        else {
            mCity.setError(null) ;
            mCity.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateStreet() {

        String val = mStreet.getEditText().getText().toString() ;

        if (val.isEmpty()) {
            mStreet.setError("Field cannot be empty") ;
            return false ;
        }

        else {
            mStreet.setError(null) ;
            mStreet.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateCode() {
        String val = mCode.getEditText().getText().toString() ;

        if (val.isEmpty()) {
            mCode.setError("Field cannot be empty") ;
            return false ;
        }

        else if (val.length() > 10) {
            mCode.setError("Postal code is wrong") ;
            return false ;
        }

        else {
            mCode.setError(null) ;
            mCode.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateCountry() {

        String val = mCountry.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            mCountry.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(nameVal)) {
            mCountry.setError("Country is not valid") ;
            return false ;
        }

        else {
            mCountry.setError(null) ;
            mCountry.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validatePhone() {

        String val = mNumber.getEditText().getText().toString() ;
        String number = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$" ;

        if (val.isEmpty()) {
            mNumber.setError("Field cannot be empty") ;
            return false ;
        }

        else if (!val.matches(number)) {
            mNumber.setError("Phone is not valid");
            return false ;
        }

        else {
            mNumber.setError(null) ;
            mNumber.setErrorEnabled(false);
            return true ;
        }
    }
}