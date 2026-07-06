package pl.tomaszlink.deviceservice.exceptions

import jakarta.validation.ConstraintViolation
import jakarta.validation.Path
import org.springframework.context.MessageSourceResolvable
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.method.MethodValidationResult
import org.springframework.validation.method.ParameterValidationResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import pl.tomaszlink.deviceservice.models.DeviceAlreadyExistsErrorModel
import pl.tomaszlink.deviceservice.models.DeviceLocationNotFoundErrorModel
import pl.tomaszlink.deviceservice.models.DeviceNotFoundErrorModel
import spock.lang.Specification

class ExceptionsHandlerSpec extends Specification {

    ExceptionsHandler handler = new ExceptionsHandler()

    def "handleDeviceAlreadyExistsException returns 409 with the device identifier"() {
        given:
        def ex = new DeviceAlreadyExistsException("device-1")

        when:
        def response = handler.handleDeviceAlreadyExistsException(ex)

        then:
        response.statusCode.value() == 409
        response.body.error == DeviceAlreadyExistsErrorModel.ErrorEnum.DEVICE_ALREADY_EXISTS
        response.body.message.contains("device-1")
    }

    def "handleDeviceNotFoundException returns 404 with the device id"() {
        given:
        def ex = new DeviceNotFoundException("device-1")

        when:
        def response = handler.handleDeviceNotFoundException(ex)

        then:
        response.statusCode.value() == 404
        response.body.error == DeviceNotFoundErrorModel.ErrorEnum.DEVICE_NOT_FOUND
        response.body.message.contains("device-1")
    }

    def "handleDeviceLocationNotFoundException returns 404"() {
        given:
        def ex = new DeviceLocationNotFoundException("device-1")

        when:
        def response = handler.handleDeviceLocationNotFoundException(ex)

        then:
        response.statusCode.value() == 404
        response.body.error == DeviceLocationNotFoundErrorModel.ErrorEnum.DEVICE_LOCATION_NOT_FOUND
    }

    def "handleDeviceLocationPublishException returns 503"() {
        given:
        def ex = new DeviceLocationPublishException(UUID.randomUUID(), new RuntimeException("broker down"))

        when:
        def response = handler.handleDeviceLocationPublishException(ex)

        then:
        response.statusCode.value() == 503
        response.body.error == "SERVICE_UNAVAILABLE"
    }

    def "handleConcurrentModificationException returns 409"() {
        given:
        def ex = new ConcurrentModificationException("stale")

        when:
        def response = handler.handleConcurrentModificationException(ex)

        then:
        response.statusCode.value() == 409
        response.body.error == "CONCURRENT_MODIFICATION"
    }

    def "handleAuthorizationDeniedException returns 403"() {
        given:
        def ex = new AuthorizationDeniedException("denied")

        when:
        def response = handler.handleAuthorizationDeniedException(ex)

        then:
        response.statusCode.value() == 403
        response.body.error == "ACCESS_DENIED"
    }

    def "handleGeneralException returns 500 with a generic message"() {
        given:
        def ex = new RuntimeException("boom")

        when:
        def response = handler.handleGeneralException(ex)

        then:
        response.statusCode.value() == 500
        response.body.error == "ERROR"
        response.body.message == "Something went wrong"
    }

    def "handleMissingServletRequestParameterException returns 400 naming the missing parameter"() {
        given:
        def ex = new MissingServletRequestParameterException("page", "Integer")

        when:
        def response = handler.handleMissingServletRequestParameterException(ex)

        then:
        response.statusCode.value() == 400
        response.body.message == "page is required"
    }

    def "handleHttpMessageNotReadableException returns 400 with a generic message"() {
        given:
        def ex = new HttpMessageNotReadableException("bad json", Stub(HttpInputMessage))

        when:
        def response = handler.handleHttpMessageNotReadableException(ex)

        then:
        response.statusCode.value() == 400
        response.body.message == "Malformed request body"
    }

    def "handleMethodArgumentTypeMismatchException returns 400 describing the invalid value"() {
        given:
        def ex = Mock(MethodArgumentTypeMismatchException) {
            getName() >> "id"
            getValue() >> "not-a-uuid"
        }

        when:
        def response = handler.handleMethodArgumentTypeMismatchException(ex)

        then:
        response.statusCode.value() == 400
        response.body.message == "id: invalid value 'not-a-uuid'"
    }

    def "handleConstraintViolationException aggregates violation messages"() {
        given:
        def path = Stub(Path) {
            toString() >> "size"
        }
        def violation = Stub(ConstraintViolation) {
            getPropertyPath() >> path
            getMessage() >> "must be between 1 and 100"
        }
        def ex = new jakarta.validation.ConstraintViolationException("invalid", [violation] as Set)

        when:
        def response = handler.handleConstraintViolationException(ex)

        then:
        response.statusCode.value() == 400
        response.body.message == "size: must be between 1 and 100"
    }

    def "handleMethodArgumentNotValidException aggregates field error messages"() {
        given:
        def fieldError = new FieldError("object", "name", "must not be blank")
        def bindingResult = new BeanPropertyBindingResult(new Object(), "object")
        bindingResult.addError(fieldError)
        def parameter = new MethodParameter(ExceptionsHandlerSpec.getDeclaredMethod("dummyMethod", String), 0)
        def ex = new MethodArgumentNotValidException(parameter, bindingResult)

        when:
        def response = handler.handleMethodArgumentNotValidException(ex)

        then:
        response.statusCode.value() == 400
        response.body.message == "name: must not be blank"
    }

    private static void dummyMethod(String arg) {}

    def "handleHandlerMethodValidationException aggregates parameter validation errors"() {
        given:
        def methodParameter = Mock(MethodParameter) {
            getParameterName() >> "page"
        }
        def resolvableError = Stub(MessageSourceResolvable) {
            getDefaultMessage() >> "must be positive"
        }
        def parameterResult = Mock(ParameterValidationResult) {
            getMethodParameter() >> methodParameter
            getResolvableErrors() >> [resolvableError]
        }
        def validationResult = Mock(MethodValidationResult) {
            getParameterValidationResults() >> [parameterResult]
        }
        def ex = new HandlerMethodValidationException(validationResult)

        when:
        def response = handler.handleHandlerMethodValidationException(ex)

        then:
        response.statusCode.value() == 400
        response.body.message == "page: must be positive"
    }
}
