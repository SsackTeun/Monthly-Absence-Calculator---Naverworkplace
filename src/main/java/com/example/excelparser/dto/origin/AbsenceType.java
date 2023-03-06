package com.example.excelparser.dto.origin;

public enum AbsenceType{
    DAY("종일"),
    AM("오전"),
    PM ("오후");

    private final String value;

    AbsenceType(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
