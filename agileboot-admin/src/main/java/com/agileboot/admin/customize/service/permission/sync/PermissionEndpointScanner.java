package com.agileboot.admin.customize.service.permission.sync;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 扫描后台控制器中的权限点定义。
 */
@Service
public class PermissionEndpointScanner {

    private static final String SYSTEM_CONTROLLER_PACKAGE = "com.agileboot.admin.controller.system";
    private static final Pattern PERMISSION_EXPRESSION_PATTERN =
        Pattern.compile("@permission\\.has\\s*\\(\\s*(['\"])([^'\"]+)\\1\\s*\\)");

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public PermissionEndpointScanner(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    public ScanSnapshot scan() {
        Map<String, ScannedPermissionEndpoint> permissionEndpointMap = new LinkedHashMap<>();
        List<PermissionSyncItemDTO> conflicts = new ArrayList<>();
        List<PermissionSyncItemDTO> unprotected = new ArrayList<>();
        int scannedEndpointCount = 0;

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            Class<?> beanType = handlerMethod.getBeanType();
            if (!beanType.getPackageName().startsWith(SYSTEM_CONTROLLER_PACKAGE)) {
                continue;
            }
            scannedEndpointCount++;

            RequestMappingInfo requestMappingInfo = entry.getKey();
            Method method = handlerMethod.getMethod();
            String handler = beanType.getSimpleName() + "#" + method.getName();
            List<String> methods = sortRequestMethods(requestMappingInfo.getMethodsCondition().getMethods());
            List<String> paths = requestMappingInfo.getPatternValues().stream().sorted().toList();
            String summary = resolveSummary(method);

            PreAuthorize preAuthorize = AnnotatedElementUtils.findMergedAnnotation(method, PreAuthorize.class);
            if (preAuthorize == null) {
                unprotected.add(buildItem(null, summary, null, methods, paths, handler,
                    "缺少 @PreAuthorize，当前接口仅受登录校验控制"));
                continue;
            }

            Set<String> permissionCodes = extractPermissionCodes(preAuthorize.value());
            if (permissionCodes.size() != 1) {
                String details = permissionCodes.isEmpty()
                    ? "不支持的 @PreAuthorize 表达式，仅同步 @permission.has('code')"
                    : "同一个接口声明了多个权限码，无法自动同步";
                conflicts.add(buildItem(permissionCodes.isEmpty() ? null : String.join(", ", permissionCodes),
                    summary, null, methods, paths, handler, details));
                continue;
            }

            String permission = CollUtil.getFirst(permissionCodes);
            ScannedPermissionEndpoint endpoint = permissionEndpointMap.computeIfAbsent(permission, key ->
                ScannedPermissionEndpoint.builder()
                    .permission(key)
                    .menuName(summary)
                    .requestMethods(new LinkedHashSet<>())
                    .requestPaths(new LinkedHashSet<>())
                    .handlers(new LinkedHashSet<>())
                    .summaries(new LinkedHashSet<>())
                    .build());
            endpoint.getRequestMethods().addAll(methods);
            endpoint.getRequestPaths().addAll(paths);
            endpoint.getHandlers().add(handler);
            if (StrUtil.isNotBlank(summary)) {
                endpoint.getSummaries().add(summary);
                endpoint.setMenuName(selectMenuName(endpoint.getSummaries(), endpoint.getPermission()));
            }
        }

        List<ScannedPermissionEndpoint> endpoints = permissionEndpointMap.values().stream()
            .peek(this::normalizeEndpoint)
            .sorted(Comparator.comparing(ScannedPermissionEndpoint::getPermission))
            .toList();

        return new ScanSnapshot(scannedEndpointCount, endpoints, sortItems(conflicts), sortItems(unprotected));
    }

    private void normalizeEndpoint(ScannedPermissionEndpoint endpoint) {
        endpoint.setMenuName(selectMenuName(endpoint.getSummaries(), endpoint.getPermission()));
        endpoint.setRequestMethods(sortedSet(endpoint.getRequestMethods()));
        endpoint.setRequestPaths(sortedSet(endpoint.getRequestPaths()));
        endpoint.setHandlers(sortedSet(endpoint.getHandlers()));
    }

    private <T extends Comparable<T>> LinkedHashSet<T> sortedSet(Set<T> set) {
        return set.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
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

    private List<String> sortRequestMethods(Set<RequestMethod> requestMethods) {
        if (CollUtil.isEmpty(requestMethods)) {
            return List.of("ALL");
        }
        return requestMethods.stream().map(RequestMethod::name).sorted().toList();
    }

    private String selectMenuName(Set<String> summaries, String permission) {
        if (CollUtil.isEmpty(summaries)) {
            return permission;
        }
        return summaries.stream()
            .sorted(Comparator.comparingInt(String::length).thenComparing(String::compareTo))
            .findFirst()
            .orElse(permission);
    }

    private PermissionSyncItemDTO buildItem(String permission, String menuName, String parentPermission,
        List<String> requestMethods, List<String> requestPaths, String handler, String details) {
        return PermissionSyncItemDTO.builder()
            .permission(permission)
            .menuName(menuName)
            .parentPermission(parentPermission)
            .requestMethods(new ArrayList<>(requestMethods))
            .requestPaths(new ArrayList<>(requestPaths))
            .handlers(List.of(handler))
            .details(details)
            .build();
    }

    private List<PermissionSyncItemDTO> sortItems(List<PermissionSyncItemDTO> items) {
        return items.stream().sorted(PermissionSyncItemDTO.DEFAULT_COMPARATOR).toList();
    }

    public record ScanSnapshot(int scannedEndpointCount, List<ScannedPermissionEndpoint> permissionEndpoints,
        List<PermissionSyncItemDTO> conflicts, List<PermissionSyncItemDTO> unprotected) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScannedPermissionEndpoint {

        private String permission;

        private String menuName;

        @Builder.Default
        private Set<String> requestMethods = new LinkedHashSet<>();

        @Builder.Default
        private Set<String> requestPaths = new LinkedHashSet<>();

        @Builder.Default
        private Set<String> handlers = new LinkedHashSet<>();

        @Builder.Default
        private Set<String> summaries = new LinkedHashSet<>();

    }

}
