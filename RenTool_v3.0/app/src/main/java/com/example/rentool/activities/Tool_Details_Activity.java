package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Tool_Details_Activity extends AppCompatActivity {

    private ImageView toolImage;
    private TextView toolName, toolPrice, toolDescription;
    private Button toolRating, toolCondition, contactSeller, rentTool;
    private String toolId;
    private static Tool tool = null;
    private ImageView backBtn, homeBtn;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private RatingBar ratingBar;
    private float rating;
    private Dialog setDateDialog;
    private DatePickerDialog.OnDateSetListener fromDateListener, toDateListener;
    private static Date toolFromDate, toolToDate;

    private TextInputLayout fromDate, toDate;
    private TextInputEditText fromDateEdit, toDateEdit;
    private Button setDate;
    private static int fromFlag = 0, toFlag = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_tool_details);

        /* Assigning View Variables from Layout so we can
            change their values to the chosen tool
            and initializing firebase variables and instances
         */
        toolImage = findViewById(R.id.toolImage);
        toolName = findViewById(R.id.toolName);
        toolPrice = findViewById(R.id.toolPrice);
        toolDescription = findViewById(R.id.toolDescription);
        toolRating = findViewById(R.id.toolRating);
        contactSeller = findViewById(R.id.contactSeller);
        toolCondition = findViewById(R.id.toolCondition);
        rentTool = findViewById(R.id.rentTool);
        ratingBar = findViewById(R.id.ratingBar);
        backBtn = findViewById(R.id.backBtn);
        homeBtn = findViewById(R.id.homeBtn);
        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Tool_Details_Activity.this, Main_Activity.class);
                startActivity(intent);
                finish();
            }
        });


        Object userToolObject = getIntent().getSerializableExtra("toolDetails");
        String userToolId = getIntent().getStringExtra("toolId");
        if (userToolObject instanceof Tool && userToolObject != null) {
            tool = (Tool) userToolObject;
            toolId = userToolId;
            if (tool.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                ratingBar.setIsIndicator(true);
                contactSeller.setEnabled(false);
                contactSeller.setVisibility(View.INVISIBLE);
                rentTool.setEnabled(false);
                rentTool.setVisibility(View.INVISIBLE);
            }
        }

        final Object obj = getIntent().getSerializableExtra("detail");

        if (obj instanceof Tool) {
            tool = (Tool) obj;
            if (tool.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                ratingBar.setIsIndicator(true);
                contactSeller.setEnabled(false);
                contactSeller.setVisibility(View.INVISIBLE);
                rentTool.setEnabled(false);
                rentTool.setVisibility(View.INVISIBLE);
                if (tool.getRated() == 0 || tool.getRating() == 0) {
                    toolRating.setText("not rated");
                } else {
                    Double newRating = roundNum();
                    float ratingBarValue = newRating.floatValue();
                    ratingBar.setRating(ratingBarValue);
                }
            }
        }


        Glide.with(getApplicationContext()).load(tool.getImg_url()).into(toolImage);
        toolName.setText(tool.getName());
        String price = String.valueOf(tool.getPrice());
        toolPrice.setText(price + "₪" + " / day");
        toolDescription.setText(tool.getDescription());
        String rating = String.valueOf(tool.getRating());
