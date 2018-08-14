package com.times6.timeTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static com.times6.timeTracker.service.OidcHeaderFilter.USER_ID_HEADER_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OidcHeaderFilterTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private ContainerRequestContext requestContext;

    @Captor
    private ArgumentCaptor<Object> userIdAttributeCaptor;

    @Test
    public void automaticallyUsesUserIdIfPresentInRequest() {
        String userId = "user@example.com";

        OidcHeaderFilter filter = new OidcHeaderFilter(null);
        filter.setRequest(request);

        when(requestContext.getHeaderString(USER_ID_HEADER_NAME)).thenReturn(userId);

        filter.filter(requestContext);

        verify(requestContext).getHeaderString(any(String.class));
        verify(request).setAttribute(eq("userId"), userIdAttributeCaptor.capture());
        Object userIdAttribute = userIdAttributeCaptor.getValue();
        assertThat(userIdAttribute, notNullValue());
        assertThat(userIdAttribute, instanceOf(String.class));
        assertThat(userIdAttribute, equalTo(userId));
    }

    @Test
    public void usesDefaultIdIfInDevelopmentAndDefaultIdSpecified() {
        String userId = "asdf@example.com";

        ServiceConfig config = new ServiceConfig();
        config.setDefaultUserId(userId);
        config.setDevelopment(true);

        OidcHeaderFilter filter = new OidcHeaderFilter(config);
        filter.setRequest(request);

        when(requestContext.getHeaderString(USER_ID_HEADER_NAME)).thenReturn(null);

        filter.filter(requestContext);

        verify(requestContext).getHeaderString(any(String.class));
        verify(request).setAttribute(eq("userId"), userIdAttributeCaptor.capture());
        Object userIdAttribute = userIdAttributeCaptor.getValue();
        assertThat(userIdAttribute, notNullValue());
        assertThat(userIdAttribute, instanceOf(String.class));
        assertThat(userIdAttribute, equalTo(userId));
    }

    @Test
    public void rejectsRequestIfNotInDevelopment() {
        ServiceConfig config = new ServiceConfig();
        config.setDefaultUserId("aebhrtgbsb");
        config.setDevelopment(false);

        OidcHeaderFilter filter = new OidcHeaderFilter(config);
        filter.setRequest(request);

        when(requestContext.getHeaderString(USER_ID_HEADER_NAME)).thenReturn(null);

        filter.filter(requestContext);

        verify(requestContext).getHeaderString(any(String.class));
        verify(request, never()).setAttribute(any(String.class), any(Object.class));
        ArgumentCaptor<Response> responseArgumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(responseArgumentCaptor.capture());
        Response response = responseArgumentCaptor.getValue();
        assertThat(response.getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
    }
}
