package ldsb.procedures;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.follett.fsc.core.framework.persistence.IterableQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.ReferenceCode;
import com.follett.fsc.core.k12.beans.ReferenceTable;
import com.follett.fsc.core.k12.beans.StudentAlert;
import com.follett.fsc.core.k12.beans.X2BaseBean;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.x2dev.sis.model.beans.StudentAssessment;
import com.x2dev.utils.ThreadUtils;
import com.x2dev.utils.types.PlainDate;

public class ProcessDibelsAssessment extends ProcedureJavaSource {

	private static final long serialVersionUID = 1L;

	private static final String ASD_OID_SK_BEG = "ASD000000QyTnf";
	private static final String ASD_OID_SK_MID = "ASD000000QypQd";
	private static final String ASD_OID_SK_END = "ASD000000RBtKH";
	private static final String ASD_OID_GR1_BEG = "ASD000000RBtLX";
	private static final String ASD_OID_GR1_MID = "ASD000000RBtMj";
	private static final String ASD_OID_GR1_END = "ASD000000TcBHV";
	private static final String ASD_OID_GR2_BEG = "ASD000000OuZKM";
	private static final String ASD_OID_GR2_MID_END = "ASD000000RBtPx";
	private static final String ASD_OID_GR3_GR6 = "ASD000000Ncwdp";

	private static final String RTB_OID_ACADIANCE_BENCHMARKS = "RTB000000Y9fbz";
	private static final String RTB_ID_ACADIANCE_BENCHMARKS = "ACADIENCE-ASM-BENCHMRK";

	private static final String ALR_ICON = "alertIcons/cl_study.png";

	private static final List<String> ASSESSMENT_OID_LIST = Arrays.asList(ASD_OID_SK_BEG, ASD_OID_SK_MID,
			ASD_OID_SK_END, ASD_OID_GR1_BEG, ASD_OID_GR1_MID, ASD_OID_GR1_END, ASD_OID_GR2_BEG, ASD_OID_GR2_MID_END,
			ASD_OID_GR3_GR6);

	Map<Integer, Integer> m_gradeOneMidPrecisionMap = new TreeMap<>(); // Tree maps preserve the order by the key
	Map<Integer, Integer> m_gradeOneEndPrecisionMap = new TreeMap<>();
	Map<Integer, Integer> m_gradeTwoMidEndPrecisionMap = new TreeMap<>();

	Map<String, Integer> m_acadianceBenchmarkMap = new HashMap<>();

	Map<String, StudentAlert> m_getStudentBenchmarkAlert = new HashMap<>();

	PlainDate m_today = getPlainDate();

	/**
	 * Overview: The SaveNotification class should be added as a nested class to any Aspen procedure that will use
	 * notifications. The required import libs to be added to procedures are: import java.io.File; import
	 * java.io.FileWriter; import java.util.Date; import java.text.SimpleDateFormat;
	 *
	 * usage: save_notif_inst.save_notification("traised@limestone.on.ca", "this is a notification from acadience
	 * assessment");
	 *
	 * Datetime is added. Aspen_notifications.csv file is updated.
	 *
	 * MessageType can be exception, warning, error, information.
	 */
	class SaveNotification {
		public void save(String messageType, String recipients, String message) throws Exception {

			Date myDate = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
			String myDateString = simpleDateFormat.format(myDate);

			String filename = getJob().getTempFolder() + "/ldsb_assessment_imports/aspen_notifications.csv";

			File notifFile = new File(filename);

			try {

				if (!notifFile.exists()) {
					notifFile.createNewFile();
				}

				FileWriter writer = new FileWriter(notifFile, true);
				writer.write(messageType + "," + recipients + "," + message + "," + myDateString + "\n");
				writer.close();

			} catch (Exception e) {
				logMessage(e.toString());
			} finally {
			}
		}
	}

