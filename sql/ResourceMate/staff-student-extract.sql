------- Student pull

select 
student.LEGAL_SURNAME As "LastName", 
student.LEGAL_FIRST_NAME As "FirstName",  
ltu.usr_username,
substring(student.STUDENT_NO,6,4) AS "Password", 
student.STUDENT_NO AS "StudentNumber", 
--CONVERT(VARCHAR(10), student.BIRTH_DATE, 101) AS "DOB",
sc.CLASS_CODE AS "Class", 
--teacher.LEGAL_NAME_UPPER AS "Teacher",
teacher.LEGAL_SURNAME AS "Teacher Surname",
teacher.LEGAL_FIRST_NAME AS "Teacher First Name",
sr.GRADE AS "Grade"

from SCHOOL_STAFF ss

left outer join SCHOOL_CLASSES sc on
sc.SCHOOL_CODE = ss.SCHOOL_CODE and
sc.SCHOOL_YEAR = ss.SCHOOL_YEAR and
sc.REPORTING_TEACHER = ss.PERSON_ID

left outer join STUDENT_PROGRAM_CLASS_TRACKS spct on
spct.SCHOOL_CODE = ss.SCHOOL_CODE and
spct.SCHOOL_YEAR = ss.SCHOOL_YEAR  and
spct.CLASS_CODE = sc.CLASS_CODE

left outer join persons student on
student.PERSON_ID = spct.PERSON_ID

left outer join PERSONS teacher on
teacher.PERSON_ID = ss.PERSON_ID

left outer join SCHOOLS s on
s.SCHOOL_CODE = ss.SCHOOL_CODE

left outer join STUDENT_REGISTRATIONS sr on
sr.person_id = student.PERSON_ID and
sr.STUDENT_NO = student.STUDENT_NO and
spct.SCHOOL_CODE = sr.SCHOOL_CODE and
spct.SCHOOL_YEAR = sr.SCHOOL_YEAR

left outer join li_tbl_usernames ltu on
ltu.usr_std_number = student.OEN_NUMBER


where	spct.SCHOOL_YEAR = 20192020 and
		spct.SCHOOL_CODE = 'SIRJM' and
		sr.STATUS_INDICATOR_CODE = 'Active'
		
---------------------------------------- Staff Pull

select distinct
teacher.LEGAL_SURNAME As "LastName", 
teacher.LEGAL_FIRST_NAME As "FirstName",  
ltu.usr_username,
'' AS "Password", 
teacher.MEN_NUMBER AS "Employee#", 
--CONVERT(VARCHAR(10), student.BIRTH_DATE, 101) AS "DOB",
sc.CLASS_CODE AS "Class", 
--teacher.LEGAL_NAME_UPPER AS "Teacher",
sr.GRADE AS "Grade"

from SCHOOL_STAFF ss

left outer join SCHOOL_CLASSES sc on
sc.SCHOOL_CODE = ss.SCHOOL_CODE and
sc.SCHOOL_YEAR = ss.SCHOOL_YEAR and
sc.REPORTING_TEACHER = ss.PERSON_ID

left outer join STUDENT_PROGRAM_CLASS_TRACKS spct on
spct.SCHOOL_CODE = ss.SCHOOL_CODE and
spct.SCHOOL_YEAR = ss.SCHOOL_YEAR  and
spct.CLASS_CODE = sc.CLASS_CODE

left outer join PERSONS teacher on
teacher.PERSON_ID = ss.PERSON_ID

left outer join persons student on
student.PERSON_ID = spct.PERSON_ID

left outer join SCHOOLS s on
s.SCHOOL_CODE = ss.SCHOOL_CODE

left outer join STUDENT_REGISTRATIONS sr on
sr.person_id = student.PERSON_ID and
sr.STUDENT_NO = student.STUDENT_NO and
spct.SCHOOL_CODE = sr.SCHOOL_CODE and
spct.SCHOOL_YEAR = sr.SCHOOL_YEAR

left outer join li_tbl_usernames ltu on
ltu.usr_emp_number = teacher.STAFF_NO

where	spct.SCHOOL_YEAR = 20192020 and
		spct.SCHOOL_CODE = 'SIRJM'

-------------
