package com.agileboot.admin.customize.service.permission.sync;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 扫描控制器中 @PreAuthorize 注解声明的权限标识。
 */
@Service
public class PermissionEndpointScanner {

    private static final String CONTROLLER_PACKAGE = "com.agileboot.admin.controller";
    private static final Pattern PERMISSION_EXPRESSION_PATTERN =
        Pattern.compile("@permission\\.has\\s*\\(\\s*(['\"])([^'\"]+)\\1\\s*\\)");

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public PermissionEndpointScanner(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    /**
     * 扫描所有控制器，提取唯一的权限标识及其菜单名。
     * 同一个权限码被多个接口使用时，取最短的 summary 作为菜单名。
     */
    public Map<String, String> scan() {
        // permission -> summaries（用于选取最短的作为菜单名）
        Map<String, Set<String>> permissionSummaries = new LinkedHashMap<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry
            : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {

            HandlerMethod handlerMethod = entry.getValue();
            Class<?> beanType = handlerMethod.getBeanType();
            if (!beanType.getPackageName().startsWith(CONTROLLER_PACKAGE)) {
                continue;
            }

            Method method = handlerMethod.getMethod();
            PreAuthorize preAuthorize = AnnotatedElementUtils.findMergedAnnotation(method, PreAuthorize.class);
            if (preAuthorize == null) {
                continue;
            }

            Set<String> codes = extractPermissionCodes(preAuthorize.value());
            if (codes.size() != 1) {
                continue;
            }

            String permission = CollUtil.getFirst(codes);
            String summary = resolveSummary(method);
            permissionSummaries.computeIfAbsent(permission, k -> new LinkedHashSet<>()).add(summary);
        }

        return permissionSummaries.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> selectMenuName(e.getValue(), e.getKey()),
                (a, b) -> a,
                LinkedHashMap::new
            ));
    }

    private String resolveSummary(Method method) {
        Operation operation = AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);
        if (operation == null || StrUtil.isBlank(operation.summary())) {
            return method.getName();
        }
        return operation.summary().trim();
    }

    private Set<String> extractPermissionCodes(String expression) {
        Set<String> permissions = new LinkedHashSet<>();
        if (StrUtil.isBlank(expression)) {
            return permissions;
        }
        Matcher matcher = PERMISSION_EXPRESSION_PATTERN.matcher(expression);
        while (matcher.find()) {
            permissions.add(StrUtil.trim(matcher.group(2)));
        }
        return permissions;
    }

    private String selectMenuName(Set<String> summaries, String permission) {
        if (CollUtil.isEmpty(summaries)) {
            return permission;
        }
        return summaries.stream()
            .sorted((a, b) -> a.length() != b.length() ? a.length() - b.length() : a.compareTo(b))
            .findFirst()
            .orElse(permission);
    }

}
