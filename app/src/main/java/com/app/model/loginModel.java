package com.app.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;


import androidx.annotation.NonNull;

import com.app.diemdanh.DashboardActivity;
import com.app.diemdanh.LoginActivity;
import com.app.diemdanh.LoadingActivity;
import com.app.diemdanh.ProfileActivity;
import com.app.diemdanh.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class loginModel {
    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    private String txtUser = "";
    private String txtPassword = "";
    private Context cont1;

    public loginModel(Context context, String user, String password) {
        this.cont1 = context;
        this.txtUser = user;
        this.txtPassword = password;
    }
    public void updateUI(int int_case){
        switch (int_case){
            case 1 :
                Intent dashboard = new Intent(this.cont1, DashboardActivity.class);
                this.cont1.startActivity(dashboard);
                break;
            case 2 :
                Toast.makeText(this.cont1,"Incorrect username or password",Toast.LENGTH_LONG).show();
                Intent login = new Intent(cont1,LoginActivity.class);
                login.putExtra("PRE_USER",txtUser.replace("@uit.edu.vn",""));
                cont1.startActivity(login);
                break;
        }
    }
    public void firstLogin(final String username){
        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("users").document(username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("CRE", "user data: " + document.getData());

                        //get user info
                        final Map<String, Object> data = document.getData();
                        new userInfo(data);
                        updateUI(1);

                    } else {
                        Log.d("CRE", "Khong tim thay data " + username);
                        onFirstLogin(username);

                    }
                } else {
                    Log.d("CRE", "get failed with ", task.getException());
                }
            }
        });
    }
    public void onFirstLogin(String username){
        db = FirebaseFirestore.getInstance();

        final Map<String, Object> user = new HashMap<>();
        user.put("faculty", "");
        user.put("class", "");
        user.put("code", username);
        user.put("dob","");
        user.put("full_name","");
        user.put("phone","");
        user.put("year_code","");
        user.put("type","std");
        user.put("enroll",new ArrayList<>());

        db.collection("users").document(username)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", "UserInfo successfully written!");
                        new userInfo(user);
                        updateUI(1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Error writing document", e);
                    }
                });
    }

    public void login() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(this.txtUser, this.txtPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String user = mAuth.getCurrentUser().getEmail().toString();
                            Log.d("CREATION", " LOGIN SUCCESSFULLY "+ user);
                            firstLogin(user);

                        } else {
                            Log.d("CREATION", "LOGIN UNSUCCESSFULLY");
                            updateUI(2);
                        }

                    }
                });
    }
}
