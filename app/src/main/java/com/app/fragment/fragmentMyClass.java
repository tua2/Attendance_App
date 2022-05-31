package com.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.app.diemdanh.R;
import java.util.ArrayList;

public class fragmentMyClass extends Fragment {
    ListView list;

    public fragmentMyClass() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
//        LinearLayout scrollLayout = (LinearLayout) view.findViewById(R.id.dash_fragment);


        return view;
    }
}
