package com.example.thesisitfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Transpo extends AppCompatActivity {

    private Button btn_walk, btn_cycle, btn_drive;
    public static String modeofTranspo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transpo);

        btn_walk = findViewById(R.id.walkBtn);
        btn_cycle = findViewById(R.id.cycleBtn);
        btn_drive = findViewById(R.id.driveBtn);

        btn_walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), map.class);
                view.getContext().startActivity(intent);
                modeofTranspo = "walking";
            }
        });


        btn_cycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), map.class);
                view.getContext().startActivity(intent);
                modeofTranspo = "cycling";
            }
        });


        btn_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), map.class);
                view.getContext().startActivity(intent);
                modeofTranspo = "driving";
            }
        });

    }
}
