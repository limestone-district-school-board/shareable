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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

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

import net.sf.jasperreports5.engine.JRDataSource;
import net.sf.jasperreports5.engine.JRException;
import net.sf.jasperreports5.engine.JRField;
import net.sf.jasperreports5.engine.JRResultSetDataSource;
import net.sf.jasperreports5.engine.JRRewindableDataSource;

/**
 * Prepares the data for a report using a SQL query. This Java class has additional methods for releasing resources,
 * making it suitable as a base for building other reports.
 */
public class StudentListSqlData extends ReportJavaSourceNet {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// Built-in parameter provided by the 'allow-school-select' attribute on the tool-input tag
	private static final String PARAM_SCHOOL_OID = "schoolOid";

	// Inputs from the Input Definition
	private static final String PARAM_ACTIVE_ONLY = "activeOnly";
	private static final String PARAM_SORT = "sort";

	private ModelBroker m_broker;
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
		String sort = (String) getParameter(PARAM_SORT);

		// Get preference value for the active enrollment status
		String activeCode = PreferenceManager.getPreferenceValue(getOrganization(),
				SystemPreferenceDefinition.STUDENT_ACTIVE_CODE);

		// List of parameters for the SQL statement
		List<Object> params = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT  STD_ID_LOCAL, ");
		sql.append("        STD_NAME_VIEW, ");
		sql.append("        STD_HOMEROOM, ");
		sql.append("        STD_GRADE_LEVEL, ");
		sql.append("        PSN_DOB, ");
		sql.append("        PSN_PHONE_01, ");
		sql.append("        PSN_EMAIL_01, ");
		sql.append("        SKL_SCHOOL_ID, ");
		sql.append("        SKL_SCHOOL_NAME ");
		sql.append("FROM    STUDENT WITH (NOLOCK) ");
		sql.append("JOIN    PERSON WITH (NOLOCK) ON PSN_OID = STD_PSN_OID ");
		sql.append("JOIN    SCHOOL WITH (NOLOCK) ON SKL_OID = STD_SKL_OID ");

		// Apply WHERE clauses depending on inputs from user
		if (!StringUtils.isBlank(schoolOid)) {
			sql.append("WHERE   STD_SKL_OID = ? ");
			params.add(schoolOid);
		} else {
			sql.append("WHERE   STD_SKL_OID IS NOT NULL ");
		}
		if (activeOnly) {
			sql.append("AND     STD_ENROLLMENT_STATUS = ? ");
			params.add(activeCode);
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
		List<String> sanitizedList = new ArrayList<String>();

		for (String column : Arrays.asList(fieldList.split("\\s*,\\s*"))) {
			if (m_dictionary.findDataDictionaryFieldByDatabaseName(column) != null) {
				sanitizedList.add(column);
			}
		}

		return String.join(",", sanitizedList);
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