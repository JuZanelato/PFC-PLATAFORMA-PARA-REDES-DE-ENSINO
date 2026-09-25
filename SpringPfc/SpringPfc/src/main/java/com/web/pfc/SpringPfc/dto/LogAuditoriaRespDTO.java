package com.web.pfc.SpringPfc.dto;

import java.time.LocalDateTime;

public record LogAuditoriaRespDTO(
    Long id,
    String emailUsuario,
    String acao,
    LocalDateTime dataHora
) {}