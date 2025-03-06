/*
 * ====================================================================
 *
 * Follett Software Company
 *
 * Copyright (c) 2020 Follett Software Company
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is not permitted without a written agreement
 * from Follett Software Company.
 *
 * ====================================================================
 */
package com.x2dev.reports.on.reports.SuspensionLetters;

import java.util.*;
import java.util.Locale;

import com.follett.fsc.core.k12.beans.*;
import com.follett.fsc.core.k12.business.X2Broker;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.struts.util.MessageResources;

import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.Address;
import com.follett.fsc.core.k12.beans.Person;
import com.follett.fsc.core.k12.beans.ReferenceCode;
import com.follett.fsc.core.k12.beans.School;
import com.follett.fsc.core.k12.beans.SchoolCalendar;
import com.follett.fsc.core.k12.beans.Staff;
import com.follett.fsc.core.k12.beans.Student;
import com.follett.fsc.core.k12.beans.StudentContact;
import com.follett.fsc.core.k12.business.dictionary.DataDictionary;
import com.follett.fsc.core.k12.business.dictionary.DataDictionaryField;
import com.follett.fsc.core.k12.tools.reports.ReportDataGrid;
import com.follett.fsc.core.k12.tools.reports.SecondaryStudentDataSource;
import com.follett.fsc.core.k12.web.UserDataContainer;
import com.x2dev.reports.on.OntarioToolHelper;
import com.x2dev.reports.on.OntarioAlias;
import com.x2dev.sis.model.beans.ConductAction;
import com.x2dev.sis.model.beans.ConductIncident;
import com.x2dev.sis.model.beans.SisSchool;
import com.x2dev.sis.model.beans.SisSchoolCalendarDate;
import com.x2dev.utils.StringUtils;
import com.x2dev.utils.X2BaseException;
import com.x2dev.utils.types.PlainDate;
import java.text.SimpleDateFormat;
import com.x2dev.sis.model.beans.path.SisBeanPaths;
import org.apache.ojb.broker.query.ReportQueryByCriteria;

import net.sf.jasperreports.engine.JRDataSource;

/**
 * The Class SuspensionLettersData is for: Suspension Pending Letter Suspension
 * Confirmed Letter Suspension 1-5 Days Letter Suspension 6+ Days Letter
 */
public class SuspensioLettersData extends SecondaryStudentDataSource {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/*
	 * Input parameters
	 */
	private static final String INPUT_INCLUDE_COPY = "includeCopy";
	private static final String INPUT_BOARD_EXPELLED = "boardExpelled";
	private static final String INPUT_DATE_OF_LETTER = "dateOfLetter";
	private static final String INPUT_AGE_18_ONLY = "age18";
	private static final String INPUT_CONTACTS_OIDS = "contactOids";
	private static final String INPUT_V_PRINCIPAL_OIDS = "vicePrincipalStfOids";

	/*
	 * Const
	 */
	private static final String CONST_SUSPENSION_ACTION_CODE = "Suspension";
	private static final String CONST_TOOLS_FOR_PREFIX = "tools.";
	private static final String CONST_COMA = ",";
	private static final String CONST_SPACE = " ";

	// report params
	private static final String REPORT_PREFIX = "prefix";
	private static final String REPORT_SCHOOL_LOCALE = "schoolLocale";
	private Locale m_userLocale;
	public static final String RTB_CND_LOCATION = "rtbCndLocation";
	public static final String LOCALIZED_LOCATIONS_MAP = "LocalizedLocationsMap";
	public static final String SCHOOL_LANGUAGE_ALIAS = "all-skl-LanguageType";

	/*
	 * Grid fields
	 */
	private static final String FIELD_ACTION = "action";
	private static final String FIELD_ADDRESS = "mailingAddress";
	private static final String FIELD_STUDENT = "student";
	private static final String FIELD_INCIDENT = "incident";
	private static final String FIELD_PERSON = "person";
	private static final String FIELD_AGE = "age";
	private static final String FIELD_RETURN_DATE = "returnDate";
	private static final String FIELD_CONTACT = "contactPerson";
	private static final String FIELD_PRINCIPAL = "principal";
	private static final String FIELD_PRIMARY_INFRACTION = "primaryInfractionDesc";

