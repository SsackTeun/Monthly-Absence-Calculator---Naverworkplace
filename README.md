# naverworkplace-absence-monthly-report
naverworkplace 부재 파싱 -> 월별 휴가 사용시간 산출

# 만든 목적
네이버워크플레이스에서 부재일정을 집계하여, 엑셀로 내려 받을 수 있다. 다만, 내려받는 데이터가 신청일시 기준으로만 조회가 되어, 월이 넘어가는 경우나 다른 월을 미리 신청할 경우에는
월별 집계를 직접 눈으로 확인하고 수정 해야한다. 이 과정에서 누락 및 실수가 발생하며, 매월 반복하여 처리를 해야하기 때문에 만들게 되었다.

# 사용법 - 네이버워크플레이스 부재 일정 현황 (admin)
해당월의 1일 ~ 말일 까지 선택하여, 엑셀로 내려받는다
![image](https://github.com/SsackTeun/naverworkplace-absence-monthly-report/assets/24308378/f6f97f46-5b79-4119-a913-193f2cdffd38)



- ex) 워크플레이스파일, 유저 목록엑셀파일 샘플
[list (1).zip](https://github.com/SsackTeun/naverworkplace-absence-monthly-report/files/12851909/list.1.zip)


# 사용한 API
 * API 를 호출해야하므로, 외부와 네트워크통신이 가능해야함
기존에는 JAVA 의 ChineseCalendar  날짜가 달라지는 문제가 발생하였음.
대안으로 공공데이터포탈의 천문정보 API 를 사용하여, 휴일 정보를 얻어옴.

# 구동 화면
프로그램은 웹페이지로 구동하며, 제일 아래에 있는 "네이버 워크플레이스 부재 엑셀파일 업로드" 에 업로드 해준다
<img width="647" alt="image" src="https://github.com/SsackTeun/naverworkplace-absence-monthly-report/assets/24308378/a73dc038-3eff-4cff-b166-43ec4dc001dc">

유저 목록과, 엑셀 파일은 list 파일을 받아서, 업로드 후 테스트
*주의 : 엑셀 파일의 유저 목록의 email 은 엑셀파일의 email 과 같아야함. (체크하는 부분이 있음)

그다음, 추출하려고하는 년도, 월을 선택후 요청을 누르면, 엑셀 파일로 받아진다.


