package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentool.R;
import com.example.rentool.adapters.ExtendAdapter;
import com.example.rentool.domain.Tool;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main_Activity extends AppCompatActivity {

    // Initializing Variables for later use
    private FirebaseAuth mAuth ;
    private FirebaseUser currentUser ;
    private FirebaseFirestore mStore ;
    private ImageView menuBtn ;
    private TextView navUserName, navEmail ;
    private RoundedImageView navUserImage ;
    private View headerView ;
    private NavigationView navigationView ;

    private RecyclerView searchRecyclerView ;
//    private ToolsRecyclerAdapter toolsRecyclerAdapter ;
    private ExtendAdapter extendAdapter;
    private List<Tool> mToolList;
    private EditText searchText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_main);


        // FireBse Connection and references
        mAuth = FirebaseAuth.getInstance() ;
        mStore = FirebaseFirestore.getInstance() ;

        searchText = findViewById(R.id.searchText) ;
        mToolList = new ArrayList<>() ;
        searchRecyclerView = findViewById(R.id.search_recycler) ;
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        extendAdapter = new ExtendAdapter(Main_Activity.this, mToolList) ;
        searchRecyclerView.setAdapter(extendAdapter);


        mStore.collection("RentTools").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Date currentDate = new Date() ;
                    for (DocumentSnapshot doc : task.getResult()) {
                        Tool checkDateTool = doc.toObject(Tool.class) ;
                        checkDateTool.setToolId(doc.getId());
                        if (checkDateTool.getFromDate() != null && checkDateTool.getToDate() != null) {
                            Map<String, Object> availableMap = new HashMap<>() ;
                            if (currentDate.getTime() < checkDateTool.getToDate().getTime()) {
                                availableMap.put("available", true) ;
                            }
                            else {
                                availableMap.put("available", false) ;
                            }
                            mStore.collection("RentTools").document(checkDateTool.getToolId())
                                    .update(availableMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("TAG", "tool updated") ;
                                        }
                                    }) ;
                        }
                    }
                }
            }
        }) ;


        mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot userDoc : task.getResult()) {
                        if (!userDoc.getId().equals(mAuth.getCurrentUser().getUid())) {
                            mStore.collection("Users").document(userDoc.getId())
                                    .collection("Rented").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (!task.getResult().isEmpty()) {
                                                    Date currentDate = new Date();
                                                    for (DocumentSnapshot doc : task.getResult()) {
                                                        Tool rentedTool = doc.toObject(Tool.class);
                                                        rentedTool.setToolId(doc.getId());
                                                        if (rentedTool.getFromDate() != null) {
                                                            Map<String, Object> rentedToolMap = new HashMap<>();
                                                            if (currentDate.getTime() < rentedTool.getFromDate().getTime()) {
                                                                rentedToolMap.put("available", true);
                                                            }
                                                            else {
                                                                rentedToolMap.put("available", false);
                                                            }
                                                            mStore.collection("RentTools").document(rentedTool.getToolId())
                                                                    .update(rentedToolMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Log.d("TAG", "tool updated successfully") ;
                                                                        }
                                                                    }) ;
                                                            if (currentDate.getTime() > rentedTool.getToDate().getTime()) {
                                                                mStore.collection("Users").document(userDoc.getId())
                                                                        .collection("Rented").document(rentedTool.getToolId())
                                                                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    Log.d("TAG", "Tool deleted from rented") ;
                                                                                }
                                                                            }
                                                                        }) ;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }) ;
                        }
                    }
                }
            }
        }) ;


        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (editable.toString().isEmpty()) {
                        mToolList.clear();
                        extendAdapter.notifyDataSetChanged();
                    }
                    else {
                        mToolList.clear();
                        searchItem(editable.toString()) ;
                    }
                }
                catch (Exception e) {
                    Log.d("TAG", e + "") ;
                }

            }
        });


        /* Retrieving user data
         so we can use it in the Navigation header
         */
        mStore.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId().equals(mAuth.getCurrentUser().getUid())) {
                                    User user = document.toObject(User.class);
                                        navUserName.setText(user.getName());
                                        navEmail.setText(user.getEmail());
                                        Picasso.get().load(user.getImg_url()).into(navUserImage);
                                }
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });





        //Variables for Navigation Layout
        navigationView = findViewById(R.id.navigationView) ;
        headerView = navigationView.getHeaderView(0) ;
        navUserName = headerView.findViewById(R.id.userName) ;
        navUserImage = headerView.findViewById(R.id.profileImage) ;
        navEmail = headerView.findViewById(R.id.userEmail) ;
        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout) ;
        menuBtn = findViewById(R.id.menuBtn) ;
        NavigationView navigationView = findViewById(R.id.navigationView) ;
        NavController navController = Navigation.findNavController(Main_Activity.this, R.id.navHostFragment) ;
        /* Menu Button Listener
            This allows the use of the sidebar or the Navigation bar
        */
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        /* Navigation Controller so we can Use
         the Navigation buttons and redirect us to other Pages or use some functions
         */
        NavigationUI.setupWithNavController(navigationView, navController);

        final TextView textTitle = findViewById(R.id.textTitle) ;


        // Displays the name of Every Fragment we are in
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                textTitle.setText(navDestination.getLabel());
            }
        });


    }

    // Function that searches for the inputed tool
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


}