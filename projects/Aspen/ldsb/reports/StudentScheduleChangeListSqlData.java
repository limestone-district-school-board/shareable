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

import com.follett.fsc.core.k12.business.BeanPathException;
import com.follett.fsc.core.k12.business.ModelBroker;
import com.follett.fsc.core.k12.business.X2Broker;
import com.follett.fsc.core.k12.business.dictionary.DataDictionary;
import com.follett.fsc.core.k12.tools.reports.Breakable;
import com.follett.fsc.core.k12.tools.reports.ClosableDataSource;
import com.follett.fsc.core.k12.tools.reports.ReportDataGrid;
import com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet;
import com.follett.fsc.core.k12.web.UserDataContainer;
import com.x2dev.sis.model.beans.SisStudent;
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
public class StudentScheduleChangeListSqlData extends ReportJavaSourceNet {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private ModelBroker m_broker;
	private SisStudent m_currentStudent;
	private DataDictionary m_dictionary;

	/**
	 * Gather data.
	 *
	 * @return JRDataSource
	 * @see com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet#gatherData()
	 */
	@Override
	protected JRDataSource gatherData() {
		// List of parameters for the SQL statement
		List<Object> params = new ArrayList<Object>();

		// Validate that all fields in our select statement are valid Aspen database fields
		String fieldList = validatedFieldList(
				"SCC_CHANGE_TYPE_CODE, SCC_DATE, SCC_EFFECTIVE_DATE, MST_COURSE_VIEW, MST_SECTION_NUMBER, MST_TERM_VIEW, SCC_SCHEDULE_DISPLAY, SCC_USER_VIEW");

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT  " + fieldList + " ");
		sql.append("FROM    STUDENT_SCHEDULE_CHANGE ");
		sql.append("JOIN    SCHEDULE_MASTER ON MST_SCH_OID = SCC_SCH_OID ");
		sql.append("JOIN    SCHOOL_SCHEDULE_CONTEXT ON SKX_SCH_OID_ACTIVE = SCC_SCH_OID ");
		sql.append("JOIN    SCHOOL ON SKL_OID = SKX_SKL_OID ");
		sql.append("JOIN    ORGANIZATION ON ORG_OID = SKL_ORG_OID_1 ");
		sql.append("JOIN    DISTRICT_SCHOOL_YEAR_CONTEXT ctx1 ON ctx1.CTX_OID = ORG_CTX_OID_CURRENT ");
		sql.append(
				"JOIN    DISTRICT_SCHOOL_YEAR_CONTEXT ctx2 ON ctx2.CTX_SCHOOL_YEAR BETWEEN ctx1.CTX_SCHOOL_YEAR-2 AND ctx1.CTX_SCHOOL_YEAR ");
		sql.append("WHERE   SKX_CTX_OID = ctx2.CTX_OID ");
		sql.append("AND     SCC_STD_OID = '" + m_currentStudent.getOid() + "' ");
		sql.append("ORDER BY SCC_EFFECTIVE_DATE DESC, SCC_CHANGE_TYPE_CODE DESC ");

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
	 * @see com.follett.fsc.core.k12.tools.ToolJavaSource#saveState()
	 */
	@Override
	protected void saveState(UserDataContainer userData) {
		/*
		 * If we're in the context of a single student, print the report for just that student
		 */
		m_currentStudent = userData.getCurrentRecord(SisStudent.class);
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