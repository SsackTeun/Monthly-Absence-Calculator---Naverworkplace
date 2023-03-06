package com.example.excelparser.dto.origin;

import com.example.excelparser.dto.UserListDTO;
import com.example.excelparser.util.DevDataRefactor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class MergeOriginWithDurationDTO{

    //이름
    private String name;

    //로그인 아이디
    private String loginId;

    //부서
    private String department;

    //기간
    private List<Duration> durations;

    public static List<MergeOriginWithDurationDTO> convertTo(List<OriginDTO> origin, List<UserListDTO> users){
        //결과리스트
        List<MergeOriginWithDurationDTO> durations = new ArrayList<>();
        MergeOriginWithDurationDTO duration = null;

        /**
         * 원본에서 꺼내서 새로 만들어낼 데이터에 값 넣기
         */
        for (OriginDTO originDTO : origin) {
            log.info("{}", originDTO);
            duration = new MergeOriginWithDurationDTO();
            duration.setLoginId(originDTO.getLoginId());
            duration.setName(originDTO.getName());
            duration.setDepartment(originDTO.getDepartment());
            duration.setDurations(DevDataRefactor.regDuration(originDTO.getDurations()));
            durations.add(duration);
        }

        /**
         * 사본 데이터
         */
        List<MergeOriginWithDurationDTO> merges = new ArrayList<>();
        //유효한 유저 이름 기준으로 모든 데이터를 탐색
        //유효한 유저 이름
        for(UserListDTO user : users){
            if(!(user.getEmail().length() == 0)){
                MergeOriginWithDurationDTO merge = new MergeOriginWithDurationDTO();
                //유효한 유저 이름
                String email = user.getEmail();
                //모든 객체 접근하여 다시 해당 이메일과 같은 유저를 찾음
                List<Duration> mergeDuration = new ArrayList<>();
                for(MergeOriginWithDurationDTO m : durations){
                    if(m.getLoginId().equals(email)){
                        //해당 조건을 만족하면, 객체에 담기
                        merge.setName(m.getName());
                        merge.setDepartment(m.getDepartment());
                        merge.setLoginId(m.getLoginId());
                        mergeDuration.addAll(m.getDurations());
                    }
                    else{
                        merge.setName(user.getUsername());
                        merge.setDurations(null);
                        merge.setDepartment(user.getPosition());
                        merge.setLoginId(user.getEmail());
                    }
                }
                merge.setDurations(mergeDuration);
                merges.add(merge);
            }
        }
        return merges;
    }
}
