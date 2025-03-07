SELECT        Legal_first_name AS [FirstName], legal_surname AS [LastName], p.STUDENT_NO AS [ExternalDataReference], 'Secondary' AS [Level], sr.SCHOOL_CODE AS School, 
						 p.PREFERRED_FIRST_NAME AS [Preferred Given Name], 
                         p.PREFERRED_SURNAME AS [Preferred Legal Surname], p.BIRTH_DATE AS [Date of Birth], sr.GRADE AS Grade, p.STATUS_IN_CANADA_CODE AS [Status in Canada], p.OEN_NUMBER AS [Student OEN Number], 
                         p.STUDENT_NO AS [Student Number], CASE WHEN p.LITERACY_TEST_CODE IN (0, 3) 
                         THEN 'Not Taken' WHEN p.LITERACY_TEST_CODE = 1 THEN 'Successful' WHEN p.LITERACY_TEST_CODE = 2 THEN 'Not Successful' END AS [Literacy Test], 
                         CASE WHEN p.NATIVE_FLAG = 1 THEN 'First Nation' WHEN p.NATIVE_FLAG = 2 THEN 'Inuit' WHEN p.NATIVE_FLAG = 2 THEN 'Métis' ELSE 'N/A' END AS FNMI, sr.STATUS_INDICATOR_CODE AS [Status in School], 
                         ltu.usr_username + '@limestone.on.ca' AS EMAIL, sr.IEP_FLAG AS [IEP_FLAG], CONVERT(char(10), dbo.English_Stream(p.PERSON_ID)) AS [English Stream], 
						 CONVERT(char(10), dbo.Math_Stream(p.PERSON_ID)) AS [Math Stream],
						  '' AS [Home School]

FROM            dbo.PERSONS AS p INNER JOIN
                         dbo.li_TBL_USERNAMES AS ltu ON ltu.usr_std_number = p.OEN_NUMBER INNER JOIN
                         dbo.STUDENT_REGISTRATIONS AS sr ON p.PERSON_ID = sr.PERSON_ID AND p.STUDENT_NO = sr.STUDENT_NO
WHERE        (sr.SCHOOL_YEAR = 20202021) AND (sr.SCHOOL_CODE IN ('BAYSS', 'BAYLC', 'ERNST', 'FROSS', 'GRECS', 'GATEW', 'KINGS', 'KICVI', 'LSOCE', 'LSOCN', 'LASSS', 'LOCVI', 'NORAS', 'NAPDI', 'QECVI', 'SYDHS')) AND 
						(sr.PERSON_ID NOT IN (select person_id from student_registrations where school_year = '20202021' and school_code = 'RLSVL')) AND
                         (sr.STATUS_INDICATOR_CODE IN ('Active', 'Prereg')) AND (p.OEN_NUMBER IS NOT NULL) AND (p.OEN_NUMBER <> '')
UNION
SELECT        Legal_first_name AS [FirstName], legal_surname AS [LastName], p.STUDENT_NO AS [ExternalDataReference], 'Secondary' AS [Level], sr.SCHOOL_CODE AS School, 
						 p.PREFERRED_FIRST_NAME AS [Preferred Given Name], 
                         p.PREFERRED_SURNAME AS [Preferred Legal Surname], p.BIRTH_DATE AS [Date of Birth], sr.GRADE AS Grade, p.STATUS_IN_CANADA_CODE AS [Status in Canada], p.OEN_NUMBER AS [Student OEN Number], 
                         p.STUDENT_NO AS [Student Number], CASE WHEN p.LITERACY_TEST_CODE IN (0, 3) 
                         THEN 'Not Taken' WHEN p.LITERACY_TEST_CODE = 1 THEN 'Successful' WHEN p.LITERACY_TEST_CODE = 2 THEN 'Not Successful' END AS [Literacy Test], 
                         CASE WHEN p.NATIVE_FLAG = 1 THEN 'First Nation' WHEN p.NATIVE_FLAG = 2 THEN 'Inuit' WHEN p.NATIVE_FLAG = 2 THEN 'Métis' ELSE 'N/A' END AS FNMI, sr.STATUS_INDICATOR_CODE AS [Status in School], 
                         ltu.usr_username + '@limestone.on.ca' AS EMAIL, sr.IEP_FLAG AS [IEP_FLAG], 
						 CONVERT(char(10), dbo.English_Stream(p.PERSON_ID)) AS [English Stream], CONVERT(char(10), dbo.Math_Stream(p.PERSON_ID)) AS [Math Stream],

						 CASE WHEN sr.SCHOOL_CODE IN ('RLSVL') 
                         THEN (select top 1  school_code from STUDENT_REGISTRATIONS where school_year = sr.SCHOOL_YEAR AND person_id = p.PERSON_ID AND school_code not in ('RLSVL')) ELSE '' END AS [Home School]

FROM            dbo.PERSONS AS p INNER JOIN
                         dbo.li_TBL_USERNAMES AS ltu ON ltu.usr_std_number = p.OEN_NUMBER INNER JOIN
                         dbo.STUDENT_REGISTRATIONS AS sr ON p.PERSON_ID = sr.PERSON_ID AND p.STUDENT_NO = sr.STUDENT_NO
WHERE        (sr.SCHOOL_YEAR = 20202021) AND (sr.SCHOOL_CODE IN ('RLSVL')) AND 
                         (sr.STATUS_INDICATOR_CODE IN ('Active', 'Prereg')) AND (p.OEN_NUMBER IS NOT NULL) AND (p.OEN_NUMBER <> '')