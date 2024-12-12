package ldsb.procedures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;

import com.follett.fsc.core.framework.persistence.BeanQuery;
import com.follett.fsc.core.framework.persistence.SubQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.QueryIterator;
import com.follett.fsc.core.k12.beans.School;
import com.follett.fsc.core.k12.beans.Student;
import com.follett.fsc.core.k12.beans.X2BaseBean;
import com.follett.fsc.core.k12.business.ModelBroker;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.follett.fsc.core.k12.web.UserDataContainer;
import com.x2dev.sis.model.beans.Course;
import com.x2dev.sis.model.beans.MasterSchedule;
import com.x2dev.sis.model.beans.Schedule;
import com.x2dev.sis.model.beans.ScheduleTeacher;
import com.x2dev.sis.model.beans.SchoolCourse;
import com.x2dev.sis.model.beans.StudentSchedule;
import com.x2dev.sis.model.beans.path.SisBeanPaths;
import com.x2dev.utils.ThreadUtils;
import com.x2dev.utils.X2BaseException;

public class ElementarySubjectSetup extends ProcedureJavaSource {
	private static final long serialVersionUID = 1L;

	public static final String TEST_RUN_PARAM = "testRun";
	public static final String DEFAULT_TEACHER_PARAM = "defaultTeacher";
	private static final String PARAM_SCHOOL_OID = "schoolOid";

	private ModelBroker m_modelBroker;
	private String m_schoolOid;
	private String m_currentContext;

	private Map<String, School> m_schoolsMap;

	// Constants
	private static final String INACTIVE_INDICATOR = "0";
	private static final String ELEMENTARY_DESCRIPTION = "Elementary";
	private static final String ELEMENTARY_LEVEL_CODE = "01";

	private static final String OUT_OF_BOARD_SCHOOL_OID = "SKL000OnSISOOB";

	@Override
	protected void initialize() {
		loadSchools();
	}

