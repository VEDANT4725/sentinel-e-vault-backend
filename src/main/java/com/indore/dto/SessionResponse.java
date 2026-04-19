package com.indore.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Long id;
    private String device;
    private String ipAddress;
    private LocalDateTime loginTime;
    private boolean currentSession;
}