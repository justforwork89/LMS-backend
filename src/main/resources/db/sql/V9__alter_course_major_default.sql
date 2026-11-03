-- 과정 전공 기본값을 이름('공통') 대신 MAJOR 공통코드 '공통'(40)으로 바꾼다.
ALTER TABLE TB_COURSE MODIFY (MAJOR DEFAULT '40');
