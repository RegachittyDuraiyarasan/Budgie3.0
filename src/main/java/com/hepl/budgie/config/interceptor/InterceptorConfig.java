package com.hepl.budgie.config.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Component
public class InterceptorConfig implements WebMvcConfigurer {
    private final InterceptorServiceImplementation interceptorServiceImplementation;

    public InterceptorConfig(InterceptorServiceImplementation interceptorServiceImplementation) {
        this.interceptorServiceImplementation = interceptorServiceImplementation;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorServiceImplementation);
    }
}
