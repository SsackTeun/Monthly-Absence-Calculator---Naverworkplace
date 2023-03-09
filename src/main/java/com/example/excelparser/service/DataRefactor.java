package com.example.excelparser.service;

import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.dto.origin.MergeOriginWithDurationDTO;
import com.example.excelparser.dto.origin.OriginDTO;

import java.io.IOException;
import java.util.List;

public interface DataRefactor {
    /**
     * 유리티리 클래스 제거를 위해 만든 서비스 인터페이스
     */

    //엑셀로 부터, 유저 리스트를 객체로 리턴 받음
    List<UserListDTO> getUsersFromExcel() throws IOException;

    //엑셀로 부터, 원본 데이터를 객체로 리턴 받음
    List<OriginDTO> getOriginDataFromExcel() throws IOException;

    //파싱
    List<MergeOriginWithDurationDTO> durationMerge() throws IOException;

    //원본 데이터에서 기간별 정렬

    //해당 정보로, startDate ~ endDate 사이의 정보를 가공
    /**
     * 이번달 - 이번달
     * 이번달 - 다음달
     * 토요일 제외
     * 일요일 제외
     * 법정 공휴일 제외 (법정 공휴일이 주말인지 구분)
     * 대체 휴일 제외
     */
    List<MergeOriginWithDurationDTO> divideEachMonth() throws IOException;

    List<MergeOriginWithDurationDTO> isHolidayCalculate(String years, String month) throws IOException;
}
