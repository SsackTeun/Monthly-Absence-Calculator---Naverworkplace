package com.example.excelparser.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HolidayDto {
    @JsonProperty("header")
    private ResultHeader resultHeader;

    @JsonProperty("body")
    private ResultBody body;
}