	private static final String PARAM_AGE_18 = "age18";
	private static final String PARAM_DATE_OF_LETTER = "dateOfLetterParam";
	private static final String PARAM_VICE_PRINCIPAL = "vicePrincipal";
	private static final String PARAM_SUPERINTENDENT = "superintendent";
	private static final String PARAM_SUPERINTENDENT_TITLE = "superintendentTitle";
	private static final String PARAM_DIRECTOR = "director";
	private static final String PARAM_DIRECTOR_CODE = "directorCode";
	private static final String PARAM_BOARD_EXPELLED = "allSchoolsExpelled";
	private static final String LOGO = "logo";

	private static final String EXT_OID_INCIDENT_CODE = "ddxOnIncidntCd";
	private static final String ON_BOARD_CODE_LOGO = "OnBoardLogo";
	public static final String DDX_ON_INCIDNT_CD = "ddxOnIncidntCd";
	public static final String RCD_INCIDENT_DESCRIPTION = "rcdIncidentDescription";
	public static final String DATE_PATERN = "dd/MM/yyyy";
	public static final String REPORT_DATE_PATERN_FR = "jj/MM/aaaa";

	// members
	private DataDictionary m_incidentsRefCodesDDX;
	private DataDictionary m_dictionary;
	private Map<String, ReferenceCode> m_incidentCodeMap;
	private X2Criteria m_conductActionCriteria;
	private boolean m_includeCopy;
	private boolean m_boardExpelled;
	private Collection m_studentContacts;
	private boolean m_age18;
	private PlainDate m_dateOfLetter;
	private PlainDate m_birthdateFor18;
	private SchoolCalendar m_calendar;
	private SisSchool m_school;
	private ConductAction m_currentConduct;
	private String m_contactOids;
	private String m_vPrincipalStfOids;
	private String m_superintendentStfOid;
	private String m_vicePrincipalsName = "";
	private Map<String, String> superintendenceTitles;
	private String m_superintendentName = "";
	private String m_superintendentTitle = "";
	private String m_directorName = "";
	private String m_directorCode = "";

	/**
	 * @see com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet#gatherData()
	 */
	@Override
	protected JRDataSource gatherData() {
		ReportDataGrid dataGrid = new ReportDataGrid(500, 2);

		if (!StringUtils.isEmpty(m_contactOids)) {
			loadMailingContacts();
		}

		if (!StringUtils.isEmpty(m_vPrincipalStfOids)) {
			getVicePrincipals();
		}

		Map<String, String> superNames = getSuperintendentsMap(getBroker());
		getSuperintendent();
		getDirectorName();

		if (m_age18) {
			appendToGrid(dataGrid, m_currentConduct, null);
			if (m_includeCopy) {
				appendToGrid(dataGrid, m_currentConduct, null);
			}
		}

		if (m_studentContacts != null) {
			Iterator contactIterator = m_studentContacts.iterator();
			while (contactIterator.hasNext()) {
				StudentContact contact = (StudentContact) contactIterator.next();
				appendToGrid(dataGrid, m_currentConduct, contact);
				if (m_includeCopy) {
					appendToGrid(dataGrid, m_currentConduct, contact);
				}
			}
		}

		addParameter(PARAM_AGE_18, m_age18);
		addParameter(PARAM_DATE_OF_LETTER, m_dateOfLetter);
		if (!StringUtils.isEmpty(m_vicePrincipalsName)) {
			addParameter(PARAM_VICE_PRINCIPAL, m_vicePrincipalsName);
		}

		addParameter(PARAM_SUPERINTENDENT, superNames.get((String) getSchool().getFieldValueByAlias("all-skl-School-Family")));
		if ((String) getSchool().getFieldValueByAlias("all-skl-School-Family")!=null
				&&superintendenceTitles.get((String) getSchool().getFieldValueByAlias("all-skl-School-Family"))!=null){
			addParameter(PARAM_SUPERINTENDENT_TITLE, superintendenceTitles.get((String) getSchool().getFieldValueByAlias("all-skl-School-Family")));
		}

		if (!StringUtils.isEmpty(m_directorName)) {
			addParameter(PARAM_DIRECTOR, m_directorName);
		}

		addParameter(PARAM_DIRECTOR_CODE, m_directorCode);
		addParameter(LOGO, OntarioToolHelper.getBase64ImageString(ON_BOARD_CODE_LOGO, getBroker()));
		addParameter(PARAM_BOARD_EXPELLED, m_boardExpelled);
		dataGrid.beforeTop();

		return dataGrid;
	}

