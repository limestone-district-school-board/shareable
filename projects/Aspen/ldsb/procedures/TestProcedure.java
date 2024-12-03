package ldsb.procedures;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.math.NumberUtils;

import com.follett.fsc.core.framework.persistence.BeanQuery;
import com.follett.fsc.core.framework.persistence.IterableQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.ReferenceCode;
import com.follett.fsc.core.k12.beans.ReferenceTable;
import com.follett.fsc.core.k12.business.StudentManager;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.x2dev.sis.model.beans.SisSchool;
import com.x2dev.sis.model.beans.SisStudent;
import com.x2dev.utils.ThreadUtils;

public class TestProcedure extends ProcedureJavaSource {

	private static final long serialVersionUID = 1L;

	private static final String RTB_OID_ACADIANCE_BENCHMARKS = "RTB000000PuYnu";
	private static final String RTB_ID_ACADIANCE_BENCHMARKS = "ACADIENCE-ASM-BENCHMRK";

	Map<Integer, Integer> m_gradeOneMidPrecisionMap = new TreeMap<>(); // Tree maps preserve the order by the key
	Map<Integer, Integer> m_gradeOneEndPrecisionMap = new TreeMap<>();
	Map<Integer, Integer> m_gradeTwoMidEndPrecisionMap = new TreeMap<>();

	Map<String, Integer> m_acadianceBenchmarkMap = new HashMap<>();

	@Override
	protected void execute() throws Exception {
//		initializeBenchmarkMap();
//		initializeScoringMaps();

		queryStudentsInASchool();

	}

	private void queryStudentsInASchool() {
		/*
		 * Example of OR criteria
		 */
		X2Criteria criteria = new X2Criteria();
		criteria.addIn(SisStudent.COL_ENROLLMENT_STATUS,
				StudentManager.getActiveStudentCodeList(getOrganization().getRootOrganization()));
		criteria.addEqualTo(SisStudent.REL_SCHOOL + "." + SisSchool.COL_SCHOOL_LEVEL_CODE, "02");

		X2Criteria orCriteria = new X2Criteria();
		orCriteria.addEqualTo(SisStudent.REL_SCHOOL + "." + SisSchool.COL_SCHOOL_ID, "ERNST");
		orCriteria.addOrEqualTo(SisStudent.REL_SCHOOL + "." + SisSchool.COL_SCHOOL_ID, "KINGS");
		orCriteria.addOrEqualTo(SisStudent.REL_SCHOOL + "." + SisSchool.COL_SCHOOL_ID, "LOCVI");

		criteria.addAndCriteria(orCriteria);

		BeanQuery query = new BeanQuery(SisStudent.class, criteria);
		logMessage("Total students in these schools = " + getBroker().getCount(query));
		logMessage("Where enrolment status is "
				+ StudentManager.getActiveStudentCodeList(getOrganization().getRootOrganization()).toString());

		logMessage("\nThe query being run is::" + getBroker().getSelectSql(query));
		logMessage("\nThe criteria being used is::" + criteria.toString());

	}

	/**
	 * Query for reference codes in the "Acadience Assessment Benchmarks" reference table (OID = RTB000000PuYnu)
	 */
	private void initializeBenchmarkMap() {
		// Criteria
		X2Criteria criteria = new X2Criteria();
		criteria.addEqualTo(ReferenceCode.COL_REFERENCE_TABLE_OID, RTB_OID_ACADIANCE_BENCHMARKS);
		criteria.addOrEqualTo(ReferenceCode.REL_REFERENCE_TABLE + "." + ReferenceTable.COL_ID,
				RTB_ID_ACADIANCE_BENCHMARKS);

		// Iterate over the results, and populate the map
		try (IterableQuery<ReferenceCode> query = new IterableQuery(ReferenceCode.class, criteria, getBroker())) {
			for (ReferenceCode referenceCode : query) {
				ThreadUtils.checkInterrupt();
				int compositeScore = NumberUtils.toInt(referenceCode.getFieldA006());
				String gradeLevel = referenceCode.getFieldB006();
				String sequence = referenceCode.getFieldB007();

				m_acadianceBenchmarkMap.put(gradeLevel + sequence, compositeScore);
			}
		}
	}

