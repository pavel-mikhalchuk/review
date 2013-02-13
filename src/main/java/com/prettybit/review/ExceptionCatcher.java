package com.prettybit.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Pavel Mikhalchuk
 */
public class ExceptionCatcher implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ExceptionCatcher.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("I will catch the exception!");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            log.error("I have caught the exception!", e);
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof ServletException) {
                throw (ServletException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                //This should never be hit
                throw new RuntimeException("Unexpected Exception", e);
            }
        }
    }

    @Override
    public void destroy() {
        log.info("I will catch the exception later!");
    }

}