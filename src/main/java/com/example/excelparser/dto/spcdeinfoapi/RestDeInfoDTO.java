package com.example.excelparser.dto.spcdeinfoapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestDeInfoDTO{
    private Response response;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Header header;
        private Body body;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Body {
        private Object items;
        private String numOfRows;
        private String pageNo;
        private String totalCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String dateKind;
        private String dateName;
        private String isHoliday;
        private String locdate;
        private String seq;
    }
}
