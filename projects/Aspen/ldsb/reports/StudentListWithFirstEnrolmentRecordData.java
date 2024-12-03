package ldsb.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.follett.fsc.core.k12.beans.ReferenceCode;
import com.follett.fsc.core.k12.beans.SystemPreferenceDefinition;
import com.follett.fsc.core.k12.business.BeanPathException;
import com.follett.fsc.core.k12.business.ModelBroker;
import com.follett.fsc.core.k12.business.PreferenceManager;
import com.follett.fsc.core.k12.business.X2Broker;
import com.follett.fsc.core.k12.business.dictionary.DataDictionary;
import com.follett.fsc.core.k12.tools.reports.Breakable;
import com.follett.fsc.core.k12.tools.reports.ClosableDataSource;
import com.follett.fsc.core.k12.tools.reports.ReportDataGrid;
import com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet;
import com.x2dev.utils.StringUtils;
import com.x2dev.utils.X2BaseException;
import com.x2dev.utils.X2RuntimeException;
import com.x2dev.utils.types.PlainDate;

import net.sf.jasperreports5.engine.JRDataSource;
import net.sf.jasperreports5.engine.JRException;
import net.sf.jasperreports5.engine.JRField;
import net.sf.jasperreports5.engine.JRResultSetDataSource;
import net.sf.jasperreports5.engine.JRRewindableDataSource;

/**
 * Report for the first enrolment record of each student in a date range
 */
public class StudentListWithFirstEnrolmentRecordData extends ReportJavaSourceNet {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// Built-in parameter provided by the 'allow-school-select' attribute on the tool-input tag
	private static final String PARAM_SCHOOL_OID = "schoolOid";

	// Inputs from the Input Definition
	private static final String PARAM_ACTIVE_ONLY = "activeOnly";
	private static final String PARAM_START_DATE = "startDate";
	private static final String PARAM_END_DATE = "endDate";
	private static final String PARAM_GRADE_LEVEL_CODES = "gradeLevelCodes";
	private static final String PARAM_SORT = "sort";

	private static final String RTB_USER_NAME_BOARD_RESIDENT_STATUS = "OnSIS Student Board Resident Status";

	private ModelBroker m_broker;
	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private DataDictionary m_dictionary;

