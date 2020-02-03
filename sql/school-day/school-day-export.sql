
SET NOCOUNT ON

select '"BOARDCODE"','"SCHOOLCODE"','"TYPE"','"ACTION"','"PERSONID"','"FIRSTNAME"','"SURNAME"','"CLASSCODE"','"EMAIL"','"HOMEROOM"','"NEWBOARDCODE"','"NEWSCHOOLCODE"'

SELECT '"' + 'LDSB' + '"'					AS BOARDCODE
    , '"' + b.school_code + '"'				AS SCHOOLCODE
    , '"' + 'Student' + '"'					AS TYPE
    , '"' + 'A' + '"'						AS ACTION --, CASE d.status_indicator_code WHEN 'Active' THEN 'A' WHEN 'PreReg' THEN 'A' END AS Action
    , '"' + c.student_no + '"'				AS PERSONID
    , '"' + c.preferred_first_name + '"'	AS FIRSTNAME
    , '"' + c.preferred_surname + '"'		AS SURNAME
    , '"' + IsNull(b.class_code, '') + '"'				AS CLASSCODE
    --, '"' + '1' + '"'						AS Semester
    --, '"' + convert(VARCHAR, a.start_date, 120) + '"'              AS Start_date
    --, '"' + convert(VARCHAR, a.end_date, 120) + '"'                AS End_date
    , '"' + '' + '"'                        AS EMAIL
    , '"' + IsNull(b.class_code, '') + '"'	AS HOMEROOM
	, '"' + '' + '"'                        AS NEWBOARDCODE
	, '"' + '' + '"'                        AS NEWSCHOOLCODE

    --, null /*a.last_update_date_time*/ AS Last_update_date_time
  FROM student_program_class_tracks a
    , school_classes b    
    , persons c
    --, persons e
    , student_registrations d
  WHERE a.class_code         = b.class_code
  AND a.school_code          = b.school_code
  AND a.school_year          = b.school_year
  AND a.person_id            = c.person_id
  --AND b.reporting_teacher	 = e.person_id
  AND a.school_code          = d.school_code
  AND a.school_year          = d.school_year
  AND a.person_id            = d.person_id
  AND a.school_year          = '20192020'
  AND b.take_attendance_flag = 'x'
  AND d.status_indicator_code in ('Active', 'PreReg')
  AND a.school_code in ('SVIEW','ENTPR','TAMWH','SELBY','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC', 'BAYRI',
   'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA')
  --AND CONVERT(DATETIME,a.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
  --AND CONVERT(DATETIME,a.end_date, 110)		>= CONVERT(DATETIME, getDate(), 110)
  AND CONVERT(VARCHAR,a.start_date,110)   >= CONVERT(DATETIME,'03-SEP-2019', 110)
  AND CONVERT(VARCHAR,a.end_date,110)    <= CONVERT(DATETIME,'30-JUN-2020', 110)
  
  -- secondary students
  UNION
   SELECT '"' + 'LDSB' + '"'				AS BOARDCODE
    , '"' + b.school_code + '"'				AS SCHOOLCODE
    , '"' + 'Student' + '"'					AS TYPE
    , '"' + 'A' + '"'						AS ACTION --, CASE d.status_indicator_code WHEN 'Active' THEN 'A' WHEN 'PreReg' THEN 'A' END AS Action
    , '"' + c.student_no + '"'				AS PERSONID
    , '"' + c.preferred_first_name + '"'	AS FIRSTNAME
    , '"' + c.preferred_surname + '"'		AS SURNAME
    , '"' + IsNull(b.class_code, '') + '"'	AS CLASSCODE
    --, '"' + '1' + '"'						AS Semester
    --, '"' + convert(VARCHAR, a.start_date, 120) + '"'              AS Start_date
    --, '"' + convert(VARCHAR, a.end_date, 120) + '"'                AS End_date
    , '"' + '' + '"'                        AS EMAIL
    , '"' + IsNull((Select top 1
		student_program_class_tracks.class_code
		From school_classes
		LEFT Outer Join persons
		On school_classes.reporting_teacher = persons.person_id
		inner join student_program_class_tracks
		on   ( student_program_class_tracks.school_code = school_classes.school_code )
		And ( student_program_class_tracks.school_year = school_classes.school_year )
		And ( student_program_class_tracks.class_code = school_classes.class_code )
		Where
		(student_program_class_tracks.school_code = a.SCHOOL_CODE)
		And (student_program_class_tracks.school_year = a.SCHOOL_YEAR)
		And (student_program_class_tracks.person_id = a.PERSON_ID)
		--And (school_classes.class_homeroom_flag = 'p')
		--And (student_program_class_tracks.start_date <= a_effective_date )
		--And (student_program_class_tracks.end_date >= a_effective_date
		And (CONVERT(VARCHAR,student_program_class_tracks.start_date,110) >= CONVERT(DATETIME,'30-JAN-2020', 110))
		And (CONVERT(VARCHAR,student_program_class_tracks.end_date,110) <= CONVERT(DATETIME,'30-JUN-2020', 110)
		--AND CONVERT(DATETIME,student_program_class_tracks.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
		--AND CONVERT(DATETIME,student_program_class_tracks.end_date, 110)	>= CONVERT(DATETIME, getDate(), 110)
			Or student_program_class_tracks.end_date Is Null)), '') + '"'	AS HOMEROOM
	, '"' + '' + '"'                        AS NEWBOARDCODE
	, '"' + '' + '"'                        AS NEWSCHOOLCODE

  FROM student_program_class_tracks a
    , school_classes b    
    , persons c
    --, persons e
	, term_teachers tt
    , student_registrations d
  WHERE a.class_code         = b.class_code
  AND a.school_code          = b.school_code
  AND a.school_year          = b.school_year
  AND a.person_id            = c.person_id
  --AND b.reporting_teacher	 = e.person_id
  AND a.school_code          = d.school_code
  AND a.school_year          = d.school_year
  AND a.person_id            = d.person_id
  AND a.school_year          = '20192020'
  AND tt.school_code = b.school_code
  AND tt.school_year = b.school_year
  AND tt.semester = 2
  --AND b.take_attendance_flag = 'x'
  AND d.status_indicator_code in ('Active', 'PreReg')
  AND a.school_code in ('NAPDI','GRECS', 'BAYSS', 'FROSS')
  AND CONVERT(DATETIME,a.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
  AND CONVERT(DATETIME,a.end_date, 110)		>= CONVERT(DATETIME, getDate(), 110)
  AND CONVERT(VARCHAR,a.start_date,110)   >= CONVERT(DATETIME,'30-JAN-2020', 110)
  AND CONVERT(VARCHAR,a.end_date,110)    <= CONVERT(DATETIME,'30-JUN-2020', 110)

-- get Secondary students who are in full year courses
UNION
     SELECT '"' + 'LDSB' + '"'				AS BOARDCODE
    , '"' + b.school_code + '"'				AS SCHOOLCODE
    , '"' + 'Student' + '"'					AS TYPE
    , '"' + 'A' + '"'						AS ACTION --, CASE d.status_indicator_code WHEN 'Active' THEN 'A' WHEN 'PreReg' THEN 'A' END AS Action
    , '"' + c.student_no + '"'				AS PERSONID
    , '"' + c.preferred_first_name + '"'	AS FIRSTNAME
    , '"' + c.preferred_surname + '"'		AS SURNAME
    , '"' + IsNull(b.class_code, '') + '"'	AS CLASSCODE
    --, '"' + '1' + '"'						AS Semester
    --, '"' + convert(VARCHAR, a.start_date, 120) + '"'              AS Start_date
    --, '"' + convert(VARCHAR, a.end_date, 120) + '"'                AS End_date
    , '"' + '' + '"'                        AS EMAIL
    , '"' + IsNull((Select top 1
		student_program_class_tracks.class_code
		From school_classes
		LEFT Outer Join persons
		On school_classes.reporting_teacher = persons.person_id
		inner join student_program_class_tracks
		on   ( student_program_class_tracks.school_code = school_classes.school_code )
		And ( student_program_class_tracks.school_year = school_classes.school_year )
		And ( student_program_class_tracks.class_code = school_classes.class_code )
		Where
		(student_program_class_tracks.school_code = a.SCHOOL_CODE)
		And (student_program_class_tracks.school_year = a.SCHOOL_YEAR)
		And (student_program_class_tracks.person_id = a.PERSON_ID)
		--And (school_classes.class_homeroom_flag = 'p')
		--And (student_program_class_tracks.start_date <= a_effective_date )
		--And (student_program_class_tracks.end_date >= a_effective_date
		And (CONVERT(VARCHAR,student_program_class_tracks.start_date,110) >= CONVERT(DATETIME,'03-SEP-2019', 110))
		And (CONVERT(VARCHAR,student_program_class_tracks.end_date,110) <= CONVERT(DATETIME,'30-JUN-2020', 110)
		--AND CONVERT(DATETIME,student_program_class_tracks.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
		--AND CONVERT(DATETIME,student_program_class_tracks.end_date, 110)	>= CONVERT(DATETIME, getDate(), 110)
			Or student_program_class_tracks.end_date Is Null)), '') + '"'	AS HOMEROOM
	, '"' + '' + '"'                        AS NEWBOARDCODE
	, '"' + '' + '"'                        AS NEWSCHOOLCODE

  FROM student_program_class_tracks a
    , school_classes b    
    , persons c
    --, persons e
    , student_registrations d
  WHERE a.class_code         = b.class_code
  AND a.school_code          = b.school_code
  AND a.school_year          = b.school_year
  AND a.person_id            = c.person_id
  --AND b.reporting_teacher	 = e.person_id
  AND a.school_code          = d.school_code
  AND a.school_year          = d.school_year
  AND a.person_id            = d.person_id
  AND a.school_year          = '20192020'
  --AND b.take_attendance_flag = 'x'
  AND d.status_indicator_code in ('Active', 'PreReg')
  AND a.school_code in ('NAPDI','GRECS', 'BAYSS', 'FROSS')
  AND CONVERT(DATETIME,a.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
  AND CONVERT(DATETIME,a.end_date, 110)		>= CONVERT(DATETIME, getDate(), 110)
  AND CONVERT(VARCHAR,a.start_date,110) >= CONVERT(DATETIME,'03-SEP-2019', 110)
  AND CONVERT(VARCHAR,a.end_date,110) = CONVERT(DATETIME,'26-JUN-2020', 110)
  AND (a.class_code like '%BLC%' OR a.class_code like '%ADULT%')

  UNION
  --Selecting Elem class information
  SELECT DISTINCT '"' + 'LDSB' + '"'		AS BOARDCODE 
    , '"' + a.school_code + '"'				AS SCHOOLCODE
    , '"' + 'Classroom' + '"'				AS TYPE
    , '"' + 'A' + '"'						AS ACTION
    , '"' + '' + '"'						AS PERSONID 
    , '"' + '' + '"'						AS FIRSTNAME
    , '"' + '' + '"'						AS SURNAME 
    , '"' + a.class_code + '"'				AS CLASSCODE
    , '"' + '' + '"'						AS EMAIL
    --, t.semester_start        AS Semester_Start
    --, t.semester_end          AS Semester_End
    , '"' + '' + '"'						AS HOMEROOM
	, '"' + '' + '"'                        AS NEWBOARDCODE
	, '"' + '' + '"'                        AS NEWSCHOOLCODE
  FROM school_classes a
    --, course_codes c
    , STUDENT_PROGRAM_CLASS_TRACKS t
  WHERE 
  -- a.class_code              = c.course_code
  -- AND a.school_year         = c.school_year
  -- AND a.school_code         = c.school_code
  a.school_code         = t.school_code
  AND a.school_year         = t.school_year
  AND a.class_code          = t.class_code
  AND CONVERT(VARCHAR,t.start_date,110) >= CONVERT(DATETIME,'03-SEP-2019', 110)
  AND CONVERT(VARCHAR,t.end_date,110) <= CONVERT(DATETIME,'30-JUN-2020', 110)
  --AND CONVERT(DATETIME,t.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
  --AND CONVERT(DATETIME,t.end_date, 110)	>= CONVERT(DATETIME, getDate(), 110)
  AND a.school_code in ('SVIEW','ENTPR','TAMWH','SELBY','NAPDI','GRECS','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC',
   'BAYSS', 'BAYRI', 'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'FROSS', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA')
  AND a.take_attendance_flag = 'x'

  UNION
select distinct 
'"' + 'LDSB' + '"' 				AS BOARDCODE
    , '"' + e.school_code + '"' 					AS SCHOOLCODE
    , '"' + 'Teacher' + '"' 						AS TYPE
    , '"' + 'A' + '"' 								AS ACTION
    , '"' + d.person_id + '"' 						AS PERSONID
    , '"' + d.preferred_first_name + '"'  			AS FIRSTNAME
    , '"' + d.preferred_surname + '"' 				AS SURNAME
    , '"' + e.class_code + '"' 						AS CLASSCODE
    , '"' + b.usr_username + '@limestone.on.ca"' 	AS Email
    , '"' + '' + '"' 								AS HOMEROOM
, '"' + '' + '"'                        			AS NEWBOARDCODE
, '"' + '' + '"'                        			AS NEWSCHOOLCODE
from school_staff ss

join TERM_TEACHERS tt
on ss.person_id = tt.person_id and 
ss.school_code = tt.school_code and
ss.school_year = tt.school_year

join school_classes e on
tt.school_code = e.school_code and
tt.school_year = e.school_year
and tt.class_code = e.class_code

join persons d on
d.person_id = ss.person_id

join LI_TBL_USERNAMES b
on b.usr_emp_number = d.staff_no
 
where ss.school_code in ('SVIEW','ENTPR','TAMWH','SELBY','NAPDI','GRECS','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC',
 'BAYSS', 'BAYRI', 'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'FROSS', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA')
 and ss.school_year = '20192020' AND
staff_type_name IN ('Teacher','Teacher LTO', 'ECE', 'Occasional Teach', 'Teacher SPED')
AND ss.FTE = (select MAX(FTE) from school_staff where person_id = d.person_id and school_year = '20192020'
				AND school_code in ('SVIEW','ENTPR','TAMWH','SELBY','NAPDI','GRECS','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC',
				'BAYSS', 'BAYRI', 'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'FROSS', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA'))

UNION

SELECT DISTINCT '"' + 'LDSB' + '"' 				AS BOARDCODE
    , '"' + c.school_code + '"' 					AS SCHOOLCODE
    , '"' + 'Teacher' + '"' 						AS TYPE
    , '"' + 'A' + '"' 								AS ACTION
    , '"' + d.person_id + '"' 						AS PERSONID
    , '"' + d.preferred_first_name + '"'  			AS FIRSTNAME
    , '"' + d.preferred_surname + '"' 				AS SURNAME
    , '"' + '' + '"'  								AS CLASSCODE
    , '"' + b.usr_username + '@limestone.on.ca"' 	AS Email
    , '"' + '' + '"' 								AS HOMEROOM
	, '"' + '' + '"'                        		AS NEWBOARDCODE
	, '"' + '' + '"'                        		AS NEWSCHOOLCODE
  FROM LI_TBL_USERNAMES b
 
inner join persons d on
	d.staff_no = b.usr_emp_number
 
inner join school_staff c on
	c.person_id = d.person_id 
	AND c.school_year = '20192020'
	AND d.person_id not in (select person_id from TERM_TEACHERS 
						where school_year = '20192020')
	AND c.FTE = (select MAX(FTE) from school_staff where person_id = d.person_id and school_year = '20192020'
				AND school_code in ('SVIEW','ENTPR','TAMWH','SELBY','NAPDI','GRECS','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC',
				'BAYSS', 'BAYRI', 'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'FROSS', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA'))
--and
--c.school_code = e.school_code and
--c.school_year = e.school_year
  WHERE
  --e.REPORTING_TEACHER IS NULL AND
  c.staff_type_name IN ('Teacher','Teacher LTO', 'ECE', 'Occasional Teach', 'Teacher SPED')
  AND CONVERT(DATETIME,c.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
  AND CONVERT(DATETIME,c.end_date, 110)		>= CONVERT(DATETIME, getDate(), 110)
  AND CONVERT(VARCHAR,c.start_date,110)   >= CONVERT(DATETIME,'03-SEP-2019', 110)
  AND CONVERT(VARCHAR,c.end_date,110)    <= CONVERT(DATETIME,'30-JUN-2020', 110)
  AND c.school_code in ('SVIEW','ENTPR','TAMWH','SELBY','NAPDI','GRECS','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC',
   'BAYSS', 'BAYRI', 'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'FROSS', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA')

--------------------

SELECT DISTINCT '"' + 'LDSB' + '"' 				AS BOARDCODE
    , '"' + c.school_code + '"' 					AS SCHOOLCODE
    , '"' + 'Teacher' + '"' 						AS TYPE
    , '"' + 'A' + '"' 								AS ACTION
    , '"' + d.person_id + '"' 						AS PERSONID
    , '"' + d.preferred_first_name + '"'  			AS FIRSTNAME
    , '"' + d.preferred_surname + '"' 				AS SURNAME
    , '"' + e.class_code + '"' 						AS CLASSCODE
    , '"' + b.usr_username + '@limestone.on.ca"' 	AS Email
    , '"' + '' + '"' 								AS HOMEROOM
	, '"' + '' + '"'                        		AS NEWBOARDCODE
	, '"' + '' + '"'                        		AS NEWSCHOOLCODE
  FROM LI_TBL_USERNAMES b
 
inner join persons d on
	d.staff_no = b.usr_emp_number
 
inner join fs_onsis_teachers fot on
	fot.person_id = d.person_id
 
inner join school_classes e on
fot.school_code = e.school_code and
fot.school_year = e.school_year and
fot.class_code = e.class_code
 
inner join school_staff c on
c.person_id = d.person_id and
c.school_code = e.school_code and
c.school_year = e.school_year and
c.FTE = (select MAX(FTE) from school_staff where person_id = d.person_id and school_year = '20192020'
				AND school_code in ('SVIEW','ENTPR','TAMWH','SELBY','NAPDI','GRECS','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC',
				'BAYSS', 'BAYRI', 'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'FROSS', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA'))

WHERE
c.person_id in (select person_id from fs_onsis_teachers where school_year = '20192020') -- AND CONVERT(DATETIME,effective_date, 110) <= CONVERT(DATETIME, getDate(), 110))
--AND c.person_id not in (select person_id from term_teachers where school_year = '20192020')
AND c.school_year = '20192020'
AND c.staff_type_name IN ('ECE')
--AND CONVERT(DATETIME,c.start_date, 110)   <= CONVERT(DATETIME, getDate(), 110)
--AND CONVERT(DATETIME,c.end_date, 110)		>= CONVERT(DATETIME, getDate(), 110)
AND CONVERT(VARCHAR,c.start_date,110)   >= CONVERT(DATETIME,'03-SEP-2019', 110)
AND CONVERT(VARCHAR,c.end_date,110)    <= CONVERT(DATETIME,'30-JUN-2020', 110)
AND c.school_code in ('SVIEW','ENTPR','TAMWH','SELBY','NAPDI','GRECS','PRCHL','NWBGH','CTRVL','GRECE','LANOL','CLARC',
 'BAYSS', 'BAYRI', 'CATAR', 'LANCD', 'RGSIN', 'TRUED', 'FROSS', 'EDACA', 'COLLB', 'JRHEN', 'HOLSG', 'WELBA')
  
SET NOCOUNT OFF