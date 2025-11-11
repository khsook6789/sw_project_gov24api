배포 시
application.properties 파일에 db 부분 수정
회원가입, 로그인, 인증 기능 테스트 완료
gradle.properties 파일은 삭제 or 수정


ALTER TABLE app_user CHANGE username email VARCHAR(100) NOT NULL UNIQUE;
ALTER TABLE app_user ADD COLUMN username VARCHAR(50) NOT NULL AFTER email;
sql 입력
