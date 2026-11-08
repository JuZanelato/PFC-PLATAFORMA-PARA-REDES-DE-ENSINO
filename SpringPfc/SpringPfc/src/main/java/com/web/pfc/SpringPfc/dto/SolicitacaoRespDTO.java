package com.web.pfc.SpringPfc.dto;

import java.time.LocalDateTime;

public record SolicitacaoRespDTO(
        Long id,
        Long recursoId,
        String recursoNome,
        String recursoCategoria,
        Integer quantidade,
        String justificativa,
        String motivoRecusa,
        String status,
        String solicitanteNome,
        Long instituicaoSolicitanteId,
        String instituicaoSolicitanteNome,
        Long instituicaoDonaId,
        String instituicaoDonaNome,
        LocalDateTime criadaEm,
        LocalDateTime respondidaEm,
        LocalDateTime retiradaEm,
        LocalDateTime devolvidaEm) {
}
