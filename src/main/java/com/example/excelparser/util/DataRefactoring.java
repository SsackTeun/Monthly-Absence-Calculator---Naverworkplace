package com.example.excelparser.util;

import com.example.excelparser.dto.MergeDTO;
import com.example.excelparser.dto.origin.OriginDTO;
import com.example.excelparser.dto.RefactorDTO;
import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.util.excel.ExcelParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataRefactoring {

    /* 엑셀 파싱 유틸리티 클래스 */
    private static ExcelParserUtil excelParserUtil;

    public DataRefactoring() {
        excelParserUtil = new ExcelParserUtil();
    }

    /** 유저풀 가져오기 **/
    public static List<UserListDTO> getAllUsers() throws IOException {
        return excelParserUtil.getUsers();
    }


    /** 원본데이터 가져오기 **/
    public static List<OriginDTO> origin() throws IOException {
        return excelParserUtil.readAbsenceTimeOffList();
    }



    /** refactoring
     * 02/09
     * 02/10 : 엑셀 시트에 없는 유저 추가하기
     * */
    public static List<RefactorDTO> refactor() throws IOException {
        /** 결과객체 **/
        List<RefactorDTO> refactorDTOList = new ArrayList<>();

        /** init 초기화 **/
        /** 모든 유저 아이디 : 이메일 : 직급 **/
        List<UserListDTO> allUsers = getAllUsers();
        allUsers.forEach(x -> {
            RefactorDTO refactorDTO = new RefactorDTO();
            refactorDTO.setEmail(x.getEmail());
            refactorDTO.setName(x.getUsername());
            refactorDTO.setPosition(x.getPosition());
            refactorDTO.setDuration(null);
            refactorDTOList.add(refactorDTO);
            log.info("이메일 목록 : {}", x.getEmail());
        });


        /** 부재일정사용리스트 **/
        List<OriginDTO> absenceList = origin();
        List<String> durations;
        for(int i = 0; i < refactorDTOList.size(); i++){
            //모든 유저 이메일로 찾는다.
            durations = new ArrayList<>();
            String emailInAllUsers = allUsers.get(i).getEmail();
            for(int j = 0; j < absenceList.size(); j++){
                // 부재를 사용한 유저
                String emailInAbsence = absenceList.get(j).getLoginId();
                if(emailInAbsence.equals(emailInAllUsers)){
                    //부재를 사용한 유저
                    durations.add(absenceList.get(j).getDurations());
                }
            }
            refactorDTOList.get(i).setDuration(durations);
        }
        return refactorDTOList;
    }

    /** refactoring
     * 02/09
     * */
    public static List<MergeDTO> merge() throws IOException {

        /** 가공한 데이터 가져오기
         * email (unique), name, department, duration
         * **/
        List<RefactorDTO> origin = refactor();

        /**
         * 1.원본에서 유니크키인 이메일을 추출
         * 2.중복제거후
         * 3.이메일로 다시 탐색
         * {
         *     email: "xx@onware.co.kr"
         *     name: "xxx"
         *     department: "xxxxx"
         *     durations: [
         *
         *     ]
         * }
         */

        /** 중복제거된 이메일 리스트 **/
        HashSet<String> emailDuplicationRemove = new HashSet<>();
        if(origin.size() != 0){
            for (int i = 0; i < origin.size(); i++){
                if(!origin.get(i).getEmail().equals("")){
                    emailDuplicationRemove.add(origin.get(i).getEmail());
                }
            }
        }

        log.info("emails size {}", emailDuplicationRemove);

        List<String> emails = emailDuplicationRemove.stream().collect(Collectors.toList());
        String email = null;

        List<MergeDTO> result = new ArrayList<>();
        MergeDTO mergeDTO = null;

        //duration "/" 기준으로 자르기
        List<String> parseDuration = null;
        List<String> temp = null;

        /** origin 에서 이메일 값이 동일한 것 찾아서 duration을 합쳐주기 **/
        if(origin.size() != 0){
            //중복제거된 이메일 리스트로 origin 을 검색
            for(int i = 0; i < emails.size(); i ++){
                email = emails.get(i);
                temp = new ArrayList<>();

                log.info("email:{}", email);
                mergeDTO = new MergeDTO();
                for(int j = 0; j < origin.size(); j++){
                    if(email.equals(origin.get(j).getEmail())){
                        //origin 과 email 이 같은 것을 찾아서 dto 에 넣기
                        mergeDTO.setName(origin.get(j).getName());
                        //mergeDTO.setDepartment(origin.get(j).getDepartment());
                        mergeDTO.setEmail(origin.get(j).getEmail());
                        mergeDTO.setPosition(origin.get(j).getPosition());
                        if(origin.get(j).getDuration() != null){
                            parseDuration = durationsSplit(origin.get(j).getDuration());
                            log.info("durations {}", origin.get(j).getDuration());
                            for(int k = 0; k < parseDuration.size(); k++){
                                temp.add(parseDuration.get(k));
                            }
                        }
                    }
                    mergeDTO.setDuration(temp);
                }
                result.add(mergeDTO);
            }
        }
        return result;
    }
    /** created at 02/09
     * "/" 단위로 휴가일자 자르기
     * */
    public static List<String> durationsSplit(List<String> duration){
        List<String> result = new ArrayList<>();
        String[] temp ;
        for(int i = 0; i < duration.size(); i++){
            temp = duration.get(i).split("/");

            if(temp.length > 1){
                for(int j = 0; j < temp.length; j++){
                    result.add(temp[j]);
                }
            }else{
                result.add(duration.get(i));
            }
        }

        log.info("xxxxx:::{}", duration);
        return result;
    }

    public static List<MergeDTO> divideMonth() throws IOException {
        List<MergeDTO> origin = merge(); // 원본데이터

        /**
         * 2. JSON 배열에서 각 유저별로 접근
         */
        for(int i = 0; i < origin.size(); i++){
            log.info("{}", origin.get(i).getName());
            List<String> durations =  origin.get(i).getDuration();
            /**
             * duration 순환
             */
            //step1 ~ step3
//            List<String> result = sliceMonth(durations);
//            Collections.sort(result);
            origin.get(i).setDuration(sliceMonth(durations).stream().collect(Collectors.toList()));
        }
        return origin;
    }

    public static List<String> sliceMonth(List<String> duration){

        /** 1. HashSet -> List **/
        List<String> refactor = duration;

        log.info("####{}", refactor.stream().collect(Collectors.toSet()));

        List<String> save =new ArrayList<>();;

        String startDate = null;
        String endDate = null;

        int size = refactor.size();

        /** 2. duration[] 접근 **/
        for(int i = 0; i < size; i++) {
            /** 데이터 형변환 :
             * 2023.01.30(종일) ~ 2023.02.02(종일) : before
             * 2023-01-30-종일 ~ 2023-02-02-종일 : after
             **/
            log.info("{} {}", refactor.get(i).split("~"));

            startDate = refactor.get(i).split("~")[0].trim();
            endDate = refactor.get(i).split("~")[1].trim();

            //log.info("2::{} {}", startDate, endDate);

            startDate = convertDateformat(startDate);
            endDate = convertDateformat(endDate);

            /**
             * 1. startDate :년월일타입
             * 1. endDate :년월일타입
             **/
            String sYear = startDate.split("-")[0];
            String sMonth = startDate.split("-")[1];
            String sDay = startDate.split("-")[2];
            String sType = startDate.split("-")[3];

            String eYear = endDate.split("-")[0];
            String eMonth = endDate.split("-")[1];
            String eDay = endDate.split("-")[2];
            String eType = endDate.split("-")[3];


            /** 시작년월 == 종료년월 **/
            if (sYear.equals(eYear) && sMonth.equals(eMonth)) {
                /** 가공 불필요 **/
                //log.info("3::{}", refactor.get(i));
                save.add(startDate + " ~ " + endDate);
            }

            /** 시작년월 != 종료년월 **/
            if (!sMonth.equals(eMonth)) {
                /** 시작일 ~ 시작일 말일 **/
                LocalDate beforeLdStart = LocalDate.of(
                        Integer.parseInt(sYear),
                        Integer.parseInt(sMonth),
                        Integer.parseInt(sDay));

                /** 해당월의 끝일 구하기 **/
                LocalDate beforeLdEnd = LocalDate.of(
                        Integer.parseInt(sYear),
                        Integer.parseInt(sMonth),
                        Integer.parseInt(sDay)); //sMonth 의 마지막날

                YearMonth ymEnd = YearMonth.from(beforeLdEnd);


                beforeLdEnd = LocalDate.of(
                        Integer.parseInt(sYear),
                        Integer.parseInt(sMonth),
                        Integer.parseInt(String.valueOf(ymEnd.atEndOfMonth().getDayOfMonth())));

                /** 해당월의 시작일 구하기 **/
                LocalDate afterLdStart = LocalDate.of(
                        Integer.parseInt(eYear),
                        Integer.parseInt(eMonth),
                        Integer.parseInt("1") //eMonth 의 시작일
                );

                LocalDate afterLdEnd = LocalDate.of(
                        Integer.parseInt(eYear),
                        Integer.parseInt(eMonth),
                        Integer.parseInt(eDay));


                /** 시작일 ~ 시작일월의 말일 **/
                //log.info("4::{} ~ {}", beforeLdStart + "-" + sType, beforeLdEnd + "-" + sType);

                /** 종료월의 시작 ~ 종료일 **/
                //log.info("5::{} ~ {}", afterLdStart + "-" + eType, afterLdEnd + "-" + eType);

                save.add(beforeLdStart + "-" + sType + " ~ " + beforeLdEnd + "-" + sType);
                save.add(afterLdStart + "-" + eType + " ~ " + afterLdEnd + "-" + eType);
            }

        }
        return save.stream().collect(Collectors.toList());
    }

    public static String convertDateformat(String date){
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);
        String type = date.substring(11, 13);

        return year+"-"+month+"-"+day+"-"+type;
    }

    public static List<MergeDTO> getEachMonthWithSelectMonth(String year, String month) throws IOException {
        List<MergeDTO> origin = divideMonth();
        log.info("{}", origin);

        List<MergeDTO> refactor = new ArrayList<>();

        for(int i = 0; i < origin.size(); i++){
            List<String> durations = origin.get(i).getDuration();
            MergeDTO mergeDTO = new MergeDTO();
            mergeDTO.setSelect(year+"-"+month);
            mergeDTO.setDepartment(origin.get(i).getDepartment());
            mergeDTO.setName(origin.get(i).getName());
            mergeDTO.setEmail(origin.get(i).getEmail());
            mergeDTO.setPosition(origin.get(i).getPosition());

            String collect = null;
            List<String> monthDurations = new ArrayList<>();
            for(int j = 0; j < durations.size(); j++){
                collect = selectWithYearMonth(year, month, durations.get(j));
                if(collect != null){
                    monthDurations.add(collect);
                    log.info("durations :: {}", durations);
                    log.info("collect :: {}", collect);
                }
            }
            mergeDTO.setDays(periodCalculator(monthDurations));
            mergeDTO.setWorkTimes(208);
            mergeDTO.setAbsenceTimes((int) (periodCalculator(monthDurations) * 8));
            mergeDTO.setRealWorkTimes(208 - mergeDTO.getAbsenceTimes());
            mergeDTO.setDuration(monthDurations);
            refactor.add(mergeDTO);
        }
        return refactor;
    }

    public static String selectWithYearMonth(String stdYear, String stdMonth, String duration){
        String start = duration.split("~")[0].trim();
        String end = duration.split("~")[1].trim();

        String sYear = start.split("-")[0].trim();
        String sMonth = start.split("-")[1].trim();

        String eYear = end.split("-")[0].trim();
        String eMonth = end.split("-")[1].trim();

        String result = null;
        //일치여부
        if(stdYear.equals(sYear) && stdMonth.equals(sMonth) && stdYear.equals(eYear) && stdMonth.equals(eMonth)) {
            result = duration;
        }
        return result;
    }

    public static float periodCalculator(List<String> durations){
        List<String> origin = durations;

        float result = 0f;

        String startDate;
        String endDate;

        String sYear;
        String sMonth;
        String sDay;
        String sType;

        String eYear;
        String eMonth;
        String eDay;
        String eType;

        for(int i = 0; i < origin.size(); i++){
            startDate = origin.get(i).split("~")[0].trim();
            endDate = origin.get(i).split("~")[1].trim();

            sYear = startDate.split("-")[0].trim();
            sMonth = startDate.split("-")[1].trim();
            sDay = startDate.split("-")[2].trim();
            sType = startDate.split("-")[3].trim();

            eYear = endDate.split("-")[0].trim();
            eMonth = endDate.split("-")[1].trim();
            eDay = endDate.split("-")[2].trim();
            eType = endDate.split("-")[3].trim();

            log.info("{} {}", startDate, endDate);
            log.info("{} {} {} {}", sYear, sMonth, sDay, sType);
            log.info("{} {} {} {}", eYear,eMonth, eDay, eType);


            LocalDate start = LocalDate.of(Integer.parseInt(sYear), Integer.parseInt(sMonth), Integer.parseInt(sDay));
            LocalDate end = LocalDate.of(Integer.parseInt(eYear), Integer.parseInt(eMonth), Integer.parseInt(eDay));

            Period period = Period.between(start, end);

            if(sType.equals("종일")){
                result += (period.getDays()+1) * 1.0f;
            }
            if(sType.equals("오전") || sType.equals("오후")){
                result += (period.getDays()+1) * 0.5f;
            }

            log.info("{}", result);
        }

        return result;
    }
}
