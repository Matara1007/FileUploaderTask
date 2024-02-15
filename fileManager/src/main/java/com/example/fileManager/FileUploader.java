package com.example.fileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.mashape.unirest.http.*;
import java.text.SimpleDateFormat;

public class FileUploader {
	private static final String EXPORT_FOLDER_PATH = "C:\\Users\\PlugelM.AMPLEXOR\\Desktop\\struktura\\export";
	private static final String DONE_FOLDER_PATH = "C:\\Users\\PlugelM.AMPLEXOR\\Desktop\\struktura\\done";
	private static final String ERROR_FOLDER_PATH = "C:\\Users\\PlugelM.AMPLEXOR\\Desktop\\struktura\\error";
	private static final String UPLOAD_ENDPOINT = "http://localhost:8080/upload"; // endpoint from Task 1

	public static void main(String[] args) {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(FileUploader::processFiles, 0, 10, TimeUnit.SECONDS);
	}

	private static void processFiles() {
		File exportFolder = new File(EXPORT_FOLDER_PATH);
		File[] files = exportFolder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					try {
						String fileName = file.getName();
						String[] parts = fileName.split("_");
						if (parts.length == 4) {
							Integer id = Integer.valueOf(parts[0]);
							String ime = parts[1];
							String prezime = parts[2];
							String randomText = parts[3];
							String extension = randomText.substring(randomText.lastIndexOf(".") + 1);
							BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
							FileTime creationTime = attr.creationTime();

							Date creationDate = new Date(creationTime.toMillis());

							// Format creation time as "yyyy-MM-dd HH:mm:ss"
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String fileCreationTime = dateFormat.format(creationDate);

							String filePath = file.getAbsolutePath();
							// Call upload method
							HttpResponse<String> uploadSuccessful = uploadFileEx(file, id, ime, prezime, fileCreationTime, filePath);

							// Move file to appropriate folder
							if (uploadSuccessful.getStatus()==200) {
								moveFileToFolder(file, DONE_FOLDER_PATH);
							} else {
								moveFileToFolder(file, ERROR_FOLDER_PATH);
							}
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private static HttpResponse<String> uploadFileEx(File file, Integer id, String ime, String prezime, String creationTime, String filePath) throws Exception {
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.post(UPLOAD_ENDPOINT)
			.field("data", String.format("{\"id\": %d, \"ime\": \"%s\", \"prezime\": \"%s\", \"vrijeme_kreiranja\": \"%s\"}", id, ime, prezime, creationTime), "application/json")
			.field("file", new File(filePath))
			.asString();
		System.out.println(response.getBody());
		return response;
	}

	private static void moveFileToFolder(File file, String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File newFile = new File(folder, file.getName());
		file.renameTo(newFile);
	}
}
