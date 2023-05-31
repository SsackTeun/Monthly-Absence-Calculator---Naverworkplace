package com.example.excelparser.util.date;

import com.example.excelparser.dto.HolidayAPIDTO;
import com.ibm.icu.util.ChineseCalendar;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return webClient;
    }

    public Set<String> holidayArray(String yyyy, String month) throws SSLException {
        holidaysSet.clear();

        HolidayAPIDTO holidayAPIDTO = webClient("http://apis.data.go.kr").get()
                .uri("/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?ServiceKey=DVcn3lMambqbUG2eCIroEmHMdcjD8IADQy/2+Q1nr7S23NymKJNSduAKjSFMnRKavNuUkGoD0ZyOgUC+B/jQ4g==&solYear="+ yyyy+ "&solMonth=" + month +"&_type=json")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new IllegalArgumentException("계정정보가 잘못 되었습니다")))
                .bodyToMono(HolidayAPIDTO.class)
                .block();

        holidayAPIDTO.getResponse().getBody().getItems().getItem().stream().map(item -> holidaysSet.add(item.getLocdate()));

        log.info("{}", holidayAPIDTO.toString());

        return holidaysSet;
    }
}