//        toolRating.setText("★" + rating);
        toolCondition.setText(tool.getCondition());

        if (tool.getRated() == 0 || tool.getRating() == 0) {
            toolRating.setText("not rated");
        } else {
            Double newRating = roundNum();
            toolRating.setText("★" + newRating);
        }


        mStore.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Rating")
                .document(tool.getToolId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                DocumentSnapshot doc = task.getResult();
                                Double userRating = doc.getDouble("rating");
                                float userRatingFloat = userRating.floatValue();
                                ratingBar.setRating(userRatingFloat);
                                ratingBar.setIsIndicator(true);
                            } else {
                                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                    @Override
                                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                                        Float rating = ratingBar.getRating();
                                        Double ratingDouble = rating.doubleValue();
                                        ratingBar.setIsIndicator(true);
                                        Map<String, Double> ratingMap = new HashMap<>();
                                        ratingMap.put("rating", ratingDouble);

                                        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                                .collection("Rating").document(tool.getToolId()).set(ratingMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Log.d("TAG", "Rating saved");
                                                    }
                                                });
                                        Map<String, Object> updateSoldRating = new HashMap<>();
                                        updateSoldRating.put("rating", tool.getRating() + ratingDouble);
                                        updateSoldRating.put("rated", tool.getRated() + 1);
                                        mStore.collection("RentTools").document(tool.getToolId())
                                                .update(updateSoldRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Log.d("Tag", "Document updated successfully");
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    }
                });

        setDateDialog = new Dialog(Tool_Details_Activity.this);
        setDateDialog.setContentView(R.layout.set_tool_date);
        setDateDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        setDateDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setDateDialog.setCancelable(true);
        setDateDialog.getWindow().getAttributes().windowAnimations = R.style.animation;


        fromDate = setDateDialog.findViewById(R.id.fromDate);
        toDate = setDateDialog.findViewById(R.id.toDate);
        fromDateEdit = setDateDialog.findViewById(R.id.fromDateEdit);
        toDateEdit = setDateDialog.findViewById(R.id.toDateEdit);
        setDate = setDateDialog.findViewById(R.id.setDate);

        fromDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dateDialog = new DatePickerDialog(
                        Tool_Details_Activity.this, R.style.datePickerStyle, fromDateListener,
                        year, month, day);

                Date currentDate = new Date();
                mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot userDoc : task.getResult()) {
                                if (!userDoc.getId().equals(mAuth.getCurrentUser().getUid())) {
                                    mStore.collection("Users").document(userDoc.getId())
                                            .collection("Rented").document(tool.getToolId())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult().exists()) {
                                                            fromFlag = 1 ;
                                                            Tool rentedTool = task.getResult().toObject(Tool.class);
                                                            long newMax = ((rentedTool.getFromDate().getTime() / 1000) - 86400) * 1000 ;
                                                            dateDialog.getDatePicker().setMaxDate(newMax);
                                                            if (currentDate.getTime() < rentedTool.getFromDate().getTime()) {
                                                                if (currentDate.getTime() > tool.getFromDate().getTime()) {
                                                                    dateDialog.getDatePicker().setMinDate(currentDate.getTime());
                                                                }
                                                                else {
                                                                    dateDialog.getDatePicker().setMinDate(tool.getFromDate().getTime());
                                                                }
                                                            }
                                                            dateDialog.getWindow();
                                                            dateDialog.show();
                                                        }
                                                        else {
                                                            if (fromFlag == 0) {
                                                                dateDialog.getDatePicker().setMaxDate(tool.getToDate().getTime());
                                                                if (currentDate.getTime() < tool.getFromDate().getTime()) {
                                                                    dateDialog.getDatePicker().setMinDate(tool.getFromDate().getTime());
                                                                }
                                                                else {
                                                                    if (currentDate.getTime() < tool.getToDate().getTime())
                                                                        dateDialog.getDatePicker().setMinDate(currentDate.getTime());
                                                                }
                                                                dateDialog.getWindow();
                                                                dateDialog.show();
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
            }
        });

        toDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dateDialog = new DatePickerDialog(
                        Tool_Details_Activity.this, R.style.datePickerStyle, toDateListener,
                        year, month, day);

                Date currentDate = new Date();
                mStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot userDoc : task.getResult()) {
                                if (!userDoc.getId().equals(mAuth.getCurrentUser().getUid())) {
                                    mStore.collection("Users").document(userDoc.getId())
                                            .collection("Rented").document(tool.getToolId())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult().exists()) {
                                                            toFlag = 1 ;
                                                            Tool rentedTool = task.getResult().toObject(Tool.class);
                                                            long newMax = ((rentedTool.getFromDate().getTime() / 1000) - 86400) * 1000 ;
                                                            dateDialog.getDatePicker().setMaxDate(newMax);
                                                            if (currentDate.getTime() < rentedTool.getFromDate().getTime()) {
                                                                if (currentDate.getTime() > tool.getFromDate().getTime()) {
                                                                    dateDialog.getDatePicker().setMinDate(currentDate.getTime());
                                                                }
                                                                else {
                                                                    dateDialog.getDatePicker().setMinDate(tool.getFromDate().getTime());
                                                                }
                                                            }
                                                            dateDialog.getWindow();
                                                            dateDialog.show();
                                                        }
                                                        else {
                                                            if (toFlag == 0) {
                                                                dateDialog.getDatePicker().setMaxDate(tool.getToDate().getTime());
                                                                if (currentDate.getTime() < tool.getFromDate().getTime()) {
                                                                    dateDialog.getDatePicker().setMinDate(tool.getFromDate().getTime());
                                                                }
                                                                else {
                                                                    if (currentDate.getTime() < tool.getToDate().getTime())
                                                                        dateDialog.getDatePicker().setMinDate(currentDate.getTime());
                                                                }
                                                                dateDialog.getWindow();
                                                                dateDialog.show();
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
            }
        });

        fromDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String fromDateTxt = day + "/" + (month + 1) + "/" + year;
                fromDate.getEditText().setText(fromDateTxt);
                Calendar fromCal = Calendar.getInstance();
                fromCal.set(year, month, day);
                toolFromDate = fromCal.getTime();
                tool.setFromDate(toolFromDate);
            }
        };

        toDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String toDateTxt = day + "/" + (month + 1) + "/" + year;
                toDate.getEditText().setText(toDateTxt);
                Calendar toCal = Calendar.getInstance();
                toCal.set(year, month, day);
                toolToDate = toCal.getTime();
                tool.setToDate(toolToDate);
            }
        };

        setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateDates()) {
                    return;
                } else {
                    setDateDialog.dismiss();
                    Intent intent = new Intent(Tool_Details_Activity.this, Address_Activity.class);
                    intent.putExtra("toolDetails", tool);
                    intent.putExtra("toolId", tool.getToolId());
                    intent.putExtra("pageSource", "toolDetailPage");
                    startActivity(intent);
                    finish();
                }
            }
        });


        rentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateDialog.show();
            }
        });


        contactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Tool_Details_Activity.this, User_Detail_Activity.class);
                intent.putExtra("toolDetails", tool);
                intent.putExtra("toolId", tool.getToolId());
                startActivity(intent);
                finish();
            }
        });
    }


    private double roundNum() {
        double newRating = tool.getRating() / tool.getRated();
        DecimalFormat formatRating = new DecimalFormat("0.0");
        String finalRating = formatRating.format(newRating);
        double toBeFormated = Double.valueOf(finalRating);
        return toBeFormated;
    }

    private boolean validateDates() {
        String fromDateVal = fromDate.getEditText().getText().toString();
        String toDateVal = toDate.getEditText().getText().toString();

        if (fromDateVal.isEmpty() || toDateVal.isEmpty()) {
            Toast.makeText(this, "Must fill dates", Toast.LENGTH_SHORT).show();
            return false;
        } else if (toolToDate.getTime() < toolFromDate.getTime()) {
            Toast.makeText(this, "Dates are not valid", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}