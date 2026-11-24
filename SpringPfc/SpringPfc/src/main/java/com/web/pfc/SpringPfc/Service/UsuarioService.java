package com.web.pfc.SpringPfc.Service;

import com.web.pfc.SpringPfc.Repository.LogAuditoriaRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.Repository.TokenSenhaRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.web.pfc.SpringPfc.dto.AtualizarMeusDadosDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRep usuarioRep;
    private final AuditoriaService auditoriaService;
    private final PasswordEncoder passwordEncoder;
    private final SolicitacaoRep solicitacaoRep;
    private final LogAuditoriaRep logAuditoriaRep;
    private final TokenSenhaRep tokenSenhaRep;

    // NOVO: domínio usado nos e-mails anonimizados (não existe, ninguém recebe nada nele)
    static final String DOMINIO_ANONIMIZADO = "@anonimizado.local";
    private static final List<StatusSolicitacao> EM_ANDAMENTO =
            List.of(StatusSolicitacao.PENDENTE, StatusSolicitacao.APROVADA, StatusSolicitacao.RETIRADA);

    public UsuarioRespDTO toRespDTO(Usuario usuario) {
        return new UsuarioRespDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name(),
                usuario.getInstituicao().getNome(), usuario.isAtivo(), usuario.isAguardandoAprovacao());
    }
    
    public List<UsuarioRespDTO> listarDaInstituicao(Usuario diretor) {
        return usuarioRep.findByInstituicao_IdOrderByNomeAsc(diretor.getInstituicao().getId())
                .stream().map(this::toRespDTO).toList();
    }

    public List<UsuarioRespDTO> listarPendentes(Usuario diretor) {
        return usuarioRep.findByInstituicao_IdAndAguardandoAprovacaoTrueOrderByCriadoEmAsc(diretor.getInstituicao().getId())
                .stream().map(this::toRespDTO).toList();
    }

    public void inativar(Long id, Usuario responsavel) {
        Usuario usuario = buscarDaMesmaInstituicao(id, responsavel);
        if (Objects.equals(usuario.getId(), responsavel.getId())) {
            throw new NegocioException("Para desativar a sua própria conta, use a tela Meus dados.");
        }
        if (!usuario.isAtivo()) {
            throw new NegocioException("Este usuário já está inativo.");
        }
        usuario.setAtivo(false);
        usuarioRep.save(usuario);
        auditoriaService.registrarInativacaoUsuario(responsavel, id);
    }

    @Transactional
    public UsuarioRespDTO reativar(Long id, Usuario diretor) {
        Usuario usuario = buscarDaMesmaInstituicao(id, diretor);
        if (usuario.getEmail().endsWith(DOMINIO_ANONIMIZADO)) {
            throw new NegocioException("Os dados deste usuário foram excluídos e a conta não pode ser reativada.");
        }
        if (usuario.isAguardandoAprovacao()) {
            throw new NegocioException("Este cadastro está aguardando aprovação. Use o botão Aprovar.");
        }
        if (usuario.isAtivo()) {
            throw new NegocioException("Este usuário já está ativo.");
        }
        usuario.setAtivo(true);
        usuarioRep.save(usuario);
        auditoriaService.registrarReativacaoUsuario(diretor, id);
        return toRespDTO(usuario);
    }

    @Transactional
    public void desativarMinhaConta(Usuario usuarioLogado, String senhaAtual) {
        Usuario usuario = usuarioRep.findById(usuarioLogado.getId())
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));

        if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new NegocioException("A senha atual está incorreta.");
        }
        exigirOutroDiretorAtivo(usuario, "desativar");

        usuario.setAtivo(false);
        usuarioRep.save(usuario);
        auditoriaService.registrarInativacaoUsuario(usuario, usuario.getId());
    }

    private void exigirOutroDiretorAtivo(Usuario usuario, String acao) {
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR
                && usuarioRep.countByInstituicao_IdAndPerfilAndAtivoTrue(
                        usuario.getInstituicao().getId(), Perfil.ADMINISTRADOR) <= 1) {
            throw new NegocioException("Você é o único diretor ativo desta escola. Para " + acao
                    + " a sua conta, cadastre antes outro diretor na tela Usuários.");
        }
    }
    private Usuario buscarDaMesmaInstituicao(Long id, Usuario diretor) {
        Usuario usuario = usuarioRep.findById(id)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));
        if (!Objects.equals(usuario.getInstituicao().getId(), diretor.getInstituicao().getId())) {
            throw new AccessDeniedException("Você só pode gerenciar usuários da sua instituição.");
        }
        return usuario;
    }

    @Transactional
    public UsuarioRespDTO aprovar(Long id, Usuario diretor) {
        Usuario usuario = buscarDaMesmaInstituicao(id, diretor);
        if (!usuario.isAguardandoAprovacao()) {
            throw new NegocioException("Este usuário não está aguardando aprovação.");
        }
        usuario.setAguardandoAprovacao(false);
        usuarioRep.save(usuario);
        auditoriaService.registrarAprovacaoUsuario(diretor, id);
        return toRespDTO(usuario);
    }

    @Transactional
    public void recusar(Long id, Usuario diretor) {
        Usuario usuario = buscarDaMesmaInstituicao(id, diretor);
        if (!usuario.isAguardandoAprovacao()) {
            throw new NegocioException("Este usuário não está aguardando aprovação.");
        }
        usuario.setAguardandoAprovacao(false);
        usuario.setAtivo(false);
        usuarioRep.save(usuario);
        auditoriaService.registrarRecusaUsuario(diretor, id);
    }

    @Transactional
    public UsuarioRespDTO atualizarMeusDados(Usuario usuarioLogado, AtualizarMeusDadosDTO dto) {
        Usuario usuario = usuarioRep.findById(usuarioLogado.getId())
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));

        usuario.setNome(dto.nome().trim());

        boolean trocouSenha = dto.novaSenha() != null && !dto.novaSenha().isBlank();
        if (trocouSenha) {
            if (dto.senhaAtual() == null || !passwordEncoder.matches(dto.senhaAtual(), usuario.getSenhaHash())) {
                throw new NegocioException("A senha atual está incorreta.");
            }
            if (dto.novaSenha().length() < 6) {
                throw new NegocioException("A nova senha deve ter ao menos 6 caracteres.");
            }
            usuario.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
        }

        usuarioRep.save(usuario);
        auditoriaService.registrarAlteracaoDadosProprios(usuario, trocouSenha);
        return toRespDTO(usuario);
    }

    @Transactional
    public void excluirMinhaConta(Usuario usuarioLogado, String senhaAtual) {
        Usuario usuario = usuarioRep.findById(usuarioLogado.getId())
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));

        if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new NegocioException("A senha atual está incorreta.");
        }
        exigirOutroDiretorAtivo(usuario, "excluir");

        String emailAnonimizado = anonimizar(usuario);
        auditoriaService.registrarAnonimizacaoUsuario(emailAnonimizado, usuario.getId());
    }

    
    @Transactional
    public void excluirDadosDeUsuario(Long id, Usuario diretor) {
        Usuario usuario = buscarDaMesmaInstituicao(id, diretor);
        if (Objects.equals(usuario.getId(), diretor.getId())) {
            throw new NegocioException("Para excluir a sua própria conta, use a tela Meus dados.");
        }

        anonimizar(usuario);
        auditoriaService.registrarAnonimizacaoUsuario(diretor.getEmail(), usuario.getId());
    }

    private String anonimizar(Usuario usuario) {
        if (usuario.getEmail().endsWith(DOMINIO_ANONIMIZADO)) {
            throw new NegocioException("Os dados deste usuário já foram excluídos.");
        }
        if (solicitacaoRep.existsBySolicitante_IdAndStatusIn(usuario.getId(), EM_ANDAMENTO)) {
            throw new NegocioException("Existem empréstimos em andamento (pendentes, aprovados ou retirados) "
                    + "feitos por este usuário. Conclua ou cancele esses empréstimos antes de excluir os dados.");
        }
        return aplicarAnonimizacao(usuario);
    }

    @Transactional
    public int anonimizarTodosDaInstituicao(Long instituicaoId) {
        int total = 0;
        for (Usuario usuario : usuarioRep.findByInstituicao_IdOrderByNomeAsc(instituicaoId)) {
            if (!usuario.getEmail().endsWith(DOMINIO_ANONIMIZADO)) {
                aplicarAnonimizacao(usuario);
                total++;
            }
        }
        return total;
    }

    private String aplicarAnonimizacao(Usuario usuario) {
        String emailAntigo = usuario.getEmail();
        String emailAnonimizado = "removido-" + usuario.getId() + DOMINIO_ANONIMIZADO;

        usuario.setNome("Usuário removido");
        usuario.setEmail(emailAnonimizado);
        usuario.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setAtivo(false);
        usuario.setAguardandoAprovacao(false);
        usuarioRep.save(usuario);

        logAuditoriaRep.anonimizarEmail(emailAntigo, emailAnonimizado);
        tokenSenhaRep.deleteByUsuario_Id(usuario.getId());
        return emailAnonimizado;
    }
}