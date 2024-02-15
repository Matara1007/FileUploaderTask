package com.example.fileManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SpringBootApplication
public class FileManagerApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(FileManagerApplication.class, args);

	}
}

@RestController
class FileUploadController {

	@PostMapping(value = "/upload")
	public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file,
											 @RequestPart("data") Metadata metadata) {
		try {
			saveToDatabase(file, metadata);
			return ResponseEntity.ok("File uploaded successfully!");
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Error uploading file!");
		}
	}

	private void saveToDatabase(MultipartFile file, Metadata metadata) throws IOException, SQLException {
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/FilesDatabase", "root", "admin");
		String sql = "INSERT INTO files (id, ime, prezime, vrijeme_kreiranja, file_data) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1, metadata.getId());
		statement.setString(2, metadata.getIme());
		statement.setString(3, metadata.getPrezime());
		statement.setString(4, metadata.getVrijeme_kreiranja());
		statement.setBytes(5, file.getBytes());
		statement.executeUpdate();
		statement.close();
		connection.close();
	}
}

class Metadata {
	private int id;
	private String ime;
	private String prezime;
	private String vrijeme_kreiranja;

	// Getters and setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIme() {
		return ime;
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public String getPrezime() {
		return prezime;
	}

	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}

	public String getVrijeme_kreiranja() {
		return vrijeme_kreiranja;
	}

	public void setVrijeme_kreiranja(String vrijeme_kreiranja) {
		this.vrijeme_kreiranja = vrijeme_kreiranja;
	}
}


