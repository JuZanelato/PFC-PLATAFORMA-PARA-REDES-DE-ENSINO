package com.web.pfc.SpringPfc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name= "instituicao")
@NoArgsConstructor
@AllArgsConstructor
@Data

public class Instituicao{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }

    @NotEmpty(message = "Obrigatório nome ser preenchido")
    @Column(name="nome", length = 120)
    private String nome;

    @NotEmpty(message = "CNPJ obrigatório ser preenchido")
    @Column(name="cnpj", length=14)
    private String cnpj;

    @NotEmpty(message = "Endereço obrigatório ser preenchido")
    @Column(name="endereco", length=150)
    private String endereco;

    @Column (name= "Status:")
    private StatusInst status;
}