	@Override
	protected void execute() throws Exception {
		// Variables
		
		List<String> sectionsCreated = new ArrayList<String>();

		// Get input parameters
		boolean isTestRun = ((Boolean) getParameter(TEST_RUN_PARAM)).booleanValue();
		boolean defaultTeacher = ((Boolean) getParameter(DEFAULT_TEACHER_PARAM)).booleanValue();
		
        m_schoolsMap = new HashMap<>();
		
		m_schoolOid = (String) getParameter(PARAM_SCHOOL_OID);
		
		if (m_schoolOid != null)
			
			
		// Gather subjects mapped out by grade level.
		Map<String, Collection<SchoolCourse>> schoolCoursesGradeMap = getSchoolCourses();

		// Gather Homerooms minus ALLP and LSKKG
		List<MasterSchedule> masterScheduleList = getHomerooms();

		// Gather all sections
		List<String> mstSections = getSections();

		// Add heading to log if in Preview Mode
		if (isTestRun) {
			logMessage("PREVIEW MODE");
			logMessage("-------------------------------");
		}

		masterScheduleList.forEach(mst -> {
			// List of students in the Homeroom
			List<Student> studentList = getStudents(mst.getOid());

			// Distinct grade levels found in the Homeroom
			List<String> gradeLevels = getGradeLevels(studentList);

			// Get homeroom teacher
			ScheduleTeacher mtc = getDefaultTeacher(mst);

			// Print results to log
			logMessage(mst.getCourseView() + " - Grade Levels : " + gradeLevels.toString() + " Students : "
					+ studentList.size());

			String regex = "^(JK|SK|01|02|03|04|05|06|07|08)$";

			if (!isTestRun) {

				// Setup subjects for gradelevels of students found in the homeroom.
				gradeLevels.forEach(grdLvl -> {

					if (grdLvl.matches(regex)) {

						Collection<SchoolCourse> schoolCourseCollection = schoolCoursesGradeMap.get(grdLvl);

						if (schoolCourseCollection != null)
							logMessage("school course collection size " + schoolCourseCollection.size());
						else {
							logMessage("school course collection is null ");

						}

						if (schoolCourseCollection != null)
							for (SchoolCourse schoolCourse : schoolCourseCollection) {

								String courseView = schoolCourse.getNumber() + "-" + mst.getSectionNumber();
								// logMessage("Course View : " + courseView);
								// if (subjectVerification(schoolCourse.getOid(),mst.getPrimaryRoomOid()))
								if (!mstSections.contains(courseView)) {

									// logMessage("*** Section created : " + courseView);

									sectionsCreated.add("1");

									// Create new subject school course
									MasterSchedule new_mst = new MasterSchedule(m_modelBroker.getPersistenceKey());
									new_mst.setSchoolCourseOid(schoolCourse.getOid());
									new_mst.setSectionNumber(mst.getSectionNumber());
									new_mst.setPrimaryRoomOid(mst.getPrimaryRoomOid());
									new_mst.setScheduleTermOid(mst.getScheduleTermOid());
									new_mst.setScheduleOid(mst.getScheduleOid());
									new_mst.setEnrollmentMax(35);
									new_mst.setEnrollmentMaxCloseIndicator(true);
									new_mst.setPlatoonCode(mst.getPlatoonCode());
									new_mst.setCourseView(mst.getSectionNumber());
									new_mst.setDescription(schoolCourse.getNumber() + "-" + mst.getSectionNumber());
									new_mst.setTermView(mst.getScheduleTerm().getCode());

									if (new_mst.isDirty()) {
										m_modelBroker.saveBeanForced(new_mst);
									}

									if (defaultTeacher) {
										// Add default teacher to subject
										addDefaultTeacher(new_mst, mtc);
									}

									List enrolTotal = new ArrayList<>();
									// School manually schedules students into Native Languages
									if (!schoolCourse.getNumber().contains("0060")) {
										studentList.forEach(std -> {
											if (std.getGradeLevel().equals(grdLvl)) {
												// increment enrolment
												enrolTotal.add("1");

												// Add new subject to students schedule
												StudentSchedule ssc = new StudentSchedule(
														m_modelBroker.getPersistenceKey());
												ssc.setStudentOid(std.getOid());
												ssc.setSectionOid(new_mst.getOid());
												ssc.setScheduleOid(new_mst.getScheduleOid());
												ssc.setTermView(mst.getScheduleTerm().getCode());
												if (ssc.isDirty()) {
													m_modelBroker.saveBeanForced(ssc);
												}
											}
										});
									}
									new_mst.setEnrollmentTotal(enrolTotal.size());

									if (new_mst.isDirty()) {
										m_modelBroker.saveBeanForced(new_mst);
									}

								}
								// Checking for tool cancellations
								ThreadUtils.checkInterrupt();

							}
					}

				});

			}
		});
		logMessage("-------------------------------");
		logMessage("Total Homerooms found : " + masterScheduleList.size());
		logMessage("Sections created : " + sectionsCreated.size());
	}

	private ScheduleTeacher getDefaultTeacher(MasterSchedule mst) {

		X2Criteria mtcCriteria = new X2Criteria();
		mtcCriteria.addEqualTo(ScheduleTeacher.COL_STAFF_OID, mst.getPrimaryStaffOid());
		mtcCriteria.addEqualTo(ScheduleTeacher.COL_SECTION_OID, mst.getOid());

		QueryByCriteria mtcQuery = new QueryByCriteria(ScheduleTeacher.class, mtcCriteria);
		ScheduleTeacher mtc = m_modelBroker.getBeanByQuery(mtcQuery);

		return mtc;

	}

	private void addDefaultTeacher(MasterSchedule new_mst, ScheduleTeacher mtc) {
		if (mtc instanceof ScheduleTeacher) {
			ScheduleTeacher new_mtc = new ScheduleTeacher(m_modelBroker.getPersistenceKey());
			new_mtc.setScheduleTermOid(mtc.getScheduleTermOid());
			new_mtc.setScheduleDisplay(mtc.getScheduleDisplay());
			new_mtc.setRole("Primary");
			new_mtc.setStaffOid(mtc.getStaffOid());
			new_mtc.setPrimaryTeacherIndicator(true);
			new_mtc.setGradebookAccessIndicator(true);
			new_mtc.setSectionOid(new_mst.getOid());

			if (new_mtc.isDirty()) {
				m_modelBroker.saveBeanForced(new_mtc);
			}
			try {
				new_mst.setStaffView(new_mtc.getStaff().getNameView());
				if (new_mst.isDirty()) {
					m_modelBroker.saveBeanForced(new_mst);
				}
			} catch (Exception e) {
			}
		}
	}

