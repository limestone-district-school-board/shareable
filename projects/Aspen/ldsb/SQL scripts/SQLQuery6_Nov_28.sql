select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info where usr_login_name = 'zainabm@keys.ca'
OR usr_login_name = 'nataliam@keys.ca'
OR usr_login_name = 'manalshalabi@keys.ca'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].contact where cnt_psn_oid = 'PSN000000Kqv0C'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info where USR_PSN_OID = 'psn01000226229'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info where usr_name_view like '%Abdali, TYLER%'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_OID = 'std01000065539'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_FIELDA_083 IS NOT NULL

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_PSN_OID = 'PSN000000dBnlg'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].staff where STF_OID = 'stf01000002066'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_OID = 'PSN0000009h0Ws'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].COMMENT_BANK_CODE

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_NAME_LAST = 'Bourke' and PSN_NAME_FIRST = 'Adeline'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_program_participation where PGM_DDX_OID like '%Rem%'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_program_participation where PGM_PROGRAM_CODE like '%Empower%'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_program_participation where PGM_START_DATE > getdate() - 3

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].school where skl_school_name like '%Amherstview%'


select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person_address where ADR_OID = 'adr07000000011'

select addr.ADR_ADDRESS_LINE_02 from [ASPEN].[AspenDB-LDSB-PD].[dbo].school sch
join [ASPEN].[AspenDB-LDSB-PD].[dbo].person_address addr
on sch.SKL_ADR_OID = addr.ADR_OID
where addr.ADR_ADDRESS_LINE_02 IS NOT NULL


select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_enrollment se
join [ASPEN].[AspenDB-LDSB-PD].[dbo].student st
on st.STD_OID = se.ENR_STD_OID
where se.enr_skl_oid = 'skl01000000078'

select distinct ENR_FIELDC_001, ENR_FIELDA_008, ENR_FIELDA_009 from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_enrollment


select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].school where skl_oid = 'skl01000000004';

select distinct usr_login_status from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info 

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_case where stc_name_view like '%O''Connor, Reegan%'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_case where stc_name_view like '%Connor%'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_case_person where scp_fieldb_004 = 'Shaer Zadeh' --scp_stc_oid = 'STC000000bSqbZ'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].contact

-- assessments

-- ACA SK BEG ASD000000QyTnf
-- FR ACA BEG ASD000000Ut7Vf
-- Early reading asd00000000ERS

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_PSN_OID = 'PSN000000hgDug'
select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_OID = 'STD000000Po4xs'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_FIELDA_081 = '1'

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_NAME_LAST = 'Smijan' and PSN_NAME_FIRST = 'Kylee'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_std_oid = 'STD000000dBnlj'
and asm_asd_oid = 'asd00000000ERS'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].assessment_definition where asd_oid = 'ASD000000OuZKM'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'asd00000000ERS'
and ASM_LAST_MODIFIED > '1732460505000'
and ASM_GRADE_LEVEL_CODE = '06'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Ncwdp'
--and ASM_LAST_MODIFIED > '1732460505000'
and ASM_GRADE_LEVEL_CODE in ('03','04','05','06')

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'asd00000000ERS'
--and ASM_LAST_MODIFIED > '1732460505000'
and ASM_GRADE_LEVEL_CODE in ('03','04','05','06')


select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment 
where ASM_LAST_MODIFIED > '1732460505000'
and ASM_FIELDA_016 = '0'
and ASM_GRADE_LEVEL_CODE = 'SK'
and ASM_ASD_OID in 
('ASD000000Ut7Vf',
  'ASD000000Uu6xW',
 'ASD000000Uu6y1')
and ASM_STD_OID not in (select ASM_STD_OID from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where ASM_ASD_OID = 'asd00000000ERS'
and ASM_GRADE_LEVEL_CODE = 'SK'
and ASM_LAST_MODIFIED > '1732460505000')
order by ASM_DATE desc

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment 
where ASM_GRADE_LEVEL_CODE = 'SK'
and ASM_ASD_OID in 
( 'ASD000000Ut7Vf',
 'ASD000000Uu6xW',
 'ASD000000Uu6y1')
 and ASM_FIELDA_040 = '0'

