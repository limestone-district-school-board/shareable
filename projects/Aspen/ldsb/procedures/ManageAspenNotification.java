package ldsb.procedures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.follett.fsc.core.k12.tools.procedures.ProcedureJavaSource;

public class ManageAspenNotification extends ProcedureJavaSource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void execute() throws Exception {
		clearNotificationsFile();
	}

	/**
	 * Find notifications file and empty it's contents
	 * 
	 * - Copies aspen_notifications_buffer.csv to aspen_notifications.csv - Empties aspen_notifications_buffer.csv
	 * 
	 */
	private void clearNotificationsFile() throws Exception {
		// File folder = FolderUtils.createNewFolder(getJob().getTempFolder(), folderName);

		String sourceFolderPath = "/data/app/aspen-data/nfs/tool-staging/ldsbsis/export/aspen_notifications_buffer.csv";
		String targetFolderPath = "/data/app/aspen-data/nfs/tool-staging/ldsbsis/export/aspen_notifications.csv";

		File sourceFolder = new File(sourceFolderPath);
		File targetFolder = new File(targetFolderPath);

		try {
			// Step 1: Copy the source file to the target location
			Files.copy(sourceFolder.toPath(), targetFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Step 2: Empty the contents of the copied (target) file
			try (FileWriter writer = new FileWriter(sourceFolder, false)) {
				writer.write(""); // Explicitly write empty content, though opening with false already truncates
			}

			System.out.println("File copied and target emptied successfully.");
		} catch (IOException e) {
			System.err.println("An error occurred: " + e.getMessage());
		}
	}

}
