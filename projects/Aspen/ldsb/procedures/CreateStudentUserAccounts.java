package ldsb.procedures;

import static com.follett.fsc.core.k12.beans.SystemPreferenceDefinition.STUDENT_ACTIVE_CODE;
import static com.follett.fsc.core.k12.business.CreateUserAccountsManager.PASSWORD_FORMAT_CONSTANT;
import static com.follett.fsc.core.k12.business.CreateUserAccountsManager.PASSWORD_FORMAT_NUMERIC;
import static com.follett.fsc.core.k12.business.ModelProperty.PATH_DELIMITER;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ojb.broker.query.QueryByCriteria;

import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.QueryIterator;
import com.follett.fsc.core.k12.business.CreateUserAccountsManager;
import com.follett.fsc.core.k12.business.ModelBroker;
import com.follett.fsc.core.k12.business.PreferenceManager;
import com.follett.fsc.core.k12.business.dictionary.DataDictionary;
import com.follett.fsc.core.k12.business.dictionary.DataDictionaryField;
import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;
import com.follett.fsc.core.k12.web.UserDataContainer;
import com.x2dev.sis.model.beans.SisPerson;
import com.x2dev.sis.model.beans.SisSchool;
import com.x2dev.sis.model.beans.SisStudent;
import com.x2dev.sis.model.beans.SisUser;
import com.x2dev.utils.converters.Converter;
import com.x2dev.utils.converters.ConverterFactory;
import com.x2dev.utils.converters.DateConverter;

/**
 * 
 * Procedure for Fall River to create parent user accounts.
 * 
 * 
 * 
 * @author X2 Development Corporation
 * 
 */

public class CreateStudentUserAccounts extends ProcedureJavaSource {

	private static final long serialVersionUID = 1L;

	// Input parameters

	public static final String LOGIN_ID_CUSTOM_EXPRESSION_PARAM = "loginIdCustomExpression";
	public static final String LOGIN_ID_FORMAT_PARAM = "loginIdFormat";
	public static final String PASSWORD_CONSTANT_VALUE_PARAM = "passwordConstantValue";
	public static final String PASSWORD_FORMAT_PARAM = "passwordFormat";
	public static final String PASSWORD_NUMERIC_LENGTH_PARAM = "passwordNumericLength";
	public static final String PREVIEW_ONLY_PARAM = "previewOnly";
	public static final String ROLE_OIDS_PARAM = "roleOids";
	public static final String SCHOOL_OIDS_PARAM = "schoolOids";
	private ModelBroker m_broker;
	private UserDataContainer m_userData;

	SaveNotification notifier = new SaveNotification();

	/**
	 * Overview: The SaveNotification class should be added as a nested class to any Aspen procedure that will use
	 * notifications. The required import libs to be added to procedures are: import java.io.File; import
	 * java.io.FileWriter; import java.util.Date; import java.text.SimpleDateFormat;
	 *
	 * usage: save_notif_inst.save_notification("INFO", "traised@limestone.on.ca", "this is a notification from
	 * acadience assessment");
	 *
	 * Datetime is added. Aspen_notifications.csv file is updated.
	 *
	 * MessageType can be exception, warning, error, information.
	 */
	public class SaveNotification {

		public SaveNotification() {
		}

		public void save(String messageType, String recipients, String message) throws Exception {

			Date myDate = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
			String myDateString = simpleDateFormat.format(myDate);

			String filename = "/data/app/aspen-data/nfs/tool-staging/ldsbsis/export/aspen_notifications_buffer.csv";

			// logMessage("File Name: " + filename);
			// getJob().getTempFolder() + "/aspen_notifications.csv";

			File notifFile = new File(filename);

			try {

				if (!notifFile.exists()) {
					notifFile.createNewFile();
				}

				FileWriter writer = new FileWriter(notifFile, true);
				writer.write(messageType + "," + recipients + "," + message + "," + myDateString + "\n");
				writer.close();

			} catch (Exception e) {
				// logMessage(e.toString());
				throw e;
			} finally {
			}
		}
	}

	/**
	 * 
	 * Properties to set on the user account.
	 * 
	 */

	private static final String[] USER_PROPERTIES = new String[] {

			SisUser.COL_ACCOUNT_EXPIRATION_DATE,

			SisUser.COL_ALLOW_FROM,

			SisUser.COL_LOGIN_STATUS,

			SisUser.COL_PASSWORD_EXPIRATION_DATE,

			SisUser.COL_TIMEOUT

	};

	/**
	 * 
	 * @see com.x2dev.sis.tools.procedures.ProcedureJavaSource#execute()
	 * 
	 */

	@Override

