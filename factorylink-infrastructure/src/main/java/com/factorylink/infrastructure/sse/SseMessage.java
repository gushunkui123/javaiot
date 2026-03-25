package com.factorylink.infrastructure.sse;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SseMessage {

    private SseMessageLevel level;

    private String title;

    private String content;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
