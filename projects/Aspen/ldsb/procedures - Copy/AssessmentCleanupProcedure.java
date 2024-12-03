package ldsb.procedures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.follett.fsc.core.framework.persistence.IterableQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.StudentAlert;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.x2dev.sis.model.beans.StudentAssessment;
import com.x2dev.utils.ThreadUtils;

public class AssessmentCleanupProcedure extends ProcedureJavaSource {

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

	private static final String ALR_ICON = "alertIcons/cl_study.png";

	private static final List<String> ASSESSMENT_OID_LIST = Arrays.asList(ASD_OID_SK_BEG, ASD_OID_SK_MID,
			ASD_OID_SK_END, ASD_OID_GR1_BEG, ASD_OID_GR1_MID, ASD_OID_GR1_END, ASD_OID_GR2_BEG, ASD_OID_GR2_MID_END,
			ASD_OID_GR3_GR6);

	Map<String, StudentAlert> m_getStudentBenchmarkAlert = new HashMap<>();

	@Override
	protected void execute() throws Exception {

		boolean isGarbage = false;
		boolean isIncomplete = false;

		X2Criteria criteria = new X2Criteria();
		// Specific assessment definitions
		criteria.addIn(StudentAssessment.COL_ASSESSMENT_DEFINITION_OID, ASSESSMENT_OID_LIST);

		String currentOid = null;

		try (IterableQuery<StudentAssessment> query = new IterableQuery(StudentAssessment.class, criteria,
				getBroker())) {
//			Specify any ordering for the query results, if needed
//			query.addOrderByAscending(StudentAssessment.COL_FIELD_B001);
			for (StudentAssessment studentAssessment : query) {

				isGarbage = false;
				isIncomplete = false;

				try {
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
					// int mazeCorrect = NumberUtils.toInt(studentAssessment.getFieldA002());
					// int mazeIncorrect = NumberUtils.toInt(studentAssessment.getFieldA003());
					// int orfAcc = NumberUtils.toInt(studentAssessment.getFieldA007());
					int retell = NumberUtils.toInt(studentAssessment.getFieldA008());
					int retellQor = NumberUtils.toInt(studentAssessment.getFieldA009());
					int mazeCorrect = NumberUtils.toInt(studentAssessment.getFieldA014());
					int mazeIncorrect = NumberUtils.toInt(studentAssessment.getFieldA015());
					int mazeAdjusted = NumberUtils.toInt(studentAssessment.getFieldA010());

					String gradeLevel = studentAssessment.getGradeLevelCode();
					if (StringUtils.isBlank(gradeLevel)) {
						gradeLevel = studentAssessment.getStudent().getGradeLevel();
					}

					switch (studentAssessment.getAssessmentDefinitionOid()) {
					case ASD_OID_SK_BEG:
						// if values are null, is garbage
						if ((studentAssessment.getFieldA002() == null) && (studentAssessment.getFieldA012() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA002())
								|| isNullOrEmpty(studentAssessment.getFieldA012())) {
							isIncomplete = true;
						}

						break;

					case ASD_OID_SK_MID:
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

						break;

					case ASD_OID_SK_END:
						// lnf + psf + nwfCls;

						if ((studentAssessment.getFieldA012() == null) && (studentAssessment.getFieldA003() == null)
								&& (studentAssessment.getFieldA004() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA005())) {
							isIncomplete = true;
						}

						break;

					case ASD_OID_GR1_BEG:
						// lnf + psf + nwfCls;

						if ((studentAssessment.getFieldA012() == null) && (studentAssessment.getFieldA003() == null)
								&& (studentAssessment.getFieldA004() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA012())
								|| isNullOrEmpty(studentAssessment.getFieldA003())
								|| isNullOrEmpty(studentAssessment.getFieldA005())) {
							isIncomplete = true;
						}

						break;

					case ASD_OID_GR1_MID:
						// nwfCls + nwfWwr + orfWc

						if ((studentAssessment.getFieldA004() == null) && (studentAssessment.getFieldA005() == null)
								&& (studentAssessment.getFieldA006() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA004())
								|| isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

						break;

					case ASD_OID_GR1_END:
					case ASD_OID_GR2_BEG:
						// nwfWwr + orfWc

						if ((studentAssessment.getFieldA005() == null) && (studentAssessment.getFieldA006() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA005())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

						break;

					case ASD_OID_GR2_MID_END:
						// orfWc + retell

						if ((studentAssessment.getFieldA008() == null) && (studentAssessment.getFieldA006() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA008())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

						break;

					case ASD_OID_GR3_GR6:
						// orfWc + retell

						if ((studentAssessment.getFieldA008() == null) && (studentAssessment.getFieldA006() == null)) {
							isGarbage = true;
						} else if (isNullOrEmpty(studentAssessment.getFieldA008())
								|| isNullOrEmpty(studentAssessment.getFieldA006())) {
							isIncomplete = true;
						}

					default:
						break;
					}

					logMessage("asm oid " + studentAssessment.getOid());
					String a1 = "ASM000000bhRbn";
					String a2 = "ASM000000bhRc2";
					String a3 = "ASM000000QWUAP";
					String a4 = "ASM000000QWUA1";

					if ((studentAssessment.getOid().trim().equals(a1.trim()))
							|| (studentAssessment.getOid().trim().equals(a2.trim()))
							|| (studentAssessment.getOid().trim().equals(a3.trim()))
							|| (studentAssessment.getOid().trim().equals(a4.trim()))) {

						logMessage("This is running ");

						if (isIncomplete == true) {
							// set the column and save the assessment as incomplete
							logMessage("Updated assessment set incomplete " + studentAssessment.getOid());
							studentAssessment.setFieldA016("1");
						} else {
							studentAssessment.setFieldA016("0");
						}

						// Save changes
						if (studentAssessment.isDirty()) {
							getBroker().saveBeanForced(studentAssessment);
						}

						if (isGarbage == true) {
							// delete this assessment
							logMessage("Deleting benchmark " + studentAssessment.getOid());
							getBroker().deleteBean(studentAssessment);
						}
					}

				} catch (Exception e) {
					// logMessage("Exception error occurred with record " + currentOid + ":\n"
					// + ExceptionUtils.getFullStackTrace(e));
				}
			}
		} catch (Exception e) {
			// logMessage(
			// "Exception error occurred with record " + currentOid + ":\n" + ExceptionUtils.getFullStackTrace(e));
		}

	}

	public boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

}