select distinct ASM_DATE from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'asd00000000ERS'

select ASM_STD_OID, ASM_FIELDA_040 from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000QyTnf'
and ASM_GRADE_LEVEL_CODE = 'SK' and ASM_FIELDA_001 = 'BEG'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment  where asm_asd_oid = 'asd00000000ERS'
and ASM_SKL_OID = 'skl01000000037'
and ASM_LAST_MODIFIED > '1732460505000'

where ASM_FIELDA_040 IS NOT NULL
and ASM_FIELDA_040 IS NOT NULL

and ASM_DATE IS NOT NULL
and ASM_GRADE_LEVEL_CODE IS NOT NULL
and ASM_STD_OID IS NOT NULL

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_alert where alr_std_oid = 'std01000048418'

--- checking the early reading assessment

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000OuZKM'
and ASM_FIELDA_018 IS NULL
and ASM_DATE > '2024-09-05'
order by ASM_DATE desc

-- find this student
select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_OID = 'STD000000QxhWV'

delete from student_assessment where asm_asd_oid = 'asd00000000ERS' 
and ASM_GRADE_LEVEL_CODE IS NULL and ASM_DATE IS NULL

select * from student_assessment where asm_asd_oid = 'asd00000000ERS' 
and ASM_GRADE_LEVEL_CODE IS NULL and ASM_DATE IS NULL

select * from student_assessment where asm_asd_oid = 'asd00000000ERS' 
and ASM_DATE > '2024-09-05'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'asd00000000ERS'
and ASM_LAST_MODIFIED > '1732460505000'
and ASM_GRADE_LEVEL_CODE in ('03','04','05','06')

--- DDT

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
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
and ASM_DATE < '2025-01-05'
and ASM_FIELDA_001 = 'MID'
order by ASM_DATE asc
----



----- check if student has both fr and en assessments this year

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where ASM_STD_OID = 'STD000000hgDuj'
and ASM_DATE > '2024-09-05'

select st.STD_NAME_VIEW, st.STD_ID_LOCAL, st.STD_OID, sk.SKL_SCHOOL_NAME FROM [ASPEN].[AspenDB-LDSB-PD].[dbo].student st
join [ASPEN].[AspenDB-LDSB-PD].[dbo].school sk 
on st.STD_SKL_OID = sk.SKL_OID
where st.STD_OID in (
select ASM_STD_OID from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
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
GROUP BY ASM_STD_OID
HAVING COUNT(ASM_STD_OID) > 1)

