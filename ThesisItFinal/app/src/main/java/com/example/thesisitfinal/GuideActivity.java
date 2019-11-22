package com.example.thesisitfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GuideActivity extends AppCompatActivity {
    private TextView[] mDots;
    private ViewPager vPager;
    private ImageAdapter imageAdapter;
    private LinearLayout mDotLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        Toast.makeText(this, "Swipe left!", Toast.LENGTH_SHORT).show();

        vPager = findViewById(R.id.viewPageAndroid);
        mDotLayout = findViewById(R.id.dotsLayout);

        imageAdapter = new ImageAdapter(this);
        vPager.setAdapter(imageAdapter);

        addDotsIndicator(0);
        vPager.addOnPageChangeListener(viewListener);
    }

    //Page indicator
    public void addDotsIndicator(int position){

        mDots = new TextView[3];
        mDotLayout.removeAllViews();

        for(int i = 0; i < mDots.length; i++){
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("•"));
            mDots[i].setTextSize(45);
            mDots[i].setTextColor(getResources().getColor(R.color.transparentWhite));

            mDotLayout.addView(mDots[i]);
        }

        if(mDots.length > 0){
            mDots[position].setTextColor(getResources().getColor(R.color.white));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }

        @Override
        public void onPageSelected(int i) {
            addDotsIndicator(i);
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    };

}
