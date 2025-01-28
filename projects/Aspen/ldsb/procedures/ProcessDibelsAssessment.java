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
import com.follett.fsc.core.k12.beans.StudentAlert;
import com.follett.fsc.core.k12.beans.X2BaseBean;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.x2dev.sis.model.beans.StudentAssessment;
import com.x2dev.utils.ThreadUtils;
import com.x2dev.utils.types.PlainDate;

public class ProcessDibelsAssessment extends ProcedureJavaSource {

	private static final long serialVersionUID = 1L;

	private static final String ASD_OID_GR_3 = "ASD000000UYc8z";
	private static final String ASD_OID_GR_4_8 = "ASD000000RuoTr";

	private static final String RTB_OID_ACADIANCE_BENCHMARKS = "RTB000000T8RRP";
	private static final String RTB_ID_ACADIANCE_BENCHMARKS = "DIBELS-ASM-BENCHMRK";
	private static final String ACADIANCE_BENCHMARKS_CATEGORY = "BENCHMARK";

	private static final String ALR_ICON = "alertIcons/cl_study.png";

	private static final List<String> ASSESSMENT_OID_LIST = Arrays.asList(ASD_OID_GR_3, ASD_OID_GR_4_8);

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

		// set constants for all dibels assessments

		boolean isAccuracyCalc = false;
		boolean isMazeCalc = false;
		boolean isIncomplete = false;
		boolean isGradeLevelEmpty = false;

		int step5_const = 40;

		double LNFweight = 0.0;
		double PSFweight = 0.0;
		double nwfCLSweight = 0.0;
		double orfWRCweight = 0.0;
		double nwfWRCWeight = 0.0;
		double WRF_weight = 0.0;
		double ORF_Correct_weight = 0.0;
		double orfWRCErrors = 0.0;
		double ORF_Acc_weight = 0.0;
		double MAZEweight = 0.0;
		double WRFWeight = 0.0;
		double Maze_Incorrect_weight = 0.0;

		int Beginning_val = 0;
		int Middle_val = 0;
		int End_val = 0;

		double stepTwo = 0.0;
		double stepThree = 0.0;
		double stepFour = 0.0;
		double stepFive = 0.0;

		double tmpAcc = 0.0;
		double tmpStepFour = 0.0;

		int ORFAccuracy = 0;
		double mazeAdjustedScore = 0;

		int scalingConstant = 0;

		int finalCompositeScore = 0;

		int rscScore = 0;
		int orfAccuracy = 0;

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
				currentOid = studentAssessment.getOid();

				// set constants for all dibels assessments

				isAccuracyCalc = false;
				isMazeCalc = false;
				isIncomplete = false;
				isGradeLevelEmpty = false;

				step5_const = 40;

				LNFweight = 0.0;
				PSFweight = 0.0;
				nwfCLSweight = 0.0;
				orfWRCweight = 0.0;
				nwfWRCWeight = 0.0;
				WRF_weight = 0.0;
				ORF_Correct_weight = 0.0;
				orfWRCErrors = 0.0;
				ORF_Acc_weight = 0.0;
				MAZEweight = 0.0;
				WRFWeight = 0.0;
				Maze_Incorrect_weight = 0.0;

				Beginning_val = 0;
				Middle_val = 0;
				End_val = 0;

				stepTwo = 0.0;
				stepThree = 0.0;
				stepFour = 0.0;
				tmpStepFour = 0.0;
				stepFive = 0.0;

				ORFAccuracy = 0;
				tmpAcc = 0;
				mazeAdjustedScore = 0.0;

				scalingConstant = 0;

				finalCompositeScore = 0;

				rscScore = 0;
				orfAccuracy = 0;

