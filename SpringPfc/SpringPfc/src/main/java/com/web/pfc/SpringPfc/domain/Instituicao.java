package com.web.pfc.SpringPfc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @NotEmpty(message = "Obrigatório nome ser preenchido")
    @Column(name = "nome", length = 120)
    private String nome;

    @NotEmpty(message = "CNPJ obrigatório ser preenchido")
    @Column(name = "cnpj", length = 14)
    private String cnpj;


    @Column(name = "cep", length = 9)
    private String cep;

   
    @NotEmpty(message = "Número obrigatório ser preenchido")
    @Column(name = "numero", length = 10)
    private String numero;


    @NotEmpty(message = "Endereço obrigatório ser preenchido")
    @Column(name = "endereco", length = 150)
    private String endereco;

    @NotEmpty(message = "E-mail obrigatório ser preenchido")
    @Email(message = "E-mail inválido")
    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "status")
    private StatusInst status;

    @Column(name = "cadastrado_por", length = 150)
    private String cadastradoPor;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}