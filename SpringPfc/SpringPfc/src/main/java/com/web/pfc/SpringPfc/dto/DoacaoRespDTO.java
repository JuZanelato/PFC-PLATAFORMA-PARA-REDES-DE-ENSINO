package com.web.pfc.SpringPfc.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DoacaoRespDTO(
        Long id,
        BigDecimal valor,
        String mensagem,
        String doadorNome,
        String instituicaoDoadora,
        LocalDateTime dataHora) {
}