				ThreadUtils.checkInterrupt();
				String sequence = studentAssessment.getFieldA001();
				// int fsf = NumberUtils.toInt(studentAssessment.getFieldA002());
				// int lnf = NumberUtils.toInt(studentAssessment.getFieldA012());
				// int psf = NumberUtils.toInt(studentAssessment.getFieldA003());
				int wrf = NumberUtils.toInt(studentAssessment.getFieldA015());
				int nwfCls = NumberUtils.toInt(studentAssessment.getFieldA004());
				int nwfWwr = NumberUtils.toInt(studentAssessment.getFieldA005());
				int orfWc = NumberUtils.toInt(studentAssessment.getFieldA006());
				int orfErr = NumberUtils.toInt(studentAssessment.getFieldA007());
				// int orfAcc = NumberUtils.toInt(studentAssessment.getFieldA007());
				int mazeCorrect = (int) NumberUtils.toDouble(studentAssessment.getFieldA010());
				int mazeIncorrect = (int) NumberUtils.toDouble(studentAssessment.getFieldA008());
				// int mazeAdjusted = NumberUtils.toInt(studentAssessment.getFieldA014());

				String gradeLevel = studentAssessment.getGradeLevelCode();
				if (StringUtils.isBlank(gradeLevel)) {
					isGradeLevelEmpty = true;
					gradeLevel = studentAssessment.getStudent().getGradeLevel();
				}

				double dorfWc = orfWc;
				double dorfErr = orfErr;

				if (orfWc == 0)
					continue;

				if (isNullOrEmpty(studentAssessment.getFieldA001()))
					continue;

				// for all grades 3-8
				switch (studentAssessment.getFieldA001()) {
				case "BEG":
					scalingConstant = 360;
					break;
				case "MID":
					scalingConstant = 400;
					break;
				case "END":
					scalingConstant = 440;
				default:
					break;
				}

				switch (studentAssessment.getAssessmentDefinitionOid()) {

				case ASD_OID_GR_3:

					if (isNullOrEmpty(studentAssessment.getFieldA006())
							|| isNullOrEmpty(studentAssessment.getFieldA004())
							|| isNullOrEmpty(studentAssessment.getFieldA005())
							|| isNullOrEmpty(studentAssessment.getFieldA007())
							|| isNullOrEmpty(studentAssessment.getFieldA010())
							|| isNullOrEmpty(studentAssessment.getFieldA008()))
						isIncomplete = true;

					isAccuracyCalc = true;
					isMazeCalc = true;

					orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

					tmpAcc = orfAccuracy;

					mazeAdjustedScore = mazeCorrect - (0.5 * mazeIncorrect);

					if (mazeAdjustedScore < 0) {
						mazeAdjustedScore = 0;
					}

					nwfCLSweight = 40.02 * nwfCls;

					nwfWRCWeight = 11.80 * nwfWwr;
					WRFWeight = 19.83 * wrf;

					orfWRCweight = 39.42 * orfWc;

					orfWRCErrors = 0.09 * tmpAcc;
					MAZEweight = 4.79 * mazeAdjustedScore;

					stepTwo = nwfCLSweight + nwfWRCWeight + WRFWeight + orfWRCweight + orfWRCErrors + MAZEweight;

					stepThree = stepTwo - 10051;
					stepFour = stepThree / 4379;

					tmpStepFour = stepFour;

					stepFive = Math.round(tmpStepFour * 40);

					logMessage("Student: " + studentAssessment.getStudentOid() + "mazeCorrect " + mazeCorrect
							+ " mazeIncorrect " + mazeIncorrect + " mazeAdjustedScore " + mazeAdjustedScore
							+ " orfWRCErrors " + orfWRCErrors + " orfWRCweight " + orfWRCweight + " MAZEweight "
							+ MAZEweight + " stepTwo " + stepTwo + " wordsCorrect: " + dorfWc + " stepThree: "
							+ stepThree + " stepFour: " + stepFour + " step five: " + stepFive);

					break;
				case ASD_OID_GR_4_8:

					switch (gradeLevel) {

					case "08":

						if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA007())
								|| isNullOrEmpty(studentAssessment.getFieldA010())
								|| isNullOrEmpty(studentAssessment.getFieldA008()))
							isIncomplete = true;

						orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						if (orfAccuracy > 0)
							isAccuracyCalc = true;

						tmpAcc = orfAccuracy;