	@Override
	protected void execute() throws Exception {
		initializeBenchmarkMap();
		initializeScoringMaps();
		initializeStudentAlertMap();

		List<String> queryList = new ArrayList<>();

		X2Criteria criteria = new X2Criteria();
		// Specific assessment definitions
		criteria.addIn(StudentAssessment.COL_ASSESSMENT_DEFINITION_OID, ASSESSMENT_OID_LIST);
		// Modified in the last three days

		Instant now = Instant.now();

		Instant fiveDaysAgo = now.minus(50, ChronoUnit.DAYS);

		// Convert to milliseconds
		long timestampMillis = fiveDaysAgo.toEpochMilli();

		// logMessage("last modified: " + StudentAssessment.COL_LAST_MODIFIED_TIME + " " + "Compare time: " +
		// timestampMillis);
		// System.currentTimeMillis() - (300 * (24 * 60 * 60 * 1000))

		criteria.addGreaterOrEqualThan(StudentAssessment.COL_LAST_MODIFIED_TIME, timestampMillis);

		String currentOid = null;

		try (IterableQuery<StudentAssessment> query = new IterableQuery(StudentAssessment.class, criteria,
				getBroker())) {
//			Specify any ordering for the query results, if needed
//			query.addOrderByAscending(StudentAssessment.COL_FIELD_B001);
			for (StudentAssessment studentAssessment : query) {
				currentOid = studentAssessment.getOid();

				ThreadUtils.checkInterrupt();
				String sequence = studentAssessment.getFieldA001();
				int fsf = NumberUtils.toInt(studentAssessment.getFieldA002());
				int lnf = NumberUtils.toInt(studentAssessment.getFieldA012());
				int psf = NumberUtils.toInt(studentAssessment.getFieldA003());
				int nwfCls = NumberUtils.toInt(studentAssessment.getFieldA004());
				int nwfWwr = NumberUtils.toInt(studentAssessment.getFieldA005());
				int orfWc = NumberUtils.toInt(studentAssessment.getFieldA006());
				int orfErr = NumberUtils.toInt(studentAssessment.getFieldA012());
				// int orfAcc = NumberUtils.toInt(studentAssessment.getFieldA007());
				int retell = NumberUtils.toInt(studentAssessment.getFieldA008());
				int retellQor = NumberUtils.toInt(studentAssessment.getFieldA009());
				int mazeAdjusted = NumberUtils.toInt(studentAssessment.getFieldA010());

				String gradeLevel = studentAssessment.getGradeLevelCode();
				if (StringUtils.isBlank(gradeLevel)) {
					gradeLevel = studentAssessment.getStudent().getGradeLevel();
				}

				int rscScore = 0;
				int orfAccuracy = 0;

				switch (studentAssessment.getAssessmentDefinitionOid()) {
				case ASD_OID_SK_BEG:
					rscScore = fsf + lnf;
					break;

				case ASD_OID_SK_MID:
					rscScore = fsf + lnf + psf + nwfCls;
					break;

				case ASD_OID_SK_END:
					rscScore = lnf + psf + nwfCls;
					break;

				case ASD_OID_GR1_BEG:
					rscScore = lnf + psf + nwfCls;
					break;

				case ASD_OID_GR1_MID:
					orfAccuracy = (orfWc / (orfWc + orfErr)) * 100;
					rscScore = nwfCls + nwfWwr + orfWc + getDiff(m_gradeOneMidPrecisionMap, orfAccuracy);
					break;

				case ASD_OID_GR1_END:
					orfAccuracy = (orfWc / (orfWc + orfErr)) * 100;
					rscScore = (nwfWwr * 2) + orfWc + getDiff(m_gradeOneEndPrecisionMap, orfAccuracy);
					break;

				case ASD_OID_GR2_MID_END:
					orfAccuracy = (orfWc / (orfWc + orfErr)) * 100;
					rscScore = orfWc + getDiff(m_gradeTwoMidEndPrecisionMap, orfAccuracy) + (retell * 2);
					break;
				case ASD_OID_GR3_GR6:
					orfAccuracy = (orfWc / (orfWc + orfErr)) * 100;
					rscScore = orfWc + getDiff(m_gradeTwoMidEndPrecisionMap, orfAccuracy) + (retell * 2);
				default:
					break;
				}

				/*
				 * TODO: Expand the switch statement above with new case for **GR2 BEG**
				 * 
				 * int floPrecisionGR2BEG = getGradeOneENDPrecision(orfAcc); rscScore = (nwfWwr * 2) + orfWc +
				 * floPrecisionGR2BEG;
				 * 
				 * int floPrecisionGR3to6 = getGradeTwoMIDENDPrecision(orfAcc); rscScore = orfWc + (retell * 2) +
				 * (mazeAdjusted * 4) + floPrecisionGR3to6;
				 * 
				 */

				// Format an integer so that it sorts properly
				String scoreAsString = StringUtils.leftPad(String.valueOf(rscScore), 3, "0");
				studentAssessment.setFieldA011(scoreAsString);

				logMessage("RSC Score: " + scoreAsString + " student " + studentAssessment.getStudentOid());

				// Save changes
				if (studentAssessment.isDirty()) {
//					Use this if you want to display any validation errors encountered on save
//					List<ValidationError> errorList = getBroker().saveBean(studentAssessment);
//					for (ValidationError error : errorList) {
//						logMessage("Error saving " + studentAssessment.getOid() + "::" + error.toString());
//					}

					getBroker().saveBeanForced(studentAssessment);

					// set student flag based on benchmark?
//					if (isBelowBenchmark(gradeLevel, sequence, rscScore)) {
//						saveAlert.saveAlert(studentAssessment.getStudentOid(), gradeLevel, sequence, true);
//					}

					boolean isBelowBenchmark = isBelowBenchmark(gradeLevel, sequence, rscScore);
					createOrUpdateBenchmarkAlert(studentAssessment.getStudentOid(), gradeLevel, sequence,
							isBelowBenchmark);
				}
			}
		} catch (Exception e) {
			logMessage(
					"Exception error occurred with record " + currentOid + ":\n" + ExceptionUtils.getFullStackTrace(e));
		}
	}

