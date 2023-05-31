package com.x2dev.reports.on;
import static com.follett.fsc.core.k12.beans.SystemPreferenceDefinition.STUDENT_ACTIVE_CODE;
import static com.follett.fsc.core.k12.business.ModelProperty.PATH_DELIMITER;
import com.follett.fsc.core.framework.persistence.SubQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.*;
import com.follett.fsc.core.k12.business.PreferenceManager;
import com.follett.fsc.core.k12.business.dictionary.DataDictionary;
import com.follett.fsc.core.k12.business.dictionary.DataDictionaryField;
import com.follett.fsc.core.k12.tools.reports.ReportDataGrid;
import com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet;
import com.follett.fsc.core.k12.web.UserDataContainer;
import com.x2dev.sis.model.beans.SisSchool;
import com.x2dev.sis.model.beans.SisStudent;
import com.x2dev.sis.model.beans.StudentEnrollment;
import com.x2dev.utils.CollectionUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.x2dev.utils.types.PlainDate;
import net.sf.jasperreports.engine.JRDataSource;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;

import com.follett.fsc.core.k12.business.localization.LocalizationCache;
import com.x2dev.utils.StringUtils;
import com.x2dev.utils.X2BaseException;
import org.apache.struts.util.MessageResources;
import org.joda.time.DateTime;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Follett School Solutions
 */
public class StudentEmergencyCardsConfirmationData extends ReportJavaSourceNet {
    /**
     * Name for the "active only" report parameter. The value is an Boolean.
     */
    private static final String ACTIVE_ONLY_PARAM = "activeOnly";

    /**
     * Name for the enumerated "extra contact count" report parameter. The value is an Integer.
     */
    private static final String EXTRA_CONTACT_COUNT_PARAM = "extraContactCount";

    /**
     * Name for the enumerated "minimum contact count" report parameter. The value is an Integer.
     */
    private static final String MINIMUM_CONTACT_COUNT_PARAM = "minimumContactCount";

    /**
     * Name for the enumerated "selection" report parameter. The value is an Integer.
     */
    private static final String QUERY_BY_PARAM = "queryBy";

    /**
     * Name for the "selection value" report parameter. The value is a String.
     */
    private static final String QUERY_STRING_PARAM = "queryString";

    /**
     * Name for the enumerated "student sort" report parameter. The value is an Integer.
     */
    private static final String STUDENT_SORT_PARAM = "studentSort";

    // ReportDataGrid fields
    private static final String CONTACT_FIELD = "contact";
    private static final String STUDENT_FIELD = "student";
    private static final String STUDENT_CONTACT_FIELD = "studentContact";

    // Additional report parameters
    private static final String ENROLLMENT_MAP_PARAM = "enrollmentsMap";
    private static final String PRIMARY_CONTACT_MAP = "primaryContacts";
    private static final String SECONDARY_CONTACT_MAP = "secondaryContacts";
    private static final String RTB_PERSON_RELATE = "rtbPsnRelate";
    private static final String RELATIONSHIP_CODE_MAP = "relationshipCodesMap";
    private static final String RTB_LANGUAGE = "rtbLanguage";
    private static final String HOME_LANGUAGE_MAP  = "homeLanguagesMap";

    private SisStudent m_currentStudent;
    private Map<String, StudentEnrollment> m_enrollments;
    
    // Localization
    private Map<String, String> m_validLocales;
    private String m_defaultLocale; // Usually English
    private MessageResources m_default_message_resource;
        private Locale m_userLocale;
    private String m_userLocaleLanguage;
    private static final String PARAM_USER_LOCALE = "userLocale";
    private static final String PARAM_PREFIX = "prefix";
    private static final String PARAM_REPORT_LOCALE = "reportLocale";
    private static final String PARAM_LOCALES = "locales";
    private static final String PARAM_DEFAULT_LOCALE = "default_locale";
    private static final String PARAM_DEFAULT_LANGUAGE = "default_language";
    private static final String CONST_TOOLS_FOR_PREFIX = "tools.";
    private static final String CONST_AMERICAN_ENGLISH_LOCALE = "en_US";
    private static final String CONST_AMERICAN_ENGLISH_LANGUAGE = "English";
    DataDictionary m_districtDictionary;
    DataDictionaryField m_contactStartDate;
    DataDictionaryField m_contactEndDate;
    private PlainDate m_today;

