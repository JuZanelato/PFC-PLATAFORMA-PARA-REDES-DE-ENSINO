package com.web.pfc.SpringPfc.dto;

public record CadUsuarioDTO(
        String nome, String email, String senha, Long instituicaoId, String perfil) {
}
