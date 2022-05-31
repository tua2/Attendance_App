package com.app.model;

import java.util.ArrayList;

public class Student {
    private String name ;

    private String stdCode;
    private int attendCount = 0;

    public Student(String name, String stdCode, int attendCount, ArrayList<String> sessionArr) {


        if(name.equals("")){
            this.name = "Họ và Tên";
        }else this.name = name ;

        this.stdCode = stdCode;
        this.attendCount = attendCount;
    }

    public int getAttendCount() {
        return attendCount;
    }

    public void setAttendCount(int attendCount) {
        this.attendCount = attendCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStdCode() {
        return stdCode;
    }

    public void setStdCode(String stdCode) {
        this.stdCode = stdCode;
    }

}
