package com.codeit.otboo.global.async;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class ContextCopyingDecorator implements TaskDecorator {
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        SecurityContext context = SecurityContextHolder.getContext();
        SecurityContext copiedContext = SecurityContextHolder.createEmptyContext();
        copiedContext.setAuthentication(context.getAuthentication());

        Map<String, String> mdc = MDC.getCopyOfContextMap();

        return () -> {
            try {
                SecurityContextHolder.setContext(copiedContext);
                if (mdc != null) {
                    MDC.setContextMap(mdc);
                }
                runnable.run();
            } finally {
                SecurityContextHolder.clearContext();
                MDC.clear();
            }
        };
    }
}
