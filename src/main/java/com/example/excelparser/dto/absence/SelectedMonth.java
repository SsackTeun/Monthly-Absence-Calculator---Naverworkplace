package com.example.excelparser.dto.absence;

import lombok.Data;

import java.util.List;

@Data
public class SelectedMonth {
    private String month;
    private String days;
    private List<String> dates;
    private List<String> sortedByMonth;
}
