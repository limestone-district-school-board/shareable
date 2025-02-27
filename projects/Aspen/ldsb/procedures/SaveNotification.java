package ldsb.procedures;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Overview: The SaveNotification class should be added as a nested class to any Aspen procedure that will use
 * notifications. The required import libs to be added to procedures are: import java.io.File; import
 * java.io.FileWriter; import java.util.Date; import java.text.SimpleDateFormat;
 *
 * usage: save_notif_inst.save_notification("traised@limestone.on.ca", "this is a notification from acadience
 * assessment");
 *
 * Datetime is added. Aspen_notifications.csv file is updated.
 *
 * MessageType can be exception, warning, error, information.
 */
public class SaveNotification {

	public SaveNotification() {
	}

	public void save(String fileName, String messageType, String recipients, String message) throws Exception {

		Date myDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
		String myDateString = simpleDateFormat.format(myDate);

		String filename = fileName;

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
