package com.example.lms.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    public String handleTypeMismatch(Exception e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        String base = (uri == null || uri.isBlank()) ? "/" : uri;
        String encoded = URLEncoder.encode("입력값 형식이 올바르지 않습니다.", StandardCharsets.UTF_8);
        return "redirect:" + base + "?message=" + encoded;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String handleBusinessException(Exception e, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String base = (referer == null || referer.isBlank()) ? "/" : referer;

        // 기존 message 파라미터 누적 방지
        base = base.replaceAll("([?&])message=[^&]*&?", "$1")
                .replaceAll("[?&]$", "");

        String raw = e.getMessage() == null ? "요청 처리 중 오류가 발생했습니다." : e.getMessage();
        String msg = raw.startsWith("Conversion") ? "입력값 형식이 올바르지 않습니다." : raw;

        String separator = base.contains("?") ? "&" : "?";
        String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "redirect:" + base + separator + "message=" + encoded;
    }
}
