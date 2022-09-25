package com.example.rentool.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentool.R;
import com.example.rentool.domain.AddressClass;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    Context applicationContext ;
    List<AddressClass> mAddressClassList;
    private RadioButton mSelectedRadioButton ;
    SelectedAddress selectedAddress ;

    public AddressAdapter(Context applicationContext, List<AddressClass> mAddressClassList, SelectedAddress selectedAddress) {
        this.applicationContext = applicationContext ;
        this.mAddressClassList = mAddressClassList;
        this.selectedAddress = selectedAddress ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(applicationContext).inflate(R.layout.single_address_item,parent,false);

        return new ViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (mAddressClassList.size() > 0) {
            mAddressClassList.get(0).setSelected(true);
            if(mSelectedRadioButton != null){
                mSelectedRadioButton.setChecked(false);
            }
            mSelectedRadioButton = (RadioButton) holder.mRadio;
            mSelectedRadioButton.setChecked(true);
        }
        holder.mAddress.setText(mAddressClassList.get(position).getAddress());
        holder.mRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(AddressClass addressClass : mAddressClassList){
                    addressClass.setSelected(false);
                }

                mAddressClassList.get(position).setSelected(true);

                if(mSelectedRadioButton != null){
                    mSelectedRadioButton.setChecked(false);
                }
                mSelectedRadioButton = (RadioButton) view;
                mSelectedRadioButton.setChecked(true);
                selectedAddress.setAddress(mAddressClassList.get(position).getAddress());
            }
        });
        holder.mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(AddressClass addressClass : mAddressClassList){
                    addressClass.setSelected(false);
                }

                mAddressClassList.get(position).setSelected(true);

                if(mSelectedRadioButton != null){
                    mSelectedRadioButton.setChecked(false);
                }
                mSelectedRadioButton = (RadioButton) holder.mRadio;
                mSelectedRadioButton.setChecked(true);
                selectedAddress.setAddress(mAddressClassList.get(position).getAddress());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAddressClassList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mAddress;
        private RadioButton mRadio;
        private LinearLayout location ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAddress=itemView.findViewById(R.id.address_add);
            mRadio=itemView.findViewById(R.id.select_address);
            location = itemView.findViewById(R.id.location) ;
        }
    }

    public interface SelectedAddress {
        public void setAddress(String s) ;
    }


//    public interface selectedRadio {
//
//    }

}
