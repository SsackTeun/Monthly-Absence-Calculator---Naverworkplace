package com.example.excelparser.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.util.annotation.Nullable;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultBody {
    @JsonProperty(defaultValue = "")
    private Object items;
    private String numOfRows;
    private String pageNo;
    private String totalCount;
}
