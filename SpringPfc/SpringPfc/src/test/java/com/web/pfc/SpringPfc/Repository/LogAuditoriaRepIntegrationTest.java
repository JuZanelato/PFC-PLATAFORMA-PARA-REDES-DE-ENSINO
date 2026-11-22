package com.web.pfc.SpringPfc.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.LogAuditoria;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.Usuario;

@SpringBootTest
@Transactional
class LogAuditoriaRepIntegrationTest {

    @Autowired
    LogAuditoriaRep logAuditoriaRep;
    @Autowired
    InstituicaoRep instituicaoRep;
    @Autowired
    UsuarioRep usuarioRep;

    private Instituicao escola(String nome, String cnpj) {
        Instituicao i = new Instituicao();
        i.setNome(nome);
        i.setCnpj(cnpj);
        i.setNumero("1");
        i.setEndereco("Rua Teste, 1");
        i.setEmail(cnpj + "@escola.edu.br");
        i.setStatus(StatusInst.ATIVA);
        return instituicaoRep.save(i);
    }

    private void usuario(String email, Instituicao instituicao) {
        Usuario u = new Usuario();
        u.setNome("Usuário " + email);
        u.setEmail(email);
        u.setSenhaHash("hash");
        u.setPerfil(Perfil.FUNCIONARIO);
        u.setInstituicao(instituicao);
        u.setCriadoEm(LocalDateTime.now());
        usuarioRep.save(u);
    }

    private void log(String email, String acao) {
        LogAuditoria l = new LogAuditoria();
        l.setEmailUsuario(email);
        l.setAcao(acao);
        l.setDataHora(LocalDateTime.now());
        logAuditoriaRep.save(l);
    }

    @Test
    void deveTrazerSoOsRegistrosDosUsuariosDaInstituicao() {

        Instituicao minha = escola("Escola A", "11111111000111");
        Instituicao outra = escola("Escola B", "22222222000122");
        usuario("ana@escolaa.com", minha);
        usuario("bruno@escolab.com", outra);

        log("ana@escolaa.com", "LOGIN_SUCESSO");
        log("ana@escolaa.com", "LOGOUT");
        log("bruno@escolab.com", "LOGIN_SUCESSO");

        Page<LogAuditoria> resultado = logAuditoriaRep.buscarDaInstituicao(minha.getId(), PageRequest.of(0, 20));

        assertEquals(2, resultado.getTotalElements());
        resultado.forEach(l -> assertEquals("ana@escolaa.com", l.getEmailUsuario()));
    }
}