package com.example.excelparser.dto.original;

import lombok.Data;

@Data
public class OriginDTO {
    //문서번호
    private String doc_num;

    //이름
    private String name;

    //로그인 아이디
    private String loginId;

    //직책
    private String position;

    //부재 항목
    private String absentCase;

    //일수
    private String days;

    //기간
    private String durations;

    //작성일
    private String requestDate;
}
