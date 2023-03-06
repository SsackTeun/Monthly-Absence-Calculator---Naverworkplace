package com.example.excelparser.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MergeDTO {
    private String select;
    private String name;

    private String email;
    private String department;
    private List<String> duration;
    private float days;

    private String position;
    private int absenceTimes;

    private int workTimes;

    private int realWorkTimes;
}
