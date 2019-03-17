package com.sukaidev.profilephotoview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by sukaidev on 2019/03/12.
 */
public class MainActivity extends AppCompatActivity {

    ProfilePhotoView profilePhotoView;

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn_change);
        LinearLayout ll = findViewById(R.id.linear_layout);

        profilePhotoView = new ProfilePhotoView(this);

        profilePhotoView.setViewSize(150, 150);
        profilePhotoView.setFormat(ProfilePhotoView.CIRCLE);
        profilePhotoView.setImageResource(R.drawable.profile_photo_300_300);
        ll.addView(profilePhotoView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profilePhotoView.setViewSize(100, 100);
                profilePhotoView.setImageResource(R.drawable.profile_photo_439_507);
                profilePhotoView.setFormat(ProfilePhotoView.RECTANGLE);
                profilePhotoView.setRadius(30);
            }
        });
    }
}
