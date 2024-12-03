package abcd.procedures;

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
import com.follett.fsc.core.k12.beans.StudentAlert;
import com.follett.fsc.core.k12.beans.X2BaseBean;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.x2dev.sis.model.beans.StudentAssessment;
import com.x2dev.utils.ThreadUtils;
import com.x2dev.utils.types.PlainDate;

public class ProcessIdapelAssessment extends ProcedureJavaSource {

	private static final long serialVersionUID = 1L;

	private static final String ASD_OID_SK_BEG = "ASD000000Ut7Vf";
	private static final String ASD_OID_SK_MID = "ASD000000Uu6xW";
	private static final String ASD_OID_SK_END = "ASD000000Uu6y1";
	private static final String ASD_OID_GR1_BEG = "ASD000000Uu6yS";
	private static final String ASD_OID_GR1_MID = "ASD000000Uu6yu";
	private static final String ASD_OID_GR1_END = "ASD000000hYG2a";
	private static final String ASD_OID_GR2_BEG = "ASD000000Ut8EL";
	private static final String ASD_OID_GR2_MID = "ASD000000hLn6D";
	private static final String ASD_OID_GR2_END = "ASD000000Uu6zL";

	private static final String RTB_OID_ACADIANCE_BENCHMARKS = "RTB000000Y9fbz";
	private static final String RTB_ID_ACADIANCE_BENCHMARKS = "ACADIENCE-ASM-BENCHMRK";
	private static final String ACADIANCE_BENCHMARKS_CATEGORY = "BENCHMARK";

	private static final String ALR_ICON = "alertIcons/cl_study.png";

	private static final List<String> ASSESSMENT_OID_LIST = Arrays.asList(ASD_OID_SK_BEG, ASD_OID_SK_MID,
			ASD_OID_SK_END, ASD_OID_GR1_BEG, ASD_OID_GR1_MID, ASD_OID_GR1_END, ASD_OID_GR2_BEG, ASD_OID_GR2_MID,
			ASD_OID_GR2_END);

	Map<Integer, Integer> m_gradeOneMidPrecisionMap = new TreeMap<>(); // Tree maps preserve the order by the key
	Map<Integer, Integer> m_gradeOneEndPrecisionMap = new TreeMap<>();
	Map<Integer, Integer> m_gradeTwoMidPrecisionMap = new TreeMap<>();
	Map<Integer, Integer> m_gradeTwoEndPrecisionMap = new TreeMap<>();

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

		boolean isAccuracyCalc = false;
		boolean isIncomplete = false;
		boolean isGradeLevelEmpty = false;

		List<String> queryList = new ArrayList<>();

		X2Criteria criteria = new X2Criteria();
		// Specific assessment definitions
		criteria.addIn(StudentAssessment.COL_ASSESSMENT_DEFINITION_OID, ASSESSMENT_OID_LIST);
		// Modified in the last three days

		Instant now = Instant.now();

		Instant fiveDaysAgo = now.minus(5, ChronoUnit.DAYS);

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

				isIncomplete = false;
				isGradeLevelEmpty = false;

				currentOid = studentAssessment.getOid();

				ThreadUtils.checkInterrupt();
				String sequence = studentAssessment.getFieldA001();
				int fps = NumberUtils.toInt(studentAssessment.getFieldA010());
				int fdl = NumberUtils.toInt(studentAssessment.getFieldA012());
				int fsp = NumberUtils.toInt(studentAssessment.getFieldA003());
				int fnm_nsc = NumberUtils.toInt(studentAssessment.getFieldA004());
				int fnm_mle = NumberUtils.toInt(studentAssessment.getFieldA005());
				int flo_mc = NumberUtils.toInt(studentAssessment.getFieldA002());
				int flo_err = NumberUtils.toInt(studentAssessment.getFieldA007());
				int flo_pre = NumberUtils.toInt(studentAssessment.getFieldA009());
				int flo_ro = NumberUtils.toInt(studentAssessment.getFieldA006());

				String gradeLevel = studentAssessment.getGradeLevelCode();
				if (StringUtils.isBlank(gradeLevel)) {
					isGradeLevelEmpty = true;
					gradeLevel = studentAssessment.getStudent().getGradeLevel();
				}

				int rscScore = 0;
				int orfAccuracy = 0;

				double dorfWc = flo_mc;
				double dorfErr = flo_err;

