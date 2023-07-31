package com.example.excelparser.dto;

import com.example.excelparser.dto.origin.BodyDeserializer;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

    @JsonProperty(value = "body", defaultValue = "")
    //@JsonDeserialize(using = BodyDeserializer.class)
    private ResultBody body;
}
