package com.example.excelparser.util.excel;

import com.example.excelparser.dto.MergeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelCreation {
    private Workbook workbook;

    private Sheet sheet;

    private Row titleRow ;

    private Cell titleCell;

    private CellStyle style;

    private XSSFFont font;

    public Workbook writeExcelSheet(List<MergeDTO> data){
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("연차 사용 내역");
        sheet.setColumnWidth(0, 13*256);
        sheet.addMergedRegion(new CellRangeAddress(0,1, 0, 6));

        sheet.setColumnWidth(1, 13*256);
        sheet.setColumnWidth(2, 32*256);
        sheet.setColumnWidth(3, 13*256);
        sheet.setColumnWidth(4, 13*256);
        sheet.setColumnWidth(5, 15*256);
        sheet.setColumnWidth(6, 15*256);

        CellStyle align = workbook.createCellStyle();
        align.setVerticalAlignment(VerticalAlignment.CENTER);
        align.setAlignment(HorizontalAlignment.CENTER);
        align.setWrapText(true);

        CellStyle size1 = workbook.createCellStyle();

        titleRow = sheet.createRow(0);
        titleCell = titleRow.createCell(0);
        titleCell.setCellValue(data.get(0).getSelect() + "월 휴가 사용 내역");
        titleCell.setCellStyle(align);

        titleRow.setHeight((short) 1000);


        /** 헤더 **/
        List<String> header = new ArrayList<>();

        header.add("이름");
        header.add("직급");
        header.add("당월 휴가 사용일");
        header.add("휴가 사용 일수");
        header.add("부재 시간 환산");
        header.add("당월 총 근로시간");
        header.add("당월 실 근로시간");

        titleRow = sheet.createRow(2);
        for(int i = 0; i < header.size(); i++){
            log.info(header.get(i));
            titleCell = titleRow.createCell(i);
            titleCell.setCellValue(header.get(i));
            titleCell.setCellStyle(align);
        }

        int BODY_INDEX = 3;
        int ADDITIONAL_ROW = 0;
        /** 내용 **/
        for(int i = 0; i < data.size(); i++) {
            log.info("{}", data);
            titleRow = sheet.createRow(BODY_INDEX++);
            titleCell = titleRow.createCell(0);
            titleCell.setCellValue(data.get(i).getName());
            titleCell.setCellStyle(align);

            titleCell = titleRow.createCell(1);
            titleCell.setCellValue(data.get(i).getPosition());
            titleCell.setCellStyle(align);

            titleCell = titleRow.createCell(2);
            titleCell.setCellValue(data.get(i).getDuration().toString().replace("[", "").replace("]", "").replace(", ", "\n").trim());
            titleCell.setCellStyle(align);

            titleCell = titleRow.createCell(3);
            titleCell.setCellValue(data.get(i).getDays());
            titleCell.setCellStyle(align);

            titleCell = titleRow.createCell(4);
            titleCell.setCellValue(data.get(i).getAbsenceTimes());
            titleCell.setCellStyle(align);

            titleCell = titleRow.createCell(5);
            titleCell.setCellValue(data.get(i).getWorkTimes());
            titleCell.setCellStyle(align);

            titleCell = titleRow.createCell(6);
            titleCell.setCellValue(data.get(i).getRealWorkTimes());
            titleCell.setCellStyle(align);
        }
        ADDITIONAL_ROW = BODY_INDEX + 1;
        titleRow = sheet.createRow(ADDITIONAL_ROW);

        titleCell = titleRow.createCell(5);
        titleCell.setCellValue("총 근로일수");

        titleCell = titleRow.createCell(6);
        titleCell.setCellValue("0 일");

        return workbook;
    }

    public void configSheetStyle(){
        style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);

        font = ((XSSFWorkbook) workbook).createFont();
        font.setBold(true);
        font.setFontHeight((short) (20*20));
        style.setFont(font);
    }

    public void createFile(HttpServletResponse response, List<MergeDTO> data, String year, String month) throws IOException {
        File currDir = new File(".");                // 현재 프로젝트 경로를 가져옴
        String path = currDir.getAbsolutePath();
        String filename = "휴가 사용 내역_"+ year+"년"+month+"월.xlsx";
        String fileLocation = path.substring(0, path.length() - 1) +filename;   // 파일명 설정

        log.info("current path:::{} \n path:::{} \n fileLocation ::: {}", currDir, path, fileLocation);

        workbook = writeExcelSheet(data);        // workbook을 반환하는 메서드(직접작성한 메서드로 무시해도 됨..)

//        FileOutputStream fileOutputStream = new FileOutputStream(fileLocation);        // 파일 생성
//        workbook.write(fileOutputStream);                                            // 엑셀파일로 작성
//        workbook.close();

        // Download
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode(filename, "UTF-8"));
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        try {
            workbook.write(response.getOutputStream());
        } finally {
            workbook.close();
        }
    }
}
