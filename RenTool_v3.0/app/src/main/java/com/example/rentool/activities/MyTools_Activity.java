package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.rentool.R;
import com.example.rentool.adapters.ExtendAdapter;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyTools_Activity extends AppCompatActivity {
    private RecyclerView myToolsRecycleView ;
    private FirebaseFirestore mStore ;
    private FirebaseAuth mAuth ;
    private ImageView backBtn ;
    private List<Tool> toolList ;
    private ExtendAdapter myToolsAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tools);

        mAuth = FirebaseAuth.getInstance() ;
        mStore = FirebaseFirestore.getInstance() ;

        backBtn = findViewById(R.id.backBtn) ;
        myToolsRecycleView = findViewById(R.id.myToolsRecycleView) ;

        toolList = new ArrayList<>() ;
        myToolsRecycleView.setLayoutManager(new LinearLayoutManager(MyTools_Activity.this));
        myToolsRecycleView.setHasFixedSize(true);
        myToolsAdapter = new ExtendAdapter(MyTools_Activity.this, toolList) ;
        myToolsRecycleView.setAdapter(myToolsAdapter);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
//                Intent intent = new Intent(MyTools_Activity.this, Buy_Main_Activity.class) ;
//                startActivity(intent);
//                finish();
            }
        });

        mStore.collection("RentTools").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Tool tool = doc.toObject(Tool.class) ;
                        tool.setToolId(doc.getId());
                        if (tool.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                            toolList.add(tool) ;
                            myToolsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }) ;


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyTools_Activity.this, Main_Activity.class) ;
        startActivity(intent);
        finish();
    }
}