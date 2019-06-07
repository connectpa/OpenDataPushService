package it.connectpa.odatapushservice.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BadRequestException extends AbstractRequestException {

    private static final long serialVersionUID = 8963068848642800655L;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BadRequestException(
            @JsonProperty("code") final Integer code,
            @JsonProperty("message") final String message) {

        super(code, message);
    }
}
