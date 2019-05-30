-- d2l related script for Mark Lee

select distinct tu.usr_username, p.OEN_NUMBER from SCHOOL_CLASSES sc 
join student_program_class_tracks spct on sc.school_code = spct.school_code and spct.school_year = sc.SCHOOL_YEAR
join student_registrations sr on spct.person_id = sr.person_id
join persons p on p.person_id = sr.PERSON_ID
join li_TBL_USERNAMES tu on tu.usr_std_number = p.OEN_NUMBER
and sc.CLASS_CODE = spct.CLASS_CODE where sc.school_year = '20182019'
AND CONVERT(VARCHAR,spct.start_date,110) <= CONVERT(DATETIME,getDate(), 110)
AND CONVERT(VARCHAR,spct.end_date,110) >= CONVERT(DATETIME,getDate(), 110)
AND spct.COURSE_CODE in ('MPM1D0', 'MFM1P0')
AND sr.GRADE = '09'
