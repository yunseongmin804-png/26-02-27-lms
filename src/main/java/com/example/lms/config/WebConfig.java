package com.example.lms.config;

import com.example.lms.common.web.AdminGuardInterceptor;
import com.example.lms.common.web.AuthInterceptor;
import com.example.lms.common.web.ManagerGuardInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final ManagerGuardInterceptor managerGuardInterceptor;
    private final AdminGuardInterceptor adminGuardInterceptor;

    public WebConfig(AuthInterceptor authInterceptor,
                     ManagerGuardInterceptor managerGuardInterceptor,
                     AdminGuardInterceptor adminGuardInterceptor) {
        this.authInterceptor = authInterceptor;
        this.managerGuardInterceptor = managerGuardInterceptor;
        this.adminGuardInterceptor = adminGuardInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/error",
                        "/favicon.ico",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                );

        registry.addInterceptor(managerGuardInterceptor)
                .addPathPatterns(
                        "/courses/new", "/courses/*/edit",
                        "/assignments/manage", "/assignments/submissions/*/grade",
                        "/notices/*/edit",
                        "/qna/answer"
                );

        registry.addInterceptor(adminGuardInterceptor)
                .addPathPatterns(
                        "/users/new",
                        "/admin/**"
                );
    }
}
