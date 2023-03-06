package com.example.excelparser.controller;

import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.dto.origin.MergeOriginWithDurationDTO;
import com.example.excelparser.dto.origin.OriginDTO;
import com.example.excelparser.service.DataRefactorService;
import com.example.excelparser.util.DevDataRefactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/test")
@RestController
public class ControllerT {

    private DataRefactorService dataRefactorService;

    @Autowired
    public ControllerT(){
        this.dataRefactorService = new DataRefactorService();
    }

    @GetMapping("/origin")
    public List<OriginDTO> getOrigin() throws IOException {
        return dataRefactorService.getOriginDataFromExcel();
    }

    @GetMapping("/users")
    public List<UserListDTO> getUsers() throws IOException {
        return dataRefactorService.getUsersFromExcel();
    }

    @GetMapping("/merge")
    public List<MergeOriginWithDurationDTO> merge() throws IOException {
        return dataRefactorService.getMergeWithDuration();
    }
}
