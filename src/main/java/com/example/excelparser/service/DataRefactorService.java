package com.example.excelparser.service;

import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.dto.original.MergeOriginWithDurationDTO;
import com.example.excelparser.dto.original.OriginDTO;
import com.example.excelparser.util.excel.ExcelParserUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DataRefactorService implements DataRefactor{

    /**
     *
     * @return /data/list/list.xlsx 를 읽어  를 읽어 UserListDTO 형태로 저장하여, 리스트로 반환
     * @throws IOException
     */
    @Override
    public List<UserListDTO> getUsersFromExcel() throws IOException {
        return ExcelParserUtil.getUsers();
    }

    /**
     *
     * @return /data/absence/absence.xlsx 를 읽어 OriginDTO 형태로 저장하여, 리스트로 반환
     * @throws IOException
     */
    @Override
    public List<OriginDTO> getOriginDataFromExcel() throws IOException {
        return ExcelParserUtil.readAbsenceTimeOffList();
    }

    /**
     *
     * @return OriginDTO 리스트에서 데이터를 읽어서, 흩어진 기간데이터를 유저 단위로 합친다
     * @throws IOException
     */
    @Override
    public List<MergeOriginWithDurationDTO> durationMerge() throws IOException {

        //원본데이터
        List<OriginDTO> origin = getOriginDataFromExcel();
        List<UserListDTO> users = getUsersFromExcel();

        //날짜 데이터 변환 및 유저로그인 아이디 기준으로 병합
        return MergeOriginWithDurationDTO.convertTo(origin, users);
    }

    /**
     *
     * @return 기간이 합쳐진 MergeOriginWithDurationDTO 객체리스트에서 월별로 다시 기간을 나눈다.
     * @throws IOException
     */
    @Override
    public List<MergeOriginWithDurationDTO> divideEachMonth() throws IOException {
        //유저단위로 기간이 합쳐진 데이터
        List<MergeOriginWithDurationDTO> merges = durationMerge();
        return MergeOriginWithDurationDTO.compareMonth(merges);
    }

    @Override
    public List<MergeOriginWithDurationDTO> isHolidayCalculate(String years, String month) throws IOException {
        List<MergeOriginWithDurationDTO> merges = divideEachMonth();
        return MergeOriginWithDurationDTO.compareMonth(merges, years, month);
    }


}
