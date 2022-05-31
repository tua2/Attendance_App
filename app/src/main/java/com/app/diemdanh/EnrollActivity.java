package com.app.diemdanh;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.model.userInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class EnrollActivity extends Activity {
    FirebaseFirestore db;
    TextView txtSubCode, txtTittle, txtSubName, txtSubTime, txtTchName, txtStdCount;
    Button btnDangky, btnBack;
    TabLayout tablayoutDashBoard;

    String[] stdList = null;
    String classCode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enrollclass);

        classCode = getIntent().getStringExtra("CLASS_CODE");
        Object classInfo = (Object) getIntent().getSerializableExtra("CLASS_INFO");

        txtSubCode = (TextView) findViewById(R.id.txtSubCode);
        txtTittle = (TextView) findViewById(R.id.txtTittle);
        txtSubName = (TextView) findViewById(R.id.txtSubname);
        txtSubTime = (TextView) findViewById(R.id.txtSubTime);
        txtTchName = (TextView) findViewById(R.id.txtTchName);

        btnDangky = (Button) findViewById(R.id.btnMolop);
        btnBack = (Button) findViewById(R.id.btnBack);


        tablayoutDashBoard = (TabLayout) findViewById(R.id.tablayoutDashBoard);


        txtSubCode.setText(classCode);
        txtSubName.setText(((String[]) classInfo)[1]);
        txtSubTime.setText(((String[]) classInfo)[2]);
        getTchName(((String[]) classInfo)[3]);

        tablayoutDashBoard.getTabAt(0).setText(classCode);



        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnDangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enroll();
            }
        });
    }
    public void enroll(){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EnrollActivity.this);
        builder.setMessage("Xác nhận đăng ký lớp "+classCode);
        builder.setTitle("Thông báo !");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db = FirebaseFirestore.getInstance();
                db.collection("class").document(classCode)
                        .update("student", FieldValue.arrayUnion(userInfo.getCode()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("CRE", classCode + " Dang ky thanh cong : " + userInfo.getCode());
                                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EnrollActivity.this);
                                builder.setMessage("Đăng ký lớp thành công  "+classCode);
                                builder.setTitle("Thông báo !");
                                builder.setCancelable(false);
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        Intent dashboard = new Intent(EnrollActivity.this, com.app.diemdanh.DashboardActivity.class);
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
                                Log.w("CRE", "Dang ky that bai ", e);
                                builder.setMessage("Đăng ký lớp thành công ");
                            }
                        });

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void getTchName(String tchCode){
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(tchCode+"@uit.edu.vn");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        txtTchName.setText(document.getData().get("full_name").toString());
                    } else {
                        Log.d("FIRE", "No such document");
                    }
                } else {
                    Log.d("FIRE", "get failed with ", task.getException());
                }
            }
        });
    }
}
