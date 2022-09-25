package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rentool.R;
import com.example.rentool.domain.Order;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Button placeOrder;
    private ImageView backBtn ;
    private String userId, toolId;
    private List<Tool> toolList;
    private TextView total_amount ;
    private String orderId ;
    private List<Order> orders ;
    private TextView date ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        backBtn = findViewById(R.id.backBtn) ;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TestActivity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid().toString();

        date = findViewById(R.id.date) ;

        toolList = new ArrayList<>();
        cartRecyclerView = findViewById(R.id.cart_item_container);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(TestActivity.this));
        cartRecyclerView.setHasFixedSize(true);
//        CartAdapter cartAdapter = new CartAdapter(toolList) ;
//        cartRecyclerView.setAdapter(cartAdapter);



//        mStore.collection("RentTools").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (DocumentSnapshot doc : task.getResult()) {
////                        Tool tool = doc.toObject(Tool.class) ;
//                        Map<String, Object> rated = new HashMap<>() ;
////                        rated.put("rated", 0) ;
////                        rated.put("rating", 0.0) ;
////                        rated.put("sold", 0) ;
//                        Date currentDate = new Date() ;
//
//                        Calendar cal = Calendar.getInstance() ;
//                        cal.set(cal.get(Calendar.YEAR), 9, 25) ;
//                        Date testDate = cal.getTime() ;
//                        rated.put("fromDate", currentDate) ;
//                        rated.put("toDate", testDate) ;
//                        mStore.collection("RentTools").document(doc.getId())
//                                .update(rated).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                            }
//                        }) ;
//                    }
//                }
//            }
//        }) ;



//                mStore.collection("RentTools").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (DocumentSnapshot doc : task.getResult()) {
//                        Tool tool = doc.toObject(Tool.class) ;
//                        mStore.collection("BackUp_Tools").document(doc.getId())
//                                .set(tool).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                Log.d("TAG", "added") ;
//                            }
//                        }) ;
//                    }
//                }
//            }
//        }) ;


//        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
//                .collection("Order").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for(DocumentSnapshot doc : task.getResult()) {
//                                orderId = doc.getId() ;
//                                SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
//                                Date docDate = doc.getDate("dateTime") ;
//                                String newDate = formatter.format(docDate).toString() ;
//
//                                Order order = doc.toObject(Order.class) ;
//                                date.setText(newDate);
//                                toolList = order.getToolList() ;
//                                cartAdapter.notifyDataSetChanged();
//                                mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
//                                        .collection("Order").document(orderId).collection("orderTools")
//                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                if (task.isSuccessful()) {
//                                                    if (task.getResult() != null) {
//                                                        for (DocumentSnapshot doc : task.getResult()) {
//                                                            Tool tool = doc.toObject(Tool.class) ;
//                                                            toolList.add(tool) ;
//                                                            cartAdapter.notifyDataSetChanged();
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }) ;
//                            }
//                        }
//                    }
//                }) ;


//        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
//                .collection("Order").document().collection("orderTools")
//                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            if (task.getResult() != null) {
//                                for(DocumentSnapshot doc : task.getResult().getDocuments()) {
//                                    Tool tool = doc.toObject(Tool.class) ;
//                                    tool.setToolId(doc.getId());
//                                    toolList.add(tool) ;
//                                    cartAdapter.notifyDataSetChanged();
//                                }
//
//                            }
//                            else {
//                                Toast.makeText(TestActivity.this, "Empty", Toast.LENGTH_SHORT).show();
//
//                            }
//                        }
//                        else {
//                            Toast.makeText(TestActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            Toast.makeText(TestActivity.this, "Stam", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }) ;








    }
}