package com.example.rentool.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.activities.Main_Activity;
import com.example.rentool.activities.Tool_Details_Activity;
import com.example.rentool.domain.Tool;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ToolsRecyclerAdapter extends RecyclerView.Adapter<ToolsRecyclerAdapter.ViewHolder> {
    Context context ;
    List<Tool> mToolList ;

    public ToolsRecyclerAdapter(Context context, List<Tool> mToolList) {
        this.context = context;
        this.mToolList = mToolList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_item_layout, parent, false) ;
        return new ToolsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolsRecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.toolPrice.setText(mToolList.get(position).getPrice() + "₪");
        holder.toolName.setText(mToolList.get(position).getName());
        if (mToolList.get(position).getSold() == 0 || mToolList.get(position).getRating() == 0) {
            holder.toolRating.setText("Not Rated");
        }
        else {
            double newRating = mToolList.get(position).getRating() / mToolList.get(position).getSold() ;
            DecimalFormat formatRating = new DecimalFormat("0.0") ;
            String finalRating = formatRating.format(newRating) ;
            double toBeFormated = Double.valueOf(finalRating) ;
            holder.toolRating.setText("★" + toBeFormated);
        }
        if (!(context instanceof Main_Activity)) {
            Glide.with(context).load(mToolList.get(position).getImg_url()).into(holder.toolImage) ;
        }
        else {
            holder.toolImage.setVisibility(View.GONE);
        }

        holder.toolImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, Tool_Details_Activity.class);
                intent.putExtra("detail", mToolList.get(position));
                context.startActivity(intent);
            }
        });

        holder.toolName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, Tool_Details_Activity.class);
                intent.putExtra("detail", mToolList.get(position));
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mToolList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView toolImage ;
        private TextView toolName, toolPrice, toolRating ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            toolImage = itemView.findViewById(R.id.itemImage) ;
            toolName = itemView.findViewById(R.id.itemName) ;
            toolPrice = itemView.findViewById(R.id.itemPrice) ;
            toolRating = itemView.findViewById(R.id.itemRating) ;
        }
    }

    public void filteredList(ArrayList<Tool> filteredList) {
        mToolList = filteredList ;
        notifyDataSetChanged();
    }

}
