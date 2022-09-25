package com.example.rentool.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.activities.Tool_Details_Activity;
import com.example.rentool.domain.Tool;

import java.util.List;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.ViewHolder>{
    Context context ;
    List<Tool> toolList ;
    public HomePageAdapter(Context context, List<Tool> mFeatureList) {
        this.context = context ;
        this.toolList = mFeatureList ;
    }

    @NonNull
    @Override
    public HomePageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_feature_item, parent, false) ;

        return new HomePageAdapter.ViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.toolPrice.setText(toolList.get(position).getPrice() + "₪");
        holder.toolName.setText(toolList.get(position).getName());
        Glide.with(context).load(toolList.get(position).getImg_url()).into(holder.toolImg) ;
        holder.toolImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Bundle data = new Bundle() ;
                data.putString("toolId", toolList.get(position).getToolId());
                AppCompatActivity activity = (AppCompatActivity) view.getContext() ;
                Intent intent = new Intent(context, Tool_Details_Activity.class) ;
                intent.putExtra("detail", toolList.get(position)) ;
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return toolList.size() ;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView toolImg ;
        TextView toolName, toolPrice ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            toolImg = itemView.findViewById(R.id.toolImg) ;
            toolName = itemView.findViewById(R.id.toolName) ;
            toolPrice = itemView.findViewById(R.id.toolPrice) ;
        }
    }
}
