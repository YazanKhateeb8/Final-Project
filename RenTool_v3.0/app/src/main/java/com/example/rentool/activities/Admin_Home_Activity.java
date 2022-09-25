package com.example.rentool.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rentool.R;
import com.example.rentool.domain.Category;
import com.example.rentool.domain.Tool;
import com.example.rentool.fragments.LogoutFragment;
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

import java.util.HashMap;
import java.util.Map;

public class Admin_Home_Activity extends AppCompatActivity {
    private androidx.appcompat.widget.Toolbar toolBar ;
    private FirebaseFirestore mStore ;
    private FirebaseAuth mAuth ;
    private StorageReference mStorageRef ;
    private Button addAdmin, addCategory, addProfession, statisticsBtn ;
    private ProgressBar progressBar ;
    // Dialogs
    private Dialog addManagerDialog, addCategoryDialog, addProfessionDialog;
    // Dialog layout variables
    private Button adminSubmitBtn, categorySubmitBtn, professionSubmitBtn ;
    private TextInputLayout managerEmail, categoryName, professionName ;
    private ImageView categoryImage ;
    private Uri mImageUri;
    private String img_url ;
    private static int imgFlag = 0 ;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int RESULT_OK = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mStore = FirebaseFirestore.getInstance() ;
        mAuth = FirebaseAuth.getInstance() ;
        mStorageRef = FirebaseStorage.getInstance().getReference("Category") ;

        toolBar = findViewById(R.id.layoutToolBar) ;
        setSupportActionBar(toolBar);

        addCategory = findViewById(R.id.addCategory) ;
        addAdmin = findViewById(R.id.addAdmin) ;
        addProfession = findViewById(R.id.addProfession) ;
        statisticsBtn = findViewById(R.id.statisticsBtn) ;


        // Add manager section
        addManagerDialog = new Dialog(Admin_Home_Activity.this);
        addManagerDialog.setContentView(R.layout.add_admin_dialog);
        addManagerDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        addManagerDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addManagerDialog.setCancelable(true);
        addManagerDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        managerEmail = addManagerDialog.findViewById(R.id.managerEmail) ;
        adminSubmitBtn = addManagerDialog.findViewById(R.id.adminSubmitBtn) ;

        addAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addManagerDialog.show();
            }
        });

        adminSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateManagerEmail()) {
                    return;
                }
                else {
                    String newManagerEmail = managerEmail.getEditText().getText().toString() ;
                    Map managerMap = new HashMap() ;
                    managerMap.put("admin", true) ;
                    mStore.collection("Users").whereEqualTo("email", newManagerEmail)
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {
                                            for (DocumentSnapshot doc : task.getResult()) {
                                                mStore.collection("Users").document(doc.getId())
                                                        .update(managerMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                Toast.makeText(Admin_Home_Activity.this, "Manager Added", Toast.LENGTH_SHORT).show();
                                                                managerEmail.getEditText().setText("");
                                                                addManagerDialog.dismiss();
                                                            }
                                                        }) ;
                                            }
                                        }
                                        else {
                                            managerEmail.setError("Email does not exist") ;
                                        }
                                    }
                                }
                            }) ;

                }
            }
        });


        // Add category section
        addCategoryDialog = new Dialog(Admin_Home_Activity.this);
        addCategoryDialog.setContentView(R.layout.add_category_dialog);
        addCategoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        addCategoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addCategoryDialog.setCancelable(true);
        addCategoryDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        categoryName = addCategoryDialog.findViewById(R.id.categoryName) ;
        categorySubmitBtn = addCategoryDialog.findViewById(R.id.categorySubmitBtn) ;
        categoryImage = addCategoryDialog.findViewById(R.id.categoryImage) ;
        progressBar = addCategoryDialog.findViewById(R.id.progressBar) ;

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCategoryDialog.show();
            }
        });

        categoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser() ;
            }
        });

        categorySubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateCategoryName() || imgFlag != 1) {
                    if (imgFlag == 0) {
                        Toast.makeText(Admin_Home_Activity.this, "You have to add an image", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                else {
                    String catName = categoryName.getEditText().getText().toString() ;
                    Category newCategory = new Category(catName, img_url) ;
                    mStore.collection("Category").document(catName).set(newCategory)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(Admin_Home_Activity.this, "Category added", Toast.LENGTH_SHORT).show();
                                    categoryName.getEditText().setText("");
                                    Glide.with(getApplicationContext()).clear(categoryImage);
                                    addCategoryDialog.dismiss();
                                }
                            }) ;
                }
            }
        });


        // Add profession section
        addProfessionDialog = new Dialog(Admin_Home_Activity.this);
        addProfessionDialog.setContentView(R.layout.add_profession_dialog);
        addProfessionDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        addProfessionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addProfessionDialog.setCancelable(true);
        addProfessionDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        professionSubmitBtn = addProfessionDialog.findViewById(R.id.professionSubmitBtn) ;
        professionName = addProfessionDialog.findViewById(R.id.professionName) ;

        addProfession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProfessionDialog.show();
            }
        });


        professionSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateProfessionName()) {
                    return;
                }
                else {
                    String profName = professionName.getEditText().getText().toString() ;
                    Map<String, Object> professionMap = new HashMap<>() ;
                    professionMap.put("name", profName) ;
                    mStore.collection("Profession").document(profName)
                            .set(professionMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(Admin_Home_Activity.this, "Profession added", Toast.LENGTH_SHORT).show();
                                    professionName.getEditText().setText("");
                                    addProfessionDialog.dismiss();
                                }
                            }) ;
                }
            }
        });


        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Admin_Home_Activity.this, Statistics_Activity.class) ;
                startActivity(intent);
                finish();
            }
        });


    }

    private boolean validateManagerEmail() {
        String val = managerEmail.getEditText().getText().toString() ;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.+[a-zA-Z]+" ;

        if (val.isEmpty()) {
            managerEmail.setError("Field cannot be empty") ;
            return false ;
        }
        else if (!val.matches(emailPattern)) {
            managerEmail.setError("Enter a valid email address");
            return false ;
        }
        else {
            managerEmail.setError(null) ;
            managerEmail.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateCategoryName() {
        String val = categoryName.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            categoryName.setError("Field cannot be empty") ;
            return false ;
        }
        else if (!val.matches(nameVal)) {
            categoryName.setError("Enter a valid category name");
            return false ;
        }
        else {
            categoryName.setError(null) ;
            categoryName.setErrorEnabled(false);
            return true ;
        }
    }

    private boolean validateProfessionName() {
        String val = professionName.getEditText().getText().toString() ;
        String nameVal = "[a-zA-Z ]+" ;

        if (val.isEmpty()) {
            professionName.setError("Field cannot be empty") ;
            return false ;
        }
        else if (!val.matches(nameVal)) {
            professionName.setError("Enter a valid profession name");
            return false ;
        }
        else {
            professionName.setError(null) ;
            professionName.setErrorEnabled(false);
            return true ;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutMenuBtn) {
            mAuth.signOut();
            Intent intent = new Intent(Admin_Home_Activity.this, Login.class) ;
            startActivity(intent);
            finish();
        }

        return true ;
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
                            Toast.makeText(Admin_Home_Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(Admin_Home_Activity.this, "No file selected", Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imgFlag = 1 ;
            Glide.with(getApplicationContext()).load(mImageUri).into(categoryImage) ;
//            Picasso.get().load(mImageUri).into(categoryImage);
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


}