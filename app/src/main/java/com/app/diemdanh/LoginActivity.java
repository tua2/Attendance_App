package com.app.diemdanh;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.app.model.loginModel;
import com.bumptech.glide.Glide;

public class LoginActivity extends Activity  {
    private String username = "";
    private String pass = "";

    Button btnLogin ;
    TextView inputUser ;
    TextView inputPass ;
    ImageView imgGif,imgBackg;

    loginModel loginmodel;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        inputUser = (TextView) findViewById(R.id.inputUser) ;
        inputPass = (TextView) findViewById(R.id.inputPassword) ;

        String userName = getIntent().getStringExtra("PRE_USER");
        inputUser.setText(userName);

        btnLogin.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputUser.getText().toString().trim().equalsIgnoreCase("")){
                    inputUser.setError("Nhập email");
                }else if(inputPass.getText().toString().trim().equalsIgnoreCase("")){
                    inputUser.setError("Nhập mật khẩu");
                }else{

                    username = inputUser.getText().toString() + "@uit.edu.vn";
                    pass = inputPass.getText().toString();
                    loginmodel = new loginModel(LoginActivity.this, username, pass);
                    loginmodel.login();

                    setContentView(R.layout.loadingpage);
                    imgGif = findViewById(R.id.imgGif);
                    imgBackg = findViewById(R.id.imgBack);
                    imgBackg.setMaxHeight(100);
                    imgBackg.setMaxWidth(100);
                    imgBackg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imgBackg.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));


                    Glide.with(LoginActivity.this).load(R.drawable.loadinggif).into(imgGif);
//                    Glide.with(LoginActivity.this).load(R.drawable.logoulwback).into(imgBackg);
                }


            }
        });

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);


    }


}

