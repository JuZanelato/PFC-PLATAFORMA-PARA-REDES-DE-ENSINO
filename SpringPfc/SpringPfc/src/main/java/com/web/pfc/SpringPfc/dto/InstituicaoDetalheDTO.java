package com.web.pfc.SpringPfc.dto;

import java.util.List;

public record InstituicaoDetalheDTO(
        Long id,
        String nome,
        String endereco,
        String numero,
        String cep,
        String email,
        String chavePix,
        boolean minhaInstituicao,
        List<UsuarioDaEscola> usuarios,
        List<RecursoDaEscola> recursos) {

    public record UsuarioDaEscola(String nome, String perfil) {
    }

    public record RecursoDaEscola(Long id, String nome, String categoria, Integer quantidade,
            String condicao, boolean disponivel) {
    }
}