package com.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.diemdanh.R;
import com.app.model.Student;
import com.app.model.getStudentList;

import java.util.List;

public class CustomListAdapter  extends BaseAdapter {

    private List<Student> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapter(Context aContext,  List<Student> listData) {
        this.context = aContext;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_layout, null);
            holder = new ViewHolder();
            holder.stdNameView = (TextView) convertView.findViewById(R.id.txtName);
            holder.stdCode = (TextView) convertView.findViewById(R.id.txtStdCode);
            holder.attendCount = (TextView) convertView.findViewById(R.id.txtAttendCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Student student = this.listData.get(position);
        holder.stdNameView.setText(student.getName());
        holder.stdCode.setText(student.getStdCode());
        holder.attendCount.setText("Chuyên cần : "+student.getAttendCount() +"/"+ (getStudentList.sessionCount-1));

        return convertView;
    }


    static class ViewHolder {
        TextView stdCode;
        TextView stdNameView;
        TextView attendCount;
    }

}
