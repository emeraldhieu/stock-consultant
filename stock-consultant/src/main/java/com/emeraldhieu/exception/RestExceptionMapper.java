package com.emeraldhieu.exception;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.ReaderException;
import org.jboss.resteasy.spi.WriterException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class RestExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    private ResourceInfo resourceInfo;

    public Response toResponse(Throwable exception) {
        Response.StatusType status = Status.INTERNAL_SERVER_ERROR;
        String errorCode = "unknown";
        String errorMessage = exception.getMessage();
        if (exception instanceof WebApplicationException) {
            if (exception instanceof NotAuthorizedException) {
                errorCode = "notAuthorized";
            } else if (exception instanceof BadRequestException) {
                errorCode = "badRequest";
            } else if (exception instanceof NotAllowedException) {
                errorCode = "methodNotAllowed";
            } else if (exception instanceof NotAcceptableException) {
                errorCode = "notAcceptable";
            } else if (exception instanceof NotFoundException) {
                errorCode = "notFound";
            } else if (exception instanceof ReaderException) {
                errorCode = "readerError";
            } else if (exception instanceof WriterException) {
                errorCode = "writerError";
            }

            if (errorMessage.startsWith("RESTEASY")) {
                errorMessage = StringUtils.substringAfter(errorMessage, ": ");
            }

            Response response = ((WebApplicationException) exception).getResponse();
            status = response.getStatusInfo();
            if (exception instanceof NotAcceptableException || this.resourceInfo.getResourceMethod() == null) {
                return Response.status(status).type("application/json").entity(new RestError(errorCode, errorMessage)).build();
            }
        }

        log.error("Exception thrown executing REST endpoint, returning {}", status.getStatusCode(), exception);
        return Response.status(status).entity(new RestError(errorCode, errorMessage)).build();
    }
}