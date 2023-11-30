package com.example.excelparser.util.date;

import com.example.excelparser.dto.spcdeinfoapi.RestDeInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LunarCalendar {

    static Set<String> holidaysSet = new HashSet<>();
    private static WebClient webClient;

    private static WebClient webClient(String baseUrl) throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return webClient;
    }

    public Set<String> holidayArray(String yyyy, String month) throws SSLException, JsonProcessingException, UnsupportedEncodingException {
        holidaysSet.clear();


        String decodeServiceKey = "DVcn3lMambqbUG2eCIroEmHMdcjD8IADQy/2+Q1nr7S23NymKJNSduAKjSFMnRKavNuUkGoD0ZyOgUC+B/jQ4g==";
        String encodeServiceKey = URLEncoder.encode(decodeServiceKey, "UTF-8");

        URI uri = UriComponentsBuilder.fromUriString("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
                .queryParam("serviceKey", encodeServiceKey)
                .queryParam("solYear", yyyy)
                .queryParam("numOfRows", 100)
                .queryParam("solMonth", month)
                .build(true)
                .toUri();

        RestDeInfoDTO restDeInfoDTO = WebClient.create()
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new IllegalArgumentException("계정정보가 잘못 되었습니다")))
                .bodyToMono(RestDeInfoDTO.class)
                .block();

        log.info("date : {}", restDeInfoDTO.toString());

//        Object items = HolidayAPIDTO.getResponse().getBody().getItems();
        Object items = restDeInfoDTO.getResponse().getBody().getItems();
        log.info("{}::::asdf", items.getClass().getTypeName());
        Set<String> holidaysSet = new HashSet<>();

        if (items instanceof LinkedHashMap) {
            log.info("asdf instance type : {}" , items.getClass());

            log.info("{}", ((LinkedHashMap<?, ?>) items).get("item"));
            ObjectMapper objectMapper = new ObjectMapper();

            Object items1 = objectMapper.convertValue(((LinkedHashMap<?, ?>) items).get("item"), new TypeReference<>() {});

            log.info("asdf  {}"  ,items1.getClass().getName());

            if(items1 instanceof ArrayList){
                log.info("{}", "1");
                ((ArrayList<?>) items1).forEach(x -> {
                    RestDeInfoDTO.Item i = objectMapper.convertValue(x, RestDeInfoDTO.Item.class);
                    holidaysSet.add(i.getLocdate());
                });


            }else if(items1 instanceof LinkedHashMap){
                //단일
                log.info("{}", "2");
                ((LinkedHashMap<?, ?>) items1).forEach((x, y) -> {
                    if(x.equals("locdate")){
                        holidaysSet.add(y.toString());
                    }
                });

            }
        }
        else if (items instanceof String) {
            log.info(" instance type : {}" , items.getClass());
            holidaysSet.add(null);
        }

        return holidaysSet;


    }

    /* 실근무일수 반환 */
    public int totalWorkingDay(String years,
                               String month) throws IOException {

        LunarCalendar lunarCalendar = new LunarCalendar();
        Set<String> localDate = lunarCalendar.holidayArray(years, month);


        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");

        /* 공휴일이면서, 주말에 낀 것 카운트 */
        AtomicInteger isHolidayAndWeekend = new AtomicInteger();

        localDate.stream().forEach(
                s -> {
                    if (s != null) {
                        //log.info("test: {}", LocalDate.parse(s, format).getDayOfWeek());
                        if (LocalDate.parse(s, format).getDayOfWeek().toString().equals("SATURDAY")
                                || LocalDate.parse(s, format).getDayOfWeek().toString().equals("SUNDAY")) {
                            isHolidayAndWeekend.getAndIncrement();
                        }
                    } else {
                        // s가 null인 경우에 대한 처리를 여기에 추가
                        // 예: log.warning("날짜가 null입니다.");
                    }
                }
        );

        /* 찐 공휴일 */
        int realHolidayCount = localDate.size() - isHolidayAndWeekend.get();
        log.info("찐 공휴일 = {해당월의 국경일 수} - {국경일이면서, 주말인 것} : {}",  localDate.size() - isHolidayAndWeekend.get());

        /* #### 해당 월의 총 일수를 구하고, 토요일과 일요일을 제외한 평일 수 구하기 */
        LocalDate firstDayOfMonth = LocalDate.of(Integer.parseInt(years), Integer.parseInt(month), 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        // 해당 월의 총 일수 계산
        int totalDaysMonth = lastDayOfMonth.getDayOfMonth();

        // 평일(토요일과 일요일 제외)의 일수 계산
        int weekdays = 0;
        LocalDate currentDate = firstDayOfMonth;
        while (!currentDate.isAfter(lastDayOfMonth)) {
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        System.out.println(years + "년 " + month + "월의 총 일수: " + totalDaysMonth);
        System.out.println("평일(토요일과 일요일 제외)의 일수: " + weekdays);

        int realWorkingDay = (weekdays - realHolidayCount);
        log.info("실 근로일수 : {}", realWorkingDay);

        return realWorkingDay;
    }
}

