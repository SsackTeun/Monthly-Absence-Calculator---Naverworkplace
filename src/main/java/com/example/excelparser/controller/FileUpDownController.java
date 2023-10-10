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
    private String multipart_location = System.getProperty("user.dir")+"/data";
    
    /* 사용자 리스트 파일 업로드파일 저장 위치 */
    private String upload_list_path = multipart_location +"/list";
    private final String userListFileName = "list.xlsx";

    /* 네이버워크플레이스 부재 엑셀파일 업로드파일 저장 위치 */
    private String upload_absence_path =multipart_location +"/absence";
    private final String absenceFileName = "absence.xlsx";

    public FileUpDownController(DataRefactorService dataRefactorService) {
        this.dataRefactorService = dataRefactorService;
    }


    // 메인 뷰
    @GetMapping("/")
    public ModelAndView main(ModelAndView mav) throws IOException {
        Path list = Paths.get(upload_list_path + "/" + userListFileName);
        if(Files.exists(list)){
            BasicFileAttributes basicFileAttributes1
                    = Files.readAttributes(list, BasicFileAttributes.class);

            Date time1 = new Date(basicFileAttributes1.lastAccessTime().toMillis());
            String current_upload_time1 = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초").format(time1);

            mav.addObject("list_current_upload_time", current_upload_time1);
            mav.addObject("list_filename", list.getFileName());
        }

        Path absence = Paths.get(upload_absence_path + "/" + absenceFileName);
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


    /* list.xlsx 파일데이터에서 유저정보 json 으로 변환하여 반환 */
    @GetMapping("/files/userlist/users")
    public List<UserListDTO> users() throws IOException {
        return DataRefactoring.getAllUsers();
    }

    /* 업로드 유저 리스트 파일 */
    @PostMapping("/files/upload/userlist")
    public RedirectView uploadUserListExcelFile(@RequestParam("file")MultipartFile userListFile) throws IOException {

        /* /root/list 위치에 파일 생성 */
        File directory = new File(upload_list_path);

        /* upload_list_path 디렉토리 생성 */
        if(!directory.exists()) {
            directory.mkdirs();
            log.info("created directory :  {} " + directory);
        }else{
            log.info("already exist");
        }

        /* upload_list_path 경로에 list.xlsx 파일이 있는지 체크 */
        Path list = Paths.get(upload_list_path + "/" + userListFileName);

        /* list.xlsx 파일이 이미 존재하면, 삭제할 것 */
        if(Files.exists(list)){
            Files.delete(list);
        }

        /* MultipartFile 로 업로드하는 파일을 list.xlsx 이름으로 저장할 것 */
        File saveFile = new File(upload_list_path + "/" + userListFileName);
        userListFile.transferTo(saveFile);

        return new RedirectView("/");
    }

    /* 다운로드 유저 리스트 파일 */
    @GetMapping("/files/download/userlist")
    public ResponseEntity<UrlResource> downloadList() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_list_path + "/" + userListFileName);
        String contentDisposition = "attachment; filename=\""+ userListFileName + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    /* 업로드 네이버워크플레이스 부재일정 파일 */
    @PostMapping("/files/upload/absence")
    public RedirectView upload_absence(@RequestParam("file")MultipartFile file) throws IOException {

        File dir = new File(upload_absence_path + "/" + absenceFileName);

        if(!dir.exists()) {
            dir.mkdirs();
            log.info("폴더 생성 성공 {} " + dir);
        }else{
            System.out.println("폴더가 이미 존재합니다.");
        }

        Path absence = Paths.get(upload_absence_path + "/" + absenceFileName);
        if(Files.exists(absence)){
            Files.delete(absence);
        }

        File saveFile = new File(upload_absence_path + "/" + absenceFileName);
        file.transferTo(saveFile);
        return new RedirectView("/");
    }
    /* 다운로드 네이버워크플레이스 부재일정 파일 */
    @GetMapping("/files/download/absence")
    public ResponseEntity<UrlResource> downloadAbsence() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_absence_path + "/absence.xlsx");
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
