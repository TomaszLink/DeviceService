package pl.tomaszlink.deviceservice.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.tomaszlink.deviceservice.model.DeviceAlreadyExistsErrorModel;
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
