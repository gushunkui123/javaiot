package com.agileboot.admin.controller.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.agileboot.admin.customize.service.permission.sync.PermissionEndpointScanner;
import com.agileboot.admin.testsupport.ExternalControllerStub;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class PermissionEndpointScannerTest {

    @Test
    void testScanShouldReturnUniquePermissionsWithMenuName() throws NoSuchMethodException {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        PermissionEndpointScanner scanner = new PermissionEndpointScanner(handlerMapping);

        DemoSystemController controller = new DemoSystemController();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(buildRequestMappingInfo("/system/role", RequestMethod.POST),
            buildHandlerMethod(controller, "edit"));
        handlerMethods.put(buildRequestMappingInfo("/system/role/{roleId}/status", RequestMethod.PUT),
            buildHandlerMethod(controller, "changeStatus", Long.class));
        handlerMethods.put(buildRequestMappingInfo("/system/role/authority", RequestMethod.GET),
            buildHandlerMethod(controller, "unsupported"));
        handlerMethods.put(buildRequestMappingInfo("/system/role/open", RequestMethod.GET),
            buildHandlerMethod(controller, "open"));
        when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        Map<String, String> result = scanner.scan();

        // 只有 system:role:edit 能被提取（edit 和 changeStatus 共享同一权限码）
        // unsupported 使用 hasAuthority 表达式 → 不支持，跳过
        // open 没有 @PreAuthorize → 跳过
        assertEquals(1, result.size());
        assertTrue(result.containsKey("system:role:edit"));
        assertEquals("修改角色", result.get("system:role:edit"));
    }

    @Test
    void testScanShouldSkipMultiplePermissionCodes() throws NoSuchMethodException {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        PermissionEndpointScanner scanner = new PermissionEndpointScanner(handlerMapping);

        DemoSystemController controller = new DemoSystemController();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(buildRequestMappingInfo("/system/role/authority", RequestMethod.GET),
            buildHandlerMethod(controller, "unsupported"));
        when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        Map<String, String> result = scanner.scan();

        // hasAuthority 表达式无法提取权限码
        assertTrue(result.isEmpty());
    }

    @Test
    void testScanShouldReturnEmptyForNoControllers() {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        PermissionEndpointScanner scanner = new PermissionEndpointScanner(handlerMapping);
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of());

        Map<String, String> result = scanner.scan();

        assertTrue(result.isEmpty());
    }

    @Test
    void testScanShouldIgnoreNonControllerPackage() throws NoSuchMethodException {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        PermissionEndpointScanner scanner = new PermissionEndpointScanner(handlerMapping);

        ExternalControllerStub externalController = new ExternalControllerStub();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(buildRequestMappingInfo("/external/test", RequestMethod.GET),
            buildHandlerMethod(externalController, "test"));
        when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        Map<String, String> result = scanner.scan();

        // ExternalController 不在 com.agileboot.admin.controller 包下 → 跳过
        assertFalse(result.containsKey("external:test:list"));
    }

    private RequestMappingInfo buildRequestMappingInfo(String path, RequestMethod method) {
        return RequestMappingInfo.paths(path).methods(method).build();
    }

    private HandlerMethod buildHandlerMethod(Object bean, String methodName, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        Method method = bean.getClass().getDeclaredMethod(methodName, parameterTypes);
        return new HandlerMethod(bean, method);
    }

    /**
     * 模拟 com.agileboot.admin.controller 包下的控制器。
     * 当前测试类所在包是 com.agileboot.admin.controller.system，满足扫描前缀，因此内部类会被扫描。
     */
    private static class DemoSystemController {

        @Operation(summary = "修改角色")
        @PreAuthorize("@permission.has('system:role:edit')")
        @PostMapping("/system/role")
        public void edit() {
        }

        @Operation(summary = "修改角色状态")
        @PreAuthorize("@permission.has('system:role:edit') AND @dataScope.checkDeptId(#roleId)")
        @PutMapping("/system/role/{roleId}/status")
        public void changeStatus(@PathVariable Long roleId) {
        }

        @Operation(summary = "unsupported")
        @PreAuthorize("hasAuthority('annie')")
        @GetMapping("/system/role/authority")
        public void unsupported() {
        }

        @Operation(summary = "open")
        @GetMapping("/system/role/open")
        public void open() {
        }

    }
}
