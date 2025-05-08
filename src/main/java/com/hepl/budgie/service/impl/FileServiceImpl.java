package com.hepl.budgie.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.service.FilePathService;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

	private final Map<String, FilePathService> filePath;

	public FileServiceImpl(List<FilePathService> filePathList) {
		this.filePath = filePathList.stream()
				.collect(Collectors.toMap(path -> path.getClass().getSimpleName(), Function.identity()));
	}

	@Override
	public void init() {
		log.info("Creating upload directories");
		filePath.values().forEach(path -> {
			try {
				Files.createDirectories(path.getDestinationPath());
			} catch (IOException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.INIT_STORAGE);
			}
		});
	}

	@Override
	public String uploadFile(MultipartFile files, FileType fileType, String fileName) throws IOException {

		String uniqueFileName = fileName.isEmpty() ? files.getOriginalFilename()
				: fileName + "." + FileNameUtils.getExtension(files.getOriginalFilename());
		String filename = System.currentTimeMillis() + "-" + uniqueFileName;
		Path destinationFile;

		Path location = filePath.getOrDefault(fileType.label, null).getDestinationPath();
		destinationFile = location.resolve(Paths.get(filename)).normalize().toAbsolutePath();

		if (!destinationFile.getParent().equals(location.toAbsolutePath()) || filename.split("\\.").length > 2) {
			// This is a security check
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badFilePath");
		}
		try (InputStream inputStream = files.getInputStream()) {
			Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
		}
		return filename;
	}

	@Override
	public String uploadFile(byte[] fileByte, FileType fileType, String fileName) throws IOException {
		String filename = System.currentTimeMillis() + "-" + fileName;
		Path destinationFile;

		Path location = filePath.getOrDefault(fileType.label, null).getDestinationPath();
		destinationFile = location.resolve(Paths.get(filename)).normalize().toAbsolutePath();

		if (!destinationFile.getParent().equals(location.toAbsolutePath())) {
			// This is a security check
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.badFilePath");
		}
		Files.write(destinationFile, fileByte, StandardOpenOption.CREATE);
		return filename;
	}

	@Override
	public void deleteFile(String filename, FileType fileType) throws IOException {
		log.info("Delete file ... {}", filename);
		Path file = load(filename, fileType);

		Files.delete(file);
	}

	@Override
	public Resource loadAsResource(String filename, FileType fileType) throws MalformedURLException {
		log.info("Downloading file ... {}", filename);
		Path file = load(filename, fileType);

		Resource resource = new UrlResource(file.toUri());
		if (resource.exists() || resource.isReadable()) {
			return resource;
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FILE_NOT_FOUND);

		}
	}

	@Override
	public void moveToBin(String filename, FileType fileType) throws IOException {
		log.info("Moving file to bin ... {}", filename);
		Path currentfile = load(filename, fileType);
		Path destinationFile = filePath.getOrDefault(FileType.BIN.label, null).getDestinationPath()
				.resolve(Paths.get(filename)).normalize().toAbsolutePath();
		Files.move(currentfile, destinationFile, StandardCopyOption.REPLACE_EXISTING);

	}

	private Path load(String filename, FileType fileType) {
		return filePath.getOrDefault(fileType.label, null).getDestinationPath().resolve(filename);
	}

}