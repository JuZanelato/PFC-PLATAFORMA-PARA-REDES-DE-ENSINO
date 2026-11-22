package com.web.pfc.SpringPfc.dto;

import jakarta.validation.constraints.NotBlank;

public record ExcluirInstituicaoDTO(
        @NotBlank(message = "Informe a sua senha para confirmar a exclusão.") String senhaAtual,
        @NotBlank(message = "Digite o nome da instituição para confirmar.") String nomeConfirmacao,
        @NotBlank(message = "Digite o CNPJ da instituição para confirmar.") String cnpjConfirmacao) {
}