package com.example.rentool.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.activities.User_Detail_Activity;
import com.example.rentool.domain.User;

import java.util.List;

public class ContractorAdapter extends RecyclerView.Adapter<ContractorAdapter.ViewHolder>{
    Context context ;
    List<User> usersList ;
    public ContractorAdapter(Context context, List<User> usersList) {
        this.context = context ;
        this.usersList = usersList ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_contractor_item, parent, false) ;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.contractorName.setText(usersList.get(position).getName());
        holder.contractorProfession.setText(usersList.get(position).getProfession());
        if (usersList.get(position).getImg_url() != null) {
            Glide.with(context).load(usersList.get(position).getImg_url()).into(holder.contractorImage) ;
            holder.contractorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, User_Detail_Activity.class);
                    intent.putExtra("userDetail", usersList.get(position));
                    context.startActivity(intent);
                }
            });
        }
        holder.contractorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, User_Detail_Activity.class);
                intent.putExtra("userDetail", usersList.get(position));
                intent.putExtra("pageSource", "home") ;
                context.startActivity(intent);
            }
        });
        holder.contractorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, User_Detail_Activity.class);
                intent.putExtra("userDetail", usersList.get(position));
                intent.putExtra("pageSource", "home") ;
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size() ;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView contractorImage ;
        TextView contractorName, contractorProfession ;
        CardView contractorCard ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contractorImage = itemView.findViewById(R.id.contractorImage) ;
            contractorName = itemView.findViewById(R.id.contractorName) ;
            contractorProfession = itemView.findViewById(R.id.contractorProfession) ;
            contractorCard = itemView.findViewById(R.id.contractorCard) ;
        }
    }
}
