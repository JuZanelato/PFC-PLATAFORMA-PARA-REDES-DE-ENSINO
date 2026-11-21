package com.web.pfc.SpringPfc.dto;

import jakarta.validation.constraints.NotBlank;

public record ExcluirContaDTO(
        @NotBlank(message = "Informe a sua senha para confirmar a exclusão.") String senhaAtual) {
}