						mazeAdjustedScore = mazeCorrect - (0.5 * mazeIncorrect);

						if (mazeAdjustedScore < 0) {
							mazeAdjustedScore = 0;
						}

						if (mazeAdjustedScore > 0)
							isMazeCalc = true;

						orfWRCweight = 37.69 * dorfWc;

						orfWRCErrors = 0.03 * tmpAcc;
						MAZEweight = 6.75 * mazeAdjustedScore;

						stepTwo = orfWRCweight + orfWRCErrors + MAZEweight;

						stepThree = stepTwo - 4824;

						stepFour = stepThree / 1506;

						tmpStepFour = stepFour;

						stepFive = Math.round(tmpStepFour * 40);

						break;

					case "07":

						if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA007())
								|| isNullOrEmpty(studentAssessment.getFieldA010())
								|| isNullOrEmpty(studentAssessment.getFieldA008()))
							isIncomplete = true;

						orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						if (orfAccuracy > 0)
							isAccuracyCalc = true;

						tmpAcc = orfAccuracy;

						mazeAdjustedScore = mazeCorrect - (0.5 * mazeIncorrect);

						if (mazeAdjustedScore > 0)
							isMazeCalc = true;

						orfWRCweight = 40.55 * dorfWc;

						orfWRCErrors = 0.06 * tmpAcc;
						MAZEweight = 7.34 * mazeAdjustedScore;

						stepTwo = orfWRCweight + orfWRCErrors + MAZEweight;

						stepThree = stepTwo - 6444;
						stepFour = stepThree / 1960;

						tmpStepFour = stepFour;

						stepFive = Math.round(tmpStepFour * 40);

						break;

					case "06":

						if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA007())
								|| isNullOrEmpty(studentAssessment.getFieldA010())
								|| isNullOrEmpty(studentAssessment.getFieldA008()))
							isIncomplete = true;

						orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						if (orfAccuracy > 0)
							isAccuracyCalc = true;

						tmpAcc = orfAccuracy;

						mazeAdjustedScore = mazeCorrect - (0.5 * mazeIncorrect);

						if (mazeAdjustedScore > 0)
							isMazeCalc = true;

						orfWRCweight = 40.71 * orfWc;

						orfWRCErrors = 0.05 * tmpAcc;
						MAZEweight = 5.03 * mazeAdjustedScore;

						stepTwo = orfWRCweight + orfWRCErrors + MAZEweight;

						stepThree = stepTwo - 6087;
						stepFour = stepThree / 1685;

						tmpStepFour = stepFour;

						stepFive = Math.round(tmpStepFour * 40);

						break;

					case "05":

						if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA007())
								|| isNullOrEmpty(studentAssessment.getFieldA010())
								|| isNullOrEmpty(studentAssessment.getFieldA008()))
							isIncomplete = true;

						orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						if (orfAccuracy > 0)
							isAccuracyCalc = true;

						tmpAcc = orfAccuracy;

						mazeAdjustedScore = mazeCorrect - (0.5 * mazeIncorrect);

						if (mazeAdjustedScore > 0)
							isMazeCalc = true;

						orfWRCweight = 31.12 * orfWc;

						orfWRCErrors = 0.03 * tmpAcc;
						MAZEweight = 4.58 * mazeAdjustedScore;

						stepTwo = orfWRCweight + orfWRCErrors + MAZEweight;

						stepThree = stepTwo - 4085;
						stepFour = stepThree / 1299;

						tmpStepFour = stepFour;

						stepFive = Math.round(tmpStepFour * 40);

						break;

					case "04":

						if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA007())
								|| isNullOrEmpty(studentAssessment.getFieldA010())
								|| isNullOrEmpty(studentAssessment.getFieldA008()))
							isIncomplete = true;

						orfAccuracy = Math.round((float) ((dorfWc / (dorfWc + dorfErr)) * 100));

						if (orfAccuracy > 0)
							isAccuracyCalc = true;

						tmpAcc = orfAccuracy;

						mazeAdjustedScore = mazeCorrect - (0.5 * mazeIncorrect);

						if (mazeAdjustedScore > 0)
							isMazeCalc = true;

						orfWRCweight = 36.42 * orfWc;

						orfWRCErrors = 0.06 * tmpAcc;
						MAZEweight = 6.29 * mazeAdjustedScore;

						stepTwo = orfWRCweight + orfWRCErrors + MAZEweight;

						stepThree = stepTwo - 4563;
						stepFour = stepThree / 1771;

						tmpStepFour = stepFour;

						stepFive = Math.round(tmpStepFour * 40);

						break;

					default:
						break;
					}

				default:
					break;
				}

				finalCompositeScore = (int) Math.round(stepFive) + scalingConstant;

				String scoreAsString = String.valueOf(finalCompositeScore);
				studentAssessment.setFieldA011(scoreAsString);

				if (isGradeLevelEmpty) {
					studentAssessment.setGradeLevelCode(gradeLevel);
				}

				if (isAccuracyCalc) {
					String orfAccuracyAsString = String.valueOf(orfAccuracy);
					studentAssessment.setFieldA013(orfAccuracyAsString);
				}

				if (isMazeCalc) {
					String mazeAdjustedScoreAsString = String.valueOf(Math.round(mazeAdjustedScore));
					studentAssessment.setFieldA014(mazeAdjustedScoreAsString);
				}

				// logMessage("ORF Accuracy " + orfAccuracyAsString + " Student: " + "Maze adjusted score "
				// + mazeAdjustedScoreAsString + " Student: " + studentAssessment.getStudentOid());

				/**
				 * Testing block if ((studentAssessment.getOid().equals("ASM000000QQfBU"))) { logMessage("RSC Score: " +
				 * scoreAsString + " student " + studentAssessment.getStudentOid()); logMessage("orfAccuracy Score: " +
				 * orfAccuracyAsString + " student " + studentAssessment.getStudentOid() + "ORF Acc: " + orfAccuracy + "
				 * student id: " + studentAssessment.getStudentOid()); } -- end testing block
				 **/

				// Save changes
				// if (studentAssessment.isDirty()) {
				// Use this if you want to display any validation errors encountered on save
				// List<ValidationError> errorList = getBroker().saveBean(studentAssessment);
				// for (ValidationError error : errorList) {
				// logMessage("Error saving " + studentAssessment.getOid() + "::" + error.toString());
				// }

				// logMessage("ORF Accuracy " + orfAccuracy + " Student: " + studentAssessment.getStudentOid() + " "
				// + scoreAsString);

				if (!isIncomplete) {

					// logMessage("Student: " + studentAssessment.getStudentOid() + "Score: " + finalCompositeScore
					// + " stepFive: " + Math.round(stepFive) + "scalingConstant: " + scalingConstant);

					getBroker().saveBeanForced(studentAssessment);

					boolean isBelowBenchmark = isBelowBenchmark(gradeLevel, sequence, finalCompositeScore);

					// logMessage("Below benchmark? " + isBelowBenchmark);
					createOrUpdateBenchmarkAlert(studentAssessment.getStudentOid(), gradeLevel, sequence,
							isBelowBenchmark);
				} else if (isAccuracyCalc || isMazeCalc) {
					getBroker().saveBeanForced(studentAssessment);
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

		// logMessage("Is student below benchmark " + studentOid + ": " + isBelowBenchmark);

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
						"Student scored below benchmark in Dibels Reading Composite Score for grade " + gradeLevel
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

		// logMessage("Benchmark test " + gradeLevel + " " + sequence + " "
		// + m_acadianceBenchmarkMap.get(gradeLevel + sequence) + " " + score);

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
		criteria.addEqualTo(ReferenceCode.COL_CATEGORY, ACADIANCE_BENCHMARKS_CATEGORY);
		// criteria.addOrEqualTo(ReferenceCode.REL_REFERENCE_TABLE + "." + ReferenceTable.COL_ID,
		// RTB_ID_ACADIANCE_BENCHMARKS);

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

	/**
	 * Check value validity for completion check
	 */
	public boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

}