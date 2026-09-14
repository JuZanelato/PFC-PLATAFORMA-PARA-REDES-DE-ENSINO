package com.web.pfc.SpringPfc.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_auditoria")
@Data
@NoArgsConstructor
public class LogAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emailUsuario;
    private String acao;
    private LocalDateTime dataHora;
}