	/**
	 * Gather data.
	 *
	 * @return JRDataSource
	 * @see com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet#gatherData()
	 */
	@Override
	protected JRDataSource gatherData() {
		// Get inputs from user
		boolean activeOnly = ((Boolean) getParameter(PARAM_ACTIVE_ONLY)).booleanValue();
		String schoolOid = (String) getParameter(PARAM_SCHOOL_OID);
		String startDate = m_dateFormat.format((PlainDate) getParameter(PARAM_START_DATE));
		String endDate = m_dateFormat.format((PlainDate) getParameter(PARAM_END_DATE));
		String gradeLevelCodes = (String) getParameter(PARAM_GRADE_LEVEL_CODES);
		String sort = (String) getParameter(PARAM_SORT);

		// Get preference value for the active enrollment status
		String activeCode = PreferenceManager.getPreferenceValue(getOrganization(),
				SystemPreferenceDefinition.STUDENT_ACTIVE_CODE);

		// List of parameters for the SQL statement
		List<Object> params = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT STD_NAME_VIEW, ");
		sql.append("       STD_ID_LOCAL, ");
		sql.append("       STD_GRADE_LEVEL, ");
		sql.append("       SKL_SCHOOL_NAME, ");
		sql.append("       ENR_ENROLLMENT_TYPE, ");
		sql.append("       ENR_ENROLLMENT_DATE, ");
		sql.append("       ENR_ENROLLMENT_CODE, ");
		sql.append("       ENROLMENT_REGISTER, ");
		sql.append("       FTE, ");
		sql.append("       RCD_DESCRIPTION AS BOARD_RESIDENT_STATUS ");
		sql.append("FROM ");
		sql.append("( ");
		sql.append("  SELECT  ENR_STD_OID, ");
		sql.append("          ENR_SKL_OID, ");
		sql.append("          ENR_ENROLLMENT_TYPE, ");
		sql.append("          ENR_ENROLLMENT_DATE, ");
		sql.append("          ENR_ENROLLMENT_CODE, ");
		sql.append("          ENR_FIELDA_002 AS ENROLMENT_REGISTER, ");
		sql.append("          ENR_FIELDA_006 AS FTE, ");
		sql.append("          ENR_FIELDA_005 AS BOARD_RESIDENT_STATUS, ");
		sql.append(
				"          row_number() over (partition by ENR_STD_OID order by ENR_ENROLLMENT_DATE asc, ENR_TIMESTAMP asc) rn ");
		sql.append("  FROM    STUDENT_ENROLLMENT ");
		sql.append("  WHERE   ENR_ENROLLMENT_TYPE IN ('E', 'S') ");
		sql.append("          AND ENR_ENROLLMENT_DATE >= ? ");
		params.add(startDate);
		sql.append("          AND ENR_ENROLLMENT_DATE <= ? ");
		params.add(endDate);
		sql.append(") ENR ");
		sql.append("JOIN   SCHOOL ON SKL_OID = ENR_SKL_OID ");
		sql.append("JOIN   STUDENT ON STD_OID = ENR_STD_OID ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("  SELECT  RCD_CODE, ");
		sql.append("          RCD_DESCRIPTION ");
		sql.append("  FROM    REF_CODE ");
		sql.append("  JOIN    REF_TABLE ON RTB_OID = RCD_RTB_OID ");
		sql.append("  WHERE   RTB_USER_NAME = ? ");
		params.add(RTB_USER_NAME_BOARD_RESIDENT_STATUS);
		sql.append(") RCD ON RCD_CODE = BOARD_RESIDENT_STATUS ");
		sql.append("WHERE  rn = 1 ");

		// Apply WHERE clauses depending on inputs from user
		if (!StringUtils.isBlank(schoolOid)) {
			sql.append("AND     STD_SKL_OID = ? ");
			params.add(schoolOid);
		}

		if (activeOnly) {
			sql.append("AND     STD_ENROLLMENT_STATUS = ? ");
			params.add(activeCode);
		}

		if (!StringUtils.isBlank(gradeLevelCodes)) {
			sql.append("AND     STD_GRADE_LEVEL IN ( " + addCodeParameters(gradeLevelCodes, params) + " ) ");
		}

		// Apply ORDER BY clause depending on input from user
		if (!StringUtils.isBlank(sort)) {
			// School name must be first since the report format groups by school name
			sql.append("ORDER BY SKL_SCHOOL_NAME, " + validatedFieldList(sort));
		} else {
			// Default the sort order by just school name and student name
			sql.append("ORDER BY SKL_SCHOOL_NAME, STD_NAME_VIEW ");
		}

		return new SqlJasperEngine5DataSource(sql.toString(), params.toArray(), m_broker);
	}

	/**
	 * @see com.follett.fsc.core.k12.tools.ToolJavaSource#initialize()
	 */
	@Override
	protected void initialize() throws X2BaseException {
		super.initialize();
		m_broker = new ModelBroker(getPrivilegeSet());
		m_dictionary = DataDictionary.getDistrictDictionary(getBroker().getPersistenceKey());
	}

	/**
	 * Release resources.
	 *
	 * @see com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet#releaseResources()
	 */
	@Override
	protected void releaseResources() {
		super.releaseResources();
		boolean logMemoryUsedStatisticsInToolLog = false;
		clearLocallyDeclaredVariables(this.getClass(), logMemoryUsedStatisticsInToolLog);
	}

	/**
	 * Returns a string of parameters and populates the parameter list with the code values. This method can be used
	 * with any reference code picklist to return a string of parameter characters (?, ?, ?, etc.), and populate the
	 * list of code values (e.g., "01", "02", "03" for a picklist of grade levels)
	 * 
	 * @param refCodesList  - Comma separated list of Reference Code OIDs (selected from a picklist)
	 * @param parameterList - List of parameters for the SQL query
	 * @return
	 */
	private String addCodeParameters(String refCodesList, List<Object> parameterList) {
		String parameterString = "";
		// Loop through all the reference code OIDs
		for (String rcdOid : Arrays.asList(refCodesList.split("\\s*,\\s*"))) {
			// Retrieve the reference code bean for the OID selected in the picklist
			ReferenceCode referenceCode = m_broker.getBeanByOid(ReferenceCode.class, rcdOid);
			if (referenceCode != null) {
				// Add ? to the parameter string, and add the reference code value to the parameter list
				parameterString += ", ?";
				parameterList.add(referenceCode.getCode());
			}
		}
		// Remove leading comma
		parameterString = parameterString.replaceFirst(",", "");

		return parameterString;
	}

	/**
	 * Method to sift through all your declared member variables and sets them to null for proper garbage collection.
	 *
	 * @param classToClear                     Class
	 * @param logMemoryUsedStatisticsInToolLog boolean
	 */
	private void clearLocallyDeclaredVariables(Class classToClear, boolean logMemoryUsedStatisticsInToolLog) {
		if (logMemoryUsedStatisticsInToolLog) {
			logToolMessage(Level.INFO, "Memory used by tool: " + getTotalMemoryUsedByClass(classToClear, true), false);
		}
		for (Field field : classToClear.getDeclaredFields()) {
			if (field.getModifiers() == Modifier.PRIVATE) {
				field.setAccessible(true);
			}
			try {
				field.set(this, null);
			} catch (Exception e) {
				// Don't log exceptions
			}
		}
		if (logMemoryUsedStatisticsInToolLog) {
			logToolMessage(Level.INFO, "Memory used after clearing: " + getTotalMemoryUsedByClass(classToClear, false),
					false);
		}
	}

	/**
	 * Returns approximate footprint of on an object in memory.
	 *
	 * @param map     Map
	 * @param boolean logMemoryUsedStatisticsInToolLog
	 * @return
	 */
	private long getObjectSize(Object obj, boolean logMemoryUsedStatisticsInToolLog) {
		long memmoryUsed = 0;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
			if (logMemoryUsedStatisticsInToolLog) {
				logToolMessage(Level.INFO,
						obj + " Object Data Size: " + com.x2dev.utils.MemoryUtils.formatMemoryUsed(baos.size()), false);
			}
			memmoryUsed += baos.size();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return memmoryUsed;
	}

	/**
	 * Method that relies on reflection to gather the size of all locally declared variables.
	 * 
	 * @param classToMeasure
	 * @param logEachFieldSize
	 * @return Easy to read memory size as a string
	 */
	private String getTotalMemoryUsedByClass(Class classToMeasure, boolean logEachFieldSize) {
		long totalMemoryUsed = 0;
		for (Field field : classToMeasure.getDeclaredFields()) {
			if (field.getModifiers() == java.lang.reflect.Modifier.PRIVATE) {
				field.setAccessible(true);
			}
			try {
				long thisObjectSize = getObjectSize(field.get(this), false);
				if (logEachFieldSize) {
					logToolMessage(Level.INFO, field.getName() + " used " + thisObjectSize, false);
				}
				totalMemoryUsed = totalMemoryUsed + thisObjectSize;
			} catch (IllegalArgumentException e) {
				// null
			} catch (IllegalAccessException e) {
				// null
			}
		}
		return com.x2dev.utils.MemoryUtils.formatMemoryUsed(totalMemoryUsed);
	}

	/**
	 * Returns a comma separated list of fields that have been validated to prevent SQL injection
	 * 
	 * @param sort
	 * @return
	 */
	private String validatedFieldList(String fieldList) {
		List<String> validatedList = new ArrayList<String>();

		for (String column : Arrays.asList(fieldList.split("\\s*,\\s*"))) {
			if (m_dictionary.findDataDictionaryFieldByDatabaseName(column) != null) {
				validatedList.add(column);
			}
		}

		return String.join(",", validatedList);
	}

	/**
	 * Data source for reports based on an SQL statement. This class assumes responsibility for all JDBC tasks (i.e.,
	 * creating and executing statements, managing result sets). This allows client code to simply pass an SQL statement
	 * along with an array of parameters.
	 *
	 * This class supports Jaspersoft Engine 5 and implement JRRewindableDataSource for scrollable queries
	 *
	 * @author Follett School Solutions
	 * @copyright 2021
	 */
	public class SqlJasperEngine5DataSource
			implements JRDataSource, ClosableDataSource, Breakable, JRRewindableDataSource {
		private X2Broker m_broker = null;
		private ResultSet m_resultSet = null;
		private JRResultSetDataSource m_resultSetDataSource = null;
		private PreparedStatement m_statement = null;

		/**
		 * Constructs a new SqlJasperEngine5DataSource.
		 *
		 * @param sql        String
		 * @param parameters Object[]
		 * @param broker     X2Broker
		 */
		public SqlJasperEngine5DataSource(String sql, Object[] parameters, X2Broker broker) {
			m_broker = broker;

			Connection connection = m_broker.borrowConnection();

			try {
				m_statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				for (int i = 1; i <= parameters.length; i++) {
					m_statement.setObject(i, parameters[i - 1]);
				}

				m_resultSet = m_statement.executeQuery();
				m_resultSetDataSource = new JRResultSetDataSource(m_resultSet);
			} catch (SQLException sqle) {
				throw new X2RuntimeException(sqle);
			}
		}

		/**
		 * @see com.follett.fsc.core.k12.tools.reports.Breakable#getDataSources(java.lang.String)
		 */
		@Override
		public Collection<Object> getDataSources(String breakCriteria)
				throws BeanPathException, IndexOutOfBoundsException, SQLException {
			HashMap<Object, Object> lookupMap = new HashMap<Object, Object>();

			ResultSetMetaData metaData = m_resultSet.getMetaData();

			while (m_resultSet.next()) {
				Object breakValue = m_resultSet.getObject(breakCriteria);

				ReportDataGrid grid;
				if (lookupMap.containsKey(breakValue)) {
					grid = (ReportDataGrid) lookupMap.get(breakValue);
				} else {
					grid = new ReportDataGrid(breakValue);

					lookupMap.put(breakValue, grid);
				}

				// Add the resultSet row to the current ReportDataGrid
				grid.append();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					grid.set(metaData.getColumnName(i), m_resultSet.getObject(i));
				}
			}

			// Reset the grid pointer back to the start
			for (Object grid : lookupMap.values()) {
				((ReportDataGrid) grid).beforeTop();
			}

			return lookupMap.values();
		}

		/**
		 * Gets the break value. Method should be overwritten if you need to return a new DataSource for each break
		 * value.
		 *
		 * @see com.follett.fsc.core.k12.tools.reports.Breakable#getBreakValue()
		 */
		@Override
		public Object getBreakValue() {
			return null;
		}

		/**
		 * DB connection close.
		 *
		 * @see com.follett.fsc.core.k12.tools.reports.ClosableDataSource#close()
		 */
		@Override
		public void close() {
			if (m_resultSet != null) {
				try {
					m_resultSet.close();
				} catch (SQLException sqle) {
					throw new X2RuntimeException(sqle);
				}
			}

			if (m_statement != null) {
				try {
					m_statement.close();
				} catch (SQLException sqle) {
					throw new X2RuntimeException(sqle);
				}
			}

			m_broker.returnConnection();

		}

		/**
		 * Gets the field value.
		 *
		 * @param arg0 JRField
		 * @return Object
		 * @throws JRException exception
		 * @see net.sf.jasperreports5.engine.JRDataSource#getFieldValue(net.sf.jasperreports5.engine.JRField)
		 */
		@Override
		public Object getFieldValue(JRField arg0) throws JRException {
			return m_resultSetDataSource.getFieldValue(arg0);
		}

		/**
		 * * Next.
		 *
		 * @return true, if successful
		 * @throws JRException exception
		 * @see net.sf.jasperreports5.engine.JRDataSource#next()
		 */
		@Override
		public boolean next() throws JRException {
			return m_resultSetDataSource.next();
		}

		/**
		 * @see net.sf.jasperreports5.engine.JRRewindableDataSource#moveFirst()
		 */
		@Override
		public void moveFirst() throws JRException {
			try {
				if (m_resultSet.getType() == ResultSet.TYPE_SCROLL_SENSITIVE) {
					m_resultSet.beforeFirst();
				}
			} catch (SQLException e) {
				throw new JRException("Query is not scrollable: " + e);
			}
		}
	}

}