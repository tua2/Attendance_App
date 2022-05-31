package com.app.diemdanh;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.app.model.Student;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ListSessionActivity extends Activity {
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


        listSession = findViewById(R.id.listStd);
        tablayoutDashBoard = findViewById(R.id.tablayoutDashBoard);
        btnBack = findViewById(R.id.btnBack);


        tablayoutDashBoard.getTabAt(0).setText(classCode);

        String [] a = {"1","2","3"};

//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , a);
//        listSession.setAdapter(arrayAdapter);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        listStd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
//                Object obj = listStd.getItemAtPosition(position);
//                Student std = (Student) obj;
//                Toast.makeText(SessionListActivity.this, "Selected :" + " " + std, Toast.LENGTH_LONG).show();
//            }
//        });

    }
}