				switch (studentAssessment.getAssessmentDefinitionOid()) {
				case ASD_OID_SK_BEG:
					if (isNullOrEmpty(studentAssessment.getFieldA010())
							|| isNullOrEmpty(studentAssessment.getFieldA012()))
						isIncomplete = true;

					rscScore = fdl + fps;

					break;

				case ASD_OID_SK_MID:

					if (isNullOrEmpty(studentAssessment.getFieldA010())
							|| isNullOrEmpty(studentAssessment.getFieldA012())
							|| isNullOrEmpty(studentAssessment.getFieldA003())
							|| isNullOrEmpty(studentAssessment.getFieldA004()))
						isIncomplete = true;

					rscScore = fps + fdl + fsp + fnm_nsc;
					break;

				case ASD_OID_SK_END:

					if (isNullOrEmpty(studentAssessment.getFieldA012())
							|| isNullOrEmpty(studentAssessment.getFieldA003())
							|| isNullOrEmpty(studentAssessment.getFieldA004()))
						isIncomplete = true;

					rscScore = fps + fsp + fnm_nsc;
					break;

				case ASD_OID_GR1_BEG:

					if (isNullOrEmpty(studentAssessment.getFieldA012())
							|| isNullOrEmpty(studentAssessment.getFieldA003())
							|| isNullOrEmpty(studentAssessment.getFieldA004()))
						isIncomplete = true;

					if (fsp > 30)
						fsp = 30;

					rscScore = fdl + fsp + fnm_nsc;

					break;

				case ASD_OID_GR1_MID:

					isAccuracyCalc = true;

					if (isNullOrEmpty(studentAssessment.getFieldA004())
							|| isNullOrEmpty(studentAssessment.getFieldA005())
							|| isNullOrEmpty(studentAssessment.getFieldA002()))
						isIncomplete = true;

					orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

					rscScore = fnm_nsc + fnm_mle + flo_mc + getDiff(m_gradeOneMidPrecisionMap, orfAccuracy);
					break;

				case ASD_OID_GR1_END:

					isAccuracyCalc = true;

					if (isNullOrEmpty(studentAssessment.getFieldA004())
							|| isNullOrEmpty(studentAssessment.getFieldA005())
							|| isNullOrEmpty(studentAssessment.getFieldA002()))
						isIncomplete = true;

					// if (flo_mc != 0)
					orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

					rscScore = fnm_nsc + fnm_mle + flo_mc + getDiff(m_gradeOneEndPrecisionMap, orfAccuracy);
					break;

				case ASD_OID_GR2_BEG:

					isAccuracyCalc = true;

					try {

						if (isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						if (flo_mc != 0)
							orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						rscScore = (fnm_mle * 3) + flo_mc + getDiff(m_gradeOneMidPrecisionMap, orfAccuracy);

						logMessage("rscScore: " + rscScore + " orfAccuracy " + orfAccuracy + "flo_mc " + flo_mc
								+ " flo err " + flo_err);

					} catch (Exception e) {
					}
					break;

				case ASD_OID_GR2_MID:

					isAccuracyCalc = true;

					try {

						if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						if (flo_mc != 0)
							orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						rscScore = flo_mc + flo_ro + getDiff(m_gradeTwoMidPrecisionMap, orfAccuracy);

					} catch (Exception e) {
					}
					break;

				case ASD_OID_GR2_END:

					isAccuracyCalc = true;

					try {

						if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						if (flo_mc != 0)
							orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						rscScore = flo_mc + getDiff(m_gradeTwoEndPrecisionMap, orfAccuracy) + (flo_ro * 2);

					} catch (Exception e) {
					}

					break;

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
				// String scoreAsString = StringUtils.leftPad(String.valueOf(rscScore), 3, "0");
				String scoreAsString = String.valueOf(rscScore);
				studentAssessment.setFieldA011(scoreAsString);

				if (isGradeLevelEmpty) {
					studentAssessment.setGradeLevelCode(gradeLevel);
				}

				if (isAccuracyCalc) {
					String orfAccuracyAsString = String.valueOf(orfAccuracy);
					studentAssessment.setFieldA009(orfAccuracyAsString);
				}

				// Save changes
				// if (studentAssessment.isDirty()) {
//					Use this if you want to display any validation errors encountered on save
//					List<ValidationError> errorList = getBroker().saveBean(studentAssessment);
//					for (ValidationError error : errorList) {
//						logMessage("Error saving " + studentAssessment.getOid() + "::" + error.toString());
//					}

				// set student flag based on benchmark
				if (!isIncomplete) {

					String markBenchmark = "0";

					boolean isBelowBenchmark = isBelowBenchmark(gradeLevel, sequence, rscScore);

					if (!isBelowBenchmark)
						markBenchmark = "1";

					studentAssessment.setFieldA040(markBenchmark);

					getBroker().saveBeanForced(studentAssessment);

					// logMessage("benchmark key: " + gradeLevel + sequence);

					createOrUpdateBenchmarkAlert(studentAssessment.getStudentOid(), gradeLevel, sequence,
							isBelowBenchmark);

				}
				// }
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
						"Student scored below benchmark in French Acadience Reading Composite Score for grade "
								+ gradeLevel + " sequence period " + sequence + "\n");
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
	 * Return true if the passed score is below the benchmark score in the reference table with OID = 'RTB000000Y9fbz',
	 * for a given grade level and sequence
	 *
	 * @param gradeLevel
	 * @param sequence
	 * @param score
	 * @return
	 */
	private boolean isBelowBenchmark(String gradeLevel, String sequence, int score) {
		boolean belowBenchmark = false;

		try {
			if (score < m_acadianceBenchmarkMap.get(gradeLevel + sequence)) {
				belowBenchmark = true;
			}

		} catch (Exception e) {
			logMessage("Exception caught in benchmark compare " + e.getMessage());
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
		// criteria.addOrEqualTo(ReferenceCode.REL_REFERENCE_TABLE + "." + ReferenceTable.COL_ID,
		// RTB_ID_ACADIANCE_BENCHMARKS);

		criteria.addEqualTo(ReferenceCode.COL_CATEGORY, ACADIANCE_BENCHMARKS_CATEGORY);

		// Iterate over the results, and populate the map
		try (IterableQuery<ReferenceCode> query = new IterableQuery(ReferenceCode.class, criteria, getBroker())) {
			for (ReferenceCode referenceCode : query) {
				ThreadUtils.checkInterrupt();

				if ((referenceCode.getCategory() != null)
						&& referenceCode.getCategory().equals(ACADIANCE_BENCHMARKS_CATEGORY)) {
					int compositeScore = NumberUtils.toInt(referenceCode.getFieldB004());
					String gradeLevel = referenceCode.getFieldB006();
					String sequence = referenceCode.getFieldB005();

					// logMessage(referenceCode.getReferenceTableOid() + " " + referenceCode.getParentOidColumn());
					// logMessage("'" + referenceCode.getCategory() + "' '" + ACADIANCE_BENCHMARKS_CATEGORY + "'");
					// logMessage(gradeLevel + sequence + compositeScore);

					if ((gradeLevel != null) || (sequence != null))
						m_acadianceBenchmarkMap.put(gradeLevel + sequence, compositeScore);
				}
			}
		}
	}

	private void initializeScoringMaps() {
		// Go to 1 past the top score
		// Gr1 MID
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
		// GR 1 END GR 2 BEG
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
		// GR2 MID
		m_gradeTwoMidPrecisionMap.put(74, 0);
		m_gradeTwoMidPrecisionMap.put(76, 4);
		m_gradeTwoMidPrecisionMap.put(78, 12);
		m_gradeTwoMidPrecisionMap.put(80, 20);
		m_gradeTwoMidPrecisionMap.put(82, 28);
		m_gradeTwoMidPrecisionMap.put(84, 36);
		m_gradeTwoMidPrecisionMap.put(86, 44);
		m_gradeTwoMidPrecisionMap.put(88, 52);
		m_gradeTwoMidPrecisionMap.put(90, 60);
		m_gradeTwoMidPrecisionMap.put(92, 68);
		m_gradeTwoMidPrecisionMap.put(94, 76);
		m_gradeTwoMidPrecisionMap.put(96, 84);
		m_gradeTwoMidPrecisionMap.put(98, 92);
		m_gradeTwoMidPrecisionMap.put(100, 100);
		m_gradeTwoMidPrecisionMap.put(101, 108);

		// Go to 1 past the top score
		// GR2 END
		m_gradeTwoEndPrecisionMap.put(85, 0);
		m_gradeTwoEndPrecisionMap.put(86, 18);
		m_gradeTwoEndPrecisionMap.put(87, 24);
		m_gradeTwoEndPrecisionMap.put(88, 30);
		m_gradeTwoEndPrecisionMap.put(89, 36);
		m_gradeTwoEndPrecisionMap.put(90, 42);
		m_gradeTwoEndPrecisionMap.put(91, 48);
		m_gradeTwoEndPrecisionMap.put(92, 54);
		m_gradeTwoEndPrecisionMap.put(93, 60);
		m_gradeTwoEndPrecisionMap.put(94, 66);
		m_gradeTwoEndPrecisionMap.put(95, 72);
		m_gradeTwoEndPrecisionMap.put(96, 78);
		m_gradeTwoEndPrecisionMap.put(97, 84);
		m_gradeTwoEndPrecisionMap.put(98, 90);
		m_gradeTwoEndPrecisionMap.put(99, 96);
		m_gradeTwoEndPrecisionMap.put(100, 102);
		m_gradeTwoEndPrecisionMap.put(101, 108);

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

	/**
	 * Check value validity for completion check
	 */
	public boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}
}
