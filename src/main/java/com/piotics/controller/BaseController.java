package com.piotics.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piotics.constants.MessageType;
import com.piotics.exception.FileException;
import com.piotics.resources.ExceptionResource;

@ControllerAdvice(annotations = RestController.class)
public class BaseController {

	private static Logger logger = LogManager.getLogger(BaseController.class);

	@Autowired
	private MessageSource msgSource;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ResponseEntity<ExceptionResource> processValidationError(MethodArgumentNotValidException ex) {
		ex.printStackTrace();
		BindingResult result = ex.getBindingResult();
		ObjectError error = result.getAllErrors().get(0);
		ExceptionResource exceptionResource = new ExceptionResource(error.getDefaultMessage());
		return new ResponseEntity<>(exceptionResource,HttpStatus.BAD_REQUEST);
	}

	private ExceptionResource processFieldError(FieldError error) {
		ExceptionResource message = null;
		if (error != null) {
			Locale currentLocale = LocaleContextHolder.getLocale();
			String msg = msgSource.getMessage(error.getDefaultMessage(), null, currentLocale);
			message = new ExceptionResource(MessageType.ERROR, msg);
		}
		return message;
	}

	@ExceptionHandler({ HttpClientErrorException.class })
	public ResponseEntity<ExceptionResource> handleHttpClientException(HttpClientErrorException ex) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ExceptionResource exceptionResource = mapper.readValue(ex.getResponseBodyAsString(), ExceptionResource.class);

		return new ResponseEntity<>(exceptionResource, ex.getStatusCode());
	}

	@ExceptionHandler({ HttpServerErrorException.class })
	public ResponseEntity<ExceptionResource> handleHttpServerException(HttpServerErrorException ex) throws IOException {
		logger.log(Level.INFO, ex.getMessage(), ex);
		ObjectMapper mapper = new ObjectMapper();
		ExceptionResource exceptionResource = mapper.readValue(ex.getResponseBodyAsString(), ExceptionResource.class);
		if (ex.getStatusText() != null) {
			exceptionResource.setMessage(ex.getStatusText());
		}
		return new ResponseEntity<>(exceptionResource, HttpStatus.EXPECTATION_FAILED);
	}

	@ExceptionHandler({ ConnectException.class })
	public ResponseEntity<ExceptionResource> handleConnectException(ConnectException ex) {
		logger.fatal("service unavailable - " + ex.getCause() + " - " + ex.getLocalizedMessage(), ex);
		return new ResponseEntity(new Exception("Service Unavailable"), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler({ ResourceAccessException.class })
	public ResponseEntity<ExceptionResource> handleConnectException(ResourceAccessException ex) {
		logger.fatal("service unavailable - " + ex.getCause() + " - " + ex.getLocalizedMessage(), ex);
		return new ResponseEntity(new Exception("Service Unavailable"), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<ExceptionResource> handleException(Exception ex) throws IOException {

		logger.log(Level.INFO, ex.getMessage(), ex);
		ExceptionResource exceptionResource = new ExceptionResource(ex.getMessage());

		if (ex.getMessage().equals("wrong password"))
			return new ResponseEntity<>(exceptionResource, HttpStatus.BAD_REQUEST);
		if(ex.getMessage().equals("Access is denied"))
			return new ResponseEntity<>(exceptionResource, HttpStatus.UNAUTHORIZED);
		if (ex.getMessage().equals("no change found"))
			return new ResponseEntity<>(exceptionResource, HttpStatus.NOT_MODIFIED);
		if (ex.getMessage().equals("user not invited") || ex.getMessage().equals("no valid request for change mail")
				|| ex.getMessage().equals("not a valid email") || ex.getMessage().equals("wrong password"))
			return new ResponseEntity<>(exceptionResource, HttpStatus.FORBIDDEN);

		return new ResponseEntity<>(exceptionResource, HttpStatus.EXPECTATION_FAILED);
	}
	
	@ExceptionHandler({ FileException.class })
	public ResponseEntity<ExceptionResource> handleFileException(Exception ex) throws IOException {

		logger.log(Level.INFO, ex.getMessage(), ex);
		ExceptionResource exceptionResource = new ExceptionResource(ex.getMessage());

		if (ex.getMessage().equals("unsuppoted file format"))
			return new ResponseEntity<>(exceptionResource, HttpStatus.UNSUPPORTED_MEDIA_TYPE);

		return new ResponseEntity<>(exceptionResource, HttpStatus.EXPECTATION_FAILED);
	}

}
