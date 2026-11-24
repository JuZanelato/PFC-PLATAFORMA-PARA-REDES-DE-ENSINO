package com.web.pfc.SpringPfc.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.Usuario;

@SpringBootTest
@Transactional
class UsuarioRepIntegrationTest {

    @Autowired
    UsuarioRep usuarioRep;
    @Autowired
    InstituicaoRep instituicaoRep;

    @Test
    void deveEncontrarOEmailSemDiferenciarMaiusculasEMinusculas() {
        Instituicao escola = new Instituicao();
        escola.setNome("Escola A");
        escola.setCnpj("33333333000133");
        escola.setNumero("1");
        escola.setEndereco("Rua Teste, 1");
        escola.setEmail("contato@escolaa.com");
        escola.setStatus(StatusInst.ATIVA);
        instituicaoRep.save(escola);

        Usuario ana = new Usuario();
        ana.setNome("Ana");
        ana.setEmail("ana@escola.com");
        ana.setSenhaHash("hash");
        ana.setPerfil(Perfil.FUNCIONARIO);
        ana.setInstituicao(escola);
        ana.setCriadoEm(LocalDateTime.now());
        usuarioRep.save(ana);

        assertTrue(usuarioRep.findByEmail("ANA@Escola.com").isPresent());
        assertEquals("ana@escola.com", usuarioRep.findByEmail(" Ana@ESCOLA.COM ").get().getEmail());
        assertTrue(usuarioRep.existsByEmail("Ana@Escola.Com"));
        assertFalse(usuarioRep.existsByEmail("outra@escola.com"));
    }
}
