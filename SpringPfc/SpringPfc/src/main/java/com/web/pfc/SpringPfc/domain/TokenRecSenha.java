package com.web.pfc.SpringPfc.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_recuperacao_senha")
@Data
@NoArgsConstructor
public class TokenRecSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @Column(unique = true, nullable = false)
    private String token;

    private LocalDateTime expiraEm;
    private boolean utilizado = false;
}