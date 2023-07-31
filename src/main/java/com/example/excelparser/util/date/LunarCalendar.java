package com.example.excelparser.util.date;

import com.example.excelparser.dto.HolidayAPIDTO;
import com.example.excelparser.dto.Item;
import com.example.excelparser.dto.Items;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ChineseCalendar;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.iterators.ArrayListIterator;
import org.apache.poi.openxml4j.opc.internal.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        HolidayAPIDTO holidayAPIDTO = WebClient.create()
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new IllegalArgumentException("계정정보가 잘못 되었습니다")))
                .bodyToMono(HolidayAPIDTO.class)
                .block();

        log.info("date : {}", holidayAPIDTO.toString());

        Object items = holidayAPIDTO.getResponse().getBody().getItems();

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
                    Item i = objectMapper.convertValue(x, Item.class);
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
}

