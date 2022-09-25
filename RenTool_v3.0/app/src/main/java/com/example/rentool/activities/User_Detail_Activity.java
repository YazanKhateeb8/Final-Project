package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.domain.Tool;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class User_Detail_Activity extends AppCompatActivity {
    private ImageView backBtn, homeBtn, sellerImage ;
    private TextView sellerEmail, sellerPhoneNum, sellerAddress, sellerName, sellerProfession, pageTitle ;
    private Button rentTool ;
    private Tool tool ;
    private FirebaseFirestore mStore ;
    private FirebaseAuth mAuth ;
    private String toolId ;
    private User sellerInfo ;
    public static int btnFlag = 0 ;
    private static int userFlag = 0 ;
    private static int PERMESSION_CODE = 100 ;
    private static int locationFalg = 0 ;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        backBtn = findViewById(R.id.backBtn) ;
        homeBtn = findViewById(R.id.homeBtn) ;
        rentTool = findViewById(R.id.rentTool) ;
        sellerImage = findViewById(R.id.sellerImage) ;
        sellerEmail = findViewById(R.id.sellerEmail) ;
        sellerPhoneNum = findViewById(R.id.sellerPhoneNum) ;
        sellerAddress = findViewById(R.id.sellerAddress) ;
        sellerName = findViewById(R.id.sellerName) ;
        sellerProfession = findViewById(R.id.sellerProfession) ;
        pageTitle = findViewById(R.id.pageTitle) ;

        mStore = FirebaseFirestore.getInstance() ;
        mAuth = FirebaseAuth.getInstance() ;

        final Object obj = getIntent().getSerializableExtra("toolDetails");
        toolId = getIntent().getStringExtra("toolId") ;
        if (obj instanceof Tool && obj != null) {
            tool = (Tool) obj ;
            getSellerInfo() ;
            userFlag = 1 ;
            sellerEmail.setTextColor(Color.BLUE);
            sellerPhoneNum.setTextColor(Color.BLUE);
        }
        else {
            final Object contractor = getIntent().getSerializableExtra("userDetail");
            if (contractor instanceof User && contractor != null) {
                sellerInfo = (User) contractor ;
                getContractorInfo(sellerInfo) ;
                userFlag = 1 ;
                sellerEmail.setTextColor(Color.BLUE);
                sellerPhoneNum.setTextColor(Color.BLUE);
            }
            else {
                getUserInfo() ;
            }
        }

        /* Allows the user to send an email
            to the seller via clicking on the seller's email.
         */
        sellerEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userFlag == 1) {
                    sendMail() ;
                }
            }
        });

        /* Allows the user to call the seller via
            pressing on the seller's phone number.
         */
        sellerPhoneNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userFlag == 1) {
                    callSeller() ;
                }
            }
        });

        sellerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userFlag == 1) {
                    if (locationFalg == 1) {
                        Intent intent = new Intent(User_Detail_Activity.this, Map_Activity.class);
                        intent.putExtra("address", sellerInfo.getAddress());
                        intent.putExtra("latLong", sellerInfo.getLocation());
                        intent.putExtra("userDetail", sellerInfo);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(User_Detail_Activity.this, "The seller did not set an address", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnFlag == 0) {
                    Intent intent = new Intent(User_Detail_Activity.this, Main_Activity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(User_Detail_Activity.this, Tool_Details_Activity.class);
                    intent.putExtra("toolDetails", tool) ;
                    intent.putExtra("toolId", toolId) ;
                    startActivity(intent);
                    finish();
                }
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(User_Detail_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });

        rentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnFlag == 0) {
                    Intent intent = new Intent(User_Detail_Activity.this, Profile_Activity.class) ;
                    startActivity(intent);
                    finish();
                }
                else {

                }
            }
        });


    }


    @Override
    public void onBackPressed() {
        if (btnFlag == 0) {
            Intent intent = new Intent(User_Detail_Activity.this, Main_Activity.class);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent = new Intent(User_Detail_Activity.this, Tool_Details_Activity.class);
            intent.putExtra("toolDetails", tool) ;
            intent.putExtra("toolId", toolId) ;
            startActivity(intent);
            finish();
        }
    }

    private void callSeller() {
        if (ContextCompat.checkSelfPermission(User_Detail_Activity.this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(User_Detail_Activity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMESSION_CODE) ;
        }
        String phone = sellerPhoneNum.getText().toString() ;

        Intent intent = new Intent(Intent.ACTION_CALL) ;
        intent.setData(Uri.parse("tel:" + phone)) ;
        startActivity(intent);
    }

    // Does NOT work in emulator, only on the phone
    private void sendMail() {
        String emailAddress = sellerInfo.getEmail() ;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("mailto:" + emailAddress)) ;
            intent.putExtra(Intent.EXTRA_EMAIL, emailAddress) ;
            if (intent.resolveActivity(getPackageManager()) != null) {

                startActivity(intent);
            }
            else {
                Toast.makeText(User_Detail_Activity.this, "No app installed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d("Tag", e + "") ;
        }

//        intent.setType("message/rfc822") ;
//        startActivity(Intent.createChooser(intent, "Choose an email Client"));

    }

    private void getSellerInfo() {
        mStore.collection("Users").document(tool.getUserId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            sellerInfo = task.getResult().toObject(User.class) ;
                            userFlag = 1 ;
                            if (sellerInfo.getImg_url() != null) {
                                Glide.with(getApplicationContext()).load(sellerInfo.getImg_url()).into(sellerImage) ;

                            }
                            if (sellerInfo.getAddress() != null) {
                                sellerAddress.setText(sellerInfo.getAddress());
                            }
                            if (sellerInfo.getProfession() != null) {
                                sellerProfession.setText(sellerInfo.getProfession());
                            }
                            if (sellerInfo.getLocation() != null) {
                                locationFalg = 1 ;
                                sellerAddress.setTextColor(Color.BLUE);
                            }
                            sellerEmail.setText(sellerInfo.getEmail());
                            sellerPhoneNum.setText(sellerInfo.getPhoneNum());
                            sellerName.setText(sellerInfo.getName());
                            pageTitle.setText("Seller Profile");
                            btnFlag = 1 ;
                        }
                    }
                }) ;
    }

    private void getContractorInfo(User contractor) {
        mStore.collection("Users").document(contractor.getId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            sellerInfo = task.getResult().toObject(User.class) ;
                            userFlag = 1 ;
                            if (sellerInfo.getImg_url() != null) {
                                Glide.with(getApplicationContext()).load(sellerInfo.getImg_url()).into(sellerImage) ;
                            }
                            if (sellerInfo.getAddress() != null) {
                                sellerAddress.setText(sellerInfo.getAddress());
                            }
                            if (sellerInfo.getProfession() != null) {
                                sellerProfession.setText(sellerInfo.getProfession());
                            }
                            if (sellerInfo.getLocation() != null) {
                                locationFalg = 1 ;
                                sellerAddress.setTextColor(Color.BLUE);
                            }
                            sellerEmail.setText(sellerInfo.getEmail());
                            sellerPhoneNum.setText(sellerInfo.getPhoneNum());
                            sellerName.setText(sellerInfo.getName());
                            rentTool.setVisibility(View.INVISIBLE);

                        }
                    }
                }) ;
    }

    private void getUserInfo() {
        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            sellerInfo = task.getResult().toObject(User.class) ;
                            userFlag = 0 ;
                            if (sellerInfo.getImg_url() != null) {
                                Glide.with(getApplicationContext()).load(sellerInfo.getImg_url()).into(sellerImage) ;
                            }
                            if (sellerInfo.getAddress() != null) {
                                sellerAddress.setText(sellerInfo.getAddress());
                            }
                            if (sellerInfo.getProfession() != null) {
                                sellerProfession.setText(sellerInfo.getProfession());
                            }
                            sellerEmail.setText(sellerInfo.getEmail());
                            sellerPhoneNum.setText(sellerInfo.getPhoneNum());
                            sellerName.setText(sellerInfo.getName());
                            pageTitle.setText("User Profile");
                            rentTool.setText("Edit Profile");
                        }
                    }
                }) ;
    }


}