	private void getDirectorName() {
		if (getOrganization().getFieldValueByAlias("all-org-Director")!=null) {
			m_directorName = (String) getOrganization().getFieldValueByAlias("all-org-Director");
			
			m_directorCode = (String) getOrganization().getFieldValueByAlias("all-org-DirTitle");
		}
	}

	private  Map<String, String> getSuperintendentsMap(X2Broker broker) {
		Map<String, String> superintendence = new HashMap<>();
		superintendenceTitles = new HashMap<>();
		ReferenceTable familyOfschoolRefTable = broker.getBeanByOid(ReferenceTable.class, OntarioAlias.REF_OID_FAMILY_SCHOL);

		ExtendedDataDictionary ddxExtended = familyOfschoolRefTable.getExtendedDataDictionary();
		DataDictionary ddx = DataDictionary.getDistrictDictionary(ddxExtended, broker.getPersistenceKey());

		DataDictionaryField superIntendentNameField = ddx.findDataDictionaryFieldByAlias(OntarioAlias.ALIAS_EXT_RCD_SKL_SUPERINTENDENT_NAME);
		DataDictionaryField superIntendentTitleField = ddx.findDataDictionaryFieldByAlias("rcd-skl-supt-title");

		Criteria criteria = new Criteria();
		criteria.addEqualTo(SisBeanPaths.REF_CODE.referenceTableOid().getPath(), OntarioAlias.REF_OID_FAMILY_SCHOL);

		String[] columns = new String[] {
				SisBeanPaths.REF_CODE.code().getPath(),
				superIntendentNameField.getJavaName(),
				superIntendentTitleField.getJavaName()
		};

		ReportQueryByCriteria query = new ReportQueryByCriteria(ReferenceCode.class, columns, criteria);
		try (ReportQueryIterator iterator = broker.getReportQueryIteratorByQuery(query)) {
			while (iterator.hasNext()) {
				Object[] row = (Object[]) iterator.next();

				String rcdCode = (String) row[0];
				String superIntendentName = (String) row[1];
				String superIntendentTitle = (String) row[2];

				superintendence.put(rcdCode, superIntendentName);
				superintendenceTitles.put(rcdCode, superIntendentTitle);
			}
		}

		return superintendence;
	}

	private void loadRefTableIncidentsCodesDDX(X2Broker broker) {
		ExtendedDataDictionary incidentsRefCodesDDX = broker.getBeanByOid(ExtendedDataDictionary.class, DDX_ON_INCIDNT_CD);
		m_incidentsRefCodesDDX = DataDictionary.getDistrictDictionary(incidentsRefCodesDDX, broker.getPersistenceKey());
	}

