package com.factorylink.infrastructure.sse;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Data
@AllArgsConstructor
public class SseUserConnection {

    private Long userId;

    private String username;

    private SseEmitter emitter;
}
