package com.web.pfc.SpringPfc.domain;

import com.faster.jackson.annotation.jsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.noArgsConstructor;
import jakarta.persistence.column;
import jakarta.persistence.entity;
import jakarta.persistence.generatedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.id;
import jakarta.persistence.table;
import jakarta.validation.constraints.notEmpty;

@entity
@table (name= "instituicao")
@noArgsConstructor
@AllArgsConstructor
@Data

public class instituicao{
    @id
    @generatedValue(strategy=GenerationType.Identity)
    @column(name="id")
    private Integer id;

    @notEmpty(message = "Obrigatório nome ser preenchido")
    @column(name="nome", length = 120)
    private String nome;

    @notEmpty(message = "CNPJ obrigatório ser preenchido")
    @column(name="cnpj", length=14)
    private String cnpj;

    @notEmpty(message = "Endereço obrigatório ser preenchido")
    @column(name="endereco", length=150)
    private String endereco;

    @column (name= "Status:")
    private statusInst status;
}


