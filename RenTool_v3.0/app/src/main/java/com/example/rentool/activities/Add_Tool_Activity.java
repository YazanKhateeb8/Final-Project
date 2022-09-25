package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rentool.R;
import com.example.rentool.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Add_Tool_Activity extends AppCompatActivity {
    private FirebaseAuth mAuth ;
    private FirebaseFirestore mStore ;
    private StorageReference mStorageRef ;
    private ImageView backBtn, toolImage ;
    private TextInputLayout toolName, toolPrice, toolDescription, toolToDate ;
    private TextInputEditText toolToDateEdit ;
    private Button addTool ;
    private String condition[] = {"Brand New", "New", "Good", "Acceptable"} ;
    private String category[] = {"Air Compressor", "Chain Saw", "Circular Saw", "Drill", "Jig Saw", "Nail Gun", "Power Sander", "Other"} ;
    private AutoCompleteTextView toolCategoryAutoComplete, toolConditionAutoComplete ;
    private ArrayAdapter<String> toolCategoryAdapter, toolConditionAdapter ;
    private static int addImageFlag = 0 ;
    private Uri mImageUri;
    private static int imgFlag = 0 ;
    private String img_url ;
    private ProgressBar progressBar ;
    private DatePickerDialog.OnDateSetListener mDateSetListener ;
    private static Date toolToDateTime ;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int RESULT_OK = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tool);

        mAuth = FirebaseAuth.getInstance() ;
        mStore = FirebaseFirestore.getInstance() ;
        mStorageRef = FirebaseStorage.getInstance().getReference("newTools") ;

        backBtn = findViewById(R.id.backBtn) ;
        toolImage = findViewById(R.id.toolImage) ;
        toolName = findViewById(R.id.toolName) ;
        toolPrice = findViewById(R.id.toolPrice) ;
        toolDescription = findViewById(R.id.toolDescription) ;
        addTool = findViewById(R.id.addTool) ;
        toolCategoryAutoComplete = findViewById(R.id.toolCategoryAutoComplete) ;
        toolConditionAutoComplete = findViewById(R.id.toolConditionAutoComplete) ;
        progressBar = findViewById(R.id.progressBar) ;
        toolToDate = findViewById(R.id.toolToDate) ;
        toolToDateEdit = findViewById(R.id.toolToDateEdit) ;

        toolCategoryAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete_list, category) ;
        toolCategoryAutoComplete.setAdapter(toolCategoryAdapter);
        toolConditionAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete_list, condition) ;
        toolConditionAutoComplete.setAdapter(toolConditionAdapter);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Add_Tool_Activity.this, Main_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });

        toolImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser() ;
                addImageFlag = 1 ;
            }
        });

        toolToDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance() ;
                int year = cal.get(Calendar.YEAR) ;
                int month = cal.get(Calendar.MONTH) ;
                int day = cal.get(Calendar.DAY_OF_MONTH) ;

                DatePickerDialog dateDialog = new DatePickerDialog(
                        Add_Tool_Activity.this, R.style.datePickerStyle, mDateSetListener,
                        year, month, day) ;
                Calendar max = Calendar.getInstance() ;
                max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH) + 1, max.get(Calendar.DAY_OF_MONTH));

                dateDialog.getDatePicker().setMaxDate(max.getTimeInMillis());
                dateDialog.getDatePicker().setMinDate(new Date().getTime());
                dateDialog.getWindow();
                dateDialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar toDate = Calendar.getInstance() ;
                toDate.set(year, month, day);
                toolToDateTime = toDate.getTime() ;
                String toolDate = day + "/" + month + "/" + year ;
                toolToDate.getEditText().setText(toolDate);
            }
        } ;


        addTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateName() | !validateCategory() | !validateCondition() | !validatePrice() | !validateDescription() | !validateToDate()) {
                    Toast.makeText(Add_Tool_Activity.this, "Can't add tool, fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    mStore.collection("Users").document(mAuth.getCurrentUser().getUid())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User checkAddress = task.getResult().toObject(User.class) ;
                                            if (checkAddress.getAddress() != null) {
                                                uploadTool() ;
                                            }
                                            else {
                                                Toast.makeText(Add_Tool_Activity.this, "You have to add an address to add a tool", Toast.LENGTH_SHORT).show();
                                            }
                                    }
                                }
                            }) ;

                }
            }
        });

    }

    private void uploadTool() {
        String name = toolName.getEditText().getText().toString() ;
        String category = toolCategoryAutoComplete.getText().toString() ;
        String condition = toolConditionAutoComplete.getText().toString() ;
        double price = Double.parseDouble(toolPrice.getEditText().getText().toString()) ;
        String description = toolDescription.getEditText().getText().toString() ;
        int sold = 0 ;
        double rating = 0.0 ;
        boolean available = true ;
        Date uploadDate = new Date() ;

        Map<String, Object> newTool = new HashMap<>() ;
        newTool.put("name", name) ;
        newTool.put("category", category) ;
        newTool.put("condition", condition) ;
        newTool.put("price", price) ;
        newTool.put("description", description) ;
        newTool.put("sold", sold) ;
        newTool.put("rating", rating) ;
        newTool.put("available", available) ;
        newTool.put("uploadDate", uploadDate) ;
        newTool.put("fromDate", uploadDate) ;
        newTool.put("toDate", toolToDateTime) ;
        newTool.put("img_url", img_url) ;
        newTool.put("userId", mAuth.getCurrentUser().getUid()) ;


        mStore.collection("RentTools").document()
                .set(newTool).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Add_Tool_Activity.this, "Product has been added", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Add_Tool_Activity.this, Main_Activity.class) ;
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(Add_Tool_Activity.this, "Something went Wrong, try again", Toast.LENGTH_SHORT).show();
                }
            }
        }) ;

    }

    private void uploadImage() {
        if (mImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + ".jpg");
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
                            Toast.makeText(Add_Tool_Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(Add_Tool_Activity.this, "No file selected", Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imgFlag = 1 ;
            Picasso.get().load(mImageUri).into(toolImage);
            uploadImage() ;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //noinspection deprecation
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private boolean validateName() {
        String val = toolName.getEditText().getText().toString() ;

            if (val.isEmpty()) {
                toolName.setError("Field cannot be empty") ;
                return false ;
            }
            else if (val.length() < 3) {
                toolName.setError("Tool name must at least contain 3 characters") ;
                return false ;
            }
            else {
                toolName.setError(null) ;
                toolName.setErrorEnabled(false);
                return true ;
            }
    }

    private boolean validateCategory() {
        String val = toolCategoryAutoComplete.getText().toString() ;

        if (val.isEmpty()) {
            toolCategoryAutoComplete.setError("Field cannot be empty") ;
            return false ;
        }
        else {
            toolCategoryAutoComplete.setError(null) ;
            return true ;
        }
    }

    private boolean validateToDate() {
        if (toolToDate.getEditText().getText().toString().isEmpty()) {
            toolToDate.setError("Field cannot be empty") ;
            return false ;
        }
        else {
            toolToDate.setError(null) ;
            toolToDate.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateCondition() {
        String val = toolConditionAutoComplete.getText().toString() ;

        if (val.isEmpty()) {
            toolConditionAutoComplete.setError("Field cannot be empty") ;
            return false ;
        }
        else {
            toolConditionAutoComplete.setError(null) ;
            return true ;
        }
    }

    private boolean validatePrice() {
        String val = toolPrice.getEditText().getText().toString() ;

        if (val.isEmpty()) {
            toolPrice.setError("Field cannot be empty");
            return false ;
        }
        else if (val.length() > 4) {
            toolPrice.setError("Price too high");
            return false ;
        }
        else {
            toolPrice.setError(null) ;
            return true ;
        }
    }

    private boolean validateDescription() {
        String val = toolDescription.getEditText().getText().toString() ;

        if (val.isEmpty()) {
            toolDescription.setError("Field cannot be empty");
            return false ;
        }
        else if (val.length() < 10) {
            toolDescription.setError("Description too short");
            return false ;
        }
        else {
            toolDescription.setError(null) ;
            return true ;
        }
    }


}