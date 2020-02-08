package com.example.thesisitfinal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {
    private Button btn_instructions, btn_map, btn_guide;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_instructions = findViewById(R.id.instructionsBtn);
        btn_map = findViewById(R.id.mapBtn);
        btn_guide = findViewById(R.id.guideBtn);

        btn_instructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), test.class);
                view.getContext().startActivity(intent);
            }
        });

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

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task)
                    {
                        if(!task.isComplete())
                        {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                            // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"})
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, token);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();

                    }
                });
        Toast.makeText(this, "Please enable your internet and location before using the map.", Toast.LENGTH_SHORT).show();

    }

    private void showDialog()
    {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
