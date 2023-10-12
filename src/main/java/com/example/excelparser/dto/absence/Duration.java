package com.example.excelparser.dto.absence;

import com.example.excelparser.util.date.LunarCalendar;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@Slf4j
public class Duration {
    private String startDate;
    private String endDate;
    private float days;
    private List<String> dates;

    public static List<Duration> parseFromOrigin(String duration){

        /**
         * 여러 일정을 함께 등록한 경우 "/" 로 구분되는 것을 나눈다
         */
        String[] durations = duration.split("/");

        /**
         * 나눈 결과 N 개를 List<String> 형태로 변환한다.
         */
        List<String> durationslist = Arrays.stream(durations).collect(Collectors.toList());

        /**
         * durationslist index : 2023.02.27(종일) ~ 2023.02.27(종일)\
         * result : 2023.02.27(종일)
         */
        String regEx = "([0-9]{4}\\.[0-9]{2}\\.[0-9]{2})\\(([가-힣]{2})\\)[ \\t\\n\\x0B\\f\\r]~[ \\t\\n\\x0B\\f\\r]([0-9]{4}\\.[0-9]{2}\\.[0-9]{2})\\(([가-힣]{2})\\)";
        Pattern pattern = Pattern.compile(regEx);

        /**
         * Durations 객체에 값 할당
         */
        List<Duration> list = new ArrayList<>();
        durationslist.forEach(x -> {
            Matcher matcher = pattern.matcher(x.trim());
            Duration durationObj = null;
            while(matcher.find()){
                durationObj = new Duration();
                durationObj.setStartDate(matcher.group(1).concat(":"+ matcher.group(2)));
                durationObj.setEndDate(matcher.group(3).concat(":"+ matcher.group(4)));
            }
            list.add(durationObj);
        });
        return list;
    }

