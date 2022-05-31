package com.app.diemdanh;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.app.model.userInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;



public class ProfileActivity extends Activity implements BottomNavigationView.OnNavigationItemSelectedListener{
    TextView txtFullname;
    TextView txtStdCode;
    TextView txtEmail;
    TextView txtDOB;
    TextView txtFaculty;
    TextView txtYearCode;
    TextView txtClass;
    TextView txtPhone;
    Button btnBack;
    EditText ipPhone;
    Button btnUpdate;

    public static BottomNavigationView mBtmView;
    private int mMenuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        txtFullname = (TextView) findViewById(R.id.txtFullname);
        txtStdCode = (TextView) findViewById(R.id.txtStdCode);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtDOB = (TextView) findViewById(R.id.txtDOB);
        txtFaculty = (TextView) findViewById(R.id.txtFaculty);
        txtYearCode = (TextView) findViewById(R.id.txtYearCode);
        txtClass = (TextView) findViewById(R.id.txtClass);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        btnUpdate = findViewById(R.id.buttonBot);

        txtFullname.setText(userInfo.getFull_name());
        txtStdCode.setText(userInfo.getCode() + "|"+ userInfo.getType());
        txtEmail.setText(userInfo.getUsername());
        txtDOB.setText((userInfo.getDob()));
        txtFaculty.setText(userInfo.getFaculty());
        txtYearCode.setText(userInfo.getYear_code());
        txtClass.setText(userInfo.getClass_code());
        txtPhone.setText(userInfo.getPhone());

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtmView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBtmView.setOnNavigationItemSelectedListener(this);
        mBtmView.getMenu().findItem(R.id.nav_score).setChecked(true);

        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getInputCode();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // uncheck the other items.
        mMenuId = item.getItemId();
        for (int i = 0; i < mBtmView.getMenu().size(); i++) {
            MenuItem menuItem = mBtmView.getMenu().getItem(i);
            boolean isChecked = menuItem.getItemId() == item.getItemId();
            menuItem.setChecked(isChecked);
        }

        switch (item.getItemId()) {
            case R.id.nav_home :{
                Intent profile = new Intent(ProfileActivity.this,DashboardActivity.class);
                startActivity(profile);
                mBtmView.getMenu().findItem(R.id.nav_home).setChecked(true);
                finish();
            }
            break;
            case R.id.nav_score: {
            }
            break;
        }
        return true;
    }

    FirebaseFirestore db;
    public void getInputCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập số điện thoại muốn thay đổi ");

        final EditText input = new EditText(this);
        input.setPadding(20, 10, 10, 20);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db = FirebaseFirestore.getInstance();
                db.collection("users").document(userInfo.getUsername())
                        .update("phone", input.getText()+"")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                txtPhone.setText("" + input.getText());
                                Toast.makeText(ProfileActivity.this,"Thay đổi thành công" , Toast.LENGTH_LONG).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("FIRE", "Error updating document", e);
                                Toast.makeText(ProfileActivity.this,"Không thể update số điện thoại" , Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }
}