	/**
	 * Append to grid.
	 *
	 * @param dataGrid ReportDataGrid
	 * @param action ConductAction
	 * @param contact StudentContact
	 */
	private void appendToGrid(ReportDataGrid dataGrid, ConductAction action, StudentContact contact) {
		Student student = action.getStudent();
		ConductIncident incident = action.getIncident();
		Person person = student.getPerson();
		School school = student.getSchool();
		Person principal = null;

if (school != null && school.getAdministrator1() != null) {
    principal = school.getAdministrator1().getPerson();
}
		Address stdAddress = person.getMailingAddress();

		dataGrid.append();
		dataGrid.set(FIELD_ACTION, action);
		dataGrid.set(FIELD_STUDENT, student);
		dataGrid.set(FIELD_INCIDENT, incident);
		dataGrid.set(FIELD_PERSON, person);
		dataGrid.set(FIELD_AGE, person.getAge());
		if(action.getActionEndDate()!=null){
			dataGrid.set(FIELD_RETURN_DATE, getReturnDate(action.getActionEndDate()));
		}
		dataGrid.set(FIELD_PRINCIPAL, principal);
		dataGrid.set(FIELD_PRIMARY_INFRACTION, m_incidentCodeMap.containsKey(incident.getIncidentCode()) ?
				m_incidentCodeMap.get(incident.getIncidentCode()).getFieldValueByAlias(RCD_INCIDENT_DESCRIPTION, m_incidentsRefCodesDDX) : incident.getDescription());
		if (!m_age18) {
			Person contactPerson = contact.getPerson();
			Address address = contactPerson.getResolvedMailingAddress();
			dataGrid.set(FIELD_CONTACT, contactPerson);
			dataGrid.set(FIELD_ADDRESS, address);
		} else {
			dataGrid.set(FIELD_CONTACT, person);
			dataGrid.set(FIELD_ADDRESS, stdAddress);
		}
	}

	/**
	 * Load mailing contacts.
	 */
	private void loadMailingContacts() {
		List<String> contactOids = new ArrayList<String>();

		contactOids = StringUtils.convertDelimitedStringToList(m_contactOids, ",");
		Criteria criteria = new Criteria();
		criteria.addIn(StudentContact.COL_OID, contactOids);

		QueryByCriteria query = new QueryByCriteria(StudentContact.class, criteria);

		m_studentContacts = getBroker().getCollectionByQuery(query);
	}

	/**
	 * @see com.follett.fsc.core.k12.tools.ToolJavaSource#initialize()
	 */
	@Override
	protected void initialize() throws X2BaseException {
		super.initialize();

		m_includeCopy = ((Boolean) getParameter(INPUT_INCLUDE_COPY)).booleanValue();
		m_boardExpelled = ((Boolean) getParameter(INPUT_BOARD_EXPELLED)).booleanValue();
		m_dateOfLetter = (PlainDate) getParameter(INPUT_DATE_OF_LETTER);
		m_age18 = ((Boolean) getParameter(INPUT_AGE_18_ONLY)).booleanValue();
		m_school = (SisSchool) getSchool();
		m_contactOids = (String) getParameter(INPUT_CONTACTS_OIDS);
		m_vPrincipalStfOids = (String) getParameter(INPUT_V_PRINCIPAL_OIDS);
		loadRefTableIncidentsCodesDDX(getBroker());

		m_dictionary = DataDictionary.getDistrictDictionary(getUser().getPersistenceKey());
		DataDictionaryField ddf = m_dictionary.findDataDictionaryField(ConductIncident.class.getName(),
				ConductIncident.COL_INCIDENT_CODE);

		m_incidentCodeMap = new HashMap<String, ReferenceCode>();
		m_incidentCodeMap = ddf.getReferenceTable().getCodeMap();

		initializeLocalized();
		getCurrenCalendar();
	}

	/**
	 * @see com.x2dev.sis.tools.reports.ReportJavaSource#initialize(com.x2dev.sis.web.UserDataContainer)
	 */
	@Override
	protected void saveState(UserDataContainer userData) {
		m_currentConduct = userData.getCurrentRecord(ConductAction.class);
		m_userLocale = userData.getLocale();
	}

