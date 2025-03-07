-- debug ers

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_PSN_OID = 'PSN0000006l09q'
select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_OID = 'STD000000lYZbc'

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_NAME_LAST = 'Hewitt' and PSN_NAME_FIRST = 'Dieselle'


-- Hewitt: STD0000006l09t
select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'asd00000000ERS' and asm_std_oid = 'STD0000006l09t'
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_std_oid = 'STD0000006D38c'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'asd00000000ERS' and ASM_FIELDA_002 IS NOT NULL

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].assessment_definition

-- find all students with assessmsnt but no ERS created

select asm_std_oid from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
--and ASM_SKL_OID = 'skl01000000002'
where ASM_ASD_OID in ('ASD000000QyTnf',
'ASD000000QypQd',
'ASD000000RBtKH',
'ASD000000RBtLX',
'ASD000000RBtMj',
'ASD000000TcBHV',
'ASD000000OuZKM',
'ASD000000RBtPx',
'ASD000000Ncwdp',
'ASD000000Ut7Vf',
'ASD000000Uu6xW',
'ASD000000Uu6y1',
'ASD000000Uu6yS',
'ASD000000Uu6yu',
'ASD000000hYG2a',
'ASD000000Ut8EL',
'ASD000000hLn6D',
'ASD000000Uu6zL')
and ASM_DATE > '2024-09-05'
and ASM_STD_OID NOT IN (select ASM_STD_OID from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
where ASM_DATE > '2024-09-05' and ASM_ASD_OID = 'asd00000000ERS')
and ASM_GRADE_LEVEL_CODE IN ('SK', '01', '02')
and ASM_FIELDA_016 = '0'
--and ASM_STD_OID = 'STD0000007Prob'
GROUP by ASM_STD_OID
HAVING COUNT(ASM_STD_OID) > 1

-- FR

select asm_std_oid from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
--and ASM_SKL_OID = 'skl01000000002'
where ASM_ASD_OID in (
'ASD000000Ut7Vf',
'ASD000000Uu6xW',
'ASD000000Uu6y1',
 'ASD000000Uu6yS',
 'ASD000000Uu6yu',
 'ASD000000hYG2a',
 'ASD000000Ut8EL',
 'ASD000000hLn6D',
 'ASD000000Uu6zL'
)
and ASM_DATE > '2024-09-05'
and ASM_STD_OID NOT IN (select ASM_STD_OID from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
where ASM_DATE > '2024-09-05' and ASM_ASD_OID = 'asd00000000ERS')
and ASM_GRADE_LEVEL_CODE IN ('SK', '01', '02')
and ASM_FIELDA_016 = '0'
--and ASM_STD_OID = 'STD0000007Prob'
GROUP by ASM_STD_OID
HAVING COUNT(ASM_STD_OID) > 1


---- how many ERS records do not match respective assessment dates

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_std_oid = 'STD000000hHOGI'

select sa1.ASM_STD_OID, sa1.ASM_ASD_OID, sa1.ASM_DATE, sa2.ASM_DATE from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment sa1
join [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment sa2
ON sa1.ASM_STD_OID = sa2.ASM_STD_OID AND sa1.ASM_GRADE_LEVEL_CODE = sa2.ASM_GRADE_LEVEL_CODE
where sa1.ASM_ASD_OID in ('ASD000000QyTnf',
'ASD000000RBtLX',
'ASD000000OuZKM',
'ASD000000RBtPx')
and sa2.ASM_ASD_OID in ('asd00000000ERS')
and sa1.ASM_DATE > '2024-09-05'
and sa2.ASM_DATE > '2024-09-05'
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> 'MID')
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> 'END')
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> '1')
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> '2')
--and sa1.ASM_DATE < '2024-12-01'
and sa2.ASM_DATE < '2024-11-30'
and sa1.ASM_DATE <> sa2.ASM_DATE 
ORDER BY sa1.ASM_STD_OID, sa1.ASM_GRADE_LEVEL_CODE, sa1.ASM_DATE;

---- FR

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment sa1
join [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment sa2
ON sa1.ASM_STD_OID = sa2.ASM_STD_OID AND sa1.ASM_GRADE_LEVEL_CODE = sa1.ASM_GRADE_LEVEL_CODE AND sa1.ASM_DATE <> sa2.ASM_DATE
where sa1.ASM_ASD_OID in (
'ASD000000Ut7Vf',
 'ASD000000Uu6yS',
 'ASD000000Ut8EL'
)
and sa2.ASM_ASD_OID in ('asd00000000ERS')
and sa1.ASM_DATE > '2024-09-05'
and sa2.ASM_DATE > '2024-09-05'
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> 'MID')
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> 'END')
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> '1')
and (sa1.ASM_FIELDA_001 IS NULL OR TRIM(UPPER(sa1.ASM_FIELDA_001)) <> '2')
--and sa1.ASM_DATE < '2024-12-01'
and sa2.ASM_DATE < '2024-11-30'
and sa1.ASM_DATE <> sa2.ASM_DATE 
ORDER BY sa1.ASM_STD_OID, sa1.ASM_GRADE_LEVEL_CODE, sa1.ASM_DATE;

--- remove all ERS records after Sept 01 2024

select count(*) from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
where ASM_ASD_OID = 'asd00000000ERS'
and ASM_DATE > '2024-09-05'

select distinct ASM_STD_OID from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
where ASM_ASD_OID = 'asd00000000ERS'
and ASM_DATE > '2024-09-01'
and ASM_DATE < '2024-09-06'
and ASM_FIELDA_004 = 'BEG'

select count(*) from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
where ASM_ASD_OID = 'asd00000000ERS'
and ASM_DATE > '2024-11-30'


delete from student_assessment
where ASM_ASD_OID = 'asd00000000ERS'
and ASM_DATE > '2024-09-05'


select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
where 
ASM_STD_OID = 'STD000000dBnlj'
AND ASM_ASD_OID = 'asd00000000ERS'
and ASM_DATE > '2024-09-05'
and ASM_FIELDA_004 = 'BEG'
-- STD000000dBnlj