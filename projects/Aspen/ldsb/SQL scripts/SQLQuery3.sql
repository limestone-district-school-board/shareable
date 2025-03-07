select distinct tu.usr_username, p.OEN_NUMBER from persons p
join student_registrations sr on p.person_id = sr.PERSON_ID
join li_TBL_USERNAMES tu on tu.usr_std_number = p.OEN_NUMBER
where sr.school_year = '20202021'
--AND CONVERT(VARCHAR,spct.start_date,110) <= CONVERT(DATETIME,getDate(), 110)
--AND CONVERT(VARCHAR,spct.end_date,110) >= CONVERT(DATETIME,getDate(), 110)
AND sr.GRADE = '10'
AND sr.SCHOOL_CODE = 'SYDHS'
AND sr.status_indicator_code = 'Active'
--AND sr.school_year_track = 'Standard'


select * from student_registrations where person_id in ( select person_id from persons where OEN_NUMBER = '490840055')
AND school_year = '20202021'


select * from student_program_class_tracks where person_id in (select person_id from persons where student_no = '314961079')
AND school_year = '20202021'

