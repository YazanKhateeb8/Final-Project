package com.example.rentool.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.rentool.R;

public class Intro extends AppCompatActivity {

    private static int SPLASH_SCREEN = 5000 ;

    //Variables
    Animation topAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);

        //Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        //Hooks
        View image = findViewById(R.id.logo);
        View logo = findViewById(R.id.RenTool);
        View slogan = findViewById(R.id.comment_1);

        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Intro.this, Login.class) ;

            Pair[] pairs = new Pair[2] ;
            pairs[0] = new Pair<View, String> (image, "logo_image") ;
            pairs[1] = new Pair<View, String> (logo, "logo_text") ;

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Intro.this, pairs) ;
            startActivity(intent, options.toBundle());

        }, SPLASH_SCREEN) ;

    }
}