package com.hepl.budgie.config.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.service.bruteforce.IpBlockingService;
import com.hepl.budgie.utils.AppMessages;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class InterceptorServiceImplementation implements HandlerInterceptor {
    private static final int TOO_MANY_REQUESTS_STATUS_CODE = 429; // HTTP status code for "Too Many Requests"
    private final IpBlockingService ipBlockingService;
    private final Translator translator;

    public InterceptorServiceImplementation(IpBlockingService ipBlockingService, Translator translator) {
        this.ipBlockingService = ipBlockingService;
        this.translator = translator;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String ip = request.getRemoteAddr(); // Get the IP address of the client

        if (ipBlockingService.isIpBlocked(ip)) {
            log.info("IP {} is blocked.", ip);

            response.setStatus(TOO_MANY_REQUESTS_STATUS_CODE);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            GenericResponse<String> errorResponse = GenericResponse.error(
                    String.valueOf(TOO_MANY_REQUESTS_STATUS_CODE), translator.toLocale(AppMessages.IP_BLOCKED));

            // Convert the GenericResponse object to JSON and write it to the response
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

            return false;
        }

        // Record the request and block the IP if necessary
        if (ipBlockingService.recordRequest(ip)) {
            log.info("IP {} is now blocked due to too many requests.", ip);

            response.setStatus(TOO_MANY_REQUESTS_STATUS_CODE);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            GenericResponse<String> errorResponse = GenericResponse.error(
                    String.valueOf(TOO_MANY_REQUESTS_STATUS_CODE),
                    translator.toLocale(AppMessages.TOO_MANY_REQUEST));

            // Convert the GenericResponse object to JSON and write it to the response
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

            return false;
        }

        log.info("IP {} is allowed to proceed.", ip);
        return true;
    }

}