	/**
	 * Initialize localized.
	 */
	private void initializeLocalized() {
		MessageResources resourcesOut = OntarioToolHelper.initializeLocalized(getOrganization(),
				(SisSchool) getSchool(), getBroker());

		addParameter(REPORT_PREFIX, CONST_TOOLS_FOR_PREFIX + getJob().getTool().getOid() + ".");
		addParameter(REPORT_SCHOOL_LOCALE, resourcesOut);
		String schoolLanguage = (String) getSchool().getFieldValueByAlias(SCHOOL_LANGUAGE_ALIAS);
		if (schoolLanguage.equals(Locale.FRENCH.getDisplayLanguage())) {
			SimpleDateFormat shortDate = (SimpleDateFormat) getParameter("shortDateFormat");
			if (m_userLocale.getDisplayLanguage().equals(Locale.FRENCH.getDisplayLanguage())) {
				shortDate.applyLocalizedPattern(REPORT_DATE_PATERN_FR);
			} else shortDate.applyLocalizedPattern(DATE_PATERN);
		}
		addParameter(LOCALIZED_LOCATIONS_MAP, OntarioToolHelper.getLocalizedRefDescMap(getBroker(), RTB_CND_LOCATION,
				schoolLanguage));
		// Comment line below if your numeric notation, currencies and others don't display as expected
		addParameter(net.sf.jasperreports5.engine.JRParameter.REPORT_LOCALE, m_userLocale); // Only tested for JasperReports engine 5
	}

	/**
	 *
	 */
	private void getVicePrincipals() {
		List<String> vPrincipalStfOids = new ArrayList<String>();
		vPrincipalStfOids = StringUtils.convertDelimitedStringToList(m_vPrincipalStfOids, ",");

		Criteria vpCriteria = new Criteria();
		vpCriteria.addIn(Staff.COL_OID, vPrincipalStfOids);
		QueryByCriteria vpQuery = new QueryByCriteria(Staff.class, vpCriteria);
		Collection vicePrincipals = getBroker().getCollectionByQuery(vpQuery);

		Iterator i = vicePrincipals.iterator();
		while (i.hasNext()) {
			Staff vPrincipal = (Staff) i.next();
			Person vPrincipalPerson = vPrincipal.getPerson();
			m_vicePrincipalsName = m_vicePrincipalsName.concat(vPrincipalPerson.getFirstName());
			m_vicePrincipalsName = m_vicePrincipalsName.concat(CONST_SPACE);
			m_vicePrincipalsName = m_vicePrincipalsName.concat(vPrincipalPerson.getLastName());
			if (i.hasNext()) {
				m_vicePrincipalsName = m_vicePrincipalsName + CONST_COMA + CONST_SPACE;
			}
		}
	}

	private void getSuperintendent() {
		X2Criteria superCriteria = new X2Criteria();
		superCriteria.addEqualTo(Staff.COL_STAFF_TYPE, "Superintendent");
		superCriteria.addEqualTo(Staff.COL_SCHOOL_OID, getSchool().getOid());
		QueryByCriteria superQuery = new QueryByCriteria(Staff.class, superCriteria);
		Collection superintendent = getBroker().getCollectionByQuery(superQuery);

		Iterator i2 = superintendent.iterator();
		while (i2.hasNext()) {
			Staff stf = (Staff) i2.next();
			Person person = stf.getPerson();
			m_superintendentName = m_superintendentName.concat(person.getFirstName());
			m_superintendentName = m_superintendentName.concat(CONST_SPACE);
			m_superintendentName = m_superintendentName.concat(person.getLastName());
			m_superintendentName = m_superintendentName.concat(CONST_COMA + CONST_SPACE);
		}
	}

	private void getCurrenCalendar() {
		Iterator calendars = ((SisSchool) getSchool()).getSchoolCalendars().iterator();
		while (calendars.hasNext()) {
			SchoolCalendar calendar = (SchoolCalendar) calendars.next();

			if (calendar.getDistrictContext().getContextId().equals(getCurrentContext().getContextId())) {
				m_calendar = calendar;
			}
		}
	}

	private PlainDate getReturnDate(PlainDate endDate) {
		PlainDate returnDate = null;

		if (m_calendar != null) {
			Iterator dates = m_calendar.getSchoolCalendarDates().iterator();
			while (dates.hasNext()) {
				SisSchoolCalendarDate calDate = (SisSchoolCalendarDate) dates.next();
				PlainDate date = calDate.getDate();

				if (calDate.getInSessionIndicator() && date.after(endDate)
						&& (returnDate == null || date.before(returnDate))) {
					returnDate = date;
				}
			}
		}

		return returnDate;
	}
}