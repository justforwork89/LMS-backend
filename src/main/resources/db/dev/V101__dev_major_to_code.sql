-- 전공을 이름 대신 MAJOR 공통코드 값으로 저장하도록 기존 데이터를 변환한다. 이미 코드인 값은 그대로 둔다.
UPDATE TB_USER
   SET MAJOR = DECODE(MAJOR, '컴퓨터공학', '10', '정보보안', '20', '데이터분석', '30', MAJOR)
 WHERE MAJOR IS NOT NULL;

UPDATE TB_COURSE
   SET MAJOR = DECODE(MAJOR, '컴퓨터공학', '10', '정보보안', '20', '데이터분석', '30', '공통', '40', MAJOR);