    /**
     * @see com.x2dev.sis.reporting.ReportDataSource#gatherData()
     */
    @Override
    protected JRDataSource gatherData() {
        /*
         * Build the criteria based on user input
         */
        Criteria studentCriteria = new Criteria();
        Criteria contactCriteria = new Criteria();

        if (m_currentStudent != null) {
            // If we're in the context of a single student, print the report for just that student
            studentCriteria.addEqualTo(X2BaseBean.COL_OID, m_currentStudent.getOid());
        } else {
            String queryBy = (String) getParameter(QUERY_BY_PARAM);
            String queryString = (String) getParameter(QUERY_STRING_PARAM);

            addUserCriteria(studentCriteria, queryBy.replaceAll("student.", ""), queryString, SisStudent.class,
                    X2BaseBean.COL_OID);
            addUserCriteria(contactCriteria, queryBy, queryString, SisStudent.class, X2BaseBean.COL_OID,
                    StudentContact.COL_STUDENT_OID);

            if (isSchoolContext()) {
                studentCriteria.addEqualTo(SisStudent.COL_SCHOOL_OID, ((SisSchool) getSchool()).getOid());
                contactCriteria.addEqualTo(StudentContact.REL_STUDENT + PATH_DELIMITER + SisStudent.COL_SCHOOL_OID,
                        ((SisSchool) getSchool()).getOid());
            }

            boolean activeOnly = ((Boolean) getParameter(ACTIVE_ONLY_PARAM)).booleanValue();
            if (activeOnly) {
                String activeCode = PreferenceManager.getPreferenceValue(getOrganization(), STUDENT_ACTIVE_CODE);
                studentCriteria.addEqualTo(SisStudent.COL_ENROLLMENT_STATUS, activeCode);
                contactCriteria.addEqualTo(
                        StudentContact.REL_STUDENT + PATH_DELIMITER + SisStudent.COL_ENROLLMENT_STATUS, activeCode);
            }
        }

        X2Criteria startDateCriteria = new X2Criteria();
        X2Criteria startDateOrCriteria = new X2Criteria();
        startDateCriteria.addLessOrEqualThan("CAST(" + m_contactStartDate.getDatabaseName() + " as datetime)", m_today);
        startDateOrCriteria.addEmpty(StudentContact.COL_FIELD_A009, getBroker().getPersistenceKey());
        startDateCriteria.addOrCriteria(startDateOrCriteria);
        contactCriteria.addAndCriteria(startDateCriteria);

        X2Criteria endDateCriteria = new X2Criteria();
        X2Criteria endDateOrCriteria = new X2Criteria();
        endDateCriteria.addGreaterThan("CAST(" + m_contactEndDate.getDatabaseName() + " as datetime)", m_today);
        endDateOrCriteria.addEmpty(StudentContact.COL_FIELD_A010, getBroker().getPersistenceKey());
        endDateCriteria.addOrCriteria(endDateOrCriteria);
        contactCriteria.addAndCriteria(endDateCriteria);

        QueryByCriteria studentQuery = new QueryByCriteria(SisStudent.class, studentCriteria);
        QueryByCriteria contactQuery = new QueryByCriteria(StudentContact.class, contactCriteria);

        logToolMessage(Level.INFO, getBroker().getSelectSql(contactQuery) + "\n" + contactQuery.getCriteria(), false);
        logToolMessage(Level.INFO, "Count: " + getBroker().getCount(contactQuery), false);

        String sortBy = (String) getParameter(STUDENT_SORT_PARAM);
        String contactSort = "emergencyPriority";
        applyUserSort(studentQuery, sortBy);
        applyUserSort(contactQuery, contactSort);

        /*
         * Combine the results of the two queries into a single grid. Add extra (blank) rows without
         * contacts as necessary according to the user input.
         */
        int minContactCount = ((Integer) getParameter(MINIMUM_CONTACT_COUNT_PARAM)).intValue();
        int extraContactCount = ((Integer) getParameter(EXTRA_CONTACT_COUNT_PARAM)).intValue();

        ReportDataGrid grid = new ReportDataGrid(3);

        int studentCount = getBroker().getCount(studentQuery);
        Map<String, Collection<StudentContact>> contactMap =
                getBroker().getGroupedCollectionByQuery(contactQuery, StudentContact.COL_STUDENT_OID, studentCount);

        loadMembershipEnrollmentDate(studentCriteria, studentCount);

        Map<String, StudentContact> primaryContacts = new HashMap<String, StudentContact>(1024);
        Map<String, StudentContact> secondaryContacts = new HashMap<String, StudentContact>(1024);

        QueryIterator students = getBroker().getIteratorByQuery(studentQuery);
        try {
            while (students.hasNext()) {
                SisStudent student = (SisStudent) students.next();

                Collection<StudentContact> contacts = contactMap.get(student.getOid());

                int count = 0;

                if (CollectionUtils.isEmpty(contacts)) {
                    grid.append();
                    grid.set(STUDENT_FIELD, student);
                } else {
                    //contacts = contacts.stream().filter(x->x.getFieldA010()==null|| DateTime.parse(x.getFieldA010()).isAfterNow())
                            //.collect(Collectors.toList());

                    for (StudentContact contact : contacts) {
                        if (count == 0) {
                            primaryContacts.put(student.getOid(), contact);
                        } else if (count == 1) {
                            secondaryContacts.put(student.getOid(), contact);
                        } else {
                            grid.append();
                            grid.set(STUDENT_FIELD, student);
                            grid.set(CONTACT_FIELD, contact.getContact());
                            grid.set(STUDENT_CONTACT_FIELD, contact);

                        }
                        count++;
                    }
                }

                /*
                 * Start the counter at 2 to handle students without a primary and/or secondary
                 * contact. We only need to add "fake" contacts 3 and 4 because the format
                 * hard-codes the display of contacts 1 and 2.
                 */
                if (count < 2) {
                    count = 2;
                }

                int blankCount = 0;
                while (count < minContactCount || blankCount < extraContactCount) {
                    grid.append();
                    grid.set(STUDENT_FIELD, student);

                    count++;
                    blankCount++;
                }
            }
        } finally {
            students.close();
        }

        addLocalizedValues();
        addParameter(ENROLLMENT_MAP_PARAM, m_enrollments);
        addParameter(PRIMARY_CONTACT_MAP, primaryContacts);
        addParameter(SECONDARY_CONTACT_MAP, secondaryContacts);

        grid.beforeTop();

        return grid;
    }

