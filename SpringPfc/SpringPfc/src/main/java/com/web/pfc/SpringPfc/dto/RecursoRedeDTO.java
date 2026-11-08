package com.web.pfc.SpringPfc.dto;

public record RecursoRedeDTO(
        Long id,
        String nome,
        String categoria,
        Integer quantidade,
        String condicao,
        Long instituicaoId,
        String instituicaoNome) {
}
