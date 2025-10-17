package com.github.kzhunmax.jobsearch.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_HEADER;
import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @Test
    void doFilterInternal_withRequestIdHeader_setsMdcAndCallsChain() throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        when(request.getHeader(REQUEST_ID_HEADER)).thenReturn(requestId);

        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class))  {
            loggingFilter.doFilterInternal(request, response, filterChain);

            mdcMock.verify(() -> MDC.put(eq(REQUEST_ID_MDC_KEY), eq(requestId)));
            verify(filterChain).doFilter(any(), any());
            mdcMock.verify(() -> MDC.remove(eq(REQUEST_ID_MDC_KEY)));
        }
    }

    @Test
    void doFilterInternal_withoutRequestIdHeader_generatesNewIdAndSetsMdc() throws ServletException, IOException {
        when(request.getHeader(REQUEST_ID_HEADER)).thenReturn(null);
        UUID generatedUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class);
             MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            uuidMock.when(UUID::randomUUID).thenReturn(generatedUuid);

            loggingFilter.doFilterInternal(request, response, filterChain);

            mdcMock.verify(() -> MDC.put(eq(REQUEST_ID_MDC_KEY), eq(generatedUuid.toString())));
            verify(filterChain).doFilter(any(), any());
            mdcMock.verify(() -> MDC.remove(eq(REQUEST_ID_MDC_KEY)));
        }
    }

    @Test
    void doFilterInternal_emptyRequestIdHeader_generatesNewId() throws ServletException, IOException {
        when(request.getHeader(REQUEST_ID_HEADER)).thenReturn("");
        UUID generatedUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class);
             MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            uuidMock.when(UUID::randomUUID).thenReturn(generatedUuid);

            loggingFilter.doFilterInternal(request, response, filterChain);

            mdcMock.verify(() -> MDC.put(eq(REQUEST_ID_MDC_KEY), eq(generatedUuid.toString())));
            verify(filterChain).doFilter(any(), any());
            mdcMock.verify(() -> MDC.remove(eq(REQUEST_ID_MDC_KEY)));
        }
    }
}
