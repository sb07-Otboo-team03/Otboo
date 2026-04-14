package com.codeit.otboo.global.aop;

import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.domain.user.dto.request.UpdatePasswordRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Around("execution(public * com.codeit.otboo..service..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] args = safeArgs(joinPoint.getArgs());
        
        log.info("[SERVICE START] {}.{} args={}",
                className, methodName, args);

        try {
            Object result = joinPoint.proceed();

            long elapsed = System.currentTimeMillis() - start;

            log.info("[SERVICE END] {}.{} elapsed={}ms",
                    className, methodName, elapsed);

            return result;

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;

            log.warn("[SERVICE EXCEPTION] {}.{} elapsed={}ms message={}",
                    className, methodName, elapsed, e.getMessage());

            throw e;
        }
    }

    private Object[] safeArgs(Object[] args) {
        return Arrays.stream(args)
                .map(this::sanitizeArg)
                .toArray();
    }

    private Object sanitizeArg(Object arg) {
        if (arg == null) return null;

        // password
        if (arg instanceof SignInRequest req) {
            return "SignInRequest[username=%s, password=****]"
                    .formatted(req.username());
        }

        if (arg instanceof UpdatePasswordRequest req) {
            return "UpdatePasswordRequest[currentPassword=****, newPassword=****]";
        }

        // byte
        if (arg instanceof byte[] bytes) {
            return "byte[" + bytes.length + "]";
        }

        // 토큰
        if (arg instanceof String str) {
            if (str.startsWith("Bearer ")) {
                return "Bearer ****";
            }
        }

        return arg;
    }


}