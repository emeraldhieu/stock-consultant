package com.emeraldhieu.exception;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RestExceptionMapperTest {

    @InjectMocks
    private final RestExceptionMapper restExceptionMapper = new RestExceptionMapper();

    @Mock
    private ResourceInfo resourceInfo;

    @Before
    public void setUp() {
        // Init mock resource info.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testThatRestExceptionMapperDoesNotProvideAStackTrace() {
        // GIVEN
        String errorMessage = "This is a test-exception.";
        Exception exception = new Exception(errorMessage);

        // WHEN
        Response response = restExceptionMapper.toResponse(exception);

        // THEN
        assertErrorResponse(response, INTERNAL_SERVER_ERROR, errorMessage, "unknown");
    }

    @Test
    public void notFoundException() {
        // GIVEN
        String errorMessage = "Not found";
        NotFoundException exception = new NotFoundException(errorMessage);

        // WHEN
        Response response = restExceptionMapper.toResponse(exception);

        // THEN
        assertErrorResponse(response, NOT_FOUND, errorMessage, "notFound");
    }

    @Test
    public void methodNotAllowedException() {
        // GIVEN
        String errorMessage = "Method not allowed";
        NotAllowedException exception = new NotAllowedException(errorMessage, "GET", new String[]{"POST"});

        // WHEN
        Response response = restExceptionMapper.toResponse(exception);

        // THEN
        assertErrorResponse(response, METHOD_NOT_ALLOWED, errorMessage, "methodNotAllowed");
    }

    @Test
    public void notFoundWhenNoResourceMethodCalled() {
        // GIVEN
        when(resourceInfo.getResourceMethod()).thenReturn(null);
        String errorMessage = "Not found";
        NotFoundException exception = new NotFoundException(errorMessage);

        // WHEN
        Response response = restExceptionMapper.toResponse(exception);

        // THEN
        assertErrorResponse(response, NOT_FOUND, errorMessage, "notFound");
    }

    @Test
    public void trimJaxRsInformation() {
        // GIVEN
        String errorMessage = "RESTEASY003210: Could not find resource for full path: http://somethingNotFound";
        NotFoundException exception = new NotFoundException(errorMessage);

        // WHEN
        Response response = restExceptionMapper.toResponse(exception);

        // THEN
        assertErrorResponse(response, NOT_FOUND, "Could not find resource for full path: http://somethingNotFound", "notFound");
    }

    private void assertErrorResponse(Response response, Response.Status expectedStatusCode, String expectedErrorMessage, String expectedErrorCode) {
        assertEquals(expectedStatusCode, response.getStatusInfo());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof RestError);
        RestError restError = (RestError) response.getEntity();
        assertNotNull(restError.getErrorContainer().getCode());
        assertEquals(expectedErrorCode, restError.getErrorContainer().getCode());
        assertEquals(expectedErrorMessage, restError.getErrorContainer().getMessage());
    }
}