	private void initializeScoringMaps() {
		// Go to 1 past the top score
		m_gradeOneMidPrecisionMap.put(50, 0);
		m_gradeOneMidPrecisionMap.put(53, 2);
		m_gradeOneMidPrecisionMap.put(56, 8);
		m_gradeOneMidPrecisionMap.put(59, 14);
		m_gradeOneMidPrecisionMap.put(62, 20);
		m_gradeOneMidPrecisionMap.put(65, 26);
		m_gradeOneMidPrecisionMap.put(68, 32);
		m_gradeOneMidPrecisionMap.put(71, 38);
		m_gradeOneMidPrecisionMap.put(74, 44);
		m_gradeOneMidPrecisionMap.put(77, 50);
		m_gradeOneMidPrecisionMap.put(80, 56);
		m_gradeOneMidPrecisionMap.put(83, 62);
		m_gradeOneMidPrecisionMap.put(86, 68);
		m_gradeOneMidPrecisionMap.put(89, 74);
		m_gradeOneMidPrecisionMap.put(92, 80);
		m_gradeOneMidPrecisionMap.put(95, 86);
		m_gradeOneMidPrecisionMap.put(98, 92);
		m_gradeOneMidPrecisionMap.put(101, 98);

		// Go to 1 past the top score
		m_gradeOneEndPrecisionMap.put(65, 0);
		m_gradeOneEndPrecisionMap.put(67, 3);
		m_gradeOneEndPrecisionMap.put(69, 9);
		m_gradeOneEndPrecisionMap.put(71, 15);
		m_gradeOneEndPrecisionMap.put(73, 21);
		m_gradeOneEndPrecisionMap.put(75, 27);
		m_gradeOneEndPrecisionMap.put(77, 33);
		m_gradeOneEndPrecisionMap.put(79, 39);
		m_gradeOneEndPrecisionMap.put(81, 45);
		m_gradeOneEndPrecisionMap.put(83, 51);
		m_gradeOneEndPrecisionMap.put(85, 57);
		m_gradeOneEndPrecisionMap.put(87, 63);
		m_gradeOneEndPrecisionMap.put(89, 69);
		m_gradeOneEndPrecisionMap.put(91, 75);
		m_gradeOneEndPrecisionMap.put(93, 81);
		m_gradeOneEndPrecisionMap.put(95, 87);
		m_gradeOneEndPrecisionMap.put(97, 93);
		m_gradeOneEndPrecisionMap.put(99, 99);
		m_gradeOneEndPrecisionMap.put(101, 105);

		// Go to 1 past the top score
		m_gradeTwoMidEndPrecisionMap.put(86, 0);
		m_gradeTwoMidEndPrecisionMap.put(87, 8);
		m_gradeTwoMidEndPrecisionMap.put(88, 16);
		m_gradeTwoMidEndPrecisionMap.put(89, 24);
		m_gradeTwoMidEndPrecisionMap.put(90, 32);
		m_gradeTwoMidEndPrecisionMap.put(91, 40);
		m_gradeTwoMidEndPrecisionMap.put(92, 48);
		m_gradeTwoMidEndPrecisionMap.put(93, 56);
		m_gradeTwoMidEndPrecisionMap.put(94, 64);
		m_gradeTwoMidEndPrecisionMap.put(95, 72);
		m_gradeTwoMidEndPrecisionMap.put(96, 80);
		m_gradeTwoMidEndPrecisionMap.put(97, 88);
		m_gradeTwoMidEndPrecisionMap.put(98, 96);
		m_gradeTwoMidEndPrecisionMap.put(99, 104);
		m_gradeTwoMidEndPrecisionMap.put(100, 112);
		m_gradeTwoMidEndPrecisionMap.put(101, 120);
	}

}