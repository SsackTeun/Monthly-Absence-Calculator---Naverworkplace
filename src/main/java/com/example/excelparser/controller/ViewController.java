package com.example.excelparser.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@RestController
@Slf4j
public class ViewController {
    private FileUpDownController fileUpDownController;

    public ViewController(FileUpDownController fileUpDownController) {
        this.fileUpDownController = fileUpDownController;
    }

    // 메인 뷰
    @GetMapping("/")
    public ModelAndView main(ModelAndView mav) throws IOException {

        /**/
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        mav.addObject("currentYear", currentYear);
        mav.addObject("currentMonth", currentMonth);

        /* MAIN 로드할 때, 업로드 시간 표시할 파일 경로 */
        String upload_list_path = fileUpDownController.UPLOAD_LIST_PATH;
        String userListFileName = fileUpDownController.USERLIST_FILENAME;
        String upload_absence_path = fileUpDownController.UPLOAD_ABSENCE_PATH;
        String absenceFileName = fileUpDownController.ABSENCE_FILENAME;

        log.info("{}", upload_list_path);

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
}
