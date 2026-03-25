package com.factorylink.admin.controller.business;

import com.factorylink.common.core.base.BaseController;
import com.factorylink.common.core.dto.ResponseDTO;
import com.factorylink.infrastructure.sse.SseConnectionManager;
import com.factorylink.infrastructure.user.AuthenticationUtils;
import com.factorylink.infrastructure.user.web.SystemLoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE实时推送API", description = "Server-Sent Events连接管理")
@RestController
@RequestMapping("/business/sse")
@RequiredArgsConstructor
public class BizSseController extends BaseController {

    private final SseConnectionManager sseConnectionManager;

    @Operation(summary = "建立SSE连接", description = "建立SSE长连接，用于接收服务端实时推送消息。"
            + " 单用户单连接，新连接会替换旧连接。连接超时时间30分钟")
    @PreAuthorize("@permission.has('business:sse:connect')")
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        return sseConnectionManager.createConnection(loginUser.getUserId(), loginUser.getUsername());
    }

    @Operation(summary = "断开SSE连接", description = "主动断开当前用户的SSE连接")
    @PreAuthorize("@permission.has('business:sse:connect')")
    @DeleteMapping("/disconnect")
    public ResponseDTO<Void> disconnect() {
        Long userId = AuthenticationUtils.getUserId();
        sseConnectionManager.removeConnection(userId);
        return ResponseDTO.ok();
    }
}
