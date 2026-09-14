package com.web.pfc.SpringPfc.Service;

import com.web.pfc.SpringPfc.Repository.LogAuditoriaRep;
import com.web.pfc.SpringPfc.domain.LogAuditoria;
import com.web.pfc.SpringPfc.domain.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRep logAuditoriaRep;

    public void registrarLogin(Usuario usuario) {
        salvar(usuario.getEmail(), "LOGIN_SUCESSO");
    }

    public void registrarTentativaLogin(String email, boolean sucesso) {
        salvar(email, sucesso ? "LOGIN_SUCESSO" : "LOGIN_FALHA");
    }

    public void registrarLogout(Usuario usuario) {
        salvar(usuario.getEmail(), "LOGOUT");
    }

    private void salvar(String email, String acao) {
        LogAuditoria log = new LogAuditoria();
        log.setEmailUsuario(email);
        log.setAcao(acao);
        log.setDataHora(LocalDateTime.now());
        logAuditoriaRep.save(log);
    }
}