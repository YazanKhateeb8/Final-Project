package com.example.rentool.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentool.R;
import com.example.rentool.adapters.CategoryAdapter;
import com.example.rentool.adapters.ContractorAdapter;
import com.example.rentool.adapters.HomePageAdapter;
import com.example.rentool.domain.Category;
import com.example.rentool.domain.Tool;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BuyHomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

    // Category Variables
    private List<Category> mCategoryList;
    private CategoryAdapter mCategoryAdapter;
    private RecyclerView mCatRecyclerView;

    // bestSell Variables
    private List<Tool> bestSellList;
    private HomePageAdapter bestSellAdapter;
    private RecyclerView bestSellRecycleView;

    // Recently added Tools
    private List<Tool> recentList;
    private HomePageAdapter recentToolsAdapter;
    private RecyclerView newToolsRecycleView;

    private List<Tool> recommendedTools;
    private HomePageAdapter recommendedAdapter;
    private RecyclerView recommendedRecycleView;
    private LinearLayout recommendedLayout;
    private RelativeLayout recoRel ;
    private TextView recoTxt ;

    private List<User> contractorList;
    private ContractorAdapter contractorAdapter;
    private RecyclerView contractorRecycleView;


    private List<String> toolNamesList;

    public BuyHomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy_home, container, false);

        // Starting the Firebase database so we can Get the data
        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        /* Variables for Feature Recycle View and Adapters
            so we can display the fetched data on a recycle view
         */
        recommendedLayout = view.findViewById(R.id.recommendedLayout);
        recommendedRecycleView = view.findViewById(R.id.recommendedRecycleView);
        recommendedTools = new ArrayList<>();
        recommendedAdapter = new HomePageAdapter(getContext(), recommendedTools);
        recommendedRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
//        recommendedRecycleView.setHasFixedSize(true);
        recommendedRecycleView.setAdapter(recommendedAdapter);
        recoRel = view.findViewById(R.id.recoRel) ;
        recoTxt = view.findViewById(R.id.recoTxt) ;

        recommendedLayout.setVisibility(View.GONE);
        recoRel.setVisibility(View.GONE);
        recoTxt.setVisibility(View.GONE);

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User currentUser = task.getResult().toObject(User.class) ;

                            mStore.collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot orderDoc : task.getResult()) {

                                            String targetUserId = orderDoc.getString("userId") ;
                                            String targetToolId = orderDoc.getString("toolId") ;

                                            mStore.collection("Users").document(targetUserId)
                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                User targetUser = task.getResult().toObject(User.class) ;
                                                                if (targetUser.getGender().equals(currentUser.getGender())
                                                                && targetUser.getYearOfBirth() == currentUser.getYearOfBirth()) {

                                                                    mStore.collection("RentTools").document(targetToolId)
                                                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Tool tool = task.getResult().toObject(Tool.class);
                                                                                        tool.setToolId(task.getResult().getId());

                                                                                        recommendedLayout.setVisibility(View.VISIBLE);
                                                                                        recoRel.setVisibility(View.VISIBLE);
                                                                                        recoTxt.setVisibility(View.VISIBLE);

                                                                                        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                                                                                .collection("Rented").document(tool.getToolId())
                                                                                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            if (!task.getResult().exists()) {
                                                                                                                if (!tool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                                                                                        && tool.isAvailable() != false) {
                                                                                                                    recommendedTools.add(tool);
                                                                                                                    recommendedAdapter.notifyDataSetChanged();
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }) ;
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
                            }) ;
                        }
                    }
                }) ;



        /* Variables for Category Recycle View and Adapters
            so we can display the fetched data on a recycle view
         */
        mCatRecyclerView = view.findViewById(R.id.category_recycler);
        mCategoryList = new ArrayList<>();
        mCategoryAdapter = new CategoryAdapter(getContext(), mCategoryList);
        mCatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        mCatRecyclerView.setAdapter(mCategoryAdapter);


        // Fetching the Category Data from FirebaseFireStore
        mStore.collection("Category")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Category category = document.toObject(Category.class);
                                mCategoryList.add(category);
                                mCategoryAdapter.notifyDataSetChanged();
                            }
                        }
                        else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        /* Variables for Feature Recycle View and Adapters
            so we can display the fetched data on a recycle view
         */
        bestSellRecycleView = view.findViewById(R.id.bestSellRecycleView);
        bestSellList = new ArrayList<>();
        bestSellAdapter = new HomePageAdapter(getContext(), bestSellList);
        bestSellRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        bestSellRecycleView.setHasFixedSize(true);
        bestSellRecycleView.setAdapter(bestSellAdapter);

        toolNamesList = new ArrayList<>();


        mStore.collection("Sold").orderBy("sold", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String toolName = document.getString("toolName").toString();
                                toolNamesList.add(toolName);
                            }
                            mStore.collection("RentTools").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (int i = 0; i < toolNamesList.size(); i++) {
                                            for (DocumentSnapshot doc : task.getResult()) {
                                                Tool tool = doc.toObject(Tool.class);
                                                tool.setToolId(doc.getId());
                                                int finalI = i;
                                                if (!tool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                        && tool.isAvailable() != false) {
                                                    if (tool.getName().equals(toolNamesList.get(finalI))) {
                                                        if (bestSellList.size() < 8) {
                                                            bestSellList.add(tool);
                                                            bestSellAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });


        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        newToolsRecycleView = view.findViewById(R.id.newToolsRecycleView);
        recentList = new ArrayList<>();
        recentToolsAdapter = new HomePageAdapter(getContext(), recentList);
        newToolsRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        newToolsRecycleView.setHasFixedSize(true);
        newToolsRecycleView.setAdapter(recentToolsAdapter);

        mStore.collection("RentTools").orderBy("uploadDate", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot newDoc : task.getResult()) {
                                Tool newTool = newDoc.toObject(Tool.class);
                                newTool.setToolId(newDoc.getId());
                                mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                        .collection("Rented").document(newTool.getToolId())
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (!task.getResult().exists()) {
                                                        if (!newTool.getUserId().equals(mAuth.getCurrentUser().getUid())
                                                                && newTool.isAvailable() != false) {
                                                            if (recentList.size() < 8) {
                                                                newTool.setToolId(newDoc.getId());
                                                                recentList.add(newTool);
                                                                recentToolsAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }) ;
                            }
                        }
                        else {
                            Log.d("Tag", "Error fetching data ordered by timeStamp");
                        }
                    }
                });


        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        contractorRecycleView = view.findViewById(R.id.contractorRecycleView);
        contractorList = new ArrayList<>();
        contractorAdapter = new ContractorAdapter(getContext(), contractorList);
        contractorRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        contractorRecycleView.setHasFixedSize(true);
        contractorRecycleView.setAdapter(contractorAdapter);

        mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if (!doc.getId().equals(mAuth.getCurrentUser().getUid())) {
                            User contractor = doc.toObject(User.class);
                            if (contractor.getProfession() != null) {
                                contractorList.add(contractor);
                                contractorAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });


        return view;
    }

}