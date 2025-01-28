package ldsb.procedures;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.follett.fsc.core.framework.persistence.BeanQuery;
import com.follett.fsc.core.framework.persistence.IterableQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.X2BaseBean;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.x2dev.sis.model.beans.StudentAssessment;
import com.x2dev.utils.ThreadUtils;

public class AssessmentCleanupProcedure extends ProcedureJavaSource {

	private static final long serialVersionUID = 1L;

	private static final String ASD_ACADIENCE_OID_SK_BEG = "ASD000000QyTnf";
	private static final String ASD_ACADIENCE_OID_SK_MID = "ASD000000QypQd";
	private static final String ASD_ACADIENCE_OID_SK_END = "ASD000000RBtKH";
	private static final String ASD_ACADIENCE_OID_GR1_BEG = "ASD000000RBtLX";
	private static final String ASD_ACADIENCE_OID_GR1_MID = "ASD000000RBtMj";
	private static final String ASD_ACADIENCE_OID_GR1_END = "ASD000000TcBHV";
	private static final String ASD_ACADIENCE_OID_GR2_BEG = "ASD000000OuZKM";
	private static final String ASD_ACADIENCE_OID_GR2_MID_END = "ASD000000RBtPx";
	private static final String ASD_ACADIENCE_OID_GR3_GR6 = "ASD000000Ncwdp";

	private static final String ASD_FR_ACADIENCE_OID_SK_BEG = "ASD000000Ut7Vf";
	private static final String ASD_FR_ACADIENCE_OID_SK_MID = "ASD000000Uu6xW";
	private static final String ASD_FR_ACADIENCE_OID_SK_END = "ASD000000Uu6y1";
	private static final String ASD_FR_ACADIENCE_OID_GR1_BEG = "ASD000000Uu6yS";
	private static final String ASD_FR_ACADIENCE_OID_GR1_MID = "ASD000000Uu6yu";
	private static final String ASD_FR_ACADIENCE_OID_GR1_END = "ASD000000hYG2a";
	private static final String ASD_FR_ACADIENCE_OID_GR2_BEG = "ASD000000Ut8EL";
	private static final String ASD_FR_ACADIENCE_OID_GR2_MID = "ASD000000hLn6D";
	private static final String ASD_FR_ACADIENCE_OID_GR2_END = "ASD000000Uu6zL";

	private static final String ASD_EARLY_READING_SCREENER = "asd00000000ERS";

	private static final String ALR_ICON = "alertIcons/cl_study.png";

	private static final List<String> ACA_ERS_ASSESSMENT_OID = Arrays.asList(ASD_EARLY_READING_SCREENER);

	private static final List<String> ACA_ASSESSMENT_OID_LIST = Arrays.asList(ASD_ACADIENCE_OID_SK_BEG,
			ASD_ACADIENCE_OID_SK_MID, ASD_ACADIENCE_OID_SK_END, ASD_ACADIENCE_OID_GR1_BEG, ASD_ACADIENCE_OID_GR1_MID,
			ASD_ACADIENCE_OID_GR1_END, ASD_ACADIENCE_OID_GR2_BEG, ASD_ACADIENCE_OID_GR2_MID_END,
			ASD_ACADIENCE_OID_GR3_GR6, ASD_FR_ACADIENCE_OID_SK_BEG, ASD_FR_ACADIENCE_OID_SK_MID,
			ASD_FR_ACADIENCE_OID_SK_END, ASD_FR_ACADIENCE_OID_GR1_BEG, ASD_FR_ACADIENCE_OID_GR1_MID,
			ASD_FR_ACADIENCE_OID_GR1_END, ASD_FR_ACADIENCE_OID_GR2_BEG, ASD_FR_ACADIENCE_OID_GR2_MID,
			ASD_FR_ACADIENCE_OID_GR2_END);

	Map<String, StudentAssessment> m_getStudentEarlyReadingList = new HashMap<>();
	Map<String, StudentAssessment> m_getStudentAssessmentList = new HashMap<>();

	// temp
	int totalCount = 0;

