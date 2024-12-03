package ldsb.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import com.follett.fsc.core.framework.persistence.BeanQuery;
import com.follett.fsc.core.framework.persistence.X2Criteria;
import com.follett.fsc.core.k12.business.ModelBroker;
import com.follett.fsc.core.k12.business.StudentManager;
import com.follett.fsc.core.k12.business.dictionary.DataDictionary;
import com.follett.fsc.core.k12.tools.reports.QueryIteratorDataSource;
import com.follett.fsc.core.k12.tools.reports.ReportJavaSourceNet;
import com.x2dev.sis.model.beans.SisStudent;
import com.x2dev.utils.StringUtils;
import com.x2dev.utils.X2BaseException;

import net.sf.jasperreports.engine.JRDataSource;

/**
 * Prepares the data for a report using an object query. This Java class has additional methods for releasing resources,
 * making it suitable as a base for building other reports.
 */
public class StudentListOjbData extends ReportJavaSourceNet {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// Built-in parameter provided by the 'allow-school-select' attribute on the tool-input tag
	private static final String PARAM_SCHOOL_OID = "schoolOid";

	// Inputs from the Input Definition
	private static final String PARAM_ACTIVE_ONLY = "activeOnly";

	private ModelBroker m_broker;

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

		X2Criteria criteria = new X2Criteria();
		// Only include active students if the activeOnly box is checked
		if (activeOnly) {
			criteria.addIn(SisStudent.COL_ENROLLMENT_STATUS,
					StudentManager.getActiveStudentCodeList(getOrganization()));
		}
		// Filter to a single school if user has selected one
		if (!StringUtils.isBlank(schoolOid)) {
			criteria.addEqualTo(SisStudent.COL_SCHOOL_OID, getParameter(schoolOid));
		}
		BeanQuery query = new BeanQuery(SisStudent.class, criteria);

		/*
		 * Execute the query and return the results
		 */
		return new QueryIteratorDataSource(m_broker.getIteratorByQuery(query),
				DataDictionary.getDistrictDictionary(getUser().getPersistenceKey()), true, getLocale());
	}

	/**
	 * @see com.follett.fsc.core.k12.tools.ToolJavaSource#initialize()
	 */
	@Override
	protected void initialize() throws X2BaseException {
		super.initialize();
		m_broker = new ModelBroker(getPrivilegeSet());
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

}