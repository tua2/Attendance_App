package com.app.diemdanh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class Notification extends AppCompatActivity {
    ListView listView;
    private static final String TAG = "Notification";
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        //Log.d(TAG, "onCreate: Setting up");
        setContentView(R.layout.notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView= (ListView)findViewById(R.id.listview);
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("android");
        arrayList.add("id");
        arrayList.add("123");
        arrayList.add("android");
        arrayList.add("id");
        arrayList.add("123");
        arrayList.add("android");
        arrayList.add("id");
        arrayList.add("123");
        arrayList.add("android");
        arrayList.add("id");
        arrayList.add("123");
        arrayList.add("android");
        arrayList.add("id");
        arrayList.add("123");
        arrayList.add("android");
        arrayList.add("id");
        arrayList.add("123");
        arrayList.add("android");
        arrayList.add("id");
        arrayList.add("123");
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayList);
        listView.setAdapter(arrayAdapter);
        Button closebutton =  (Button) findViewById(R.id.clear);
        closebutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Notification.this, com.app.diemdanh.ProfileActivity.class);
                startActivity(intent);


            }
        });


    }
    public void setSupportActionBar(Toolbar toolbar) {
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);



    }


}
