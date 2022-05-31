package com.app.diemdanh;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.model.userInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreateClassActivity extends Activity {
    EditText edtName, edtCode, inputT1, inputT2, inputT3;
    Button btnSummit,btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createclass);

        edtName = findViewById(R.id.edtName);
        edtCode = findViewById(R.id.edtCode);
        inputT1 = findViewById(R.id.inputT1);
        inputT2 = findViewById(R.id.inputT2);
        inputT3 = findViewById(R.id.inputT3);

        btnSummit = findViewById(R.id.btnMolop);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnSummit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtName.getText().toString().trim().equalsIgnoreCase("")) {
                    edtName.setError("Nhập tên lớp");
                } else if (edtCode.getText().toString().trim().equalsIgnoreCase("")) {
                    edtCode.setError("Nhập mã lớp");
                } else if (inputT1.getText().toString().trim().equalsIgnoreCase("")) {
                    inputT1.setError("Nhập thứ");
                } else if (inputT2.getText().toString().trim().equalsIgnoreCase("")) {
                    inputT2.setError("Nhập giờ bắt đầu ");
                } else if (inputT3.getText().toString().trim().equalsIgnoreCase("")) {
                    inputT3.setError("Nhập sô tiết học");
                } else {
                    createClass(edtName.getText().toString(),edtCode.getText().toString().toUpperCase(),userInfo.getCode(),Long.parseLong(inputT1.getText().toString()),
                            Long.parseLong(inputT2.getText().toString()),Long.parseLong(inputT3.getText().toString()));
                }

            }
        });


    }

    FirebaseFirestore db;

    public void createClass(String name, final String code, String teacherCode, Long t1, Long t2, Long t3) {
        db = FirebaseFirestore.getInstance();

        Map<String,Long> time = new HashMap<>();
        time.put("day",t1);
        time.put("time",t2);
        time.put("duration",t3);


        Map<String, Object> data = new HashMap<>();
        data.put("sub_name", name);
        data.put("student", new ArrayList<>());
        data.put("teacher", teacherCode);

        data.put("time",time);

        db.collection("class").document(code)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FIRE", "DocumentSnapshot successfully written!");
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CreateClassActivity.this);
                        builder.setMessage("Mở lớp thành công  "+code);
                        builder.setTitle("Thông báo !");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                Intent dashboard = new Intent(CreateClassActivity.this, DashboardActivity.class);
                                startActivity(dashboard);
                            }
                        });
                        android.app.AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIRE", "Error writing document", e);
                    }
                });

    }
}
