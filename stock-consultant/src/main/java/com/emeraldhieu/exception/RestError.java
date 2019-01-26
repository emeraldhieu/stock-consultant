package com.emeraldhieu.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class RestError {
    @JsonProperty("error")
    private final RestError.ErrorContainer errorContainer;

    public RestError() {
        this.errorContainer = new RestError.ErrorContainer();
    }

    public RestError(String code, String message) {
        this.errorContainer = new RestError.ErrorContainer(code, message);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorContainer {
        private String code;
        private String message;
    }
}
