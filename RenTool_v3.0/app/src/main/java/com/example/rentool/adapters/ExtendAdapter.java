package com.example.rentool.adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.activities.Extend_Time_Activity;
import com.example.rentool.activities.Tool_Details_Activity;
import com.example.rentool.activities.MyTools_Activity;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendAdapter extends RecyclerView.Adapter<ExtendAdapter.ViewHolder>{
    List<Tool> toolList;
    FirebaseFirestore mStore;
    FirebaseAuth mAuth;
    Context context ;
    DatePickerDialog.OnDateSetListener mDateSetListener ;
    Dialog setDateDialog ;
    private static Date toolSetDateTime ;

    public ExtendAdapter(List<Tool> toolList, Context context){
        this.toolList = toolList;
        this.context = context ;
        mStore = FirebaseFirestore.getInstance() ;
        mAuth = FirebaseAuth.getInstance() ;
    }

    public ExtendAdapter(Context context, List<Tool> toolList) {
        this.toolList = toolList;
        this.context = context ;
        mStore = FirebaseFirestore.getInstance() ;
        mAuth = FirebaseAuth.getInstance() ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_cart_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.cartName.setText(toolList.get(position).getName());
        holder.cartPrice.setText(toolList.get(position).getPrice() + "₪");
        Glide.with(holder.itemView.getContext()).load(toolList.get(position).getImg_url()).into(holder.cartImage);


        if (context instanceof Extend_Time_Activity) {
            holder.availableSwitch.setVisibility(View.GONE);
            // Here
            holder.extendTool.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, Object> availableMap = new HashMap<>() ;

                        Calendar cal = Calendar.getInstance() ;
                        int year = cal.get(Calendar.YEAR) ;
                        int month = cal.get(Calendar.MONTH) ;
                        int day = cal.get(Calendar.DAY_OF_MONTH) ;

                        DatePickerDialog dateDialog = new DatePickerDialog(
                                context, R.style.datePickerStyle, mDateSetListener,
                                year, month, day) ;

                        Calendar max = Calendar.getInstance() ;
                        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH) + 1, max.get(Calendar.DAY_OF_MONTH));


                        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                        .collection("Rented").document(toolList.get(position).getToolId())
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    }
                                }) ;

                        dateDialog.getDatePicker().setMaxDate(max.getTimeInMillis());
                        dateDialog.getDatePicker().setMinDate(new Date().getTime());
                        dateDialog.getWindow();
                        dateDialog.show();


                        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                Calendar toDate = Calendar.getInstance() ;
                                toDate.set(year, month, day);
                                toolSetDateTime = toDate.getTime() ;
                                Log.d("TAG", toolSetDateTime + "") ;
                                Date uploadDate = new Date() ;
                                availableMap.put("fromDate", uploadDate) ;
                                availableMap.put("toDate", toolSetDateTime) ;
                                availableMap.put("available", true) ;


                                mStore.collection("RentTools").document(toolList.get(position).getToolId())
                                        .update(availableMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("TAG", "Tool updated") ;
                                                }
                                            }
                                        }) ;
                            }
                        } ;
                }
            });
        }
        else if (context instanceof MyTools_Activity) {
            holder.extendTool.setVisibility(View.GONE);
            holder.cartPrice.setVisibility(View.GONE);
            if (toolList.get(position).isAvailable() == true) {
                holder.availableSwitch.setChecked(true);
            }
            else {
                holder.availableSwitch.setChecked(false);
            }
            holder.availableSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, Object> availableMap = new HashMap<>() ;
                    if (holder.availableSwitch.isChecked()) {

                        Calendar cal = Calendar.getInstance() ;
                        int year = cal.get(Calendar.YEAR) ;
                        int month = cal.get(Calendar.MONTH) ;
                        int day = cal.get(Calendar.DAY_OF_MONTH) ;

                        DatePickerDialog dateDialog = new DatePickerDialog(
                                context, R.style.datePickerStyle, mDateSetListener,
                                year, month, day) ;
                        Calendar max = Calendar.getInstance() ;
                        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH) + 1, max.get(Calendar.DAY_OF_MONTH));

                        dateDialog.getDatePicker().setMaxDate(max.getTimeInMillis());
                        dateDialog.getDatePicker().setMinDate(new Date().getTime());
                        dateDialog.getWindow();
                        dateDialog.show();

                        dateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                holder.availableSwitch.setChecked(false);
                            }
                        });

                        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                Calendar toDate = Calendar.getInstance() ;
                                toDate.set(year, month, day);
                                toolSetDateTime = toDate.getTime() ;
                                Log.d("TAG", toolSetDateTime + "") ;
                                Date uploadDate = new Date() ;
                                availableMap.put("fromDate", uploadDate) ;
                                availableMap.put("toDate", toolSetDateTime) ;
                                availableMap.put("available", true) ;


                                mStore.collection("RentTools").document(toolList.get(position).getToolId())
                                        .update(availableMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("TAG", "Tool updated") ;
                                                }
                                            }
                                        }) ;
                            }
                        } ;
                    }
                    else {
                        availableMap.put("available", false) ;
                        mStore.collection("RentTools").document(toolList.get(position).getToolId())
                                .update(availableMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("TAG", "Tool updated") ;
                                        }
                                    }
                                }) ;
                    }
                }
            });
        }
        else {
            holder.availableSwitch.setVisibility(View.GONE);
            holder.extendTool.setVisibility(View.GONE);
            holder.cartImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(context, Tool_Details_Activity.class);
                    intent.putExtra("detail", toolList.get(position));
                    context.startActivity(intent);
                }
            });
            holder.toolLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(context, Tool_Details_Activity.class);
                    intent.putExtra("detail", toolList.get(position));
                    context.startActivity(intent);
                }
            });
        }


    }

    public int getItemCount() {
        return toolList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cartImage;
        TextView cartName, cartPrice, extendTool;
        LinearLayout toolLayout ;
        SwitchCompat availableSwitch ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartImage = itemView.findViewById(R.id.cart_image);
            cartName = itemView.findViewById(R.id.cart_name);
            cartPrice = itemView.findViewById(R.id.cart_price);
            extendTool = itemView.findViewById(R.id.extendTool) ;
            toolLayout = itemView.findViewById(R.id.toolLayout) ;
            availableSwitch = itemView.findViewById(R.id.availableSwitch) ;
        }
    }


}
