package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rentool.R;
import com.example.rentool.domain.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map_Activity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map ;
    String address ;
    private User sellerInfo ;
    private String[] location ;
    double latitude, longitude ;
    private ImageView backBtn, homeBtn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        backBtn = findViewById(R.id.backBtn) ;
        homeBtn = findViewById(R.id.homeBtn) ;


        address = getIntent().getStringExtra("address") ;
        location = getIntent().getStringExtra("latLong").split(",") ;
        sellerInfo = (User) getIntent().getSerializableExtra("sellerInfo");

        if (sellerInfo == null) {
            Toast.makeText(this, "User is null", Toast.LENGTH_SHORT).show();
        }

        String sourcePage = getIntent().getStringExtra("sourcePage") ;


        latitude = Double.parseDouble(location[0]) ;
        longitude = Double.parseDouble(location[1]) ;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourcePage.equals("home")) {
                    Intent intent = new Intent(Map_Activity.this, Main_Activity.class) ;
                    startActivity(intent);
                    finish();
                }
                else if (sourcePage.equals("userPage")) {
                    Intent intent = new Intent(Map_Activity.this, User_Detail_Activity.class) ;
                    intent.putExtra("mapUserDetails", sellerInfo) ;
                    startActivity(intent);
                    finish();
                }
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Map_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map) ;

        mapFragment.getMapAsync(this);
    }

    // Does NOT work in emulator, only on the phone
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap ;
        LatLng userAddress = new LatLng(latitude, longitude);
        if (address != null) {
            map.addMarker(new MarkerOptions().position(userAddress).title(address)) ;
        }
        else {
            map.addMarker(new MarkerOptions().position(userAddress)) ;
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userAddress, 15));
    }
}