	private List<String> getSections() {
		List<String> sections = new ArrayList<String>();

		logMessage("get sections: DISTRICT CONTEXT " + m_currentContext + " school id: " + m_schoolOid);

		X2Criteria mstCriteria = new X2Criteria();
		mstCriteria.addEqualTo(SisBeanPaths.SCHEDULE_MASTER.schedule().districtContextOid().getPath(),
				m_currentContext);
		mstCriteria.addEqualTo(SisBeanPaths.SCHEDULE_MASTER.schedule().schoolOid().getPath(), m_schoolOid);

		// logMessage("get sections: SisBeanPaths.SCHEDULE_MASTER.schedule().schoolOid().getPath" +
		// SisBeanPaths.SCHEDULE_MASTER.schedule().schoolOid().getPath() + m_currentContext + " context: " +
		// m_currentContext);

		BeanQuery mstQuery = new BeanQuery(MasterSchedule.class, mstCriteria);
		try (QueryIterator iterator = getBroker().getIteratorByQuery(mstQuery)) {
			logMessage("Sesctions iterator: " + iterator);
			while (iterator.hasNext()) {
				MasterSchedule mst = (MasterSchedule) iterator.next();
				logMessage("Sesctions course view: " + mst.getCourseView());
				sections.add(mst.getCourseView());
			}
		} catch (Exception e) {
			logMessage("Exception message " + e.getMessage());
		}
		return sections;
	}

	private List<String> getGradeLevels(List<Student> studentList) {
		ArrayList<String> gradeLevels = new ArrayList<String>();

		studentList.forEach(std -> {
			if (!gradeLevels.contains(std.getGradeLevel())) {
				gradeLevels.add(std.getGradeLevel());
			}
			;
		});

		Collections.sort(gradeLevels);
		return gradeLevels;
	}

	private Map<String, Collection<SchoolCourse>> getSchoolCourses() {
		X2Criteria schoolCourseCriteria = new X2Criteria();
		X2Criteria courseCriteria = new X2Criteria();

		logMessage("get school courses: DISTRICT CONTEXT " + m_currentContext + " school id: " + m_schoolOid);

		courseCriteria.addEqualTo(Course.COL_DISTRICT_CONTEXT_OID, m_currentContext);
		SubQuery courseSubQuery = new SubQuery(Course.class, X2BaseBean.COL_OID, courseCriteria, true);

		schoolCourseCriteria.addContains(SchoolCourse.COL_SCHOOL_OID, m_schoolOid);
		schoolCourseCriteria.addNotContains(SchoolCourse.COL_NUMBER, "Homeroom");
		schoolCourseCriteria.addNotContains(SchoolCourse.COL_NUMBER, "LSKKG");
		schoolCourseCriteria.addIn(SchoolCourse.COL_COURSE_OID, courseSubQuery);

		BeanQuery query = new BeanQuery(SchoolCourse.class, schoolCourseCriteria, true, true);
		query.addOrderByAscending(SisBeanPaths.COURSE_SCHOOL.course().number().getPath());

		Map<String, Collection<SchoolCourse>> schoolCoursesGradeMap = getBroker().getGroupedCollectionByQuery(query,
				SchoolCourse.COL_GRADE_LEVEL, 100);

		return schoolCoursesGradeMap;
	}

	private List<MasterSchedule> getHomerooms() {
		X2Criteria homeroomCriteria = new X2Criteria();
		X2Criteria scheduleCriteria = new X2Criteria();

		logMessage("get homerooms: DISTRICT CONTEXT " + m_currentContext + " school id: " + m_schoolOid);

		scheduleCriteria.addEqualTo(Schedule.COL_DISTRICT_CONTEXT_OID, m_currentContext);
		scheduleCriteria.addEqualTo(Schedule.COL_SCHOOL_OID, m_schoolOid);
		SubQuery scheduleSubQuery = new SubQuery(Schedule.class, X2BaseBean.COL_OID, scheduleCriteria, true);

		homeroomCriteria.addContains(MasterSchedule.COL_COURSE_VIEW, "Homeroom");
		homeroomCriteria.addNotContains(MasterSchedule.COL_COURSE_VIEW, "ALLP");
		homeroomCriteria.addIn(MasterSchedule.COL_SCHEDULE_OID, scheduleSubQuery);

		BeanQuery query = new BeanQuery(MasterSchedule.class, homeroomCriteria, true, true);
		query.addOrderByAscending(MasterSchedule.COL_COURSE_VIEW);

		return (List<MasterSchedule>) m_modelBroker.getCollectionByQuery(query);
	}

