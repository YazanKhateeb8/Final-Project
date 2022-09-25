package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rentool.R;
import com.example.rentool.adapters.AddressAdapter;
import com.example.rentool.domain.AddressClass;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Address_Activity extends AppCompatActivity implements AddressAdapter.SelectedAddress {
    private RecyclerView mAddressRecyclerView;
    private AddressAdapter mAddressAdapter;
    private Button paymentBtn, mAddAddress;
    private List<AddressClass> mAddressClassList;
    private Tool tool ;
    private String toolId ;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private ImageView backBtn, homeBtn ;
    String address="";
    int flag = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_address);

        backBtn = findViewById(R.id.backBtn) ;
        homeBtn = findViewById(R.id.homeBtn) ;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Address_Activity.this, Tool_Details_Activity.class) ;
                intent.putExtra("toolDetails", tool) ;
                intent.putExtra("toolId", toolId) ;
                startActivity(intent);
                finish();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Address_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });

        /* Getting the Tools from Cart activity
            as a form of Array List
         */
        final Object obj = getIntent().getSerializableExtra("toolDetails");
        toolId = getIntent().getStringExtra("toolId") ;
        if (obj instanceof Tool && obj != null) {
            tool = (Tool) obj ;
        }


        // Assigning Variables from Layout and Firebase Connections
        mAddressRecyclerView=findViewById(R.id.address_recycler);
        paymentBtn=findViewById(R.id.payment_btn);
        mAddAddress=findViewById(R.id.add_address_btn);
        mAuth=FirebaseAuth.getInstance();
        mStore=FirebaseFirestore.getInstance();
        mAddressClassList =new ArrayList<>();
        mAddressAdapter=new AddressAdapter(getApplicationContext(), mAddressClassList,this);
        mAddressRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(mAddressRecyclerView);
        mAddressRecyclerView.setAdapter(mAddressAdapter);

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Address").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if (!task.getResult().isEmpty()) {
                                for(DocumentSnapshot doc:task.getResult().getDocuments()) {
                                    AddressClass addressClass = doc.toObject(AddressClass.class);
                                    addressClass.setDocId(doc.getId());
                                    mAddressClassList.add(addressClass);
                                    mAddressAdapter.notifyDataSetChanged();
                                }
                            }
                            else {
                                flag = 1 ;
                            }
                        }
                    }
                });

        mAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Address_Activity.this, Add_Address_Activity.class);
                intent.putExtra("toolDetails", tool) ;
                intent.putExtra("toolId", toolId) ;
                startActivity(intent);
                finish();
            }
        });


        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 1) {
                    Toast.makeText(Address_Activity.this, "You have to add an address", Toast.LENGTH_LONG).show();
                }
                else {
                        Intent intent = new Intent(Address_Activity.this, Add_Payment_Activity.class);
                        intent.putExtra("toolDetails", tool) ;
                        intent.putExtra("toolId", toolId) ;
                        startActivity(intent);
                        finish();
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Address_Activity.this, Tool_Details_Activity.class);
        intent.putExtra("toolDetails", tool) ;
        intent.putExtra("toolId", toolId) ;
        startActivity(intent);
        finish();
    }

    @Override
    public void setAddress(String s) {
        address = s;
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition() ;
            AddressClass addressClass = mAddressClassList.get(position) ;

            mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                            .collection("Address").document(addressClass.getDocId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mAddressClassList.remove(viewHolder.getAdapterPosition()) ;
                                mAddressAdapter.notifyDataSetChanged();
                            }
                        }
                    }) ;

        }
    } ;

}