package com.app.diemdanh;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.app.adapter.CustomListAdapter;
import com.app.model.Student;
import com.app.diemdanh.R;
import com.app.model.userInfo;
import com.app.model.getStudentList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

public class StudentListActivity extends Activity {
    FirebaseFirestore db;
    ListView listStd;
    Button btnBack, btnExport;
    TabLayout tablayoutDashBoard;
    String classCode;

    String[] stdList = null;
    List<Student> stdListOfClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        classCode = getIntent().getStringExtra("CLASS_CODE");

        setContentView(R.layout.stdlist);


        listStd = findViewById(R.id.listStd);
        tablayoutDashBoard = findViewById(R.id.tablayoutDashBoard);
        btnBack = findViewById(R.id.btnBack);


        tablayoutDashBoard.getTabAt(0).setText(classCode);

        try {
            getdataFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listStd.setAdapter(new CustomListAdapter(StudentListActivity.this, getStudentList.listStdForTeacher));

        for (int i = 0; i < getStudentList.listStdForTeacher.size(); i++) {
            Log.d("CRE", getStudentList.listStdForTeacher.get(i).getStdCode());
        }
//        getListData();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listStd.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object obj = listStd.getItemAtPosition(position);
                Student std = (Student) obj;
//                Toast.makeText(StudentListActivity.this, "Selected :" + " " + std, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(StudentListActivity.this, AttendDetailActivity.class);
                intent.putExtra("CLASS_CODE", classCode);
                intent.putExtra("STD_CODE", std.getStdCode());
                startActivity(intent);
            }
        });
        btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v("CRE", "Permission is granted");
                    //File write logic here
                } else {
                    ActivityCompat.requestPermissions(StudentListActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }

                try {
                    csvWrite(header, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    final ArrayList<String> header = new ArrayList<String>();
    final ArrayList<String>[] data = new ArrayList[getStudentList.listStdForTeacher.size()];

    public void getdataFile() throws IOException {

        header.add("MSSV");

        for(int i = 1 ; i<= getStudentList.sessionCount;i ++){
            header.add("Buoi "+ i );
        }

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
//                    Log.d("FIRE", "Current data: " + snapshot.getData().get("student"));

                    String[] stdList = getStudentList(obj.toString());

                    for (int i = 0; i < stdList.length; i++) {

                        final ArrayList<String> row = new ArrayList<String>();

                        row.add(stdList[i]);

                        final int finalI = i;
                        db.collection("enroll").document(classCode).collection("std")
                                .whereArrayContains("student", stdList[i])
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            ArrayList<String> sessionArr = new ArrayList<String>();

                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                sessionArr.add(document.getId());

                                            }

                                            for (int i = 1; i < getStudentList.sessionCount; i++) {
                                                if (sessionArr.contains(i + "")) {
                                                    row.add(" Yes");
                                                } else row.add(" No");
                                            }
//                                            Log.w("FIRE", row + "");
                                            try{
                                                data[finalI] = row;
                                            }catch (Exception e){

                                            }

                                        } else {
                                            Log.w("FIRE", "Error getting documents.", task.getException());
                                        }
                                    }
                                });
                    }
                    btnExport.setVisibility(View.VISIBLE);
                    btnExport.setEnabled(true);
                } else {
                    Log.d("FIRE", "Current data: null");
                }
            }
        });


    }

    public void csvWrite(ArrayList<String> header, ArrayList<String>[] data) throws IOException {

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = classCode+".csv";
        String filePath = baseDir + File.separator +"UITLog_" +fileName;
        File f = new File(filePath);
        final CSVWriter writer;

        // File exist
        if (f.exists() && !f.isDirectory()) {
            FileWriter mFileWriter = new FileWriter(filePath, false);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filePath));
        }


//        Log.d("CRE", String.valueOf(header));

//        for (ArrayList<String> d : data) {
//            Log.d("CRE", String.valueOf(d));
//        }
        writer.writeNext(new String[]{"Danh sach diem danh lop "+classCode});
        writer.writeNext(header.toArray(new String[header.size()]));
        for (int i = 0; i < data.length; i++){
            writer.writeNext(data[i].toArray(new String[header.size()]));
        }

        writer.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(StudentListActivity.this);
        builder.setMessage("Export file thành công ");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

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
