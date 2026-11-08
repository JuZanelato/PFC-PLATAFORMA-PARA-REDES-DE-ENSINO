package com.web.pfc.SpringPfc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefSenhaDTO(
        @NotBlank(message = "Token é obrigatório.") String token,

        @NotBlank(message = "Nova senha é obrigatória.") @Size(min = 6, message = "A nova senha deve ter ao menos 6 caracteres.") String novaSenha) {
}
