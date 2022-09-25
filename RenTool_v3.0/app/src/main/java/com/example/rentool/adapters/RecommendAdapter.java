package com.example.rentool.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.activities.Tool_Details_Activity;
import com.example.rentool.domain.Tool;

import java.util.List;

public class RecommendAdapter extends PagerAdapter {
    Context context ;
    List<Tool> toolList ;

    public RecommendAdapter (Context context, List<Tool> toolList) {
        this.context = context ;
        this.toolList = toolList ;
    }


    @Override
    public int getCount() {
        if (toolList.size() == 0) {
            return 0 ;
        }
        else {
            return toolList.size();
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object ;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.image_slider_layout, container, false) ;
        ImageView toolImage = view.findViewById(R.id.toolImage) ;
        TextView toolName = view.findViewById(R.id.toolName) ;

        Glide.with(context).load(toolList.get(position).getImg_url()).into(toolImage) ;
        toolName.setText(toolList.get(position).getName());

        toolImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, Tool_Details_Activity.class);
                intent.putExtra("detail", toolList.get(position));
                context.startActivity(intent);
            }
        });

        container.addView(view);
        return view ;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

    }
}
