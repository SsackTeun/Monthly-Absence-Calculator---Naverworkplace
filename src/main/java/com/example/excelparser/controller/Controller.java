package com.example.excelparser.controller;

import com.example.excelparser.dto.MergeDTO;
import com.example.excelparser.dto.origin.OriginDTO;
import com.example.excelparser.dto.RefactorDTO;
import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.util.DataRefactoring;
import com.example.excelparser.util.ExcelCreation;
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
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class Controller {
    private String multipart_location = System.getProperty("user.dir")+"/data";
    private String upload_list_path = multipart_location +"/list";

    private String upload_absence_path =multipart_location +"/absence";
    private DataRefactoring dataRefactor;
    Controller() throws IOException {
        dataRefactor = new DataRefactoring();
    }

    /**
     * 원본데이터
     * {
     *     "doc_num": "2023_TIM01_000011",
     *     "name": "유오선",
     *     "loginId": "osyu@onware.co.kr",
     *     "department": "경영지원팀",
     *     "absentCase": "연차",
     *     "days": "1.000",
     *     "duration": "2023.03.03(종일) ~ 2023.03.03(종일)",
     *     "requestDate": "2023.03.02"
     *   },
     */
    @GetMapping("/origin")
    public List<OriginDTO> origin() throws IOException {
        return DataRefactoring.origin();
    }

    @GetMapping("/users")
    public List<UserListDTO> users() throws IOException {
        return DataRefactoring.getAllUsers();
    }

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

    @GetMapping("/download/list")
    public ResponseEntity<UrlResource> downloadList() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_list_path + "/list.xlsx");
        String contentDisposition = "attachment; filename=\""+ "list.xlsx" + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @GetMapping("/download/absence")
    public ResponseEntity<UrlResource> downloadAbsence() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_absence_path + "/absence.xlsx");
        String contentDisposition = "attachment; filename=\""+ "absence.xlsx" + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

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

    @GetMapping("/refactor")
    public List<RefactorDTO> refactor() throws IOException {
        return DataRefactoring.refactor();
    }

    @GetMapping("/merge")
    public List<MergeDTO> merge() throws IOException {
        return DataRefactoring.merge();
    }

    @GetMapping("/dividemonth")
    public List<MergeDTO> divideMonth() throws IOException {
        return DataRefactoring.divideMonth();
    }

    @GetMapping("/dateselect/{year}/{month}")
    public void dateSelect(
            @PathVariable("year") String year,
            @PathVariable("month") String month,
            HttpServletResponse response
    ) throws IOException {
        new ExcelCreation().createFile(response, DataRefactoring.getEachMonthWithSelectMonth(year,month), year, month);
        //return DataRefactor.getEachMonthWithSelectMonth(year,month);
    }
}
