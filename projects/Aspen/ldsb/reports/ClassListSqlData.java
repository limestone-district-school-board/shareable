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

import org.apache.struts.util.MessageResources;

import com.follett.fsc.core.framework.persistence.IterableColumnQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.beans.X2BaseBean;
import com.follett.fsc.core.k12.business.BeanPathException;
import com.follett.fsc.core.k12.business.ModelBroker;
import com.follett.fsc.core.k12.business.X2Broker;
import com.follett.fsc.core.k12.business.dictionary.DataDictionary;
import com.follett.fsc.core.k12.business.localization.LocalizationCache;
import com.follett.fsc.core.k12.tools.reports.Breakable;
import com.follett.fsc.core.k12.tools.reports.ClosableDataSource;
import com.follett.fsc.core.k12.tools.reports.ReportDataGrid;
import com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet;
import com.follett.fsc.core.k12.web.UserDataContainer;
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
 * Prepares the data for a report using a SQL query. This Java class has additional methods for releasing resources,
 * making it suitable as a base for building other reports.
 */
public class ClassListSqlData extends ReportJavaSourceNet {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// Inputs from the Input Definition
	private static final String PARAM_QUERY_BY = "queryBy";
	private static final String PARAM_ACTIVE_SCHEDULE_OID = "activeScheduleOid";
	private static final String PARAM_INCLUDE_WITHDRAWN_STUDENTS = "includeWithdrawnStudents";
	private static final String PARAM_REPORT_LOCALE = "reportLocale";
	private static final String PARAM_SORT = "sort";
	private static final String PARAM_EFFECTIVE_DATE = "effectiveDate";

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private Class m_baseClass;
	private ModelBroker m_broker;
	private DataDictionary m_dictionary;

