package com.web.pfc.SpringPfc.domain;

import java.time.LocalDateTime;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimentacao_solicitacao")
@Data
@NoArgsConstructor
public class MovimentacaoSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusSolicitacao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", length = 20, nullable = false)
    private StatusSolicitacao statusNovo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "observacao", length = 500)
    private String observacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;
}
