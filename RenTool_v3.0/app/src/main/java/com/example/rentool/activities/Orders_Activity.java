package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.rentool.R;
import com.example.rentool.adapters.OrdersAdapter;
import com.example.rentool.adapters.RecommendAdapter;
import com.example.rentool.domain.Order;
import com.example.rentool.domain.Tool;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Orders_Activity extends AppCompatActivity {
    private List<Order> orderList ;
    private RecyclerView order_recyclerView ;
    private FirebaseAuth mAuth ;
    private FirebaseFirestore mStore ;
    private ImageView backBtn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_orders);





        backBtn = findViewById(R.id.backBtn) ;
        order_recyclerView = findViewById(R.id.order_recyclerView) ;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Orders_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });


        mStore = FirebaseFirestore.getInstance() ;
        mAuth = FirebaseAuth.getInstance() ;


        orderList = new ArrayList<>() ;
        order_recyclerView.setLayoutManager(new LinearLayoutManager(Orders_Activity.this));
        order_recyclerView.setHasFixedSize(true);
        OrdersAdapter ordersAdapter = new OrdersAdapter(orderList, Orders_Activity.this) ;
        order_recyclerView.setAdapter(ordersAdapter);

        mStore.collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Order order = doc.toObject(Order.class) ;

                        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
                        Date docDate = doc.getDate("dateTime") ;
                        String newDate = formatter.format(docDate).toString() ;
                        order.setDate(newDate);

                        if (order.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                            orderList.add(order) ;
                            ordersAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }) ;




    }
}