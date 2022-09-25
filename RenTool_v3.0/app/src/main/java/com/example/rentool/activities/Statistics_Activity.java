package com.example.rentool.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Align;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.LegendLayout;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.rentool.R;
import com.example.rentool.domain.Category;
import com.example.rentool.domain.Tool;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics_Activity extends AppCompatActivity {
    private FirebaseAuth mAuth ;
    private FirebaseFirestore mStore ;
    private static int toolsSold = 0 , usersoldItemCount = 0 ;
    private TextView totalUsers, onlineUsers, contractorCount, userToolsCount, totalTools, toolsAvailable ;
    private AnyChartView categoryGraph, revenueCategoryPieGraph ;
    private static List<DataEntry> data ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mAuth = FirebaseAuth.getInstance() ;
        mStore = FirebaseFirestore.getInstance() ;

        totalUsers = findViewById(R.id.totalUsers) ;
        onlineUsers = findViewById(R.id.onlineUsers) ;
        contractorCount = findViewById(R.id.contractorCount) ;
        userToolsCount = findViewById(R.id.userToolsCount) ;
        totalTools = findViewById(R.id.totalTools) ;
        toolsAvailable = findViewById(R.id.toolsAvailable) ;


        categoryGraph = findViewById(R.id.categoryGraph) ;
        APIlib.getInstance().setActiveAnyChartView(categoryGraph);
        revenueCategoryPieGraph = findViewById(R.id.revenueCategoryPieGraph) ;
//        APIlib.getInstance().setActiveAnyChartView(revenueCategoryPieGraph);


        // Checking number of all users
        mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int toltalUserNum = 0 ;
                    for (DocumentSnapshot doc : task.getResult()) {
                        toltalUserNum ++ ;
                    }
                    totalUsers.setText(toltalUserNum + "");
                }
            }
        }) ;


        // Checking number of online users
        mStore.collection("OnlineUsers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int toltalOnlineUserNum = 0 ;
                    for (DocumentSnapshot doc : task.getResult()) {
                        toltalOnlineUserNum ++ ;
                    }
                    onlineUsers.setText(toltalOnlineUserNum + "");
                }
            }
        }) ;

        // Checking number of contractors
        mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int contractorsNum = 0 ;
                    for (DocumentSnapshot doc : task.getResult()) {
                        User user = doc.toObject(User.class) ;
                        if (user.getProfession() != null) {
                            contractorsNum ++ ;
                        }
                    }
                    contractorCount.setText(contractorsNum + "");
                }
            }
        }) ;


        // Users who added tools & number of tools available & total number of tools
        mStore.collection("RentTools").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int userToolsNum = 0 ;
                    int totalToolNum = 0 ;
                    int availableToolsNum = 0 ;

                    List<String> userId = new ArrayList<>() ;
                    for (DocumentSnapshot doc : task.getResult()) {
                        Tool tool = doc.toObject(Tool.class) ;
                        totalToolNum ++ ;
                        if (tool.isAvailable() == true) {
                            availableToolsNum ++ ;
                        }
                        if (!userId.isEmpty()) {
                            if (!userId.contains(tool.getUserId())) {
                                userId.add(tool.getUserId()) ;
                                userToolsNum ++ ;
                            }
                        }
                        else {
                            userId.add(tool.getUserId()) ;
                            userToolsNum ++ ;
                        }
                    }
                    userToolsCount.setText(userToolsNum + "");
                    totalTools.setText(totalToolNum + "");
                    toolsAvailable.setText(availableToolsNum + "");
                }
            }
        }) ;


        mStore.collection("Category").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot categoryDoc : task.getResult()) {
                        Category cat = categoryDoc.toObject(Category.class) ;
                        mStore.collection("RentTools").whereEqualTo("category", cat.getName())
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            int toolsSold = 0;
                                            for (DocumentSnapshot toolDoc : task.getResult()) {
                                                Tool tool = toolDoc.toObject(Tool.class) ;
                                                toolsSold += tool.getSold() ;
                                            }
                                            Map<String, Object> soldPerCategory = new HashMap<>() ;
                                            soldPerCategory.put("categoryName", cat.getName()) ;
                                            soldPerCategory.put("sold", toolsSold) ;
                                            mStore.collection("soldPerCategory").document().set(soldPerCategory)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Log.d("TAG", "Added") ;
                                                        }
                                                    }) ;
                                        }
                                    }
                                }) ;
                    }
                }
            }
        }) ;



        // Sold Tools per category
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Cartesian cartesian = AnyChart.column();
                data = new ArrayList<>();
                mStore.collection("soldPerCategory").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    String categoryName = doc.getString("categoryName") ;
                                    long categorySold = doc.getLong("sold").intValue() ;
                                    data.add(new ValueDataEntry(categoryName, categorySold));
                                }
                                Column column = cartesian.column(data);
                                column.tooltip()
                                        .titleFormat("{%X}")
                                        .position(Position.CENTER_BOTTOM)
                                        .anchor(Anchor.CENTER_BOTTOM)
                                        .offsetX(0d)
                                        .offsetY(5d)
                                        .format("{%Value}{groupsSeparator: }");
                            }
                        }
                    }
                }) ;

                cartesian.animation(true);
                cartesian.title("Tools sold per category");
                cartesian.yScale().minimum(0d);
                cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");
                cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
                cartesian.interactivity().hoverMode(HoverMode.BY_X);
                cartesian.xAxis(0).title("Category");
                cartesian.yAxis(0).title("Sold");
                categoryGraph.setChart(cartesian);

            }
        }, 1000);   //5 seconds


        mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot userDoc : task.getResult()) {
                        User user = userDoc.toObject(User.class);

                        if (!userDoc.getId().equals(mAuth.getCurrentUser().getUid())) {
                        mStore.collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    usersoldItemCount = 0 ;

                                    for (DocumentSnapshot orderDoc : task.getResult()) {
                                        String sellerId = orderDoc.getString("sellerId");

                                        if (sellerId.equals(userDoc.getId())) {
                                            usersoldItemCount++;
                                        }
                                    }
                                    Log.d("TAG", user.getName() + " == " + usersoldItemCount) ;
                                    Map<String, Object> sellerMap = new HashMap<>();
                                    sellerMap.put("userName", user.getName());
                                    sellerMap.put("sold", usersoldItemCount);
                                    mStore.collection("sellerSold").document().set(sellerMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d("TAG", "added");
                                                }
                                            });
                                }
                            }
                        });

                    }
                }
                }
            }
        }) ;


        // Tools revenue per category
        Handler pieHandler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                APIlib.getInstance().setActiveAnyChartView(revenueCategoryPieGraph);

                Pie pie = AnyChart.pie();

                List<DataEntry> data = new ArrayList<>();
                mStore.collection("sellerSold").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                String name = doc.getString("userName") ;
                                int sold = doc.getLong("sold").intValue() ;
                                data.add(new ValueDataEntry(name, sold));
                            }
                            pie.data(data);

                            pie.title("Top selling users");

                            pie.labels().position("outside");

                            pie.legend().title().enabled(true);
                            pie.legend().title()
                                    .text("Users")
                                    .padding(0d, 0d, 10d, 0d);

                            pie.legend()
                                    .position("center-bottom")
                                    .itemsLayout(LegendLayout.HORIZONTAL)
                                    .align(Align.CENTER);

                            revenueCategoryPieGraph.setChart(pie);
                        }
                    }
                }) ;
            }
        }, 2000);   //2 seconds


    }

    @Override
    public void onBackPressed() {
        mStore.collection("soldPerCategory").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        mStore.collection("soldPerCategory").document(doc.getId())
                                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d("TAG", "Deleted form soldPerCategory") ;
                                    }
                                }) ;
                    }
                }
            }
        }) ;

        mStore.collection("sellerSold").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        mStore.collection("sellerSold").document(doc.getId())
                                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("TAG", "sellerSold deleted") ;
                                        }
                                    }
                                }) ;
                    }
                }
            }
        }) ;

        Intent intent = new Intent(Statistics_Activity.this, Admin_Home_Activity.class) ;
        startActivity(intent);
        finish();
    }
}