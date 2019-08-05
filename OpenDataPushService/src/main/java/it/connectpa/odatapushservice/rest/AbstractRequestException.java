package it.connectpa.odatapushservice.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractRequestException extends RuntimeException {

    private static final long serialVersionUID = -3885833017956650220L;

    private final Integer code;

    private final String message;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AbstractRequestException(
            @JsonProperty("code") final Integer code,
            @JsonProperty("message") final String message) {

        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
