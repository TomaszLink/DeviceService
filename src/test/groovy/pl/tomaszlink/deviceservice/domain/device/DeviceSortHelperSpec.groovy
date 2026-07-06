package pl.tomaszlink.deviceservice.domain.device

import spock.lang.Specification
import spock.lang.Unroll

class DeviceSortHelperSpec extends Specification {

    @Unroll
    def "maps sort field '#input' to '#expected'"() {
        expect:
        DeviceSortHelper.mapSortField(input) == expected

        where:
        input               | expected
        "type"              | "type"
        "TYPE"              | "type"
        "unique_identifier" | "uniqueIdentifier"
        "UNIQUE_IDENTIFIER" | "uniqueIdentifier"
        "name"              | "name"
        "NAME"              | "name"
        "unknownField"      | "name"
    }
}