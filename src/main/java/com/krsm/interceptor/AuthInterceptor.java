package com.krsm.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);

        // Check if userRole exists in session
        if (session != null && session.getAttribute("userRole") != null) {
            return true; // User is logged in, allow request
        }

        // User not logged in, redirect to login
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