	/**
	 * Gather data.
	 *
	 * @return JRDataSource
	 * @throws Exception
	 * @see com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet#gatherData()
	 */
	@Override
	protected JRDataSource gatherData() throws Exception {
		// Get inputs from user
		int queryBy = ((Integer) getParameter(PARAM_QUERY_BY)).intValue();
		String activeScheduleOid = (String) getParameter(PARAM_ACTIVE_SCHEDULE_OID);
		String sort = (String) getParameter(PARAM_SORT);
		PlainDate reportOnDateParameter = (PlainDate) getParameter(PARAM_EFFECTIVE_DATE);
		String reportOnDate = m_dateFormat.format(reportOnDateParameter);
		boolean includeWithdrawnStudents = ((Boolean) getParameter(PARAM_INCLUDE_WITHDRAWN_STUDENTS)).booleanValue();

		// Formatted list of mstOIDs in the current selection (will populate if user has selected Current Selection)
		String mstOids = null;
		if (1 == queryBy) {
			// This list is constructed within this class, so doesn't need to be parameterized in the SQL
			mstOids = getCurrentSelectionOids();
		}

		// List of parameters for the SQL statement
		List<Object> params = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();

		/**
		 * 1. Students currently in the section
		 */
		sql.append("SELECT  MST_COURSE_VIEW, ");
		sql.append("        MST_DESCRIPTION, ");
		sql.append("        MST_ROOM_VIEW, ");
		sql.append("        MST_SCHEDULE_DISPLAY, ");
		sql.append("        MST_STAFF_VIEW, ");
		sql.append("        MST_SECTION_TYPE_CODE, ");
		sql.append("        MST_TERM_VIEW, ");
		sql.append("        STF_NAME_VIEW, ");
		sql.append("        STF_DEPARTMENT_CODE, ");
		sql.append("        STD_ID_LOCAL, ");
		sql.append("        STD_NAME_VIEW, ");
		sql.append("        STD_GRADE_LEVEL, ");
		sql.append("        SSC_TERM_VIEW, ");
		sql.append("        SCCADD.SCC_EFFECTIVE_DATE AS START_DATE, ");
		sql.append("        SCCDROP.SCC_EFFECTIVE_DATE AS DROP_DATE ");
		sql.append("FROM    SCHEDULE_MASTER WITH (NOLOCK) ");
		sql.append("JOIN    STAFF WITH (NOLOCK) ON STF_OID = MST_STF_OID_PRIMARY ");
		sql.append("JOIN    SCHEDULE WITH (NOLOCK) ON SCH_OID = MST_SCH_OID AND SCH_OID = ? ");
		params.add(activeScheduleOid);
		sql.append("JOIN    SCHOOL WITH (NOLOCK) ON SKL_OID = SCH_SKL_OID ");
		sql.append("JOIN    STUDENT_SCHEDULE WITH (NOLOCK) ON SSC_MST_OID = MST_OID ");
		// Add record if student did not start out in this section
		sql.append("LEFT JOIN STUDENT_SCHEDULE_CHANGE SCCADD WITH (NOLOCK) ON SCCADD.SCC_STD_OID = SSC_STD_OID ");
		sql.append("          AND SCCADD.SCC_MST_OID = MST_OID ");
		sql.append("          AND SCCADD.SCC_CHANGE_TYPE_CODE = 'Add' ");
		sql.append("          AND SCCADD.SCC_EFFECTIVE_DATE <= ? ");
		params.add(reportOnDate);
		// Pending (future) drop records
		sql.append("LEFT JOIN STUDENT_SCHEDULE_CHANGE SCCDROP WITH (NOLOCK) ON SCCDROP.SCC_STD_OID = SSC_STD_OID ");
		sql.append("          AND SCCDROP.SCC_MST_OID = MST_OID ");
		sql.append("          AND SCCDROP.SCC_CHANGE_TYPE_CODE = 'Drop' ");
		sql.append("          AND SCCDROP.SCC_EFFECTIVE_DATE >= SCCADD.SCC_EFFECTIVE_DATE ");
		sql.append("          AND SCCDROP.SCC_EFFECTIVE_DATE >= ? ");
		params.add(reportOnDate);
		sql.append("JOIN    STUDENT WITH (NOLOCK) ON STD_OID = SSC_STD_OID ");
		if (mstOids != null) {
			// Limit to current selection if user selected that for the queryBy parameter
			sql.append("WHERE   MST_OID IN (" + mstOids + ") ");
		}

		sql.append("UNION ALL ");
		/**
		 * 2. Students that Added the section before the reportOnDate. Excludes any students that added but then dropped
		 * before the reportOnDate. Includes the future drop date if student later dropped AFTER the reportOnDate.
		 */
		sql.append("SELECT  MST_COURSE_VIEW, ");
		sql.append("        MST_DESCRIPTION, ");
		sql.append("        MST_ROOM_VIEW, ");
		sql.append("        MST_SCHEDULE_DISPLAY, ");
		sql.append("        MST_STAFF_VIEW, ");
		sql.append("        MST_SECTION_TYPE_CODE, ");
		sql.append("        MST_TERM_VIEW, ");
		sql.append("        STF_NAME_VIEW, ");
		sql.append("        STF_DEPARTMENT_CODE, ");
		sql.append("        STD_ID_LOCAL, ");
		sql.append("        STD_NAME_VIEW, ");
		sql.append("        STD_GRADE_LEVEL, ");
		sql.append("        NULL AS SSC_TERM_VIEW, ");
		sql.append("        SCCADD.SCC_EFFECTIVE_DATE AS START_DATE, ");
		sql.append("        NULL AS DROP_DATE ");
		sql.append("FROM    SCHEDULE_MASTER ");
		sql.append("JOIN    STAFF WITH (NOLOCK) ON STF_OID = MST_STF_OID_PRIMARY ");
		sql.append("JOIN    SCHEDULE WITH (NOLOCK) ON SCH_OID = MST_SCH_OID AND SCH_OID = ? ");
		params.add(activeScheduleOid);
		sql.append("JOIN    SCHOOL WITH (NOLOCK) ON SKL_OID = SCH_SKL_OID ");
		sql.append("JOIN ");
		sql.append("( ");
		sql.append("  SELECT  SCC_STD_OID, ");
		sql.append("          SCC_MST_OID, ");
		sql.append("          SCC_CHANGE_TYPE_CODE, ");
		sql.append("          SCC_EFFECTIVE_DATE, ");
		sql.append(
				"          row_number() over (partition by SCC_STD_OID, SCC_MST_OID, SCC_CHANGE_TYPE_CODE order by SCC_EFFECTIVE_DATE desc) rn ");
		sql.append("  FROM    STUDENT_SCHEDULE_CHANGE WITH (NOLOCK) ");
		sql.append("  WHERE   SCC_EFFECTIVE_DATE <= ? ");
		params.add(reportOnDate);
		sql.append(") SCCADD ON SCCADD.SCC_MST_OID = MST_OID ");
		sql.append("        AND SCCADD.SCC_CHANGE_TYPE_CODE = 'Add' ");
		sql.append("        AND SCCADD.SCC_EFFECTIVE_DATE <= ? ");
		params.add(reportOnDate);
		sql.append("LEFT JOIN STUDENT_SCHEDULE_CHANGE SCCDROP WITH (NOLOCK) ON SCCDROP.SCC_MST_OID = MST_OID ");
		sql.append("        AND SCCDROP.SCC_CHANGE_TYPE_CODE = 'Drop' ");
		sql.append("        AND SCCDROP.SCC_EFFECTIVE_DATE >= SCCADD.SCC_EFFECTIVE_DATE ");
		sql.append("        AND SCCDROP.SCC_EFFECTIVE_DATE <= ? ");
		params.add(reportOnDate);
		sql.append("        AND SCCDROP.SCC_STD_OID = SCCADD.SCC_STD_OID ");
		sql.append("LEFT JOIN STUDENT_SCHEDULE WITH (NOLOCK) ON SSC_STD_OID = SCCADD.SCC_STD_OID ");
		sql.append("          AND SSC_MST_OID = MST_OID ");
		sql.append("JOIN    STUDENT WITH (NOLOCK) ON STD_OID = SCCADD.SCC_STD_OID ");
		sql.append("WHERE   rn = 1 ");
		sql.append("AND     SCCDROP.SCC_OID IS NULL ");
		sql.append("AND     SSC_OID IS NULL ");
		if (mstOids != null) {
			// Limit to current selection if user selected that for the queryBy parameter
			sql.append("AND MST_OID IN (" + mstOids + ") ");
		}

		sql.append("UNION ALL ");
		/**
		 * 3. Students that Dropped the section after the reportOnDate. Excludes any students that dropped but then
		 * re-added the section and are currently in it. These students will be in the first query.
		 */
		sql.append("SELECT  MST_COURSE_VIEW, ");
		sql.append("        MST_DESCRIPTION, ");
		sql.append("        MST_ROOM_VIEW, ");
		sql.append("        MST_SCHEDULE_DISPLAY, ");
		sql.append("        MST_STAFF_VIEW, ");
		sql.append("        MST_SECTION_TYPE_CODE, ");
		sql.append("        MST_TERM_VIEW, ");
		sql.append("        STF_NAME_VIEW, ");
		sql.append("        STF_DEPARTMENT_CODE, ");
		sql.append("        STD_ID_LOCAL, ");
		sql.append("        STD_NAME_VIEW, ");
		sql.append("        STD_GRADE_LEVEL, ");
		sql.append("        NULL AS SSC_TERM_VIEW, ");
		sql.append("        NULL AS START_DATE, ");
		sql.append("        SCC_EFFECTIVE_DATE AS DROP_DATE ");
		sql.append("FROM    SCHEDULE_MASTER WITH (NOLOCK) ");
		sql.append("JOIN    STAFF WITH (NOLOCK) ON STF_OID = MST_STF_OID_PRIMARY ");
		sql.append("JOIN    SCHEDULE WITH (NOLOCK) ON SCH_OID = MST_SCH_OID AND SCH_OID = ? ");
		params.add(activeScheduleOid);
		sql.append("JOIN    SCHOOL WITH (NOLOCK) ON SKL_OID = SCH_SKL_OID ");
		sql.append("JOIN ");
		sql.append("( ");
		sql.append("  SELECT  SCC_STD_OID, ");
		sql.append("          SCC_MST_OID, ");
		sql.append("          SCC_CHANGE_TYPE_CODE, ");
		sql.append("          SCC_EFFECTIVE_DATE, ");
		sql.append(
				"          row_number() over (partition by SCC_STD_OID, SCC_MST_OID, SCC_CHANGE_TYPE_CODE order by SCC_EFFECTIVE_DATE asc) rn ");
		sql.append("  FROM    STUDENT_SCHEDULE_CHANGE WITH (NOLOCK) ");
		if (!includeWithdrawnStudents) {
			sql.append("  WHERE   SCC_EFFECTIVE_DATE >= ? ");
			params.add(reportOnDate);
		}
		sql.append(") SCC ON SCC_MST_OID = MST_OID ");
		sql.append("        AND SCC_CHANGE_TYPE_CODE = 'Drop' ");
		if (!includeWithdrawnStudents) {
			// If user did NOT select to include withdrawn students, only include withdrawn students where the drop date
			// (effective date) is after the date entered by the user
			sql.append("    AND SCC_EFFECTIVE_DATE >= ? ");
			params.add(reportOnDate);
		}
		sql.append("LEFT JOIN STUDENT_SCHEDULE WITH (NOLOCK) ON SSC_STD_OID = SCC_STD_OID ");
		sql.append("          AND SSC_MST_OID = MST_OID ");
		sql.append("JOIN    STUDENT WITH (NOLOCK) ON STD_OID = SCC_STD_OID ");
		sql.append("WHERE   rn = 1 ");
		sql.append("AND     SSC_OID IS NULL ");
		if (mstOids != null) {
			// Limit to current selection if user selected that for the queryBy parameter
			sql.append("AND MST_OID IN (" + mstOids + ") ");
		}

		/*
		 * Apply ORDER BY clause depending on input from user
		 */
		if (!StringUtils.isBlank(sort)) {
			sql.append("ORDER BY " + validatedFieldList(sort) + ", DROP_DATE, START_DATE ");
		} else {
			// Default sort order
			sql.append("ORDER BY MST_COURSE_VIEW, STD_NAME_VIEW, STD_ID_LOCAL, DROP_DATE, START_DATE ");
		}

		/**
		 * Uncomment this line to print the SQL in a tool message log
		 */
		// logToolMessage(Level.INFO, sql.toString(), false);

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
	 * Save state.
	 *
	 * @param userData UserDataContainer
	 * @see com.follett.fsc.core.k12.tools.reports.ReportJavaDataSource#saveState(com.follett.fsc.core.
	 *      k12.web.UserDataContainer)
	 */
	@Override
	protected void saveState(UserDataContainer userData) {
		m_baseClass = userData.getCurrentNode().getDataClass();

		MessageResources messageResources;
		try {
			messageResources = LocalizationCache.getMessages(getBroker().getPersistenceKey(), userData.getLocale());
		} catch (Exception e) {
			messageResources = LocalizationCache.getMessages(getBroker().getPersistenceKey(),
					LocalizationCache.getCurrentLocale());
		}
		addParameter(PARAM_REPORT_LOCALE, messageResources);
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
	 * Returns a string of OIDs for the current selection, formatted for use with a SQL IN clause
	 * 
	 * @throws Exception
	 */
	private String getCurrentSelectionOids() throws Exception {
		List<String> oidsList = new ArrayList<String>();
		X2Criteria criteria = getCurrentCriteria();
		String[] columns = { X2BaseBean.COL_OID };
		try (IterableColumnQuery query = new IterableColumnQuery(m_baseClass, columns, criteria, getBroker())) {
			for (Object[] row : query) {
				oidsList.add((String) row[0]);
			}
		}
		return "'" + String.join(",", oidsList).replaceAll(",", "','") + "'";
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
