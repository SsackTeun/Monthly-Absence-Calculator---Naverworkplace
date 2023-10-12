package com.example.excelparser.util.excel;

import com.example.excelparser.dto.absence.SourceExcelDataExtractorDTO;
import com.example.excelparser.dto.absence.UserListDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ExcelParserUtil {
    /* list.xlsx 파일 읽어서 List<UserListDTO> 객체로 변경 */
    public static List<UserListDTO> getUsers() throws IOException {
        String dir = System.getProperty("user.dir")+"/data/list/list.xlsx";
        //파일 읽기
        XSSFWorkbook workbook = new XSSFWorkbook(dir);

        //데이터 직렬화
        List<UserListDTO> list = new ArrayList<>();

        //첫번째시트 선택
        XSSFSheet sheet = workbook.getSheetAt(0);

        //전체 행 갯수 확인
        int ALL_OF_ROWS = sheet.getPhysicalNumberOfRows();

        //각 행 탐색
        for(int rowNum = 2; rowNum < ALL_OF_ROWS; rowNum++){
            XSSFRow row = sheet.getRow(rowNum); // 선택된 시트로 부터 행을 가져옴
            UserListDTO userListDTO = new UserListDTO(); // 각 셀을 담을 객체

            if(row!=null){
                int cells = row.getPhysicalNumberOfCells(); // 선택된 행이 빈값이 아니면, 셀갯수세기

                for(int cellNumber =0; cellNumber < cells; cellNumber++){
                    XSSFCell cell = row.getCell(cellNumber); // 선택된 행의 n 번째 셀
                    String value = "";

                    value = cell.getStringCellValue();

                    switch(cellNumber){
                        case 0: //이름
                            userListDTO.setUsername(value);
                            break;
                        case 1: //로그인 아이디
                            userListDTO.setEmail(value);
                            break;
                        case 2: //직급
                            userListDTO.setPosition(value);
                            break;
                    }
                    log.debug( rowNum + "번 행 : " + cellNumber + "번 열 값은: " + value);
                }
                list.add(userListDTO);
            }
        }
        return list;
    }

    /* absence.xlsx 파일을 읽어서 List<SourceExcelDataExtractorDTO> 객체로 변경 */
    public static List<SourceExcelDataExtractorDTO> readAbsenceTimeOffList() throws IOException {
        //파일 읽기
        String dir = System.getProperty("user.dir")+"/data/absence/absence.xlsx";
        XSSFWorkbook workbook = new XSSFWorkbook(dir);

        //데이터 직렬화
        List<SourceExcelDataExtractorDTO> list = new ArrayList<>();

        //첫번째시트 선택
        XSSFSheet sheet = workbook.getSheetAt(0);

        //전체 행 갯수 확인
        int ALL_OF_ROWS = sheet.getPhysicalNumberOfRows();

        //각 행 탐색
        for(int rowNum = 1; rowNum < ALL_OF_ROWS; rowNum++){
            XSSFRow row = sheet.getRow(rowNum); // 선택된 시트로 부터 행을 가져옴
            SourceExcelDataExtractorDTO sourceExcelDataExtractorDTO = new SourceExcelDataExtractorDTO(); // 각 셀을 담을 객체

            if(row!=null){
                int cells = row.getPhysicalNumberOfCells(); // 선택된 행이 빈값이 아니면, 셀갯수세기

                for(int cellNumber =0; cellNumber < cells; cellNumber++){
                    XSSFCell cell = row.getCell(cellNumber); // 선택된 행의 n 번째 셀
                    String value = "";

                    value = cell.getStringCellValue();

                    switch(cellNumber){
                        case 0: //문서번호
                            sourceExcelDataExtractorDTO.setDoc_num(value);
                            break;
                        case 1: // 이름
                            sourceExcelDataExtractorDTO.setName(value);
                            break;
                        case 2: // 로그인 아이디
                            sourceExcelDataExtractorDTO.setLoginId(value);
                            break;
                        case 3: // 직책
                            sourceExcelDataExtractorDTO.setPosition(value);
                            break;
                        case 4: //부재 항목
                            sourceExcelDataExtractorDTO.setAbsentCase(value);
                            break;
                        case 5: //일수
                            sourceExcelDataExtractorDTO.setDays(value);
                            break;
                        case 6: //기간
                            sourceExcelDataExtractorDTO.setDurations(convertDuration(value));
                            break;
                        case 7: //작성일
                            sourceExcelDataExtractorDTO.setRequestDate(value);
                            break;
                    }
                    log.debug( rowNum + "번 행 : " + cellNumber + "번 열 값은: " + value);
                }
                list.add(sourceExcelDataExtractorDTO);
            }
        }
        return list;
    }

    public static String convertDuration(String str){
        String pattern1 = "(\\d{4})\\.(\\d{2})\\.(\\d{2})\\(([가-힣]{2})\\)";
        String pattern2 ="(\\d{4})\\.(\\d{2})\\.(\\d{2})\\(([가-힣]{2})(\\s/\\s\\d{2}:\\d{2}\\s~\\s\\d{2}:\\d{2}\\))";
        // pattern1에 대한 대체 작업
        Pattern p1 = Pattern.compile(pattern1);
        Matcher m1 = p1.matcher(str);
        str = m1.replaceAll("$1.$2.$3($4)");

        // pattern2에 대한 대체 작업
        Pattern p2 = Pattern.compile(pattern2);
        Matcher m2 = p2.matcher(str);
        str= m2.replaceAll("$1.$2.$3($4) ~ $1.$2.$3($4)");
        return str;
    }
}
