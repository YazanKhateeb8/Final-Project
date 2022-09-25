package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.adapters.Profession;
import com.example.rentool.domain.AddressClass;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Profile_Activity extends AppCompatActivity implements LocationListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int RESULT_OK = -1;

    private TextInputLayout nameTxt, emailTxt, phoneTxt, professionTxt, locationTxt ;
    private ImageView userImage, backBtn, homeBtn, addCurrentLocation ;
    private Button updateProfile ;
    private FirebaseAuth mAuth ;
    private FirebaseFirestore mStore ;
    private StorageReference mSorageRef;
    private Uri mImageUri;
    private ProgressBar progressBar ;
//    private String professions[] = {"Carpenter", "Tile Setter", "Plastering", "Lumberjack", "Welder", "Plumber", "Electrician", "HVAC technicians"} ;
    private List<String> professionsList ;
    AutoCompleteTextView professionAutoComplete ;
    ArrayAdapter<String> adapterProfession ;
    private static String img_url ;
    private static int imgFlag = 0, PreqCode = 1, locationFlag = 0 ;
    private static int addImageFlag = 0 ;
    private static final int STORAGE_PERMISSION_CODE = 113 ;
    LocationManager locationManager ;
    private String currentLocation, currentAddress ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameTxt = findViewById(R.id.nameTxt) ;
        emailTxt = findViewById(R.id.emailTxt) ;
        phoneTxt = findViewById(R.id.phoneTxt) ;
        professionTxt = findViewById(R.id.professionTxt) ;
        locationTxt = findViewById(R.id.locationTxt) ;
        userImage = findViewById(R.id.userImage) ;
        updateProfile = findViewById(R.id.updateProfile) ;
        backBtn = findViewById(R.id.backBtn) ;
        homeBtn = findViewById(R.id.homeBtn) ;
        progressBar = findViewById(R.id.progressBar) ;
        progressBar.setVisibility(View.INVISIBLE);
        professionAutoComplete = findViewById(R.id.professionAutoComplete) ;
        addCurrentLocation = findViewById(R.id.addCurrentLocation) ;

        professionsList = new ArrayList<>() ;
        adapterProfession = new ArrayAdapter<String>(this, R.layout.auto_complete_list, professionsList) ;
        professionAutoComplete.setAdapter(adapterProfession);

        mAuth = FirebaseAuth.getInstance() ;
        mStore = FirebaseFirestore.getInstance() ;
        mSorageRef = FirebaseStorage.getInstance().getReference("userProfileImages") ;

        mStore.collection("Profession").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Profession pro = doc.toObject(Profession.class) ;
                        professionsList.add(pro.getName()) ;
                        adapterProfession.notifyDataSetChanged();
                    }
                }
            }
        }) ;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile_Activity.this, User_Detail_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile_Activity.this, User_Detail_Activity.class);
                startActivity(intent);
                finish();
            }
        });

        getUserData() ;

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE) ;
                openFileChooser() ;
                addImageFlag = 1 ;

            }
        });

        addCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Profile_Activity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Profile_Activity.this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                }
                getLocation() ;
            }
        });


        updateProfile.setOnClickListener(new View.OnClickListener() {
            String address = locationTxt.getEditText().getText().toString() ;
            @Override
            public void onClick(View view) {
                if (!validateName() && !validateEmail() && !validatePhone()
                        && !validateProfession() && address.isEmpty() && locationFlag == 0 && imgFlag == 0) {
                    Toast.makeText(Profile_Activity.this, "Cant't update, Fields are Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                        updateProfile() ;
                }
            }

        });



    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(0);
                }
            }, 5000);
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE) ;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, Profile_Activity.this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(Profile_Activity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Profile_Activity.this, new String[] {permission}, requestCode) ;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Profile_Activity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(Profile_Activity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
            Intent intent = new Intent(Profile_Activity.this, User_Detail_Activity.class);
            startActivity(intent);
            finish();
    }

    private void getUserData() {
        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User user = task.getResult().toObject(User.class) ;
                            if (user.getImg_url() != null) {
                                Glide.with(getApplicationContext()).load(user.getImg_url()).into(userImage) ;
                            }
                            nameTxt.setHint(user.getName());
                            emailTxt.setHint(user.getEmail());
                            phoneTxt.setHint(user.getPhoneNum());
                            if (user.getProfession() != null) {
                                professionTxt.setHint(user.getProfession());
                            }
                            if (user.getAddress() != null) {
                                locationTxt.setHint(user.getAddress());
                            }
                        }
                    }
                }) ;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //noinspection deprecation
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if (mImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            StorageReference fileRef = mSorageRef.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //noinspection deprecation
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            }, 5000);
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    img_url = task.getResult().toString();
                                    imgFlag = 1 ;
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Profile_Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(Profile_Activity.this, "No file selected", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void updateProfile() {
        Map<String, Object> updateUserMap = new HashMap<>() ;

        if (!nameTxt.getEditText().getText().toString().isEmpty()) {
            updateUserMap.put("name", nameTxt.getEditText().getText().toString()) ;
        }
        if (!emailTxt.getEditText().getText().toString().isEmpty()) {
            updateUserMap.put("email", emailTxt.getEditText().getText().toString()) ;
        }
        if (!phoneTxt.getEditText().getText().toString().isEmpty()) {
            updateUserMap.put("phoneNum", phoneTxt.getEditText().getText().toString()) ;
        }
        if (!professionAutoComplete.getText().toString().isEmpty()) {
            updateUserMap.put("profession", professionAutoComplete.getText().toString()) ;
        }
        if (locationFlag != 1) {
            if (!locationTxt.getEditText().getText().toString().isEmpty()) {
                updateUserMap.put("address", locationTxt.getEditText().getText().toString()) ;
            }
        }
        if (locationFlag == 1) {
            updateUserMap.put("location", currentLocation) ;
            updateUserMap.put("address", currentAddress) ;
        }
        if (img_url != null) {
            updateUserMap.put("img_url", img_url) ;
        }

        mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .update(updateUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (!emailTxt.getEditText().getText().toString().isEmpty()) {
                                updateEmail() ;
                            }
                            Toast.makeText(Profile_Activity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Profile_Activity.this, User_Detail_Activity.class) ;
                            startActivity(intent);
                            finish();
                        }
                    }
                }) ;
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                mImageUri = data.getData();
                imgFlag = 1 ;
                Picasso.get().load(mImageUri).into(userImage);
                uploadImage() ;
            }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(Profile_Activity.this, Locale.getDefault()) ;
            List<Address> addressClasses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1) ;
            String address = addressClasses.get(0).getAddressLine(0) ;
            locationFlag = 1 ;
            currentLocation = location.getLatitude() + "," + location.getLongitude() ;
            currentAddress = address ;
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(Profile_Activity.this, "Location is set", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    private boolean validateName() {
        String val = nameTxt.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            return false ;
        }

        if (!val.matches(nameVal)) {
            nameTxt.setError("Name is not valid") ;
            return false ;
        }
        else {
            nameTxt.setError(null) ;
            nameTxt.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateEmail() {
        String val = emailTxt.getEditText().getText().toString() ;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+" ;

        String userEmail = mAuth.getCurrentUser().getEmail() ;

        if (val.isEmpty()) {
            return false ;
        }

        if (userEmail.equals(val)) {
            emailTxt.setError("Email address is the same");
            return false ;
        }

        if (!val.matches(emailPattern)) {
            emailTxt.setError("Invalid email address");
            return false ;
        }

        else {
            emailTxt.setError(null) ;
            emailTxt.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validatePhone() {

        String val = phoneTxt.getEditText().getText().toString() ;
        String number = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$" ;

        if (val.isEmpty()) {
            return false ;
        }

        if (!val.matches(number)) {
            phoneTxt.setError("Phone is not valid");
            return false ;
        }
        else {
            phoneTxt.setError(null) ;
            phoneTxt.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateProfession() {
        String val = professionAutoComplete.getText().toString() ;

        if (val.isEmpty()) {
            return false ;
        }

        else {
            professionTxt.setError(null) ;
            professionTxt.setErrorEnabled(false);
            return true ;
        }
    }

    private void updateEmail() {
        mAuth.getCurrentUser().updateEmail(emailTxt.getEditText().getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "email updated") ;
                        }
                    }
                }) ;
    }
}