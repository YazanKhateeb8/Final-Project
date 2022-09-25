package com.example.rentool.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.domain.Order;
import com.example.rentool.domain.Tool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    List<Order> orderList ;
    Context context ;
    FirebaseFirestore mStore ;

    public OrdersAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
        mStore = FirebaseFirestore.getInstance() ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_order_item,parent,false);
        return new OrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.ViewHolder holder, int position) {
        Order order = orderList.get(position) ;

        mStore.collection("RentTools").document(order.getToolId())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Tool tool = task.getResult().toObject(Tool.class) ;
                            holder.toolName.setText(tool.getName());
                            holder.toolPrice.setText(tool.getPrice() + "₪");
                            Glide.with(context).load(tool.getImg_url()).into(holder.toolImage) ;
                        }
                    }
                }) ;

        holder.orderDate.setText(order.getDate());
        holder.totalCost.setText("₪" + order.getTotal());
    }

    @Override
    public int getItemCount() {
        if (orderList != null) {
            return orderList.size() ;
        }
        else {
            return 0 ;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderDate, totalCost, toolPrice, toolName ;
        ImageView toolImage ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.orderDate) ;
            totalCost = itemView.findViewById(R.id.totalCost) ;
            toolPrice = itemView.findViewById(R.id.toolPrice) ;
            toolName = itemView.findViewById(R.id.toolName) ;
            toolImage = itemView.findViewById(R.id.toolImage) ;
        }
    }
}
