package com.app.diemdanh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.app.model.Student;
import com.app.model.getStudentList;
import com.app.model.userInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClassActivity extends Activity implements BottomNavigationView.OnNavigationItemSelectedListener{

    FirebaseFirestore db;
    TextView txtSubCode, txtTittle, txtSubName, txtSubTime, txtTchName, txtStdCount;
    Button btnDiemdanh, btnBack, btnDetail;
    TabLayout tablayoutDashBoard;

    String[] stdList = null;
    String classCode;

    ConstraintLayout txtStudentList;

    getStudentList getStdInfo;

    static List<Student> stdListOfClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        classCode = getIntent().getStringExtra("CLASS_CODE");
        Object classInfo = (Object) getIntent().getSerializableExtra("CLASS_INFO");

        setContentView(R.layout.thongtinmonhoc);

        txtSubCode = (TextView) findViewById(R.id.txtSubCode);
        txtTittle = (TextView) findViewById(R.id.txtTittle);
        txtSubName = (TextView) findViewById(R.id.txtSubname);
        txtSubTime = (TextView) findViewById(R.id.txtSubTime);
        txtTchName = (TextView) findViewById(R.id.txtTchName);
        txtStdCount = (TextView) findViewById(R.id.txtStdCount);
        btnDiemdanh = (Button) findViewById(R.id.btnMolop);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnDetail = findViewById(R.id.btnDetail);

        tablayoutDashBoard = (TabLayout) findViewById(R.id.tablayoutDashBoard);
        txtStudentList = findViewById(R.id.txtStudentList);


        txtSubCode.setText(classCode);
        txtSubName.setText(((String[]) classInfo)[1]);
        txtSubTime.setText(((String[]) classInfo)[2]);

        tablayoutDashBoard.getTabAt(0).setText(classCode);

        mBtmView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBtmView.setOnNavigationItemSelectedListener(this);
        mBtmView.getMenu().findItem(R.id.nav_score).setChecked(false);
        mBtmView.getMenu().findItem(R.id.nav_home).setChecked(false);

        // If teacher
        if (userInfo.getType().equals("teacher")) {
            getTchName(userInfo.getCode());
            getStdInfo = new getStudentList(classCode, stdList);
            getStdInfo.getSessionCount(classCode);

            getStudentArr(classCode);

            checkClass(classCode);

        } else if (userInfo.getType().equals("std")) {
            new getStudentList(classCode, stdList).getAttendSessionList(userInfo.getCode());
            getStudentList getSessionInfo = new getStudentList(classCode, stdList);
            getSessionInfo.getAttendSessionList(userInfo.getCode());
            getSessionInfo.getSessionCount(classCode);
            txtStdCount.setText("Danh sách điểm danh ");
            getTchName(((String[]) classInfo)[4]);
        }

        btnDiemdanh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInfo.getType().equals("std")) {
                    Intent attend = new Intent(ClassActivity.this, com.app.diemdanh.AttendActivity.class);
                    attend.putExtra("CLASS_CODE", classCode);
                    startActivity(attend);
                } else if (userInfo.getType().equals("teacher")) {
                    Intent attendCre = new Intent(ClassActivity.this, com.app.diemdanh.AttendCreActivity.class);
                    attendCre.putExtra("CLASS_CODE", classCode);
                    startActivity(attendCre);
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtStudentList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInfo.getType().equals("std") && getStudentList.listStdSession != null) {
                    Log.d("CRE", String.valueOf(getStudentList.listStdSession));
                    Intent intent = new Intent(ClassActivity.this, com.app.diemdanh.AttendDetailActivity.class);
                    intent.putExtra("CLASS_CODE", classCode);
                    intent.putExtra("STD_CODE", userInfo.getCode());
                    startActivity(intent);
                } else if (userInfo.getType().equals("teacher") && getStudentList.listStdForTeacher != null) {
//                    Log.d("FIRE","Lop co "+getStudentList.listStdForTeacher.size() + " sinh vien" + getStudentList.listStdForTeacher.toString());
                    Intent intent = new Intent(ClassActivity.this, com.app.diemdanh.StudentListActivity.class);
                    intent.putExtra("CLASS_CODE", classCode);
                    startActivity(intent);
                }
            }
        });

        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInfo.getType().equals("std") && getStudentList.listStdSession != null) {
                    Log.d("CRE", String.valueOf(getStudentList.listStdSession));
                    Intent intent = new Intent(ClassActivity.this, com.app.diemdanh.AttendDetailActivity.class);
                    intent.putExtra("CLASS_CODE", classCode);
                    intent.putExtra("STD_CODE", userInfo.getCode());
                    startActivity(intent);
                } else if (userInfo.getType().equals("teacher") && getStudentList.listStdForTeacher != null) {
                    Intent intent = new Intent(ClassActivity.this, com.app.diemdanh.SessionListActivity.class);
                    intent.putExtra("CLASS_CODE", classCode);
                    startActivity(intent);
                }
            }
        });
    }

    public String[] getStudentList(String str) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] strArr = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                strArr[i] = jsonArray.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return strArr;
    }

    public void checkClass(final String classCode) {
        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("enroll").document(classCode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("CRE", "class enroll data: " + document.getData());

//                        updateUI(1);

                    } else {
                        Log.d("CRE", "Khong tim thay enroll data " + classCode);
                        createEnrollClass(classCode);
                    }
                } else {
                    Log.d("CRE", "get failed with ", task.getException());
                }
            }
        });
    }

    public void createEnrollClass(final String classCode) {
        db = FirebaseFirestore.getInstance();

        // Add session data
        final Map<String, Object> enrollData = new HashMap<>();
        enrollData.put("session", 0);
        enrollData.put("sessionCount", 0);

        db.collection("enroll").document(classCode)
                .set(enrollData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", classCode + " Enroll Class successfully written!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Error writing document", e);
                    }
                });

        // Add enroll class student info

