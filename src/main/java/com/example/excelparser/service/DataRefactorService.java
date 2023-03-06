package com.example.excelparser.service;

import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.dto.origin.MergeOriginWithDurationDTO;
import com.example.excelparser.dto.origin.OriginDTO;
import com.example.excelparser.util.DevDataRefactor;
import com.example.excelparser.util.ExcelParserUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataRefactorService implements DataRefactor{

    @Override
    public List<UserListDTO> getUsersFromExcel() throws IOException {
        return ExcelParserUtil.getUsers();
    }

    @Override
    public List<OriginDTO> getOriginDataFromExcel() throws IOException {
        return ExcelParserUtil.readAbsenceTimeOffList();
    }

    @Override
    public List<MergeOriginWithDurationDTO> getMergeWithDuration() throws IOException {

        //원본데이터
        List<OriginDTO> origin = getOriginDataFromExcel();
        List<UserListDTO> users = getUsersFromExcel();

        //날짜 데이터 변환 및 변환
        /**
         * 존재하는 유저 기준으로
         */
        List<MergeOriginWithDurationDTO> list = MergeOriginWithDurationDTO.convertTo(origin, users);
        return list;
    }
}
