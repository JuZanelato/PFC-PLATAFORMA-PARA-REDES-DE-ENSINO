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

    public void registrarCriacaoInstituicao(Usuario usuario, Long instituicaoId) {
        salvar(usuario.getEmail(), "INSTITUICAO_CRIADA:" + instituicaoId);
    }

    public void registrarAlteracaoInstituicao(Usuario usuario, Long instituicaoId) {
        salvar(usuario.getEmail(), "INSTITUICAO_ALTERADA:" + instituicaoId);
    }

    public void registrarExclusaoInstituicao(Usuario usuario, Long instituicaoId) {
        salvar(usuario.getEmail(), "INSTITUICAO_EXCLUIDA:" + instituicaoId);
    }

    public void registrarAlteracaoPix(Usuario usuario, Long instituicaoId) {
        salvar(usuario.getEmail(), "PIX_ALTERADA:" + instituicaoId);
    }

    public void registrarRemocaoPix(Usuario usuario, Long instituicaoId) {
        salvar(usuario.getEmail(), "PIX_REMOVIDA:" + instituicaoId);
    }

    public void registrarCriacaoUsuario(String emailResponsavel, String emailCriado) {
        salvar(emailResponsavel, "USUARIO_CRIADO:" + emailCriado);
    }

    public void registrarInativacaoUsuario(Usuario responsavel, Long usuarioId) {
        salvar(responsavel.getEmail(), "USUARIO_INATIVADO:" + usuarioId);
    }

    public void registrarAcessoNegado(String email, String recurso) {
        salvar(email, "ACESSO_NEGADO:" + recurso);
    }

    private void salvar(String email, String acao) {
        LogAuditoria log = new LogAuditoria();
        log.setEmailUsuario(email);
        log.setAcao(acao);
        log.setDataHora(LocalDateTime.now());
        logAuditoriaRep.save(log);
    }
}