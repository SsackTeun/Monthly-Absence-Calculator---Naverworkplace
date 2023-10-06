package com.example.excelparser.controller;

import com.example.excelparser.dto.MergeDTO;
import com.example.excelparser.dto.RefactorDTO;
import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.dto.origin.OriginDTO;
import com.example.excelparser.service.DataRefactorService;
import com.example.excelparser.util.DataRefactoring;
import com.example.excelparser.util.date.LunarCalendar;
import com.example.excelparser.util.excel.ExcelCreation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class Controller {
    
    /* 파일 저장 위치 root */
    private String multipart_location = System.getProperty("user.dir")+"/data";
    
    /* 사용자 리스트 파일 업로드파일 저장 위치 */
    private String upload_list_path = multipart_location +"/list";

    /* 네이버워크플레이스 부재 엑셀파일 업로드파일 저장 위치 */
    private String upload_absence_path =multipart_location +"/absence";
    
    /* 부재 엑셀파일 원본 데이터 가공 관련 */
    private DataRefactoring dataRefactor;
    private DataRefactorService dataRefactorService;
    Controller() throws IOException {
        dataRefactor = new DataRefactoring();
        dataRefactorService = new DataRefactorService();
    }

    /* View : absence.html
    * 최근 업로드된 파일 시간 표시
    */
    @GetMapping("/")
    public ModelAndView main(ModelAndView mav) throws IOException {
        Path list = Paths.get(upload_list_path + "/list.xlsx");
        if(Files.exists(list)){
            BasicFileAttributes basicFileAttributes1
                    = Files.readAttributes(list, BasicFileAttributes.class);

            Date time1 = new Date(basicFileAttributes1.lastAccessTime().toMillis());
            String current_upload_time1 = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초").format(time1);

            mav.addObject("list_current_upload_time", current_upload_time1);
            mav.addObject("list_filename", list.getFileName());
        }

        Path absence = Paths.get(upload_absence_path + "/absence.xlsx");
        if(Files.exists(absence)){
            BasicFileAttributes basicFileAttributes2
                    = Files.readAttributes(absence, BasicFileAttributes.class);

            Date time2 = new Date(basicFileAttributes2.lastAccessTime().toMillis());
            String current_upload_time2 = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초").format(time2);

            mav.addObject("ab_current_upload_time", current_upload_time2);
            mav.addObject("ab_filename", absence.getFileName());
        }

        mav.setViewName("absence");
        return mav;
    }

    /* 원본 엑셀 데이터에서 추출하여, json 형태로 변환하여 반환 */
    @GetMapping("/origin")
    public List<OriginDTO> origin() throws IOException {
        return DataRefactoring.origin();
    }

    /* list.xlsx 파일데이터에서 유저정보 json 으로 변환하여 반환 */
    @GetMapping("/users")
    public List<UserListDTO> users() throws IOException {
        return DataRefactoring.getAllUsers();
    }

    /* list 파일 업로드한 것 다운로드하기 */
    @GetMapping("/download/list")
    public ResponseEntity<UrlResource> downloadList() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_list_path + "/list.xlsx");
        String contentDisposition = "attachment; filename=\""+ "list.xlsx" + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    /* 네이버워크플레이스 부재 엑셀 파일 데이터 마지막 업로드 파일 다운로드 */
    @GetMapping("/download/absence")
    public ResponseEntity<UrlResource> downloadAbsence() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_absence_path + "/absence.xlsx");
        String contentDisposition = "attachment; filename=\""+ "absence.xlsx" + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    /* 유저목록 파일 업로드 */
    @PostMapping("/upload/user/lists")
    public RedirectView upload_list(@RequestParam("file")MultipartFile file,
                               ModelAndView mav) throws IOException, InterruptedException {

        File dir = new File(upload_list_path);

        if(!dir.exists()) {
            dir.mkdirs();
            log.info("폴더 생성 성공 {} " + dir);
        }else{
            System.out.println("폴더가 이미 존재합니다.");
        }
        Path list = Paths.get(upload_list_path + "/list.xlsx");

        if(Files.exists(list)){
            Files.delete(list);
        }

        File savefile = new File(upload_list_path + "/list.xlsx");
        file.transferTo(savefile);
        return new RedirectView("/");
    }
    
    /* 네이버워크플레이스 부재 엑셀 파일 데이터 업로드 */
    @PostMapping("/upload/absence")
    public RedirectView upload_absence(@RequestParam("file")MultipartFile file,
                               ModelAndView mav) throws IOException, InterruptedException {

        File dir = new File(upload_absence_path + "/absence.xlsx");

        if(!dir.exists()) {
            dir.mkdirs();
            log.info("폴더 생성 성공 {} " + dir);
        }else{
            System.out.println("폴더가 이미 존재합니다.");
        }

        Path absence = Paths.get(upload_absence_path + "/absence.xlsx");
        if(Files.exists(absence)){
            Files.delete(absence);
        }

        File savefile = new File(upload_absence_path + "/absence.xlsx");
        file.transferTo(savefile);
        return new RedirectView("/");
    }

    /* 휴가 기간 값을 list 형태로 변환 */
    @GetMapping("/refactor")
    public List<RefactorDTO> refactor() throws IOException {
        return DataRefactoring.refactor();
    }

    /* 휴가 기간 값 + 일한 일수 데이터 합치기  */
    @GetMapping("/merge")
    public List<MergeDTO> merge() throws IOException {
        return DataRefactoring.merge();
    }

    /* 달이 넘어가는 경우에 대한 처리 */
    @GetMapping("/dividemonth")
    public List<MergeDTO> divideMonth() throws IOException {
        return DataRefactoring.divideMonth();
    }

    /* 선택한 연월에 대해서 엑셀 다운로드 */
    @GetMapping("/dateselect/{year}/{month}")
    public void dateSelect(
            @PathVariable("year") String year,
            @PathVariable("month") String month,
            HttpServletResponse response
    ) throws IOException {
        new ExcelCreation().createFile(response, DataRefactoring.getEachMonthWithSelectMonth(year,month), year, month);
    }

    /* 결과 엑셀로 내려 받기 */
    @GetMapping("/calculate/isholiday/{years}/{month}")
    public void calculate(@PathVariable("years") String years,
                          @PathVariable("month") String month,
                          HttpServletResponse response) throws IOException {
        new ExcelCreation().createFile(response, MergeDTO.convert(dataRefactorService.isHolidayCalculate(years, month)), years, month);
    }



    public int  isWeekend(LocalDate localDate){
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return dayOfWeek.getValue();
    }

}