//        Map<String, Object> student_info = new HashMap<>();
//        student_info.put("code","khoideptraitest");
//        student_info.put("location","10.0001010,101.010923");

        Map<String, Object> std_attend = new HashMap<>();
//        std_attend.put("17520650",student_info);

        final Map<String, Object> sessionDataStd = new HashMap<>();
        sessionDataStd.put("std_attend", std_attend);
        sessionDataStd.put("student", new ArrayList<>());

        db.collection("enroll").document(classCode).collection("std").document("0")
                .set(sessionDataStd)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", classCode + " Enroll Class successfully written!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Error writing document", e);
                    }
                });

        final Map<String, Object> sessionDataTch = new HashMap<>();
        sessionDataTch.put("code", "hoang18520784test");
        sessionDataTch.put("day", "05/06/2021");
        sessionDataTch.put("inactive", true);
        sessionDataTch.put("location", "10.1231451,102.32342621");

        db.collection("enroll").document(classCode).collection("tch").document("0")
                .set(sessionDataTch)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", classCode + " Enroll Class successfully written!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Error writing document", e);
                    }
                });

    }


    public void getStudentArr(final String classCode) {

        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("class").document(classCode);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FIRE", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Object obj = snapshot.getData().get("student");
                    Log.d("FIRE", "Current data: " + snapshot.getData().get("student"));

                    String[] stdList = getStudentList(obj.toString());
                    txtStdCount.setText("Số lượng sinh viên : " + stdList.length);

                    getStdInfo = new getStudentList(classCode, stdList);
                    getStdInfo.getListStudentData();

                } else {
                    Log.d("FIRE", "Current data: null");
                }
            }
        });
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
    public static BottomNavigationView mBtmView;
    private int mMenuId;
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // uncheck the other items.
        mMenuId = item.getItemId();
//        for (int i = 0; i < mBtmView.getMenu().size(); i++) {
//            MenuItem menuItem = mBtmView.getMenu().getItem(i);
//            boolean isChecked = menuItem.getItemId() == item.getItemId();
//            menuItem.setChecked(isChecked);
//        }

        switch (item.getItemId()) {
            case R.id.nav_home: {
                Intent profile = new Intent(ClassActivity.this, com.app.diemdanh.DashboardActivity.class);
                startActivity(profile);
            }
            break;
            case R.id.nav_score: {
                Intent profile = new Intent(ClassActivity.this, com.app.diemdanh.ProfileActivity.class);
                startActivity(profile);
            }

            break;
        }
        return true;
    }
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        getStudentArr(classCode);
    }
}
