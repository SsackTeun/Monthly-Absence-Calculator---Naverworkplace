package com.example.excelparser.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileUpDownController {

    /* 저장 폴더 루트 위치 */
    private String multipart_location = System.getProperty("user.dir")+"/data";
    
    /* 사용자 리스트 파일 업로드파일 저장 위치 */
    private String upload_list_path = multipart_location +"/list";
    private final String userListFileName = "list.xlsx";

    /* 네이버워크플레이스 부재 엑셀파일 업로드파일 저장 위치 */
    private String upload_absence_path =multipart_location +"/absence";
    private final String absenceFileName = "absence.xlsx";
    
    /* 업로드 유저 리스트 파일 */
    @PostMapping("/upload/userlist")
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
    @GetMapping("/download/userlist")
    public ResponseEntity<UrlResource> downloadList() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_list_path + "/" + userListFileName);
        String contentDisposition = "attachment; filename=\""+ userListFileName + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    /* 업로드 네이버워크플레이스 부재일정 파일 */
    @PostMapping("/upload/absence")
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
    @GetMapping("/download/absence")
    public ResponseEntity<UrlResource> downloadAbsence() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + upload_absence_path + "/absence.xlsx");
        String contentDisposition = "attachment; filename=\""+ "absence.xlsx" + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
