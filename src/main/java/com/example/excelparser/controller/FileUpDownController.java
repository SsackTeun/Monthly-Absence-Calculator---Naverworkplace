package com.example.excelparser.controller;

import com.example.excelparser.dto.MergeDTO;
import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.service.DataRefactorService;
import com.example.excelparser.util.DataRefactoring;
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
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class FileUpDownController {

    /* */
    private DataRefactorService dataRefactorService;

    /* 저장 폴더 루트 위치 */
    public final static String MULTIPART_LOCATION = System.getProperty("user.dir")+"/data";
    
    /* 사용자 리스트 파일 업로드파일 저장 위치 */
    public final static String UPLOAD_LIST_PATH = MULTIPART_LOCATION +"/list";
    public final static String USERLIST_FILENAME = "list.xlsx";

    /* 네이버워크플레이스 부재 엑셀파일 업로드파일 저장 위치 */
    public final static String UPLOAD_ABSENCE_PATH =MULTIPART_LOCATION +"/absence";
    public final static String ABSENCE_FILENAME = "absence.xlsx";

    public FileUpDownController(DataRefactorService dataRefactorService) {
        this.dataRefactorService = dataRefactorService;
    }





    /* list.xlsx 파일데이터에서 유저정보 json 으로 변환하여 반환 */
    @GetMapping("/files/userlist/users")
    public List<UserListDTO> users() throws IOException {
        return DataRefactoring.getAllUsers();
    }

    /* 업로드 유저 리스트 파일 */
    @PostMapping("/files/upload/userlist")
    public RedirectView uploadUserListExcelFile(@RequestParam("file")MultipartFile userListFile) throws IOException {

        /* /root/list 위치에 파일 생성 */
        File directory = new File(UPLOAD_LIST_PATH);

        /* upload_list_path 디렉토리 생성 */
        if(!directory.exists()) {
            directory.mkdirs();
            log.info("created directory :  {} " + directory);
        }else{
            log.info("already exist");
        }

        /* upload_list_path 경로에 list.xlsx 파일이 있는지 체크 */
        Path list = Paths.get(UPLOAD_LIST_PATH + "/" + USERLIST_FILENAME);

        /* list.xlsx 파일이 이미 존재하면, 삭제할 것 */
        if(Files.exists(list)){
            Files.delete(list);
        }

        /* MultipartFile 로 업로드하는 파일을 list.xlsx 이름으로 저장할 것 */
        File saveFile = new File(UPLOAD_LIST_PATH + "/" + USERLIST_FILENAME);
        userListFile.transferTo(saveFile);

        return new RedirectView("/");
    }

    /* 다운로드 유저 리스트 파일 */
    @GetMapping("/files/download/userlist")
    public ResponseEntity<UrlResource> downloadList() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + UPLOAD_LIST_PATH + "/" + USERLIST_FILENAME);
        String contentDisposition = "attachment; filename=\""+ USERLIST_FILENAME + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    /* 업로드 네이버워크플레이스 부재일정 파일 */
    @PostMapping("/files/upload/absence")
    public RedirectView upload_absence(@RequestParam("file")MultipartFile file) throws IOException {

        File dir = new File(UPLOAD_ABSENCE_PATH + "/" + ABSENCE_FILENAME);

        if(!dir.exists()) {
            dir.mkdirs();
            log.info("폴더 생성 성공 {} " + dir);
        }else{
            System.out.println("폴더가 이미 존재합니다.");
        }

        Path absence = Paths.get(UPLOAD_ABSENCE_PATH + "/" + ABSENCE_FILENAME);
        if(Files.exists(absence)){
            Files.delete(absence);
        }

        File saveFile = new File(UPLOAD_ABSENCE_PATH + "/" + ABSENCE_FILENAME);
        file.transferTo(saveFile);
        return new RedirectView("/");
    }
    /* 다운로드 네이버워크플레이스 부재일정 파일 */
    @GetMapping("/files/download/absence")
    public ResponseEntity<UrlResource> downloadAbsence() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + UPLOAD_ABSENCE_PATH + "/absence.xlsx");
        String contentDisposition = "attachment; filename=\""+ "absence.xlsx" + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    /* 결과 엑셀로 내려 받기 */
    @GetMapping("/files/download/result/{years}/{month}")
    public void calculate(@PathVariable("years") String years,
                          @PathVariable("month") String month,
                          HttpServletResponse response) throws IOException {
        new ExcelCreation().createFile(response, MergeDTO.convert(dataRefactorService.isHolidayCalculate(years, month)), years, month);
    }
}
