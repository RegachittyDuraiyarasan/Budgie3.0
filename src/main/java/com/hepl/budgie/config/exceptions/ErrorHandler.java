package com.hepl.budgie.config.exceptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.service.FileService;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

	// private static final String COLLECTION_PREFIX = "collection: ";
	private static final String INDEX_PREFIX = "index: ";

	private final Translator translator;
	private final FileService fileService;

	public ErrorHandler(Translator translator, FileService fileService) {
		this.translator = translator;
		this.fileService = fileService;
	}

	@ExceptionHandler
	public ResponseEntity<GenericResponse<Object>> handleException(CustomResponseStatusException exc,
			WebRequest request) {
		GenericResponse<Object> error = GenericResponse.error(ErrorTypes.OPERATIONAL.label,
				translator.toLocale(exc.getMessage(), exc.getArgs()));
		log.error("Operational Exception occured - {}, Request Details: {}", error.getTimestamp(),
				request.getDescription(false), exc);

		return new ResponseEntity<>(error, exc.getStatus());
	}

	@ExceptionHandler
	public ResponseEntity<GenericResponse<Object>> handleException(ResponseStatusException exc, WebRequest request) {
		if (exc.getStatusCode().is2xxSuccessful()) {
			GenericResponse<Object> defaultObj = GenericResponse.success("", Map.of());
			log.error("No values found, Request Details: {}",
					request.getDescription(false), exc);

			return new ResponseEntity<>(defaultObj, exc.getStatusCode());
		} else {
			GenericResponse<Object> error = GenericResponse.error(ErrorTypes.OPERATIONAL.label,
					translator.toLocale(exc.getReason()));
			log.error("Operational Exception occured - {}, Request Details: {}", error.getTimestamp(),
					request.getDescription(false), exc);

			return new ResponseEntity<>(error, exc.getStatusCode());
		}

	}

	@ExceptionHandler
	public ResponseEntity<GenericResponse<Object>> handleException(Exception exc, WebRequest request) {
		GenericResponse<Object> error = GenericResponse.error(ErrorTypes.FATAL.label, exc.getLocalizedMessage());
		log.error("Fatal Exception occured - {}, Request Details: {}", error.getTimestamp(),
				request.getDescription(false), exc);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler
	public ResponseEntity<GenericResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException exc,
			WebRequest request) {
		Map<String, String> errors = new HashMap<>();
		exc.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = "";
			if (error instanceof FieldError fieldError) {
				fieldName = fieldError.getField();
			} else {
				ObjectError objectError = (ObjectError) error;
				log.info("{}", objectError);
			}

			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		GenericResponse<Object> error = GenericResponse.fielderror(ErrorTypes.FIELD.label,
				"Field error", errors);
		log.error("Validation error - {}, Request Details: {}", error.getTimestamp(),
				request.getDescription(false), exc);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CustomDuplicatekeyException.class)
	public ResponseEntity<GenericResponse<Object>> handleCustomException(CustomDuplicatekeyException exc,
			WebRequest request) {
		Map<String, String> errors = new HashMap<>();

		exc.getErrors().forEach((k, v) -> errors.put(k, translator.toLocale(v)));

		if (!exc.getFileInfo().isEmpty()) {
			deleteFiles(exc.getFileInfo());
		}

		GenericResponse<Object> error = new GenericResponse<>(false);
		error.setMessage("");
		error.setErrors(errors);
		error.setErrorType(ErrorTypes.FIELD.label);
		log.error("Operational Exception occurred - {}, Request Details: {}", error.getTimestamp(),
				request.getDescription(false), exc);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	private void deleteFiles(List<FileInfo> fileInfoList) {
		for (FileInfo fileInfo : fileInfoList) {
			try {
				fileService.deleteFile(fileInfo.getFileName(), fileInfo.getFileType());
			} catch (IOException e) {
				log.info("File IO error - {}", e.getLocalizedMessage());
			}
		}
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<GenericResponse<Object>> handleException(DuplicateKeyException exc, WebRequest request) {
		String message = exc.getMessage();
		// String collectionName = extractCollectionName(message);
		String fieldName = extractFieldName(message);

		Map<String, String> errors = new HashMap<>();
		errors.put(fieldName, translator.toLocale("error.valueExists"));

		GenericResponse<Object> error = new GenericResponse<>(false);
		error.setMessage(fieldName + " already exists");
		error.setErrors(errors);
		error.setErrorType(ErrorTypes.OPERATIONAL.label);
		log.error("Operational Exception occurred - {}, Request Details: {}", error.getTimestamp(),
				request.getDescription(false), exc);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// private String extractCollectionName(String message) {
	// return extractValue(message, COLLECTION_PREFIX);
	// }

	private String extractFieldName(String message) {
		String indexName = extractValue(message, INDEX_PREFIX);
		int underscoreIndex = indexName.lastIndexOf("_");

		return underscoreIndex != -1 ? indexName.substring(0, underscoreIndex) : indexName;
	}

	private String extractValue(String message, String prefix) {
		int startIndex = message.indexOf(prefix) + prefix.length();
		int endIndex = message.indexOf(" ", startIndex);

		return endIndex != -1 ? message.substring(startIndex, endIndex).trim() : message.substring(startIndex).trim();
	}

	@ExceptionHandler
	public ResponseEntity<GenericResponse<Object>> handleFieldErrorExceptions(FieldException exc, WebRequest request) {
		Map<String, String> errors = new HashMap<>();

		exc.getErrors().forEach((k, v) -> errors.put(k, translator.toLocale(v, exc.getErrorArgs().get(k))));

		GenericResponse<Object> error = GenericResponse.fielderror(ErrorTypes.FIELD.label,
				exc.getLocalizedMessage(), errors);
		log.error("Validation error - OPERATIONAL - {}, Request Details: {}", error.getTimestamp(),
				request.getDescription(false), exc);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<GenericResponse<Object>> handleConstraintViolationException(
			ConstraintViolationException exc, WebRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();

		exc.getConstraintViolations().forEach(violation -> {
			String fieldName = extractField(violation.getPropertyPath().toString());
			String errorMessage = violation.getMessage();
			errors.put(fieldName, errorMessage);
		});

		GenericResponse<Object> error = GenericResponse.fielderror(ErrorTypes.FIELD.label,
				"Field error", errors);
		log.error("Validation error - OPERATIONAL - {}, Request Details: {}", error.getTimestamp(),
				request.getDescription(false), exc);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	private String extractField(String propertyPath) {
		return propertyPath.contains(".") ? propertyPath.substring(propertyPath.indexOf('.') + 1) : propertyPath;
	}

}
