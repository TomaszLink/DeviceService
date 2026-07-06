package pl.tomaszlink.deviceservice.domain.common

import spock.lang.Specification

class ResponseEntityHelperSpec extends Specification {

    def "createResponseEntityWithHeaders maps content and exposes paging headers"() {
        given:
        def listResult = new ListResult<Integer>([1, 2, 3], 2, 25, 120, 5)

        when:
        def response = ResponseEntityHelper.createResponseEntityWithHeaders(listResult, { Integer i -> "item-" + i })

        then:
        response.statusCode.value() == 200
        response.body == ["item-1", "item-2", "item-3"]
        response.headers.getFirst("X-Total-Count") == "120"
        response.headers.getFirst("X-Total-Pages") == "5"
        response.headers.getFirst("X-Page") == "2"
        response.headers.getFirst("X-Size") == "25"
    }

    def "createResponseEntityWithHeaders handles an empty content list"() {
        given:
        def listResult = new ListResult<Integer>([], 0, 10, 0, 0)

        when:
        def response = ResponseEntityHelper.createResponseEntityWithHeaders(listResult, { Integer i -> i })

        then:
        response.body == []
        response.headers.getFirst("X-Total-Count") == "0"
        response.headers.getFirst("X-Total-Pages") == "0"
    }
}
