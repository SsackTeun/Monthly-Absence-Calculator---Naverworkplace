package com.example.excelparser.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultBody {
    private Items items;
    private String numOfRows;
    private String pageNo;
    private String totalCount;
}
