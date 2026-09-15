package com.web.pfc.SpringPfc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instituicao")
@NoArgsConstructor
@AllArgsConstructor
@Data

public class Instituicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @NotEmpty(message = "Obrigatório nome ser preenchido")
    @Column(name = "nome", length = 120)
    private String nome;

    @NotEmpty(message = "CNPJ obrigatório ser preenchido")
    @Column(name = "cnpj", length = 14)
    private String cnpj;

    @NotEmpty(message = "Endereço obrigatório ser preenchido")
    @Column(name = "endereco", length = 150)
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusInst status;

    @Column(name = "chave_pix", length = 120)
    private String chavePix;
}
