package com.agileboot.admin.controller.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.agileboot.admin.customize.service.permission.sync.PermissionEndpointScanner;
import com.agileboot.admin.customize.service.permission.sync.PermissionSyncItemDTO;
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
    void testScanShouldGroupPermissionEndpointsAndAuditUnsupportedEndpoints() throws NoSuchMethodException {
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

        PermissionEndpointScanner.ScanSnapshot snapshot = scanner.scan();

        assertEquals(4, snapshot.scannedEndpointCount());
        assertEquals(1, snapshot.permissionEndpoints().size());
        PermissionEndpointScanner.ScannedPermissionEndpoint endpoint =
            snapshot.permissionEndpoints().get(0);
        assertEquals("system:role:edit", endpoint.getPermission());
        assertEquals("修改角色", endpoint.getMenuName());
        assertIterableEquals(
            java.util.List.of("POST", "PUT"),
            endpoint.getRequestMethods().stream().toList());
        assertIterableEquals(
            java.util.List.of("/system/role", "/system/role/{roleId}/status"),
            endpoint.getRequestPaths().stream().toList());
        assertIterableEquals(
            java.util.List.of("DemoSystemController#changeStatus", "DemoSystemController#edit"),
            endpoint.getHandlers().stream().toList());

        assertEquals(1, snapshot.conflicts().size());
        PermissionSyncItemDTO unsupported = snapshot.conflicts().get(0);
        assertEquals("unsupported", unsupported.getMenuName());
        assertEquals("DemoSystemController#unsupported", unsupported.getHandlers().get(0));

        assertEquals(1, snapshot.unprotected().size());
        PermissionSyncItemDTO unprotected = snapshot.unprotected().get(0);
        assertEquals("open", unprotected.getMenuName());
        assertEquals("DemoSystemController#open", unprotected.getHandlers().get(0));
        assertNotNull(unprotected.getDetails());
    }

    private RequestMappingInfo buildRequestMappingInfo(String path, RequestMethod method) {
        return RequestMappingInfo.paths(path).methods(method).build();
    }

    private HandlerMethod buildHandlerMethod(Object bean, String methodName, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        Method method = bean.getClass().getDeclaredMethod(methodName, parameterTypes);
        return new HandlerMethod(bean, method);
    }

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
