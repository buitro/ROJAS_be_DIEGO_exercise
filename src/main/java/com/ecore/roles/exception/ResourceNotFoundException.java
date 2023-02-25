package com.ecore.roles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

import static java.lang.String.format;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public <T> ResourceNotFoundException(Class<T> resource, UUID id) {
        super(format("%s %s not found", resource.getSimpleName(), id));
    }

    public <T> ResourceNotFoundException(Class<T> resource, UUID id1, UUID id2) {
        super(format("%s %s %s not found", resource.getSimpleName(), id1, id2));
    }
}
