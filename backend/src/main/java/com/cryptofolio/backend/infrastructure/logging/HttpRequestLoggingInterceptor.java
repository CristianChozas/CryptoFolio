package com.cryptofolio.backend.infrastructure.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "http.logging.start-time";
    private static final String MESSAGE_ATTRIBUTE = "http.logging.message";
    private static final String SOURCE_ATTRIBUTE = "http.logging.source";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
        long elapsedMs = startTime instanceof Long value ? System.currentTimeMillis() - value : -1L;

        int status = response.getStatus();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String message = (String) request.getAttribute(MESSAGE_ATTRIBUTE);
        String source = (String) request.getAttribute(SOURCE_ATTRIBUTE);

        String baseMessage = "%s%s%s %s %s (%d ms)".formatted(
                colorFor(status),
                status,
                RESET,
                method,
                path,
                elapsedMs);

        if (message != null && !message.isBlank()) {
            baseMessage += " - " + message;
        }

        if (source != null && !source.isBlank()) {
            baseMessage += " - " + source;
        }

        if (status >= 500) {
            log.error(baseMessage);
            return;
        }

        if (status >= 400) {
            log.warn(baseMessage);
            return;
        }

        log.info(baseMessage);
    }

    private String colorFor(int status) {
        if (status >= 500) {
            return RED;
        }

        if (status >= 400) {
            return YELLOW;
        }

        return GREEN;
    }
}
