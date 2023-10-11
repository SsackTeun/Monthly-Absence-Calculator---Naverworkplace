package com.example.excelparser.service;

import com.example.excelparser.dto.MergeDTO;
import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.dto.original.MergeOriginWithDurationDTO;
import com.example.excelparser.dto.original.SourceExcelDataExtractorDTO;
import com.example.excelparser.util.excel.ExcelCreation;
import com.example.excelparser.util.excel.ExcelParserUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AbsenceCalculatorService implements AbsenceCalculator {
    @Override
    public void isHolidayCalculate(
            String years,
            String month,
            HttpServletResponse response) throws IOException {
        // 1. absence.xlsx 파일 읽기 - ExcelParserUtil.java readAbsenceTimeOffList()
        List<SourceExcelDataExtractorDTO> absence = ExcelParserUtil.readAbsenceTimeOffList();

        // 2. list.xlsx 파일 읽기 - ExcelParserUtil.java getUsers()
        List<UserListDTO> userList = ExcelParserUtil.getUsers();

        // 3. 날짜 데이터 변환 &  워크플레이스 아이디 기준으로 병합
        List<MergeOriginWithDurationDTO> merged = MergeOriginWithDurationDTO.convertTo(absence, userList);
        
        // 4. 3번 결과에서 월이 넘어가는 데이터가 있다면, 월을 나누는 작업
        List<MergeOriginWithDurationDTO> reform = MergeOriginWithDurationDTO.compareMonth(merged);
        
        // 5. 4번 결과에서 다시 년도와 월을 선택
        List<MergeOriginWithDurationDTO> result = MergeOriginWithDurationDTO.compareMonth(reform, years, month);
        
        // 6. 5번 결과 데이터를 토대로 엑셀 파일을 생성
        new ExcelCreation().createFile(response, MergeDTO.convert(result), years, month);
    }
}
