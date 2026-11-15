package com.web.pfc.SpringPfc.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoCriarDTO(
        @NotNull(message = "Recurso é obrigatório.") Long recursoId,

        @NotNull(message = "Quantidade é obrigatória.") @Min(value = 1, message = "A quantidade deve ser de pelo menos 1 unidade.") @Max(value = 10000, message = "Quantidade acima do limite permitido.") Integer quantidade,

        @Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres.") String justificativa,

        @NotNull(message = "Informe a data prevista de devolução.") @FutureOrPresent(message = "A data prevista de devolução não pode estar no passado.") LocalDate dataPrevistaDevolucao) {
}