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
@Table(name = "solicitacao")
@Data
@NoArgsConstructor
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_solicitante_id", nullable = false)
    private Instituicao instituicaoSolicitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_dona_id", nullable = false)
    private Instituicao instituicaoDona;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "justificativa", length = 500)
    private String justificativa;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusSolicitacao status;

    @Column(name = "respondida_por", length = 150)
    private String respondidaPor;

    @Column(name = "criada_em")
    private LocalDateTime criadaEm;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    @Column(name = "retirada_em")
    private LocalDateTime retiradaEm;

    @Column(name = "devolvida_em")
    private LocalDateTime devolvidaEm;
}
