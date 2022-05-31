package com.app.diemdanh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.model.Student;
import com.app.model.userInfo;

import com.app.model.getStudentList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.app.adapter.CustomListAdapter;
import com.app.diemdanh.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SessionListActivity extends Activity {
    FirebaseFirestore db;
    ListView listSession;
    Button btnBack;
    TabLayout tablayoutDashBoard;
    String classCode;

    String[] stdList = null;
    List<Student> stdListOfClass;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sessionlist);

        classCode = getIntent().getStringExtra("CLASS_CODE");

        listSession = findViewById(R.id.listStd);
        tablayoutDashBoard = findViewById(R.id.tablayoutDashBoard);
        btnBack = findViewById(R.id.btnBack);


        tablayoutDashBoard.getTabAt(0).setText(classCode);
        String [] sessionArr = new String[getStudentList.sessionCount-1];
        for (int i = 0 ; i< sessionArr.length; i++){
            sessionArr[i]= "Buổi "+(i+1);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.itemsimple , sessionArr);
        listSession.setAdapter(arrayAdapter);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listSession.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object obj = listSession.getItemAtPosition(position);
//                Toast.makeText(SessionListActivity.this, "Selected :" + " " + (position + 1), Toast.LENGTH_LONG).show();
                getStudentArr(classCode,position+1);
            }
        });

    }


    public void getStudentArr(final String classCode, final int session) {
        db = FirebaseFirestore.getInstance();
        db.collection("enroll").document(classCode).collection("std").document(session+"")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String [] strArr = getStudentList(document.getData().get("student").toString());
                                Log.d("CRE", "Danh sach diem danh co "+String.valueOf(strArr.length));
                                Intent detail = new Intent(SessionListActivity.this, com.app.diemdanh.SessionAttendDetail.class);
                                detail.putExtra("CLASS_CODE",classCode);
                                detail.putExtra("STD_ARR",(Serializable) strArr);
                                detail.putExtra("SESSION",session+"");
                                startActivity(detail);
                            } else {

                            }
                        } else {

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

}
