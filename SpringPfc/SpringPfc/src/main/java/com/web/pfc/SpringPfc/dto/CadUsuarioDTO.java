package com.web.pfc.SpringPfc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadUsuarioDTO(
                @NotBlank(message = "Nome é obrigatório.") @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.") String nome,

                @NotBlank(message = "E-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,

                @NotBlank(message = "Senha é obrigatória.") @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres.") String senha,

                @NotNull(message = "Instituição é obrigatória (RN01).") Long instituicaoId,

                String perfil) {
}
