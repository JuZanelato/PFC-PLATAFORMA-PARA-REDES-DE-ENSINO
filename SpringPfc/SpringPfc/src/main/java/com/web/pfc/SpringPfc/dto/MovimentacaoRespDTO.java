package com.web.pfc.SpringPfc.dto;

import java.time.LocalDateTime;

public record MovimentacaoRespDTO(
        Long id,
        Long solicitacaoId,
        String recursoNome,
        String statusAnterior,
        String statusNovo,
        String responsavel,
        String instituicaoResponsavel,
        String observacao,
        LocalDateTime dataHora) {
}
