package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Payment_Activity extends AppCompatActivity {
    private Button placeOrder ;
    private ImageView backBtn, homeBtn, toolImage ;
    private TextView toolName, toolPrice ;
    private TextView toolCost, rentFromDate, rentToDate, rentDuration, totalCost ;
    private double subTotal = 0.0 ;
    private FirebaseFirestore mStore ;
    private FirebaseAuth mAuth ;
    private String documentId = "" ;
    private Tool tool ;
    private String toolId ;
    private String soldDocId ;
    private static int finalPrice ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mStore = FirebaseFirestore.getInstance() ;
        mAuth = FirebaseAuth.getInstance() ;

        backBtn = findViewById(R.id.backBtn) ;
        homeBtn = findViewById(R.id.homeBtn) ;
        placeOrder = findViewById(R.id.placeOrder) ;
        toolImage = findViewById(R.id.toolImage) ;
        toolName = findViewById(R.id.toolName) ;
        toolPrice = findViewById(R.id.toolPrice) ;
        toolCost = findViewById(R.id.toolCost) ;
        rentFromDate = findViewById(R.id.rentFromDate) ;
        rentToDate = findViewById(R.id.rentToDate) ;
        rentDuration = findViewById(R.id.rentDuration) ;
        totalCost = findViewById(R.id.totalCost) ;


        final Object obj = getIntent().getSerializableExtra("toolDetails");
        toolId = getIntent().getStringExtra("toolId") ;
        if (obj instanceof Tool && obj != null) {
            tool = (Tool) obj ;
            toolCost.setText(tool.getPrice() + "₪");
            toolName.setText(tool.getName());
            Glide.with(getApplicationContext()).load(tool.getImg_url()).into(toolImage) ;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy") ;
            Date fromDate = tool.getFromDate() ;
            String readyFromDate = sdf.format(fromDate) ;
            rentFromDate.setText(readyFromDate);

            Date toDate = tool.getToDate() ;
            String readyToDate = sdf.format(toDate) ;
            rentToDate.setText(readyToDate);

            // For calculating the number of days between days
            long difference = Math.abs(tool.getToDate().getTime() - tool.getFromDate().getTime()) ;
            long diff = difference / (24 * 60 * 60 * 1000) ;
            String dayDifference = Long.toString(diff) ;
            int days = Integer.parseInt(dayDifference) + 1 ;
            rentDuration.setText(days + "");

            finalPrice = tool.getPrice() * days ;
            totalCost.setText(finalPrice + "₪");
        }



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Payment_Activity.this, Add_Payment_Activity.class) ;
                intent.putExtra("toolDetails", tool) ;
                intent.putExtra("toolId", tool.getToolId()) ;
                startActivity(intent);
                finish();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Payment_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });


        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> order = new HashMap<>() ;
                Date dateTime = new Date();
                order.put("dateTime", dateTime) ;
                order.put("total", finalPrice) ;
                order.put("userId", mAuth.getCurrentUser().getUid()) ;
                order.put("toolId", tool.getToolId()) ;
                order.put("sellerId", tool.getUserId()) ;

                mStore.collection("Orders").document().set(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("TAG", "Tool added to orders") ;
                    }
                }) ;

                mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                        .collection("Rented").document(toolId).set(tool)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("TAG", "tool added to rented") ;
                            }
                        }) ;

                mStore.collection("Sold").whereEqualTo("toolName",tool.getName())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        for (DocumentSnapshot doc : task.getResult()) {
                                            soldDocId = doc.getId() ;
                                            int sold = doc.getLong("sold").intValue() ;
                                            Map<String, Object> soldNum = new HashMap<>() ;
                                            soldNum.put("sold", sold + 1) ;
                                            mStore.collection("Sold").document(soldDocId)
                                                    .update(soldNum).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Log.d("TAG", "added to sold table") ;
                                                        }
                                                    }) ;
                                        }
                                    }
                                    else {
                                        Map<String, Object> newSoldMap = new HashMap<>() ;
                                        newSoldMap.put("toolName", tool.getName()) ;
                                        newSoldMap.put("sold", 1) ;
                                        mStore.collection("Sold").add(newSoldMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                Toast.makeText(Payment_Activity.this, "Map Added", Toast.LENGTH_LONG).show();
                                                Log.d("TAG", "Tool added successfully to sold table") ;
                                            }
                                        }) ;
                                    }
                                }
                            }
                        }) ;

                Toast.makeText(Payment_Activity.this, "Thank you for your purchase", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Payment_Activity.this, Orders_Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Payment_Activity.this, Add_Payment_Activity.class);
        intent.putExtra("toolDetails", tool) ;
        intent.putExtra("toolId", tool.getToolId()) ;
        startActivity(intent);
        finish();
    }
}