	/**
	 * Create a new alert or update an existing alert based on whether the score is below benchmark.
	 *
	 * If the score is above the benchmark, and there isn't an existing alert, don't create a new one
	 *
	 * @param studentOid
	 * @param gradeLevel
	 * @param sequence
	 * @param isBelowBenchmark
	 */
	private void createOrUpdateBenchmarkAlert(String studentOid, String gradeLevel, String sequence,
			boolean isBelowBenchmark) {
		StudentAlert studentAlert = m_getStudentBenchmarkAlert.get(studentOid);

		if (studentAlert != null || isBelowBenchmark) {
			if (studentAlert == null) {
				// If null, create a new one and set the attributes that are in the query
				studentAlert = X2BaseBean.newInstance(StudentAlert.class, getBroker().getPersistenceKey());
				studentAlert.setStudentOid(studentOid);
				studentAlert.setAlertType(StudentAlert.AlertType.OTHER.ordinal());
				studentAlert.setIconFilename(ALR_ICON);
			}
			// Update the alert with the appropriate values
			if (isBelowBenchmark) {
				// Set the start date and the alert description
				studentAlert.setStartDate(m_today);
				studentAlert.setEndDate(null);
				studentAlert.setAlertDescription(
						"Student scored below benchmark in Acadience Reading Composite Score for grade " + gradeLevel
								+ " sequence period " + sequence + "\n");
				studentAlert.setDisabledIndicator(false);

				if (studentAlert.isDirty()) {
					getBroker().saveBeanForced(studentAlert);
				}
			} else {
				// Delete
				getBroker().deleteBean(studentAlert);
			}

		} else {
			// Do nothing, because the score is above the benchmark and there isn't an
			// existing alert
		}

	}

	/**
	 * Return true if the passed score is below the benchmark score in the reference table with OID = 'RTB000000PuYnu',
	 * for a given grade level and sequence
	 *
	 * @param gradeLevel
	 * @param sequence
	 * @param score
	 * @return
	 */
	private boolean isBelowBenchmark(String gradeLevel, String sequence, int score) {
		boolean belowBenchmark = false;

		if (score < m_acadianceBenchmarkMap.get(gradeLevel + sequence)) {
			belowBenchmark = true;
		}

		return belowBenchmark;
	}

	/**
	 * Iterates over a map and returns the first value where the score is less than the key.
	 *
	 * @param scoreMap
	 * @param score
	 * @return
	 */
	private Integer getDiff(Map<Integer, Integer> scoreMap, int score) {
		for (Integer key : scoreMap.keySet()) {
			if (score < key) {
				return scoreMap.get(key);
			}
		}

		return null;
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

	/**
	 * Populate
	 */
	private void initializeStudentAlertMap() {
		X2Criteria criteria = new X2Criteria();
		criteria.addEqualTo(StudentAlert.COL_ALERT_TYPE, StudentAlert.AlertType.OTHER.ordinal());
		criteria.addEqualTo(StudentAlert.COL_ICON_FILENAME, ALR_ICON);

		try (IterableQuery<StudentAlert> query = new IterableQuery(StudentAlert.class, criteria, getBroker())) {
			query.addOrderByAscending(StudentAlert.COL_STUDENT_OID);
			query.addOrderByAscending(StudentAlert.COL_START_DATE);
			for (StudentAlert studentAlert : query) {
				ThreadUtils.checkInterrupt();
				m_getStudentBenchmarkAlert.put(studentAlert.getStudentOid(), studentAlert);
			}
		}
	}

}