	protected void execute() throws Exception {

		Map<DataDictionaryField, String> userValues = new HashMap<DataDictionaryField, String>(
				USER_PROPERTIES.length * 2);

		DataDictionary dictionary = DataDictionary.getDistrictDictionary(m_broker.getPersistenceKey());

		for (int i = 0; i < USER_PROPERTIES.length; i++) {

			DataDictionaryField field = dictionary.findDataDictionaryField(SisUser.class.getName(), USER_PROPERTIES[i]);

			Object value = getParameter(field.getJavaName());

			String valueAsString = "";

			if (value != null) {
				Converter converter = ConverterFactory.getConverterForClass(field.getJavaType(), getLocale(),
						field.isString());

				if (converter instanceof DateConverter) {
					valueAsString = converter.javaToString(value);
				}

				else {
					valueAsString = value.toString();
				}
			}

			userValues.put(field, valueAsString);
		}

		String loginIdFormat = (String) getParameter(LOGIN_ID_FORMAT_PARAM);

		String loginIdExpression = (String) getParameter(LOGIN_ID_CUSTOM_EXPRESSION_PARAM);

		String passwordFormat = (String) getParameter(PASSWORD_FORMAT_PARAM);

		String passwordValue = null;

		if (PASSWORD_FORMAT_NUMERIC.equals(passwordFormat)) {
			passwordValue = (String) getParameter(PASSWORD_NUMERIC_LENGTH_PARAM);
		}

		else if (PASSWORD_FORMAT_CONSTANT.equals(passwordFormat)) {
			passwordValue = (String) getParameter(PASSWORD_CONSTANT_VALUE_PARAM);
		}

		String roleOids = (String) getParameter(ROLE_OIDS_PARAM);

		String schoolOids = (String) getParameter(SCHOOL_OIDS_PARAM);

		QueryByCriteria query = getQuery();

		QueryIterator iterator = getBroker().getIteratorByQuery(query);

		try {
			CreateUserAccountsManager createAccountsManager = new CreateUserAccountsManager(getUser(), m_broker,
					getLocale(), getJob().getTempFolder(), "", "");
			String filePath = createAccountsManager.createAccounts("3", "1", "Aspen2022", "{person.email01}",
					"surStudent", "", userValues, query);

		} catch (Exception e) {
			logMessage("Encountered error: " + e);

			// DDT save notification call
			try {

				String msgStr = "An error occurred while creating a new student user account for: "
						+ getUser().getNameView() + " email: " + getUser().getLoginName() + " Error: "
						+ e.getLocalizedMessage();

				notifier.save("ERR", "traised@limestone.on.ca", msgStr);
			} catch (Exception ee) {
				ee.printStackTrace();
			}

		}
	}

	/**
	 * 
	 * @see com.x2dev.sis.tools.ToolJavaSource#initialize()
	 * 
	 */

	@Override

	protected void initialize() {
		m_broker = new ModelBroker(getPrivilegeSet());
	}

	/**
	 * 
	 * @see com.x2dev.sis.tools.ToolJavaSource#saveState(com.x2dev.sis.web.UserDataContainer)
	 * 
	 */

	@Override

	protected void saveState(UserDataContainer userData) {
		m_userData = userData;
	}

	/**
	 * 
	 * Returns the student query.
	 * 
	 * 
	 * 
	 * @return QueryByCriteria
	 * 
	 */

	private QueryByCriteria getQuery()

	{
		X2Criteria criteria = new X2Criteria();

		criteria.addNotEmpty(SisStudent.REL_PERSON + PATH_DELIMITER + SisPerson.COL_EMAIL01,
				getBroker().getPersistenceKey());

		criteria.addNotEqualTo(SisStudent.REL_PERSON + PATH_DELIMITER + SisPerson.COL_USER_INDICATOR, Boolean.TRUE);

		criteria.addEqualTo(SisStudent.COL_ENROLLMENT_STATUS,
				PreferenceManager.getPreferenceValue(getOrganization(), STUDENT_ACTIVE_CODE));

		// criteria.addEqualTo(SisStudent.REL_PERSON + PATH_DELIMITER + SisPerson.COL_EMAIL01,
		// "kd0ju09@limestodisabled");

		// logMessage("Student data: " + " Email: " + SisStudent.REL_PERSON + PATH_DELIMITER + SisPerson.COL_EMAIL01);

		logMessage("Check 1");
		if (isSchoolContext())

		{

			criteria.addEqualTo(SisStudent.COL_SCHOOL_OID, ((SisSchool) getSchool()).getOid());

		}

		return new QueryByCriteria(SisStudent.class, criteria);
	}
}