SELECT st.STD_NAME_VIEW, st.STD_ID_LOCAL, st.STD_OID, sk.SKL_SCHOOL_NAME
FROM [ASPEN].[AspenDB-LDSB-PD].[dbo].student st
JOIN [ASPEN].[AspenDB-LDSB-PD].[dbo].school sk 
ON st.STD_SKL_OID = sk.SKL_OID
WHERE st.STD_OID IN (
    SELECT ASM_STD_OID 
    FROM [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
    WHERE ASM_ASD_OID IN (
        'ASD000000QyTnf', 'ASD000000QypQd', 'ASD000000RBtKH', 'ASD000000RBtLX',
        'ASD000000RBtMj', 'ASD000000TcBHV', 'ASD000000OuZKM', 'ASD000000RBtPx',
        'ASD000000Ncwdp', 'ASD000000Ut7Vf', 'ASD000000Uu6xW', 'ASD000000Uu6y1',
        'ASD000000Uu6yS', 'ASD000000Uu6yu', 'ASD000000hYG2a', 'ASD000000Ut8EL',
        'ASD000000hLn6D', 'ASD000000Uu6zL'
    )
    AND ASM_DATE > '2024-09-05'
)
GROUP BY st.STD_NAME_VIEW, st.STD_ID_LOCAL, st.STD_OID, sk.SKL_SCHOOL_NAME
HAVING COUNT(*) > 1;

SELECT st.STD_OID
FROM [ASPEN].[AspenDB-LDSB-PD].[dbo].student st
WHERE st.STD_OID IN (
    SELECT ASM_STD_OID 
    FROM [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
    WHERE ASM_ASD_OID IN (
        'ASD000000QyTnf', 'ASD000000QypQd', 'ASD000000RBtKH', 'ASD000000RBtLX',
        'ASD000000RBtMj', 'ASD000000TcBHV', 'ASD000000OuZKM', 'ASD000000RBtPx',
        'ASD000000Ncwdp', 'ASD000000Ut7Vf', 'ASD000000Uu6xW', 'ASD000000Uu6y1',
        'ASD000000Uu6yS', 'ASD000000Uu6yu', 'ASD000000hYG2a', 'ASD000000Ut8EL',
        'ASD000000hLn6D', 'ASD000000Uu6zL'
    )
    AND ASM_DATE > '2024-09-05'
	GROUP BY ASM_ASD_OID
	HAVING COUNT(ASM_ASD_OID) > 1
)



select STD_NAME_VIEW, STD_ID_LOCAL, STD_OID FROM [ASPEN].[AspenDB-LDSB-PD].[dbo].student
where STD_OID in (
SELECT ASM_STD_OID
FROM [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment
WHERE ASM_DATE > '2024-09-05'
  AND (CASE 
         WHEN ASM_ASD_OID IN ('ASD000000QyTnf',
'ASD000000QypQd',
'ASD000000RBtKH',
'ASD000000RBtLX',
'ASD000000RBtMj',
'ASD000000TcBHV',
'ASD000000OuZKM',
'ASD000000RBtPx',
'ASD000000Ncwdp') THEN 'Group1'
         WHEN ASM_ASD_OID IN ('ASD000000Ut7Vf',
'ASD000000Uu6xW',
'ASD000000Uu6y1',
'ASD000000Uu6yS',
'ASD000000Uu6yu',
'ASD000000hYG2a',
'ASD000000Ut8EL',
'ASD000000hLn6D',
'ASD000000Uu6zL') THEN 'Group2'
         ELSE NULL
       END) IS NOT NULL
GROUP BY ASM_STD_OID
HAVING COUNT(DISTINCT 
             CASE 
               WHEN ASM_ASD_OID IN ('ASD000000QyTnf',
'ASD000000QypQd',
'ASD000000RBtKH',
'ASD000000RBtLX',
'ASD000000RBtMj',
'ASD000000TcBHV',
'ASD000000OuZKM',
'ASD000000RBtPx',
'ASD000000Ncwdp') THEN 'Group1'
               WHEN ASM_ASD_OID IN ('ASD000000Ut7Vf',
'ASD000000Uu6xW',
'ASD000000Uu6y1',
'ASD000000Uu6yS',
'ASD000000Uu6yu',
'ASD000000hYG2a',
'ASD000000Ut8EL',
'ASD000000hLn6D',
'ASD000000Uu6zL') THEN 'Group2'
             END) = 2
)

----- let's find the transition data in student table.

select 
std_FieldA_073,
std_FieldA_071,
std_FieldA_075,
std_FieldA_074,
std_FieldA_076,
std_FieldA_050,
std_FieldA_070,
std_FieldA_066,
std_FieldA_077,
std_FieldA_078,
std_FieldD_001,
std_FieldB_023,
std_FieldB_013,
std_FieldB_020,
std_FieldB_019,
std_FieldB_021,
std_FieldB_030,
std_FieldB_031,
std_FieldB_081
from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where 
std_FieldA_073 is not null OR 
std_FieldA_071 is not null OR 
std_FieldA_075 is not null OR 
std_FieldA_074 is not null OR
std_FieldA_076 is not null OR
std_FieldA_050 is not null OR
std_FieldA_070 is not null OR
std_FieldA_066 is not null OR
std_FieldA_077 is not null OR
std_FieldA_078 is not null OR
--std_FieldD_001 is not null OR
std_FieldD_001 != '' OR
std_FieldB_023 is not null OR
std_FieldB_013 is not null OR
std_FieldB_020 is not null OR
std_FieldB_019 is not null OR
std_FieldB_021 is not null OR
std_FieldB_030 is not null OR
std_FieldB_031 is not null OR
std_FieldB_081 is not null


update [ASPEN].[AspenDB-LDSB-PD].[dbo].student set 
std_FieldA_073 = NULL,
std_FieldA_071 = NULL,
std_FieldA_075 = NULL, 
std_FieldA_074 = NULL,
std_FieldA_076 = NULL,
std_FieldA_050 = NULL,
std_FieldA_070 = NULL,
std_FieldA_066 = NULL,
std_FieldA_077 = NULL,
std_FieldA_078 = NULL,
std_FieldD_001 = NULL,
std_FieldB_023 = NULL,
std_FieldB_013 = NULL,
std_FieldB_020 = NULL,
std_FieldB_019 = NULL,
std_FieldB_021 = NULL,
std_FieldB_030 = NULL,
std_FieldB_031 = NULL,
std_FieldB_081 = NULL
where 
std_FieldA_073 is not null OR 
std_FieldA_071 is not null OR 
std_FieldA_075 is not null OR 
std_FieldA_074 is not null OR
std_FieldA_076 is not null OR
std_FieldA_050 is not null OR
std_FieldA_070 is not null OR
std_FieldA_066 is not null OR
std_FieldA_077 is not null OR
std_FieldA_078 is not null OR
std_FieldD_001 != '' OR
std_FieldB_023 is not null OR
std_FieldB_013 is not null OR
std_FieldB_020 is not null OR
std_FieldB_019 is not null OR
std_FieldB_021 is not null OR
std_FieldB_030 is not null OR
std_FieldB_031 is not null OR
std_FieldB_081 is not null

------ checking assessment removal

-- ASD000000QyTnf	Acadience Reading Screener SK-BEG
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000QyTnf'
and ASM_FIELDA_002 is null and ASM_FIELDA_012 is null

-- ASD000000QypQd	Acadience Reading Screener SK-MID
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000QypQd'
and ASM_FIELDA_002 is null and ASM_FIELDA_012 is null
and ASM_FIELDA_003 is null and ASM_FIELDA_004 is null

-- ASD000000RBtKH	Acadience Reading Screener SK-END
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000RBtKH'
and ASM_FIELDA_004 is null and ASM_FIELDA_012 is null
and ASM_FIELDA_003 is null

-- ASD000000RBtLX	Acadience Reading Screener GR 1-BEG
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000RBtLX'
and ASM_FIELDA_004 is null and ASM_FIELDA_012 is null
and ASM_FIELDA_003 is null

--ASD000000RBtMj	Acadience Reading Screener GR 1-MID
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000RBtMj'
and ASM_FIELDA_004 is null and ASM_FIELDA_005 is null
and ASM_FIELDA_006 is null

-- ASD000000TcBHV	Acadience Reading Screener GR 1-END
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000TcBHV'
and ASM_FIELDA_004 is null and ASM_FIELDA_005 is null
and ASM_FIELDA_006 is null

-- ASD000000OuZKM	Acadience Reading Screener GR 2 BEG
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000OuZKM'
and ASM_FIELDA_004 is null and ASM_FIELDA_005 is null
and ASM_FIELDA_006 is null

-- ASD000000RBtPx	Acadience Reading Screener GR 2-MID-END
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000RBtPx'
and ASM_FIELDA_008 is null and ASM_FIELDA_006 is null

-- ASD000000Ncwdp	Acadience Reading Screener GR 3 - 6
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Ncwdp'
and ASM_FIELDA_008 is null and ASM_FIELDA_006 is null

-- ASD000000Ncwdp	Acadience Reading Screener GR 3 - 6
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Ncwdp'
and ASM_FIELDA_010 IS NOT NULL



-- Acadience FR

-- ASD000000Ut7Vf	FRENCH - Acadience Reading Français (IDAPEL) SK-BEG
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Ut7Vf'
and ASM_FIELDA_010 is null and ASM_FIELDA_012 is null

-- ASD000000Uu6xW	FRENCH - Acadience Reading Français (IDAPEL) SK-MID
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Uu6xW'
and ASM_FIELDA_010 is null and ASM_FIELDA_012 is null
and ASM_FIELDA_003 is null and ASM_FIELDA_004 is null

-- ASD000000Uu6y1	FRENCH - Acadience Reading Français (IDAPEL) SK-END
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Uu6y1'
and ASM_FIELDA_004 is null and ASM_FIELDA_012 is null
and ASM_FIELDA_003 is null

-- ASD000000Uu6yS	FRENCH - Acadience Reading Français (IDAPEL) GR1-BEG
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Uu6yS'
and ASM_FIELDA_004 is null and ASM_FIELDA_012 is null
and ASM_FIELDA_003 is null


-- ASD000000Uu6yu	FRENCH - Acadience Reading Français (IDAPEL) GR1-MID
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Uu6yu'
and ASM_FIELDA_004 is null and ASM_FIELDA_005 is null
and ASM_FIELDA_002 is null

-- ASD000000hYG2a	FRENCH - Acadience Reading Français (IDAPEL) GR1-END
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000hYG2a'
and ASM_FIELDA_004 is null and ASM_FIELDA_005 is null
and ASM_FIELDA_002 is null

-- ASD000000Ut8EL	FRENCH - Acadience Reading Français (IDAPEL) GR2 BEG
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Ut8EL'
and ASM_FIELDA_004 is null and ASM_FIELDA_005 is null
and ASM_FIELDA_002 is null

-- ASD000000hLn6D	FRENCH - Acadience Reading Français (IDAPEL) GR2-MID
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000hLn6D'
and ASM_FIELDA_007 is null and ASM_FIELDA_002 is null

-- ASD000000Uu6zL	FRENCH - Acadience Reading Français (IDAPEL) GR2-END
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000Uu6zL'
and ASM_FIELDA_006 is null and ASM_FIELDA_002 is null

--

--
select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = ''


------


select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_OID = 'PSN000000GMZNl'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_ENROLLMENT_STATUS = 'Active' AND STD_OID IN (
	select std_OID from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where 
	STD_GRADE_LEVEL IN ('09', '10', '11', '12') AND
	(STD_FIELDA_071 IS NOT NULL OR STD_FIELDA_073 IS NOT NULL OR STD_FIELDA_074 IS NOT NULL OR STD_FIELDA_075 IS NOT NULL OR STD_FIELDA_076 IS NOT NULL OR STD_FIELDA_050 IS NOT NULL OR STD_FIELDA_070 IS NOT NULL OR STD_FIELDA_066 IS NOT NULL OR STD_FIELDA_077 IS NOT NULL OR
	STD_FIELDA_078 IS NOT NULL OR
	STD_FIELDD_001 IS NOT NULL OR
	STD_FIELDB_023 IS NOT NULL OR
	STD_FIELDB_013 IS NOT NULL OR
	STD_FIELDB_020 IS NOT NULL OR
	STD_FIELDB_019 IS NOT NULL OR
	STD_FIELDB_021 IS NOT NULL OR
	STD_FIELDB_030 IS NOT NULL OR
	STD_FIELDB_031 IS NOT NULL OR
	STD_FIELDA_081 IS NOT NULL)
	)

	---------------------

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000RBtPx'
and ASM_FIELDA_008 = '0' or ASM_FIELDA_006 = '0' 

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].school where skl_oid like 'skl01000000043'


--- ref code stuff

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].ref_code where rcd_rtb_oid = 'RTB000000Y9fbz'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].ref_table where RTB_OID = 'RTB000000T8RRP'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].ref_table where RTB_USER_NAME like '%Acadience%'


select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].ref_code where rcd_rtb_oid = 'RTB000000PuYnu'