    /**
     * Add maps with localized values
     */
    private void addLocalizedValues() {
        Map<String, String> personRelationShipTranslatedMap = OntarioToolHelper.getLocalizedRefDescMap(getBroker(), RTB_PERSON_RELATE, m_userLocale.getDisplayLanguage());
        Map<String, String> homeLanguageTranslatedMap = OntarioToolHelper.getLocalizedRefDescMap(getBroker(), RTB_LANGUAGE, m_userLocale.getDisplayLanguage());

        addParameter(RELATIONSHIP_CODE_MAP, personRelationShipTranslatedMap);
        addParameter(HOME_LANGUAGE_MAP, homeLanguageTranslatedMap);
    }

    /**
     *
     * @see com.x2dev.sis.reporting.ReportDataSource#initialize(com.x2dev.sis.web.UserDataContainer)
     */
    @Override
    protected void saveState(UserDataContainer userData) {
        m_currentStudent = (SisStudent) userData.getCurrentRecord(SisStudent.class);

               m_userLocale = userData.getLocale();
        m_userLocaleLanguage = userData.getLocale().getDisplayLanguage();

    }

    /**
     * @see com.follett.fsc.core.k12.tools.ToolJavaSource#initialize()
     */
    @Override
    protected void initialize() throws X2BaseException {
        super.initialize();

        m_districtDictionary = DataDictionary.getDistrictDictionary(getBroker().getPersistenceKey());
        m_contactStartDate = m_districtDictionary.findDataDictionaryFieldByAlias("all-ctj-StartDate");
        m_contactEndDate = m_districtDictionary.findDataDictionaryFieldByAlias("all-ctj-EndDate");

        m_today = (PlainDate) getParameter("currentDate");
       
        // Enabling localization
        initializeLocalized();
    }


