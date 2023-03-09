package com.example.excelparser.dto;

import com.example.excelparser.dto.origin.MergeOriginWithDurationDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class MergeDTO {
    private String select;
    private String name;
    private String email;
    private String department;
    private List<String> duration;
    private float days;

    private String position;
    private int absenceTimes;

    private int workTimes;

    private int realWorkTimes;

    public static List<MergeDTO> convert(List<MergeOriginWithDurationDTO> mergeOriginWithDurationDTO){
        List<MergeDTO> list = new ArrayList<>();

        mergeOriginWithDurationDTO.forEach(x-> {
            String select = null;
            String email = null;
            String name = null;
            String department = null;
            List<String> duration = null;
            float days = 0.0f;
            String position = null;
            int absenceTimes = 0;
            int workTimes = 0;
            int realWorkTimes = 0;

            select = x.getSelectedMonth().getMonth();
            email = x.getLoginId();
            name = x.getName();
            days = Float.parseFloat(x.getSelectedMonth().getDays());
            position = x.getPosition();
            realWorkTimes = 208;
            absenceTimes = (int) (8 * days);
            workTimes = realWorkTimes - absenceTimes;

            List<String> makeDuration = new ArrayList<>();

            x.getDurations().forEach(y ->{
                makeDuration.add(y.getStartDate()+"-"+y.getEndDate());
            });


            
            list.add(new MergeDTO().builder()
                    .select(select)
                    .name(name)
                    .email(email)
                    .department(null)
                    .duration(makeDuration)
                    .absenceTimes(absenceTimes)
                    .realWorkTimes(realWorkTimes)
                    .workTimes(workTimes)
                    .position(position)
                    .days(days)
                    .build());
        });
        return list;
    }
}
