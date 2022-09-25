package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rentool.R;
import com.example.rentool.adapters.ExtendAdapter;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Extend_Time_Activity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private ImageView backBtn ;
    private List<Tool> toolList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_cart);

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        backBtn = findViewById(R.id.backBtn) ;

        toolList = new ArrayList<>();
        cartRecyclerView = findViewById(R.id.cart_item_container);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(Extend_Time_Activity.this));
        cartRecyclerView.setHasFixedSize(true);
        ExtendAdapter extendAdapter = new ExtendAdapter(toolList, Extend_Time_Activity.this) ;
        cartRecyclerView.setAdapter(extendAdapter);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Extend_Time_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });


        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Rented").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for(DocumentChange doc : task.getResult().getDocumentChanges()) {
                                    String documentId = doc.getDocument().getId();
                                    Tool tool = doc.getDocument().toObject(Tool.class) ;
                                    tool.setToolId(documentId);
                                    toolList.add(tool) ;
                                }
                                extendAdapter.notifyDataSetChanged();
                            }
                        }
                        else {
                            Toast.makeText(Extend_Time_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }) ;

    }


}