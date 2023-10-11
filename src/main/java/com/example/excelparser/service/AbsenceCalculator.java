package com.example.excelparser.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AbsenceCalculator {
    void isHolidayCalculate(String years, String month, HttpServletResponse response) throws IOException;
}
