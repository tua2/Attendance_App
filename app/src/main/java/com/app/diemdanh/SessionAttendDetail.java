package com.app.diemdanh;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;


import androidx.annotation.Nullable;

import com.app.model.Student;
import com.app.model.getStudentList;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.List;

public class SessionAttendDetail extends Activity {
    FirebaseFirestore db;
    ListView listSession;
    Button btnBack;
    TabLayout tablayoutDashBoard;
    String classCode;
    String session;

    String[] stdList = null;
    List<Student> stdListOfClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.session_attend_detail);

        classCode = getIntent().getStringExtra("CLASS_CODE");
        session = getIntent().getStringExtra("SESSION");

        listSession = findViewById(R.id.listStd);
        tablayoutDashBoard = findViewById(R.id.tablayoutDashBoard);
        btnBack = findViewById(R.id.btnBack);


        tablayoutDashBoard.getTabAt(0).setText(classCode +" Buổi "+session);
        String[] sessionArr = (String[]) getIntent().getSerializableExtra("STD_ARR");

        for(int i = 0 ; i< sessionArr.length ; i++){
            sessionArr[i] = (i+1)+") "+sessionArr[i];
        }
        if(sessionArr.length==0){
            sessionArr = new String[1];
            sessionArr[0] = "Không có sinh viên nào điểm danh";
//            Toast.makeText(SessionAttendDetail.this,"Không có học sinh điểm danh",Toast.LENGTH_LONG).show();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.itemsimple, sessionArr);
        listSession.setAdapter(arrayAdapter);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

}
