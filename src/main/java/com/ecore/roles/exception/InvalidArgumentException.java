package com.ecore.roles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static java.lang.String.format;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidArgumentException extends RuntimeException {

    public <T> InvalidArgumentException(Class<T> resource) {
        super(format("Invalid '%s' object", resource.getSimpleName()));
    }

    public <T> InvalidArgumentException(Class<T> resource, String message) {
        super(format("Invalid '%s' object. %s", resource.getSimpleName(), message));
    }
}
