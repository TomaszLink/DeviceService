package pl.tomaszlink.deviceservice.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pl.tomaszlink.deviceservice.model.DeviceAlreadyExistsErrorModel;
import pl.tomaszlink.deviceservice.model.DeviceLocationNotFoundErrorModel;
import pl.tomaszlink.deviceservice.model.DeviceNotFoundErrorModel;
import pl.tomaszlink.deviceservice.model.ErrorModel;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionsHandler {
    private static final String DEFAULT_ERROR_MESSAGE = "Something went wrong";
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String BAD_REQUEST = "BAD_REQUEST";

    @ExceptionHandler(DeviceAlreadyExistsException.class)
    public ResponseEntity<DeviceAlreadyExistsErrorModel> handleDeviceAlreadyExistsException(DeviceAlreadyExistsException ex) {
        return ResponseEntity
                .status(409)
                .body(new DeviceAlreadyExistsErrorModel()
                        .error(DeviceAlreadyExistsErrorModel.ErrorEnum.DEVICE_ALREADY_EXISTS)
                        .message(ex.getMessage()));
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<DeviceNotFoundErrorModel> handleDeviceNotFoundException(DeviceNotFoundException ex) {
        return ResponseEntity
                .status(404)
                .body(new DeviceNotFoundErrorModel()
                        .error(DeviceNotFoundErrorModel.ErrorEnum.DEVICE_NOT_FOUND)
                        .message(ex.getMessage()));
    }

    @ExceptionHandler(DeviceLocationNotFoundException.class)
    public ResponseEntity<DeviceLocationNotFoundErrorModel> handleDeviceLocationNotFoundException(DeviceLocationNotFoundException ex) {
        return ResponseEntity
                .status(404)
                .body(new DeviceLocationNotFoundErrorModel()
                        .error(DeviceLocationNotFoundErrorModel.ErrorEnum.DEVICE_LOCATION_NOT_FOUND)
                        .message(ex.getMessage()));
    }

    @ExceptionHandler(DeviceLocationPublishException.class)
    public ResponseEntity<ErrorModel> handleDeviceLocationPublishException(DeviceLocationPublishException ex) {
        log.error("Device location event could not be published", ex);
        return ResponseEntity
                .status(503)
                .body(new ErrorModel()
                        .error("SERVICE_UNAVAILABLE")
                        .message("Location could not be accepted for processing, please retry"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorModel> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(400)
                .body(new ErrorModel().error(VALIDATION_ERROR).message(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorModel> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(400)
                .body(new ErrorModel().error(VALIDATION_ERROR).message(message));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorModel> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        String message = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(400)
                .body(new ErrorModel().error(VALIDATION_ERROR).message(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorModel> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = ex.getName() + ": invalid value '" + ex.getValue() + "'";
        return ResponseEntity
                .status(400)
                .body(new ErrorModel().error(BAD_REQUEST).message(message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorModel> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(400)
                .body(new ErrorModel().error(BAD_REQUEST).message(ex.getParameterName() + " is required"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorModel> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(400)
                .body(new ErrorModel().error(BAD_REQUEST).message("Malformed request body"));
    }

    @ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<ErrorModel> handleConcurrentModificationException(ConcurrentModificationException e) {
        return ResponseEntity
                .status(409)
                .body(new ErrorModel()
                        .error("CONCURRENT_MODIFICATION")
                        .message("Let's try again."));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorModel> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        return ResponseEntity
                .status(403)
                .body(new ErrorModel()
                        .error("ACCESS_DENIED")
                        .message("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorModel> handleGeneralException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(500)
                .body(new ErrorModel()
                        .error("ERROR")
                        .message(DEFAULT_ERROR_MESSAGE));
    }
}
