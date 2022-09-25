package com.example.rentool.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rentool.R;
import com.example.rentool.activities.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogoutFragment extends Fragment {

    private FirebaseAuth mAuth ;
    private FirebaseFirestore mStore ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance() ;
        mStore = FirebaseFirestore.getInstance() ;

        mStore.collection("OnlineUsers").document(mAuth.getCurrentUser().getUid())
                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("TAG", "User is offline") ;
                    }
                }) ;
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), Login.class) ;
        startActivity(intent);

    }

    public LogoutFragment() { }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logout, container, false);
        return view ;
    }
}