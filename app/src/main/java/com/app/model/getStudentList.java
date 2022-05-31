package com.app.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class getStudentList {

    FirebaseFirestore db;
    String classCode;
    String[] stdList;

    public static List<Student> listStdForTeacher;
    public static ArrayList<String> listStdSession;

    public static int sessionCount = 0;


    public List<Student> getListStdForTeacher() {
        return listStdForTeacher;
    }


    public getStudentList(String classCode, String[] stdList) {
        this.classCode = classCode;
        this.stdList = stdList;

    }

    public void getListStudentData() {
        final List<Student> list = new ArrayList<Student>();


        String[] stdListCode = stdList;

        for (int i = 0; i < stdList.length; i++) {
            stdList[i] = stdList[i] + "@uit.edu.vn";
        }

        db = FirebaseFirestore.getInstance();
        // get Name of every student code in stdList
        for (int i = 0; i < stdList.length; i++) {
            db.collection("users").document(stdList[i])
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                final DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("FIRE", "DocumentSnapshot data: " + document.getData().get("full_name"));

                                    db.collection("enroll").document(classCode).collection("std")
                                            .whereArrayContains("student", document.getId().replace("@uit.edu.vn", ""))
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        ArrayList<String> sessionArr = new ArrayList<String>();
                                                        for (QueryDocumentSnapshot document2 : task.getResult()) {
                                                            Log.d("FIRE", "Class : ========= " + document2.getId() + " " + document.getData().get("full_name"));
                                                            sessionArr.add(document2.getId());
                                                        }
                                                        list.add(new Student(document.getData().get("full_name").toString(),
                                                                document.getId().replace("@uit.edu.vn", ""),
                                                                task.getResult().size(),sessionArr));
                                                        listStdForTeacher = list;
                                                    } else {
                                                        Log.w("FIRE", "Error getting documents.", task.getException());
                                                    }
                                                }
                                            });
                                } else {
//                            Log.d(TAG, "No such document");
                                }
                            } else {
//                        Log.d(TAG, "get failed with ", task.getException());
                            }
                        }

                    });
        }

//
//        db.collection("users").whereIn("code", Arrays.asList(stdList))
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (final QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("FIRE", "Student : ========= " + document.getId() + " " + document.getData().get("full_name"));
//                                // Get Attend count of each student
//                                db.collection("enroll").document(classCode).collection("std")
//                                        .whereArrayContains("student", document.getId().replace("@uit.edu.vn", ""))
//                                        .get()
//                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                            @Override
//                                            public void onComplete(Task<QuerySnapshot> task) {
//                                                if (task.isSuccessful()) {
//                                                   ArrayList<String> sessionArr = new ArrayList<String>();
//                                                    for (QueryDocumentSnapshot document2 : task.getResult()) {
//                                                        Log.d("FIRE", "Class : ========= " + document2.getId() + " " + document2.getData().get("full_name"));
//                                                        sessionArr.add(document2.getId());
//                                                    }
//                                                    list.add(new Student(document.getData().get("full_name").toString(),
//                                                            document.getId().replace("@uit.edu.vn", ""),
//                                                            task.getResult().size(),sessionArr));
//                                                    listStdForTeacher = list;
//                                                } else {
//                                                    Log.w("FIRE", "Error getting documents.", task.getException());
//                                                }
//                                            }
//                                        });
//                            }
//
//                        } else {
//                            Log.w("FIRE", "Error getting documents.", task.getException());
//                        }
//                    }
//                });


    }

    // for student
    public void getAttendSessionList(String stdCode){
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
                                Log.d("FIRE", "Class : ========= " + document.getId() + " " + document.getData().get("full_name"));
                                sessionArr.add(document.getId());
                            }
                            listStdSession = sessionArr;
                        } else {
                            Log.w("FIRE", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    public void getSessionCount(String classCode){
        db = FirebaseFirestore.getInstance();
        db.collection("enroll").document(classCode).collection("std")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            sessionCount = task.getResult().size();
                        } else {
                            Log.w("FIRE", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
