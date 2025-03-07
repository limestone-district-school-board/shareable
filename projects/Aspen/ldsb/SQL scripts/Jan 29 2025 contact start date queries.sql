update student_contact
set CTJ_FIELDA_009 = CAST(DATEADD(MILLISECOND, CAST(RIGHT(CTJ_LAST_MODIFIED, 3) AS SMALLINT), 
DATEADD(SECOND, CTJ_LAST_MODIFIED / 1000, '1970-01-01'))  AS DATETIME2(3))
where (CTJ_FIELDA_009 IS NULL OR CTJ_FIELDA_009 = '') 

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_contact
where (CTJ_FIELDA_009 IS NULL OR CTJ_FIELDA_009 = '') 

select count(*) from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_contact
where (CTJ_FIELDA_009 IS NULL OR CTJ_FIELDA_009 = '') 

select count(*) from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_contact
where CTJ_FIELDA_009 LIKE '2024%'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_contact
where CTJ_LAST_MODIFIED > '1738593923000' 

-- lets check to see if any start dates have been auto populated

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_contact
where CTJ_FIELDA_009 > '2025-01-23'

