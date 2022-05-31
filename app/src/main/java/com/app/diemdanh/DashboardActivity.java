package com.app.diemdanh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.app.adapter.BotNaviView;
import com.app.fragment.fragmentDashboardAdapter;
import com.app.model.userInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    LinearLayout scrollLayout;
    private fragmentDashboardAdapter mFragmentDashboardAdapter;
    private ViewPager mViewPager;

    Button btnOption,btnBack;

    List<Object> subList = new ArrayList<Object>();

    public static BottomNavigationView mBtmView;
    private int mMenuId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dasboard);
        scrollLayout = (LinearLayout) findViewById(R.id.scrollLayout);

        mFragmentDashboardAdapter = new fragmentDashboardAdapter(getSupportFragmentManager(), DashboardActivity.this);

        mViewPager = (ViewPager) findViewById(R.id.viewpage1);
        mViewPager.setAdapter(mFragmentDashboardAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayoutDashBoard);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_person);
        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);

        if (userInfo.getType().equals("st")) {
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_notifications_48);
            tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);

        }

        btnOption = findViewById(R.id.btnOption);
        if (userInfo.getType().equals("teacher")) {
            btnOption.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_24dp, 0, 0, 0);
        }

        mBtmView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBtmView.setOnNavigationItemSelectedListener(this);
        mBtmView.getMenu().findItem(R.id.nav_home).setChecked(true);
        BotNaviView.disableShiftMode(mBtmView);

        btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInfo.getType().equals("std")) {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(DashboardActivity.this);
                    builder.setTitle("Nhập mã lớp");

                    final EditText input = new EditText(DashboardActivity.this);
                    input.setPadding(20, 10, 10, 20);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getSearchClass(input.getText().toString().toUpperCase());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }else if (userInfo.getType().equals("teacher"))  {
                    Intent classActivity = new Intent(DashboardActivity.this, com.app.diemdanh.CreateClassActivity.class);
                    startActivity(classActivity);
                }
            }
        });

        btnBack= findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setMessage("Do you want to logout?");
                builder.setTitle("Alert!");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(DashboardActivity.this, com.app.diemdanh.LoginActivity.class);
                        intent.putExtra("finish", true);
                        intent.putExtra("PRE_USER",userInfo.getCode());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);


    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setMessage("Do you want to logout?");
        builder.setTitle("Alert!");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DashboardActivity.this, com.app.diemdanh.LoginActivity.class);
                intent.putExtra("finish", true);
                intent.putExtra("PRE_USER",userInfo.getCode());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

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
            }
            break;
            case R.id.nav_score: {
                Intent profile = new Intent(DashboardActivity.this, com.app.diemdanh.ProfileActivity.class);
                startActivity(profile);
            }

            break;
        }
        return true;
    }

    FirebaseFirestore db;

    public void getSearchClass(final String classCode) {
        db = FirebaseFirestore.getInstance();
        db.collection("class").document(classCode)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            Log.d("FIRE", "Class : ========= " + document.getId() + " " + document.getData());
                            try {
                                String time = "Thứ " + ((HashMap<String, Number>) document.getData().get("time")).get("day") + " : "
                                        + ((HashMap<String, Number>) document.getData().get("time")).get("time") + " Giờ, "
                                        + ((HashMap<String, Number>) document.getData().get("time")).get("duration") + " Tiết";

                                String[] obj = {document.getId()
                                        , (String) document.getData().get("sub_name")
                                        , time,(String) document.getData().get("teacher"),(String) document.getData().get("teacher")};
                                if(document.getData().get("student").toString().contains(userInfo.getCode())){
                                    Intent classActivity = new Intent(DashboardActivity.this, ClassActivity.class);
                                    classActivity.putExtra("CLASS_CODE", classCode);
                                    classActivity.putExtra("CLASS_INFO", (Serializable) obj);
                                    startActivity(classActivity);
                                }else {
                                    Intent classActivity = new Intent(DashboardActivity.this, EnrollActivity.class);
                                    classActivity.putExtra("CLASS_CODE", classCode);
                                    classActivity.putExtra("CLASS_INFO", (Serializable) obj);
                                    startActivity(classActivity);
                                }

                            } catch (Exception e) {

                            }


                        } else {
                            Log.w("FIRE", "Error getting documents.", task.getException());
                            Toast.makeText(DashboardActivity.this,"Không tìm tháy lớp",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


}