    /**
     * Initializes for localization.
     *
     * Adds the localization parameters
     * Populates the Valid Locales map
     */
    private void initializeLocalized() {
        Collection<OrganizationLocale> locales = getOrganization().getRootOrganization().getLocales();
        Map<String, MessageResources> resources = new HashMap<String, MessageResources>();
        m_validLocales = new HashMap<String, String>();

       
        if (m_userLocale != null) {
            m_default_message_resource =
                    LocalizationCache.getMessages(getBroker().getPersistenceKey(), m_userLocale);
        } else {
            m_default_message_resource = LocalizationCache.getMessages(getBroker().getPersistenceKey(), LocalizationCache.getCurrentLocale());
        }

        if (StringUtils.isBlank(m_userLocaleLanguage)) {
            m_userLocaleLanguage = LocalizationCache.getCurrentLocale().getDisplayLanguage();
        }
    
 for (OrganizationLocale loc : locales) {
            if (loc.getEnabledIndicator()) {

                MessageResources messages = LocalizationCache.getMessages(getBroker().getPersistenceKey(),
                        loc.getLocale());
                // save the messages for that language
                resources.put(loc.getLocale(), messages);

                // populate the map of valid locales
                m_validLocales.put(loc.getName(), loc.getLocale());
                if (loc.getPrimaryIndicator()) {
                    m_defaultLocale = loc.getLocale();
                }
            }
        }

        if (m_defaultLocale == null) {
            m_defaultLocale = CONST_AMERICAN_ENGLISH_LOCALE;
        }
        addParameter(PARAM_PREFIX, CONST_TOOLS_FOR_PREFIX + getJob().getTool().getOid() + ".");
        addParameter(PARAM_REPORT_LOCALE, m_default_message_resource);
        // Additional hooks for enhanced implementations
        addParameter(PARAM_LOCALES, resources);
        addParameter(PARAM_DEFAULT_LOCALE, CONST_AMERICAN_ENGLISH_LOCALE);
        addParameter(PARAM_DEFAULT_LANGUAGE, CONST_AMERICAN_ENGLISH_LANGUAGE);
        addParameter(PARAM_USER_LOCALE, m_userLocale.toString());
        // Comment line below if your numeric notation, currencies and others don't display as expected
        addParameter(net.sf.jasperreports5.engine.JRParameter.REPORT_LOCALE, m_userLocale); // Only tested for JasperReports engine 5
    }

    /**
     * Loads a map of enrollment record keyed on student oid. This only loads the first enrollment
     * record
     * from the school that this report is being run from.
     *
     * @param studentCriteria
     *
     * @param studentCount
     */
    private void loadMembershipEnrollmentDate(Criteria studentCriteria, int studentCount) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(StudentEnrollment.COL_SCHOOL_OID, ((SisSchool) getSchool()).getOid());
        criteria.addEqualTo(StudentEnrollment.COL_ENROLLMENT_TYPE, StudentEnrollment.ENTRY);

        SubQuery studentSub = new SubQuery(SisStudent.class, X2BaseBean.COL_OID, studentCriteria);
        criteria.addIn(StudentEnrollment.COL_STUDENT_OID, studentSub);

        QueryByCriteria query = new QueryByCriteria(StudentEnrollment.class, criteria);
        query.addOrderByDescending(StudentEnrollment.COL_ENROLLMENT_DATE);

        m_enrollments = getBroker().getMapByQuery(query, StudentEnrollment.COL_STUDENT_OID, studentCount);
    }

}