package com.web.pfc.SpringPfc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicSenhaDTO(
        @NotBlank(message = "E-mail é obrigatório.") @Email(message = "E-mail inválido.") String email) {
}
