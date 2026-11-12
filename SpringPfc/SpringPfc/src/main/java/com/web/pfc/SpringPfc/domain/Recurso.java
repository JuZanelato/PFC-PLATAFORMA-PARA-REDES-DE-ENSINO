package com.web.pfc.SpringPfc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recurso")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "Nome do recurso é obrigatório")
    @Column(name = "nome", length = 120)
    private String nome;

    @NotNull(message = "Categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private CategoriaRecurso categoria;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    @Column(name = "quantidade")
    private Integer quantidade;

    @NotNull(message = "Condição é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "condicao")
    private CondicaoRecurso condicao;

    @Column(name = "disponivel")
    private boolean disponivel = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusRecurso status = StatusRecurso.ATIVO;

    @ManyToOne
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;

    @Column(name = "cadastrado_por", length = 150)
    private String cadastradoPor;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}