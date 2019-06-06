select 
a1.ATD_Def_Replacement_Employee AS EIN, 
a2.Emp_First_Name AS FIRST_NAME, 
a2.Emp_Last_Name AS LAST_NAME, 
a2.Emp_Email_Address AS EMAIL, 
a1.ATD_Def_Location AS LOC_CD, 
a3.Loc_Description  AS SCHOOL_NAME,
'' AS 'GROUP',
'' AS EST_CD,
a1.ATD_Absence_Position,
a4.Position_Description AS JOB_DESCR,
'1' AS POS_FTE,
CONVERT(varchar, ATD_Start_Date, 112) AS POS_ST_DT
FROM PRATDHDR a1
join PREMPMAS a2 on a1.ATD_Def_Replacement_Employee = a2.Employee
join PRLOCTBL a3 on a3.Location = a1.ATD_Def_Location
join PRPOSTBL a4 on a1.ATD_Absence_Position = a4.Position
where ATD_Start_Date like '%2019%'