package com.web.pfc.SpringPfc.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "E-mail é obrigatório.") String email,
        @NotBlank(message = "Senha é obrigatória.") String senha) {
}
