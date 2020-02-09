package com.example.thesisitfinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private Button btn_map, btn_guide;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_map = findViewById(R.id.mapBtn);
        btn_guide = findViewById(R.id.guideBtn);

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), map.class);
                view.getContext().startActivity(intent);
            }
        });

        btn_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GuideActivity.class);
                view.getContext().startActivity(intent);}
        });

        //Subscribe to topic
        FirebaseMessaging.getInstance().subscribeToTopic("evacThesis")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed successfully";
                        if (!task.isSuccessful()) {
                            msg = "Can't subscribe";
                        }
                        Log.d(TAG, msg);
                    }
                });

        Toast.makeText(this, "Please enable your internet and location before using the map.", Toast.LENGTH_LONG).show();
    }

}
