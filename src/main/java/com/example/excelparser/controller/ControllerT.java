//package com.example.excelparser.controller;
//
//import com.example.excelparser.dto.MergeDTO;
//import com.example.excelparser.dto.UserListDTO;
//import com.example.excelparser.dto.origin.MergeOriginWithDurationDTO;
//import com.example.excelparser.dto.origin.OriginDTO;
//import com.example.excelparser.service.DataRefactorService;
//import com.example.excelparser.util.DataRefactoring;
//import com.example.excelparser.util.excel.ExcelCreation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.List;
//
//@RequestMapping("/test")
//@RestController
//public class ControllerT {
//
//    private DataRefactorService dataRefactorService;
//
//    @Autowired
//    public ControllerT(){
//        this.dataRefactorService = new DataRefactorService();
//    }
//
//    @GetMapping("/origin")
//    public List<OriginDTO> getOrigin() throws IOException {
//        return dataRefactorService.getOriginDataFromExcel();
//    }
//
//    @GetMapping("/users")
//    public List<UserListDTO> getUsers() throws IOException {
//        return dataRefactorService.getUsersFromExcel();
//    }
//
//    @GetMapping("/merge")
//    public List<MergeOriginWithDurationDTO> merge() throws IOException {
//        return dataRefactorService.durationMerge();
//    }
//
//
//
//    @GetMapping("/duration/divide")
//    public List<MergeOriginWithDurationDTO> divide() throws IOException {
//        return dataRefactorService.divideEachMonth();
//    }
//
//    @GetMapping("/calculate/isholiday/{years}/{month}")
//    public void calculate(@PathVariable("years") String years,
//                                                      @PathVariable("month") String month,
//                                                      HttpServletResponse response) throws IOException {
//        //return dataRefactorService.isHolidayCalculate(years, month);
//        new ExcelCreation().createFile(response, MergeDTO.convert(dataRefactorService.isHolidayCalculate(years, month)), years, month);
//    }
//
//    @GetMapping("/calculate/isholiday/{years}/{month}/{type}")
//    public List<MergeDTO> calculate(@PathVariable("years") String years,
//                          @PathVariable("month") String month,
//                        @PathVariable("type") String type
//                          ) throws IOException {
//        //return dataRefactorService.isHolidayCalculate(years, month);
//        return MergeDTO.convert(dataRefactorService.isHolidayCalculate(years, month));
//    }
//}
