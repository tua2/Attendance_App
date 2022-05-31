package com.app.diemdanh;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import com.app.model.Student;
import com.app.model.getStudentList;
import com.app.model.userInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import static java.security.AccessController.getContext;

public class AttendDetailActivity extends Activity {
    String classCode,stdCode;
    FirebaseFirestore db;

    TableLayout tableLayout;
    TabLayout tablayoutDashBoard;
    TextView txtInfo;
    Button btnBack;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attend_detail);

        tableLayout = findViewById(R.id.tableLayout);
        txtInfo = findViewById(R.id.txtInfo);
        classCode = getIntent().getStringExtra("CLASS_CODE");
        stdCode = getIntent().getStringExtra("STD_CODE");


        tablayoutDashBoard = (TabLayout) findViewById(R.id.tablayoutDashBoard);
        tablayoutDashBoard.getTabAt(0).setText(classCode);
        btnBack = (Button) findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getAttendDetail();
    }

    public void getAttendDetail() {
        final TableRow.LayoutParams params1 = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        final TableRow.LayoutParams params2 = new TableRow.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#dbdbdb")); // Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(0);
        gd.setStroke(5, Color.WHITE);

        final GradientDrawable gd2 = new GradientDrawable();
        gd2.setColor(Color.parseColor("#3fb5ff")); // Changes this drawbale to use a single color instead of a gradient
        gd2.setCornerRadius(0);
        gd2.setStroke(5, Color.WHITE);


        db = FirebaseFirestore.getInstance();
        db.collection("enroll").document(classCode).collection("std")
                .whereArrayContains("student", stdCode)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> sessionArr = new ArrayList<String>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FIRE", "Class : ========= " + document.getId());
                                sessionArr.add(document.getId());
                                txtInfo.setText("Chuyên cần : "+sessionArr.size() +"/"+ (getStudentList.sessionCount-1));
                            }
                            int index = 1;
                            for (int i = 0; i < (getStudentList.sessionCount / 5) + 1; i++) {
                                TableRow row = new TableRow(AttendDetailActivity.this);
                                for (int i2 = 1; i2 <= 5; i2++) {
                                    TextView txt = new TextView(AttendDetailActivity.this);
                                    txt.setPadding(20, 40, 20, 40);
                                    txt.setLayoutParams(params1);
                                    txt.setTextSize(18);
                                    txt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    if (index < getStudentList.sessionCount) {
                                        txt.setText(refact(index++) + "");
                                        txt.setBackground(gd);
                                        if(sessionArr.contains((index-1) +"")){
                                            txt.setBackground(gd2);
                                        }
                                    }
                                    row.addView(txt);
                                }
                                row.setLayoutParams(params2);
                                tableLayout.addView(row);
                            }

                        } else {
                            Log.w("FIRE", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    public String refact(int a) {
        if(a<10){
            return "0"+a;
        }else return a+"";
    }

}