	@Override
	protected void execute() throws Exception {

		initializeERSMap();
		initializeACAMap();

		// cleanupERSAssessments();

		// boolean isGarbage = false;
		// boolean isIncomplete = false;
		// boolean isEarlyReadingScreener = false;

		X2Criteria criteria = new X2Criteria(); // Specific assessment definitions
		criteria.addIn(StudentAssessment.COL_ASSESSMENT_DEFINITION_OID, ACA_ASSESSMENT_OID_LIST);

		try (IterableQuery<StudentAssessment> query = new IterableQuery(StudentAssessment.class, criteria,
				getBroker())) {

//			Specify any ordering for the query results, if needed
//			query.addOrderByAscending(StudentAssessment.COL_FIELD_B001);

			for (StudentAssessment studentAssessment : query) {

				ThreadUtils.checkInterrupt();
				String currentOid = null;

				try {

					boolean isGarbage = false;
					boolean isIncomplete = false;
					boolean isEarlyReadingScreener = false;

					currentOid = studentAssessment.getOid();

					// perhaps in this case we'll set the column variables and then assign from the table after checking
					// the assessment type.
					// ie. EN or FR

					String sequence = new String();
					int fsf = 0;
					int lnf = 0;
					int psf = 0;
					int nwfCls = 0;
					int nwfWwr = 0;
					int orfWc = 0;
					int orfErr = 0;
					int retell = 0;
					int retellQor = 0;
					int mazeCorrect = 0;
					int mazeIncorrect = 0;
					int mazeAdjusted = 0;

					sequence = studentAssessment.getFieldA001();

					if (studentAssessment.getAssessmentDefinitionOid().contains("ASD_ACADIENCE")) {

						fsf = NumberUtils.toInt(studentAssessment.getFieldA002());
						lnf = NumberUtils.toInt(studentAssessment.getFieldA012());
						psf = NumberUtils.toInt(studentAssessment.getFieldA003());
						nwfCls = NumberUtils.toInt(studentAssessment.getFieldA004());
						nwfWwr = NumberUtils.toInt(studentAssessment.getFieldA005());
						orfWc = NumberUtils.toInt(studentAssessment.getFieldA006());
						orfErr = NumberUtils.toInt(studentAssessment.getFieldA012());
						// int mazeCorrect = NumberUtils.toInt(studentAssessment.getFieldA002());
						// int mazeIncorrect = NumberUtils.toInt(studentAssessment.getFieldA003());
						// int orfAcc = NumberUtils.toInt(studentAssessment.getFieldA007());
						retell = NumberUtils.toInt(studentAssessment.getFieldA008());
						retellQor = NumberUtils.toInt(studentAssessment.getFieldA009());
						mazeCorrect = NumberUtils.toInt(studentAssessment.getFieldA014());
						mazeIncorrect = NumberUtils.toInt(studentAssessment.getFieldA015());
						mazeAdjusted = NumberUtils.toInt(studentAssessment.getFieldA010());
					} else if (studentAssessment.getAssessmentDefinitionOid().contains("ASD_FR")) {

						int fps = NumberUtils.toInt(studentAssessment.getFieldA010());
						int fdl = NumberUtils.toInt(studentAssessment.getFieldA012());
						int fsp = NumberUtils.toInt(studentAssessment.getFieldA003());
						int fnm_nsc = NumberUtils.toInt(studentAssessment.getFieldA004());
						int fnm_mle = NumberUtils.toInt(studentAssessment.getFieldA005());
						int flo_mc = NumberUtils.toInt(studentAssessment.getFieldA002());
						int flo_err = NumberUtils.toInt(studentAssessment.getFieldA007());
						int flo_pre = NumberUtils.toInt(studentAssessment.getFieldA009());
						int flo_ro = NumberUtils.toInt(studentAssessment.getFieldA006());
					}

					String gradeLevel = studentAssessment.getGradeLevelCode();
					if (StringUtils.isBlank(gradeLevel)) {
						gradeLevel = studentAssessment.getStudent().getGradeLevel();
					}

					switch (studentAssessment.getAssessmentDefinitionOid()) {
					case ASD_ACADIENCE_OID_SK_BEG:
						// if values are null, is garbage
						if ((studentAssessment.getFieldA002() == null) && (studentAssessment.getFieldA012() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA002())
								|| isNullOrEmpty(studentAssessment.getFieldA012())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_ACADIENCE_OID_SK_MID:
						// fsf + lnf + psf + nwfCls;

						if ((studentAssessment.getFieldA002() == null) && (studentAssessment.getFieldA012() == null)
								&& (studentAssessment.getFieldA003() == null)
								&& (studentAssessment.getFieldA004() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA002())
								|| isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA005())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_ACADIENCE_OID_SK_END:
						// lnf + psf + nwfCls;

						if ((studentAssessment.getFieldA012() == null) && (studentAssessment.getFieldA003() == null)
								&& (studentAssessment.getFieldA004() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA005())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_ACADIENCE_OID_GR1_BEG:
						// lnf + psf + nwfCls;

						if ((studentAssessment.getFieldA012() == null
								|| "NULL".equals(studentAssessment.getFieldA012()))
								&& (studentAssessment.getFieldA003() == null
										|| "NULL".equals(studentAssessment.getFieldA003()))
								&& (studentAssessment.getFieldA004() == null
										|| "NULL".equals(studentAssessment.getFieldA004()))) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA005())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_ACADIENCE_OID_GR1_MID:
						// nwfCls + nwfWwr + orfWc

						if ((studentAssessment.getFieldA004() == null) && (studentAssessment.getFieldA005() == null)
								&& (studentAssessment.getFieldA006() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA004())
								|| isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_ACADIENCE_OID_GR1_END:
					case ASD_ACADIENCE_OID_GR2_BEG:
						// nwfWwr + orfWc

						if ((studentAssessment.getFieldA005() == null) && (studentAssessment.getFieldA006() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_ACADIENCE_OID_GR2_MID_END:
						// orfWc + retell

						if ((studentAssessment.getFieldA008() == null) && (studentAssessment.getFieldA006() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA008())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_ACADIENCE_OID_GR3_GR6:
						// orfWc + retell

						if ((studentAssessment.getFieldA008() == null) && (studentAssessment.getFieldA006() == null)
								&& (studentAssessment.getFieldA014() == null)
								&& (studentAssessment.getFieldA015() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA008())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

						break;

					/// FR ACADIENCE

					case ASD_FR_ACADIENCE_OID_SK_BEG:
						// if values are null, is garbage
						if ((studentAssessment.getFieldA010() == null) && (studentAssessment.getFieldA012() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA010())
								|| isNullOrEmpty(studentAssessment.getFieldA012())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_FR_ACADIENCE_OID_SK_MID:

						if ((studentAssessment.getFieldA010() == null) && (studentAssessment.getFieldA012() == null)
								&& (studentAssessment.getFieldA003() == null)
								&& (studentAssessment.getFieldA004() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA010())
								|| isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA004())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_FR_ACADIENCE_OID_SK_END:

						if ((studentAssessment.getFieldA012() == null) && (studentAssessment.getFieldA003() == null)
								&& (studentAssessment.getFieldA004() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA005())) {
							isIncomplete = true;
						}

						isEarlyReadingScreener = true;

						break;

					case ASD_FR_ACADIENCE_OID_GR1_BEG:

						if ((studentAssessment.getFieldA012() == null) && (studentAssessment.getFieldA003() == null)
								&& (studentAssessment.getFieldA004() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA004()))
							isIncomplete = true;

						isEarlyReadingScreener = true;
						break;

					case ASD_FR_ACADIENCE_OID_GR1_MID:

						if ((studentAssessment.getFieldA004() == null) && (studentAssessment.getFieldA005() == null)
								&& (studentAssessment.getFieldA002() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA004())
								|| isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						isEarlyReadingScreener = true;
						break;

					case ASD_FR_ACADIENCE_OID_GR1_END:

						if ((studentAssessment.getFieldA004() == null) && (studentAssessment.getFieldA005() == null)
								&& (studentAssessment.getFieldA002() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA004())
								|| isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						isEarlyReadingScreener = true;
						break;

					case ASD_FR_ACADIENCE_OID_GR2_BEG:

						if ((studentAssessment.getFieldA004() == null) && (studentAssessment.getFieldA005() == null)
								&& (studentAssessment.getFieldA002() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA004())
								|| isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						isEarlyReadingScreener = true;
						break;

					case ASD_FR_ACADIENCE_OID_GR2_MID:

						if ((studentAssessment.getFieldA007() == null) && (studentAssessment.getFieldA002() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA007())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						isEarlyReadingScreener = true;
						break;

					case ASD_FR_ACADIENCE_OID_GR2_END:

						if ((studentAssessment.getFieldA006() == null) && (studentAssessment.getFieldA002() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA006())
								|| isNullOrEmpty(studentAssessment.getFieldA002()))
							isIncomplete = true;

						isEarlyReadingScreener = true;
						break;

					default:
						break;
					}

					// logMessage("asm oid " + studentAssessment.getOid());
					/**
					 * String a1 = "ASM000000bhRbn"; String a2 = "ASM000000bhRc2"; String a3 = "ASM000000QWUAP"; String
					 * a4 = "ASM000000QWUA1"; String a5 = "ASM000000QWUAD";
					 **/

					// if ((studentAssessment.getAssessmentDefinitionOid().trim().equals(a1.trim()))) {

					if (isIncomplete == true) {
						// set the column and save the assessment as incomplete
						// logMessage("Updated assessment set incomplete " + studentAssessment.getOid());
						studentAssessment.setFieldA016("1");
					} else {
						// logMessage("Updated assessment set complete " + studentAssessment.getOid());
						studentAssessment.setFieldA016("0");
					}

					// Save changes
					if (studentAssessment.isDirty()) {
						getBroker().saveBeanForced(studentAssessment);
					}

					// logMessage("Is Garbage? " + isGarbage + " " + studentAssessment.getOid());
					if (isGarbage == true) {
						// delete this assessment
						logMessage("Deleting assessment " + studentAssessment.getOid());
						getBroker().deleteBean(studentAssessment);

					}

					/**
					 * Here we do Early Reading Screener Records
					 * 
					 * Read from each SK, GR 1, GR 2 assessments for FR and EN acadience and write values in asd oid
					 * asd00000000ERS
					 * 
					 * Let's just start with writing the results in new screener records
					 * 
					 */

					if ((isEarlyReadingScreener == true) && ((studentAssessment.getGradeLevelCode().equals("SK"))
							|| (studentAssessment.getGradeLevelCode().equals("01"))
							|| (studentAssessment.getGradeLevelCode().equals("02")))) {
						// write this to asd00000000ERS ASD_EARLY_READING_SCREENER
						processERS(studentAssessment);
					}

				} catch (Exception e) {
					// logMessage("Something broke " + e.getMessage());
					logMessage("Exception error occurred with record " + currentOid + ":\n"
							+ ExceptionUtils.getFullStackTrace(e));
				}
			}

			logMessage("How many added " + totalCount);
		}

	}

	public boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

	/**
	 * For each assessment, make sure ERS is written of updated for this student/grade/period
	 * 
	 * @param sa
	 * @return
	 */
	public void processERS(StudentAssessment sa) {

		X2Criteria criteria2 = new X2Criteria();
		// Specific assessment definitions
		// Add in student id, grade

		String currentDateString = "2024-09-05";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		boolean noExistingAssessments = false;

		if ((sa.getDate() == null) || (isNullOrEmpty(sa.getGradeLevelCode()))
				|| (isNullOrEmpty(sa.getStudentOid()) || (sa.getFieldA040() == null))) {
			return;
		}

		criteria2.addEqualTo(StudentAssessment.COL_ASSESSMENT_DEFINITION_OID, ASD_EARLY_READING_SCREENER);
		criteria2.addEqualTo(StudentAssessment.COL_STUDENT_OID, sa.getStudentOid());
		criteria2.addEqualTo(StudentAssessment.COL_GRADE_LEVEL_CODE, sa.getGradeLevelCode());
		// criteria2.addEqualTo(StudentAssessment.COL_DATE, sa.getDate());

		criteria2.addOrderByDescending(StudentAssessment.COL_DATE);

		try {

			BeanQuery query2 = new BeanQuery(StudentAssessment.class, criteria2);

			StudentAssessment stdAssess = (StudentAssessment) getBroker().getBeanByQuery(query2);

			/**
			 * LocalDate dt = LocalDate.of(2024, 9, 5);
			 * 
			 * Date date = Date.from(dt.atStartOfDay(ZoneId.systemDefault()).toInstant());
			 * 
			 * int priorDateCompare = stdAssess.getDate().compareTo(dt);
			 * 
			 * if (priorDateCompare < 0) { return; }
			 **/

			if (stdAssess == null) {
				logMessage("Adding new ERD assessment, no previous date" + sa.getStudentOid());

				StudentAssessment stdAssessAdd = X2BaseBean.newInstance(StudentAssessment.class,
						getBroker().getPersistenceKey());

				stdAssessAdd.setStudentOid(sa.getStudentOid()); //
				stdAssessAdd.setAssessmentDefinitionOid(ASD_EARLY_READING_SCREENER); //
				stdAssessAdd.setDate(sa.getDate());
				stdAssessAdd.setSchoolOid(sa.getSchoolOid());
				stdAssessAdd.setGradeLevelCode(sa.getGradeLevelCode());
				stdAssessAdd.setFieldA001("1");
				stdAssessAdd.setFieldA002(sa.getFieldA040());

				totalCount++;
				getBroker().saveBeanForced(stdAssessAdd);
			} else {
				int dateCompare = stdAssess.getDate().compareTo(sa.getDate());

				if (dateCompare == 0) { // if this record already exists for (StudentAssessment
					// update fields 001 and 002

					// logMessage("Updating student assessment " + sa.getStudentOid());

					stdAssess.setFieldA001("1");
					stdAssess.setFieldA002(sa.getFieldA040());

					// totalCount++;

					// getBroker().saveBeanForced(stdAssess);

				} else if (dateCompare < 0) { // create new and add

					// logMessage("Adding new ERD assessment, previous dates" + sa.getStudentOid());

					StudentAssessment stdAssessAdd = X2BaseBean.newInstance(StudentAssessment.class,
							getBroker().getPersistenceKey());

					stdAssessAdd.setStudentOid(sa.getStudentOid()); //
					stdAssessAdd.setAssessmentDefinitionOid(ASD_EARLY_READING_SCREENER); //
					stdAssessAdd.setDate(sa.getDate());
					stdAssessAdd.setSchoolOid(sa.getSchoolOid());
					stdAssessAdd.setGradeLevelCode(sa.getGradeLevelCode());
					stdAssessAdd.setFieldA001("1");
					stdAssessAdd.setFieldA002(sa.getFieldA040());

					totalCount++;

					getBroker().saveBeanForced(stdAssessAdd);
				} else if (dateCompare > 0) {
					// totalCount++;
				}
			}

		} catch (Exception e) {
			logMessage("Exception error occurred in ERS function:\n" + ExceptionUtils.getMessage(e));
		}
	}

	public void initializeERSMap() {
		X2Criteria criteria = new X2Criteria();
		criteria.addEqualTo(StudentAssessment.COL_ASSESSMENT_DEFINITION_OID, ASD_EARLY_READING_SCREENER);

		try (IterableQuery<StudentAssessment> query = new IterableQuery(StudentAssessment.class, criteria,
				getBroker())) {
			for (StudentAssessment stdAssess : query) {
				ThreadUtils.checkInterrupt();

				String dateCompare = stdAssess.getDate().toString();

				m_getStudentEarlyReadingList.put(
						stdAssess.getStudentOid() + "|" + stdAssess.getGradeLevelCode() + "|" + dateCompare, stdAssess);
			}
		} catch (Exception e) {
			logMessage("Exception error occurred in initializeERSMap function:\n" + ExceptionUtils.getMessage(e));
		}
	}

	public void initializeACAMap() {
		X2Criteria criteria = new X2Criteria();
		// Specific assessment definitions
		criteria.addIn(StudentAssessment.COL_ASSESSMENT_DEFINITION_OID, ACA_ASSESSMENT_OID_LIST);

		try (IterableQuery<StudentAssessment> query = new IterableQuery(StudentAssessment.class, criteria,
				getBroker())) {

			for (StudentAssessment stdAssess : query) {
				ThreadUtils.checkInterrupt();

				String dateCompare = stdAssess.getDate().toString();
				m_getStudentAssessmentList.put(
						stdAssess.getStudentOid() + "|" + stdAssess.getGradeLevelCode() + "|" + dateCompare, stdAssess);

				// logMessage("After adding to Map: " + stdAssess.getOid() + " " + stdAssess.getStudentOid() + "|"
				// + stdAssess.getGradeLevelCode() + "|" + dateCompare);
			}
		} catch (Exception e) {
			logMessage("Exception error occurred in initializeACAMap function:\n" + ExceptionUtils.getMessage(e));
		}
	}

	/**
	 * If assessment exists in ERS but not in any assessment then delete ERS
	 *
	 * @param gradeLevel
	 * @param sequence
	 * @param score
	 * @return
	 */
	private void cleanupERSAssessments() {

		try {

			Set<String> keys1 = m_getStudentEarlyReadingList.keySet();
			Set<String> keys2 = m_getStudentAssessmentList.keySet();
			Set<String> missingFromSet2 = new HashSet<>(keys1);

			String dateString = "2024-09-05";
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

			Date schoolStartDate = formatter.parse(dateString);

			missingFromSet2.removeAll(keys2);

			if (!missingFromSet2.isEmpty()) {
				// iterate through, get the object from the map, and then delete

				missingFromSet2.forEach(element -> {

					try {
						String thisDate = element.substring(element.length() - 10);

						Date raDate = formatter.parse(thisDate);

						if (raDate.after(schoolStartDate)) {

							StudentAssessment sa = m_getStudentAssessmentList.get(element);

							// getBroker().deleteBean(sa);

							logMessage(
									"Removing ERS due to missing reading assessment: " + sa.getOid() + " " + element);
						}

					} catch (ParseException e) {
						e.printStackTrace();
					}
				});
			}

		} catch (ParseException e) {
			logMessage("Exception caught in cleanupERSAssessments " + e.getMessage());
		}
	}

	/**
	 * public void initializeERSMap() { // Criteria X2Criteria criteria = new X2Criteria();
	 * criteria.addEqualTo(ReferenceCode.COL_REFERENCE_TABLE_OID, RTB_OID_ACADIANCE_BENCHMARKS); //
	 * criteria.addOrEqualTo(ReferenceCode.REL_REFERENCE_TABLE + "." + ReferenceTable.COL_ID, //
	 * RTB_ID_ACADIANCE_BENCHMARKS);
	 * 
	 * // If ref code category = 'BENCHMARK'. This is set in the ASPEN ref_code table to determine minimum benchmark
	 * criteria.addEqualTo(ReferenceCode.COL_CATEGORY, ACADIANCE_BENCHMARKS_CATEGORY);
	 * 
	 * // Iterate over the results, and populate the map try (IterableQuery<ReferenceCode> query = new
	 * IterableQuery(ReferenceCode.class, criteria, getBroker())) { for (ReferenceCode referenceCode : query) {
	 * ThreadUtils.checkInterrupt();
	 * 
	 * if ((referenceCode.getCategory() != null) && referenceCode.getCategory().equals(ACADIANCE_BENCHMARKS_CATEGORY)) {
	 * int compositeScore = NumberUtils.toInt(referenceCode.getFieldA006()); String gradeLevel =
	 * referenceCode.getFieldB006(); String sequence = referenceCode.getFieldB007();
	 * 
	 * logMessage(referenceCode.getReferenceTableOid() + " " + referenceCode.getParentOidColumn()); logMessage("'" +
	 * referenceCode.getCategory() + "' '" + ACADIANCE_BENCHMARKS_CATEGORY + "'"); logMessage(gradeLevel + sequence +
	 * compositeScore);
	 * 
	 * if ((gradeLevel != null) || (sequence != null)) m_acadianceBenchmarkMap.put(gradeLevel + sequence,
	 * compositeScore); } } } }
	 **/

}