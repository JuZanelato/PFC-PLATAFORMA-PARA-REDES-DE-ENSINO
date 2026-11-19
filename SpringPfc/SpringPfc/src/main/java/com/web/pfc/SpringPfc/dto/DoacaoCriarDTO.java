package com.web.pfc.SpringPfc.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoacaoCriarDTO(
        @NotNull(message = "Informe o valor doado.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        @DecimalMax(value = "1000000.00", message = "O valor máximo é R$ 1.000.000,00.")
        BigDecimal valor,

        @Size(max = 300, message = "A mensagem deve ter no máximo 300 caracteres.")
        String mensagem) {
}