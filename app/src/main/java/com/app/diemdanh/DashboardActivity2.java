package com.app.diemdanh;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;
import java.util.List;

public class DashboardActivity2 extends AppCompatActivity {

    ConstraintLayout cardView, cardView1;
    LinearLayout scrollLayout;
    //    TabLayout tablayoutDashBoard;
    ViewPager viewPager1;
    String[] SubCode = {"IT0010.HTCL", "IT0011.HTCL","IT0012.HTCL","IT0013.HTCL","IT0014.HTCL"};

    List<Object> subList = new ArrayList<Object>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dasboard2);
        scrollLayout = (LinearLayout) findViewById(R.id.scrollLayout);

//        tablayoutDashBoard = (TabLayout) findViewById(R.id.tablayoutDashBoard);
        viewPager1 = (ViewPager) findViewById(R.id.viewpage1);

//        tablayoutDashBoard.setupWithViewPager(viewPager1);


//        getData();
//        addSubject();


    }


    public void getData(){
        String [] obj1 = {"IT0010.HTCL","Android","T2-13h00"};
        subList.add(obj1);
        String [] obj2 = {"IT0012.HTCL","Datamining","T2-13h00"};
        subList.add(obj2);
        String [] obj3 = {"IT0011.HTCL","DataBase","T2-13h00"};
        subList.add(obj3);
        String [] obj4 = {"IT0016.HTCL","Web","T2-13h00"};
        subList.add(obj4);
        subList.add(obj1);

    }

    public void addSubject(){
        for(int index = 0; index < subList.size(); index++) {

            LinearLayout newCardView = new LinearLayout(this);

            newCardView.setPadding(20,30,0,0);
            newCardView.setOrientation(LinearLayout.VERTICAL);


            if (index % 2 == 0) {
                newCardView.setBackgroundResource(R.drawable.rounded_corner);
            } else {
                newCardView.setBackgroundResource(R.drawable.rounded_corner2);
            }



            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.height = 250;
            params.setMargins(50,10,50,10);
            newCardView.setLayoutParams(params);

            final TextView txtCode = new TextView(this);
            txtCode.setText(((String[])subList.get(index))[0]);
            txtCode.setPadding(20,10,10,10);
            txtCode.setTextColor(Color.WHITE);
            txtCode.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);

            final TextView txtName = new TextView(this);
            txtName.setText(((String[])subList.get(index))[1]);
            txtName.setPadding(20,10,10,10);
            txtName.setTextColor(Color.WHITE);

            final TextView txtTime = new TextView(this);
            txtTime.setText(((String[])subList.get(index))[2]);
            txtTime.setPadding(20,10,30,0);
            txtTime.setTextColor(Color.WHITE);
            txtTime.setGravity(Gravity.RIGHT);

            LinearLayout.LayoutParams dimTxt = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,10,10,10);

            txtCode.setLayoutParams(dimTxt);
            txtName.setLayoutParams(dimTxt);

            newCardView.addView(txtCode,0);
            newCardView.addView(txtName,1);
            newCardView.addView(txtTime,2);

            newCardView.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v) {
                    View nextChild = ((ViewGroup)v).getChildAt(0);
                    String text = ((TextView) nextChild).getText().toString();
                    Toast.makeText(DashboardActivity2.this,"click " + text,Toast.LENGTH_LONG).show();
                }

            });

            scrollLayout.addView(newCardView);
        }
    }


}