    /**
     * List<Duration> 에서 기간 데이터를 모두 탐색후, 월이 다른것은 분리 해준다.
     */
    public static List<Duration> getListOfDuration(List<Duration> durations) {

        /**
         * 기간을 다시 생성하여 반환
         */
        List<Duration> results = durations;
        results.forEach(x -> {
            log.info("result : {}", x);
        });
        /**
         * 정규식 사용
         */
        String regex = "([0-9]{4}\\.[0-9]{2}\\.[0-9]{2}):([가-힣]{2})";
        Pattern pattern = Pattern.compile(regex);



        /**
         * 객체에서 시작일, 끝일을 가져와서 정규식으로 찾는다.
         */
        for (Duration duration : results) {
            Matcher start = pattern.matcher(duration.getStartDate());
            Matcher end = pattern.matcher(duration.getEndDate());

            /**
             * 매치되는 것을 순환하여, 년 월 일 타입으로 분리한다.
             */
            while (start.find()) {
                if (end.find()) {
                    //log.info("Start {} {}", start.group(1), start.group(2));
                    //log.info("End {} {}", end.group(1), end.group(2));

                    String sYear = start.group(1).split("\\.")[0];
                    String sMonth = start.group(1).split("\\.")[1];
                    String sDay = start.group(1).split("\\.")[2];
                    String sType = start.group(2);

                    String eYear = end.group(1).split("\\.")[0];
                    String eMonth = end.group(1).split("\\.")[1];
                    String eDay = end.group(1).split("\\.")[2];
                    String eType = end.group(2);

                    //log.info("{}{}{}{}-{}{}{}{}", sYear, sMonth, sDay, sType, eYear, eMonth, eDay, eType);

                    LocalDate startDate = LocalDate.of(
                            Integer.parseInt(sYear),
                            Integer.parseInt(sMonth),
                            Integer.parseInt(sDay));

                    LocalDate endDate = LocalDate.of(
                            Integer.parseInt(eYear),
                            Integer.parseInt(eMonth),
                            Integer.parseInt(eDay));

                    Period period = Period.between(startDate, endDate);
                    //log.info("두 날짜 사이 간격 : {}", period.getDays() + 1);

                    //INDEX FIRST , INDEX LAST
                    //INDEX FIRST + 1 ~ INDEX LAST - 1 종일로 처리
                    List<String> date = new ArrayList<>();
                    for (int i = 0; i < period.getDays() + 1; i++) {

                        int ArrayCount = period.getDays() + 1;
                        //날짜가 3개 이상일 때


                        if (ArrayCount >= 3) {
                            //log.info("ArrayCount : {}", ArrayCount);
                            if (i == 0) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + sType)));
                            } else if (i == period.getDays()) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + eType)));
                            } else {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + "종일")));
                            }
                        }
                        if (ArrayCount == 1) {
                            //log.info("ArrayCount : {}", ArrayCount);
                            date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + sType)));
                        }
                        if (ArrayCount == 2) {
                            if (i == 0) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + sType)));
                            } else if (i == 1) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + eType)));
                            }
                        }
                        startDate = startDate.plusDays(1);
                    }
                    log.info("{}", date);
                    duration.setDates(date);
                }
            }
        }
        return results;
    }

    /**
     * List<Duration> 에서 기간 데이터를 모두 탐색후, 월이 다른것은 분리 해준다.
     * +@ years 년도 기준으로 공휴일, 토일, 대체 공휴일 제외하기
     */
    public static List<Duration> getListOfDuration(List<Duration> durations, String years, String month) throws SSLException, JsonProcessingException, UnsupportedEncodingException {

        /**
         * 기간을 다시 생성하여 반환
         */
        List<Duration> results = durations;

        /**
         * 정규식 사용
         */
        String regex = "([0-9]{4}\\.[0-9]{2}\\.[0-9]{2}):([가-힣]{2})";
        Pattern pattern = Pattern.compile(regex);

        /**
         * 객체에서 시작일, 끝일을 가져와서 정규식으로 찾는다.
         */
        for (Duration duration : results) {
            Matcher start = pattern.matcher(duration.getStartDate());
            Matcher end = pattern.matcher(duration.getEndDate());

            /**
             * 매치되는 것을 순환하여, 년 월 일 타입으로 분리한다.
             */
            while (start.find()) {
                if (end.find()) {
                    //log.info("Start {} {}", start.group(1), start.group(2));
                    //log.info("End {} {}", end.group(1), end.group(2));

                    String sYear = start.group(1).split("\\.")[0];
                    String sMonth = start.group(1).split("\\.")[1];
                    String sDay = start.group(1).split("\\.")[2];
                    String sType = start.group(2);

                    String eYear = end.group(1).split("\\.")[0];
                    String eMonth = end.group(1).split("\\.")[1];
                    String eDay = end.group(1).split("\\.")[2];
                    String eType = end.group(2);

                    //log.info("{}{}{}{}-{}{}{}{}", sYear, sMonth, sDay, sType, eYear, eMonth, eDay, eType);

                    LocalDate startDate = LocalDate.of(
                            Integer.parseInt(sYear),
                            Integer.parseInt(sMonth),
                            Integer.parseInt(sDay));

                    LocalDate endDate = LocalDate.of(
                            Integer.parseInt(eYear),
                            Integer.parseInt(eMonth),
                            Integer.parseInt(eDay));

                    Period period = Period.between(startDate, endDate);
                    //log.info("두 날짜 사이 간격 : {}", period.getDays() + 1);

                    //INDEX FIRST , INDEX LAST
                    //INDEX FIRST + 1 ~ INDEX LAST - 1 종일로 처리
                    List<String> date = new ArrayList<>();
                    for (int i = 0; i < period.getDays() + 1; i++) {

                        int ArrayCount = period.getDays() + 1;
                        //날짜가 3개 이상일 때


                        if (ArrayCount >= 3) {
                            //log.info("ArrayCount : {}", ArrayCount);
                            if (i == 0) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + sType)));
                            } else if (i == period.getDays()) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + eType)));
                            } else {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + "종일")));
                            }
                        }
                        if (ArrayCount == 1) {
                            //log.info("ArrayCount : {}", ArrayCount);
                            date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + sType)));
                        }
                        if (ArrayCount == 2) {
                            if (i == 0) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + sType)));
                            } else if (i == 1) {
                                date.add(startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + eType)));
                            }
                        }
                        startDate = startDate.plusDays(1);
                    }
                    log.info("{}", date);
                    duration.setDates(isHoliday(date, years, month));

                    float days = 0f;
                    for (String durationDate : duration.getDates()) {
                        String check = durationDate.split(":")[1];
                        if(check.equals("종일")){
                            days += 1.0f * 1;
                            duration.setDays(days);
                        }else if(check.equals("오후") || check.equals("오전")){
                            days += 0.5f * 1;
                            duration.setDays(days);
                        }
                    }
                }
            }
        }
        log.info("{}",results);
        return results;
    }

    private static List<String> isHoliday(List<String> dates, String years, String month1) throws SSLException, JsonProcessingException, UnsupportedEncodingException {

        log.info("dates : {}", dates);

        List<String> durations = new ArrayList<>();
        /**
         * Util class LunarCalendar 형식에 맞추기 위한 형변환
         */
        LunarCalendar lunarCalendar = new LunarCalendar();

        /**
         * 입력 받은 연도의 공휴일을 Set 으로 받아옴.
         */
        Set<String> localDate = lunarCalendar.holidayArray(years, month1);
        log.info("asasdfqwersdf {} " , localDate.toString());


        for (String date : dates) {
            /**
             * 기간, 타입
             */
            String duration = date.split(":")[0];
            String type = date.split(":")[1];

            int year = Integer.parseInt(duration.split("\\.")[0]);
            int month = Integer.parseInt(duration.split("\\.")[1]);
            int day= Integer.parseInt(duration.split("\\.")[2]);

            LocalDate ld = LocalDate.of(year,month,day);
            String yyyyMMdd = ld.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            /**
             * 1. 첫날 끝날은 무조건 유효 (시간 연속성이 없는 데이터는 네이버워크플레이스에서 따로 나누기 때문)
             * 2. 가장먼저, 토, 일 여부 체크 : LocalDate.getDayOfWeek() == 6 or 7
             * 3. 2에 해당하면, yyyy.MM.dd:주말
             * 4. 3에 해당하지 않는 다면, 법정 공휴일 체크
             * 5. 공휴일일 경우 yyyy.MM.dd:공휴일로 표기
             */
            int dayOfWeek = ld.getDayOfWeek().getValue();

            //선택한 년도
            if(years.equals(String.valueOf(ld.getYear()))){

                //해당일이 주말인지 체크
                if(dayOfWeek == 6 || dayOfWeek == 7) {
                    //주말에해당
                    durations.add(ld.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + "주말")));
                    log.info("{}", ld.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:" + "주말")));
                }else{
                    //평일
                    //대체공휴일인가?
                    if(localDate.contains(ld.format(DateTimeFormatter.ofPattern("yyyyMMdd")))){
                        //대체 공휴일
                        log.info("{}", "대체공휴일");
                        durations.add(ld.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:"+"대체공휴일")));
                    }else{
                        durations.add(ld.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:"+type)));
                        log.info("{}",ld.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:"+type)));
                    }
                }
            }
        }
        return durations;
    }
}


