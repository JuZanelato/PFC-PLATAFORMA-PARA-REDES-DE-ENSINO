package com.web.pfc.SpringPfc.dto;

import jakarta.validation.constraints.Size;

public record RecusarSolicitacaoDTO(
                @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres.") String motivo) {
}
