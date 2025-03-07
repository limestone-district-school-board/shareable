select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info where usr_login_name = 'zainabm@keys.ca'
OR usr_login_name = 'nataliam@keys.ca'
OR usr_login_name = 'manalshalabi@keys.ca'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].contact where cnt_psn_oid = 'PSN000000Kqv0C'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info where USR_PSN_OID = 'psn01000226229'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info where usr_name_view like '%Abdali, TYLER%'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_OID = 'std01000067320'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_PSN_OID = 'PSN000000G0PXa'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].staff where STF_OID = 'stf01000002066'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_OID = 'PSN0000005iizj'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].COMMENT_BANK_CODE

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_NAME_LAST = 'Wood' and PSN_NAME_FIRST = 'Bryson'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_program_participation where PGM_DDX_OID like '%Rem%'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_program_participation where PGM_PROGRAM_CODE like '%Empower%'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_program_participation where PGM_START_DATE > getdate() - 3

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].school where skl_school_name like '%Loyalist%'


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


select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].school where skl_oid = 'skl01000000054';

select distinct usr_login_status from [ASPEN].[AspenDB-LDSB-PD].[dbo].user_info 

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_case where stc_name_view like '%Abdali, Marwa%'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_case_person where scp_fieldb_004 = 'Shaer Zadeh' --scp_stc_oid = 'STC000000bSqbZ'

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].contact

-- assessments

select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_PSN_OID = 'PSN000000IjeLk'

select top 100 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].person where PSN_NAME_LAST = 'Giacomello' and PSN_NAME_FIRST = 'Xavier'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_std_oid = 'STD000000IjeLn'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].assessment_definition where asd_oid = 'ASD000000dGVDb'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_asd_oid = 'ASD000000dGVDb'

select * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student_assessment where asm_oid = 'ASM000000QWUA1'


-- find this student
select top 10 * from [ASPEN].[AspenDB-LDSB-PD].[dbo].student where STD_OID = 'STD0000009bRU0'

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