	private List<Student> getStudents(String mstOid) {
		X2Criteria studentCriteria = new X2Criteria();
		X2Criteria scheduleCriteria = new X2Criteria();

		scheduleCriteria.addEqualTo(StudentSchedule.COL_SECTION_OID, mstOid);
		SubQuery scheduleSubQuery = new SubQuery(StudentSchedule.class, StudentSchedule.COL_STUDENT_OID,
				scheduleCriteria, true);

		studentCriteria.addIn(Student.COL_OID, scheduleSubQuery);

		BeanQuery query = new BeanQuery(Student.class, studentCriteria, true, true);

		return (List<Student>) m_modelBroker.getCollectionByQuery(query);
	}

	@Override
	protected void saveState(UserDataContainer userData) throws X2BaseException {
		super.saveState(userData);
		m_schoolOid = userData.getSchoolOid();
		m_currentContext = userData.getCurrentContext().getOid();
		m_modelBroker = new ModelBroker(userData);

	}

	private void loadSchools() {
		m_schoolsMap = new HashMap<>();

		m_schoolsMap.putAll(loadSchoolsByCriteria(buildElementarySchoolsCriteria()));
	}

	private Criteria buildElementarySchoolsCriteria() {
		Criteria criteria = new Criteria();
		criteria.addEqualTo(SisBeanPaths.SCHOOL.inactiveIndicator().getPath(), INACTIVE_INDICATOR);
		criteria.addIn(SisBeanPaths.SCHOOL.schoolLevelCode().getPath(),
				Arrays.asList(ELEMENTARY_DESCRIPTION, ELEMENTARY_LEVEL_CODE));

		// addExcludedSchoolsCriteria(criteria, null);

		return criteria;
	}

	private Map<String, School> loadSchoolsByCriteria(Criteria criteria) {
		Query query = new QueryByCriteria(School.class, criteria);
		return getBroker().getMapByQuery(query, X2BaseBean.COL_OID, 10);
	}

	/**
	 * private void addExcludedSchoolsCriteria(Criteria criteria, String schoolPath) { String path = ""; String
	 * schoolLevelPath = ""; String schoolOidPath = ""; if (StringUtils.isBlank(schoolPath)) { path =
	 * getJavaName(ALIAS_SKL_SPECIAL_CONDITION); schoolLevelPath = SisBeanPaths.SCHOOL.schoolLevelCode().getPath();
	 * schoolOidPath = SisBeanPaths.SCHOOL.oid().getPath(); } else { path = schoolPath + PATH_DELIMITER +
	 * getJavaName(ALIAS_SKL_SPECIAL_CONDITION); schoolLevelPath = schoolPath + PATH_DELIMITER +
	 * SisBeanPaths.SCHOOL.schoolLevelCode().getPath(); schoolOidPath = schoolPath + PATH_DELIMITER +
	 * X2BaseBean.COL_OID; } Criteria notInCriteria = new Criteria(); notInCriteria.addNotIn(path,
	 * StringUtils.convertDelimitedStringToList(CONTINUE_EDUCATION_CONDITION_CODES, ","));
	 * 
	 * Criteria orNullCriteria = new Criteria(); orNullCriteria.addIsNull(path);
	 * 
	 * notInCriteria.addOrCriteria(orNullCriteria); criteria.addAndCriteria(notInCriteria);
	 * 
	 * Criteria schoolLevelNotEmptyCriteria = new Criteria(); schoolLevelNotEmptyCriteria.addNotNull(schoolLevelPath);
	 * schoolLevelNotEmptyCriteria.addNotEqualTo(schoolLevelPath, "");
	 * schoolLevelNotEmptyCriteria.addNotEqualTo(schoolOidPath, OUT_OF_BOARD_SCHOOL_OID);
	 * 
	 * criteria.addAndCriteria(schoolLevelNotEmptyCriteria); }
	 **/

}
