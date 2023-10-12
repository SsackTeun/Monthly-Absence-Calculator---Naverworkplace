package com.example.excelparser.dto.absence;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class MergeOriginWithDurationDTO{

    //이름
    private String name;

    //로그인 아이디
    private String loginId;

    //직책
    private String position;

    //기간
    private List<Duration> durations;

    //해당연도 모든 일자 리스팅
    private List<String> selectedYearInAllDates;

    //해당연도의 선택한 달 리스팅
    private SelectedMonth selectedMonth;

    /**
     * UTILITY
     * @Param origin (원본 데이터), @Param users (유저 원본데이터)
     */
    public static List<MergeOriginWithDurationDTO> convertTo(List<SourceExcelDataExtractorDTO> origin, List<UserListDTO> users){
        //결과리스트
        List<MergeOriginWithDurationDTO> durations = new ArrayList<>();
        MergeOriginWithDurationDTO duration = null;

        /**
         * 원본에서 꺼내서 새로 만들어낼 데이터에 값 넣기
         */
        for (SourceExcelDataExtractorDTO sourceExcelDataExtractorDTO : origin) {
            log.debug("{}", sourceExcelDataExtractorDTO);
            duration = new MergeOriginWithDurationDTO();
            duration.setLoginId(sourceExcelDataExtractorDTO.getLoginId());
            duration.setName(sourceExcelDataExtractorDTO.getName());
            duration.setPosition(sourceExcelDataExtractorDTO.getPosition());
            duration.setDurations(Duration.parseFromOrigin(sourceExcelDataExtractorDTO.getDurations()));
            durations.add(duration);
        }

        /**
         * 사본 데이터
         */
        List<MergeOriginWithDurationDTO> merges = new ArrayList<>();
        //유효한 유저 이름 기준으로 모든 데이터를 탐색
        //유효한 유저 이름
        for(UserListDTO user : users){
            if(!(user.getEmail().length() == 0)){
                MergeOriginWithDurationDTO merge = new MergeOriginWithDurationDTO();
                //유효한 유저 이름
                String email = user.getEmail();
                //모든 객체 접근하여 다시 해당 이메일과 같은 유저를 찾음
                List<Duration> mergeDuration = new ArrayList<>();
                for(MergeOriginWithDurationDTO m : durations){
                    if(m.getLoginId().equals(email)){
                        //해당 조건을 만족하면, 객체에 담기
                        merge.setName(m.getName());
                        merge.setPosition(m.getPosition());
                        merge.setLoginId(m.getLoginId());
                        mergeDuration.addAll(m.getDurations());
                    }
                    else{
                        merge.setName(user.getUsername());
                        merge.setDurations(null);
                        merge.setPosition(user.getPosition());
                        merge.setLoginId(user.getEmail());
                    }
                }
                merge.setDurations(mergeDuration);
                merges.add(merge);
            }
        }
        return merges;
    }

    public static List<MergeOriginWithDurationDTO> compareMonth(List<MergeOriginWithDurationDTO> merges){
        log.info("{}", merges);
        /**
         * Variable
         */
        List<MergeOriginWithDurationDTO> results = new ArrayList<>();

        //데이터 순환
        for (MergeOriginWithDurationDTO merge : merges) {
            //유저당 기간 데이터 접근
            log.info("{}", merge.getName());
            MergeOriginWithDurationDTO result = new MergeOriginWithDurationDTO();
            result.setName(merge.getName());
            result.setPosition(merge.getPosition());
            result.setLoginId(merge.getLoginId());
            result.setDurations(Duration.getListOfDuration(merge.getDurations()));
            results.add(result);
        }
        return results;
    }

    public static List<MergeOriginWithDurationDTO> compareMonth(List<MergeOriginWithDurationDTO> merges, String years, String month) throws SSLException, JsonProcessingException, UnsupportedEncodingException {
        log.info("{}", merges);
        /**
         * Variable
         */
        List<MergeOriginWithDurationDTO> results = new ArrayList<>();

        //데이터 순환
        for (MergeOriginWithDurationDTO merge : merges) {
            //유저당 기간 데이터 접근
            log.info("{}", merge.getName());
            MergeOriginWithDurationDTO result = new MergeOriginWithDurationDTO();
            result.setName(merge.getName());
            result.setPosition(merge.getPosition());
            result.setLoginId(merge.getLoginId());

            List<Duration> durationList = Duration.getListOfDuration(merge.getDurations(), years, month);
            result.setDurations(durationList);

            List<String> mergeDates = new ArrayList<>();
            for (Duration duration : result.getDurations()) {
                mergeDates.addAll(duration.getDates());
            }
            result.setSelectedYearInAllDates(mergeDates);
            result.setSelectedMonth(selectedMonth(mergeDates, month));
            results.add(result);
        }
        return results;
    }

    public static SelectedMonth selectedMonth(List<String> selectedYearInAllDates, String selectMonth){

        SelectedMonth sMonth = new SelectedMonth();
        List<String> monthes = new ArrayList<>();

        float days = 0f;

        for (String selectedYearInAllDate : selectedYearInAllDates) {

            String date = selectedYearInAllDate.split(":")[0];
            String year =date.split("\\.")[0];
            String month = date.split("\\.")[1];
            String day = date.split("\\.")[2];
            String type = selectedYearInAllDate.split(":")[1];

            if(month.equals(selectMonth)) {
                if(type.equals("종일")){
                    monthes.add(selectedYearInAllDate);
                    days += 1.0f*1;
                }
                else if(type.equals("오후") || type.equals("오전")){
                    monthes.add(selectedYearInAllDate);
                    days += 0.5f * 1;
                }
            }
        }
        sMonth.setMonth(selectMonth);
        sMonth.setDays(String.valueOf(days));
        sMonth.setDates(monthes);

        return sMonth;
    }
}
