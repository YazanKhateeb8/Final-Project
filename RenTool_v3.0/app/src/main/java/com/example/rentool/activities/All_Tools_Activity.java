package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.rentool.R;
import com.example.rentool.adapters.ExtendAdapter;
import com.example.rentool.adapters.ToolsRecyclerAdapter;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class All_Tools_Activity extends AppCompatActivity {
    private FirebaseFirestore mStore ;
    private FirebaseAuth mAuth ;
    private List<Tool> mToolList ;
    private RecyclerView toolRecycleView ;
    private ToolsRecyclerAdapter toolsRecyclerAdapter ;
    private RecyclerView searchRecyclerView ;
    private ExtendAdapter extendAdapter;
    private List<Tool> searchToolList;
    private EditText searchText ;
    private String toolId, Category ;
    private ImageView back ;
    private androidx.appcompat.widget.Toolbar toolBar ;
    String sortBy = "" ;
    String ascDesc = "" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_all_tools);

        mStore = FirebaseFirestore.getInstance() ;
        mAuth = FirebaseAuth.getInstance() ;

        toolBar = findViewById(R.id.layoutToolBar) ;
        setSupportActionBar(toolBar);

        searchText = findViewById(R.id.searchText) ;
        searchToolList = new ArrayList<>() ;
        searchRecyclerView = findViewById(R.id.search_recycler) ;
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        extendAdapter = new ExtendAdapter(All_Tools_Activity.this, searchToolList) ;
        searchRecyclerView.setAdapter(extendAdapter);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    searchToolList.clear();
                    extendAdapter.notifyDataSetChanged();
                }
                else {
                    searchToolList.clear();
                    searchItem(editable.toString()) ;
                }
            }
        });

        back = findViewById(R.id.back) ;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
//                Intent intent = new Intent(All_Tools_Activity.this, Main_Activity.class) ;
//                startActivity(intent);
//                finish();
            }
        });

        /* Recycle View to display Tools in Tools Page
            with the usage of adapters
         */

        mToolList = new ArrayList<>() ;
        toolRecycleView = findViewById(R.id.item_recycler) ;
        toolRecycleView.setLayoutManager(new GridLayoutManager(this, 2));
        toolsRecyclerAdapter = new ToolsRecyclerAdapter(this, mToolList) ;
        toolRecycleView.setAdapter(toolsRecyclerAdapter);


        Category = getIntent().getStringExtra("Category") ;

        if (Category == null || Category.isEmpty()) {
            toolRecycleView.getRecycledViewPool().clear();
            toolsRecyclerAdapter.notifyDataSetChanged();
                mStore.collection("RentTools").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                toolId = doc.getId() ;
                                Tool tool = doc.toObject(Tool.class) ;
                                tool.setToolId(toolId);
                                mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                        .collection("Rented").document(toolId)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (!task.getResult().exists()) {
                                                        if (!tool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                                && tool.isAvailable() != false) {
                                                            mToolList.add(tool) ;
                                                            toolsRecyclerAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            }
                                        }) ;
                            }
                        }
                    }
                }) ;
        }



        if (Category != null) {
            toolRecycleView.getRecycledViewPool().clear();
            toolsRecyclerAdapter.notifyDataSetChanged();
            mStore.collection("RentTools").whereEqualTo("category", Category).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            toolId = doc.getId() ;
                            Tool tool = doc.toObject(Tool.class) ;
                            tool.setToolId(toolId);
                            mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                    .collection("Rented").document(toolId)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (!task.getResult().exists()) {
                                                    if (!tool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                            && tool.isAvailable() != false) {
                                                        mToolList.add(tool);
                                                        toolsRecyclerAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        }
                                    }) ;
                        }
                    }
                }
            }) ;
        }


    }

    private void searchItem(String txt) {
        if (!txt.isEmpty()) {
            try {
                mStore.collection("RentTools").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && task != null) {
                                    for(DocumentSnapshot doc : task.getResult().getDocuments()) {
                                        Tool tool = doc.toObject(Tool.class) ;
                                        tool.setToolId(doc.getId());
                                        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                                .collection("Rented").document(tool.getToolId())
                                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            if (!task.getResult().exists()) {
                                                                if (!tool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                                        && tool.isAvailable() != false) {

                                                                    if (tool.getName().toLowerCase().contains(txt.toLowerCase())) {
                                                                        mToolList.add(tool);
                                                                        extendAdapter.notifyDataSetChanged();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }) ;
                                    }
                                }
                            }
                        }) ;
            }
            catch (Exception e) {
                Log.d("TAG", e + "") ;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.sort_menu, menu);
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        //DESCENDING
        //ASCENDING
        if (id == R.id.sort_nameAsc) {
            sortBy = "name" ;
            ascDesc = "ASCENDING" ;
            mToolList.clear();
            getSortedTools(sortBy, ascDesc) ;
        }
        if (id == R.id.sort_nameDes) {
            sortBy = "name" ;
            ascDesc = "DESCENDING" ;
            mToolList.clear();
            getSortedTools(sortBy, ascDesc) ;
        }
        if (id == R.id.sort_priceAsc) {
            sortBy = "price" ;
            ascDesc = "ASCENDING" ;
            mToolList.clear();
            getSortedTools(sortBy, ascDesc) ;
        }
        if (id == R.id.sort_priceDes) {
            sortBy = "price" ;
            ascDesc = "DESCENDING" ;
            mToolList.clear();
            getSortedTools(sortBy, ascDesc) ;
        }
        if (id == R.id.sort_ratingAsc) {
            sortBy = "rating" ;
            ascDesc = "ASCENDING" ;
            mToolList.clear();
            getSortedTools(sortBy, ascDesc) ;
        }
        if (id == R.id.sort_ratingDes) {
            sortBy = "rating" ;
            ascDesc = "DESCENDING" ;
            mToolList.clear();
            getSortedTools(sortBy, ascDesc) ;
        }

        return true ;
    }

    public void getSortedTools(String sortBy, String ascDesc) {
        if (Category == null || Category.isEmpty()) {
            mStore.collection("RentTools").orderBy(sortBy, Query.Direction.valueOf(ascDesc)).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    toolId = doc.getId() ;
                                    Tool tool = doc.toObject(Tool.class) ;
                                    tool.setToolId(toolId);
                                    mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                            .collection("Rented").document(toolId)
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (!task.getResult().exists()) {
                                                            if (!tool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                                    && tool.isAvailable() != false) {
                                                                mToolList.add(tool);
                                                                toolsRecyclerAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    }
                                                }
                                            }) ;
                                }
                            }
                        }
                    }) ;
        }
        else {
            mStore.collection("RentTools").orderBy(sortBy, Query.Direction.valueOf(ascDesc)).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    toolId = doc.getId() ;
                                    Tool tool = doc.toObject(Tool.class) ;
                                    tool.setToolId(toolId);
                                    mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                            .collection("Rented").document(toolId)
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (!task.getResult().exists()) {
                                                            if (tool.getCategory().equals(Category)) {
                                                                if (!tool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                                        && tool.isAvailable() != false) {
                                                                    mToolList.add(tool);
                                                                    toolsRecyclerAdapter.notifyDataSetChanged();
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }) ;
                                }
                            }
                        }
                    }) ;
        }

    }


}