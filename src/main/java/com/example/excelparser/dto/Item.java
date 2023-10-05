package com.example.excelparser.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Item {
    private String dateKind;
    private String dateName;
    private String isHoliday;
    private String locdate;
    private String seq;
}
