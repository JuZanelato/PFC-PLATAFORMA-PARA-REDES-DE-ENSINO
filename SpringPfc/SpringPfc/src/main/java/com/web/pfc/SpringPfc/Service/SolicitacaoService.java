package com.web.pfc.SpringPfc.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.pfc.SpringPfc.Repository.MovimentacaoSolicitacaoRep;
import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.MovimentacaoSolicitacao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.Recurso;
import com.web.pfc.SpringPfc.domain.Solicitacao;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.MovimentacaoRespDTO;
import com.web.pfc.SpringPfc.dto.SolicitacaoCriarDTO;
import com.web.pfc.SpringPfc.dto.SolicitacaoRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    public static final List<StatusSolicitacao> EM_ANDAMENTO = List.of(
            StatusSolicitacao.PENDENTE, StatusSolicitacao.APROVADA, StatusSolicitacao.RETIRADA);

    private final SolicitacaoRep solicitacaoRep;
    private final MovimentacaoSolicitacaoRep movimentacaoRep;
    private final RecursoRep recursoRep;
    private final AuditoriaService auditoriaService;

    @Transactional
    public SolicitacaoRespDTO criar(SolicitacaoCriarDTO dto, Usuario usuario) {
        Recurso recurso = recursoRep.findById(dto.recursoId())
                .orElseThrow(() -> new NegocioException("Recurso não encontrado."));

        Instituicao minhaInstituicao = usuario.getInstituicao();
        if (mesmaInstituicao(recurso.getInstituicao(), minhaInstituicao)) {
            throw new NegocioException("Não é possível solicitar um recurso da sua própria instituição.");
        }
        if (recurso.getInstituicao().getStatus() == StatusInst.INATIVA) {
            throw new NegocioException("A instituição dona deste recurso está inativa.");
        }

        validarRecursoDisponivel(recurso, dto.quantidade());

        if (solicitacaoRep.existsByRecurso_IdAndInstituicaoSolicitante_IdAndStatusIn(
                recurso.getId(), minhaInstituicao.getId(), EM_ANDAMENTO)) {
            throw new NegocioException("Sua instituição já possui uma solicitação em andamento para este recurso.");
        }

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setRecurso(recurso);
        solicitacao.setSolicitante(usuario);
        solicitacao.setInstituicaoSolicitante(minhaInstituicao);
        solicitacao.setInstituicaoDona(recurso.getInstituicao());
        solicitacao.setQuantidade(dto.quantidade());
        solicitacao.setJustificativa(textoOuNulo(dto.justificativa()));
        solicitacao.setStatus(StatusSolicitacao.PENDENTE);
        solicitacao.setCriadaEm(LocalDateTime.now());

        Solicitacao salva = solicitacaoRep.save(solicitacao);
        registrarMovimentacao(salva, null, usuario, "Solicitação enviada");
        auditoriaService.registrarSolicitacao(usuario, salva.getId(), "CRIADA");
        return toDTO(salva, usuario);
    }

    @Transactional
    public SolicitacaoRespDTO aprovar(Long id, Usuario usuario) {
        Solicitacao solicitacao = buscarOuFalhar(id);
        exigirResponsavelDaDona(solicitacao, usuario);
        exigirStatus(solicitacao, StatusSolicitacao.PENDENTE, "Somente solicitações pendentes podem ser aprovadas.");

        Recurso recurso = solicitacao.getRecurso();
        validarRecursoDisponivel(recurso, solicitacao.getQuantidade());
        recurso.setQuantidade(recurso.getQuantidade() - solicitacao.getQuantidade());
        if (recurso.getQuantidade() == 0) {
            recurso.setDisponivel(false);
        }
        recursoRep.save(recurso);

        StatusSolicitacao anterior = solicitacao.getStatus();
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        solicitacao.setRespondidaEm(LocalDateTime.now());
        solicitacao.setRespondidaPor(usuario.getEmail());
        Solicitacao salva = solicitacaoRep.save(solicitacao);

        registrarMovimentacao(salva, anterior, usuario, "Solicitação aprovada");
        auditoriaService.registrarSolicitacao(usuario, id, "APROVADA");
        return toDTO(salva, usuario);
    }

    @Transactional
    public SolicitacaoRespDTO recusar(Long id, String motivo, Usuario usuario) {
        Solicitacao solicitacao = buscarOuFalhar(id);
        exigirResponsavelDaDona(solicitacao, usuario);
        exigirStatus(solicitacao, StatusSolicitacao.PENDENTE, "Somente solicitações pendentes podem ser recusadas.");

        StatusSolicitacao anterior = solicitacao.getStatus();
        solicitacao.setStatus(StatusSolicitacao.RECUSADA);
        solicitacao.setMotivoRecusa(textoOuNulo(motivo));
        solicitacao.setRespondidaEm(LocalDateTime.now());
        solicitacao.setRespondidaPor(usuario.getEmail());
        Solicitacao salva = solicitacaoRep.save(solicitacao);

        registrarMovimentacao(salva, anterior, usuario,
                salva.getMotivoRecusa() != null ? "Recusada: " + salva.getMotivoRecusa() : "Solicitação recusada");
        auditoriaService.registrarSolicitacao(usuario, id, "RECUSADA");
        return toDTO(salva, usuario);
    }

    @Transactional
    public SolicitacaoRespDTO registrarRetirada(Long id, Usuario usuario) {
        Solicitacao solicitacao = buscarOuFalhar(id);
        exigirResponsavelDaDona(solicitacao, usuario);
        exigirStatus(solicitacao, StatusSolicitacao.APROVADA, "A retirada só pode ser registrada após a aprovação.");

        StatusSolicitacao anterior = solicitacao.getStatus();
        solicitacao.setStatus(StatusSolicitacao.RETIRADA);
        solicitacao.setRetiradaEm(LocalDateTime.now());
        Solicitacao salva = solicitacaoRep.save(solicitacao);

        registrarMovimentacao(salva, anterior, usuario, "Retirada do recurso registrada");
        auditoriaService.registrarSolicitacao(usuario, id, "RETIRADA");
        return toDTO(salva, usuario);
    }

    @Transactional
    public SolicitacaoRespDTO registrarDevolucao(Long id, Usuario usuario) {
        Solicitacao solicitacao = buscarOuFalhar(id);
        exigirResponsavelDaDona(solicitacao, usuario);
        exigirStatus(solicitacao, StatusSolicitacao.RETIRADA, "A devolução só pode ser registrada após a retirada.");

        Recurso recurso = solicitacao.getRecurso();
        recurso.setQuantidade(recurso.getQuantidade() + solicitacao.getQuantidade());
        if (recurso.getStatus() == StatusRecurso.ATIVO) {
            recurso.setDisponivel(true);
        }
        recursoRep.save(recurso);

        StatusSolicitacao anterior = solicitacao.getStatus();
        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
        solicitacao.setDevolvidaEm(LocalDateTime.now());
        Solicitacao salva = solicitacaoRep.save(solicitacao);

        registrarMovimentacao(salva, anterior, usuario, "Devolução registrada — solicitação concluída");
        auditoriaService.registrarSolicitacao(usuario, id, "CONCLUIDA");
        return toDTO(salva, usuario);
    }

    @Transactional
    public SolicitacaoRespDTO cancelar(Long id, Usuario usuario) {
        Solicitacao solicitacao = buscarOuFalhar(id);

        boolean ehOProprioSolicitante = Objects.equals(solicitacao.getSolicitante().getId(), usuario.getId());
        boolean ehGestorDaSolicitante = mesmaInstituicao(solicitacao.getInstituicaoSolicitante(),
                usuario.getInstituicao())
                && ehGestorOuAdmin(usuario);
        if (!ehOProprioSolicitante && !ehGestorDaSolicitante) {
            throw new AccessDeniedException("Somente quem fez a solicitação pode cancelá-la.");
        }
        exigirStatus(solicitacao, StatusSolicitacao.PENDENTE, "Somente solicitações pendentes podem ser canceladas.");

        StatusSolicitacao anterior = solicitacao.getStatus();
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        Solicitacao salva = solicitacaoRep.save(solicitacao);

        registrarMovimentacao(salva, anterior, usuario, "Solicitação cancelada pelo solicitante");
        auditoriaService.registrarSolicitacao(usuario, id, "CANCELADA");
        return toDTO(salva, usuario);
    }

    public List<SolicitacaoRespDTO> listarEnviadas(Usuario usuario, StatusSolicitacao status) {
        return solicitacaoRep.findByInstituicaoSolicitante_IdOrderByCriadaEmDesc(usuario.getInstituicao().getId())
                .stream()
                .filter(s -> status == null || s.getStatus() == status)
                .map(s -> toDTO(s, usuario))
                .toList();
    }

    public List<SolicitacaoRespDTO> listarRecebidas(Usuario usuario, StatusSolicitacao status) {
        return solicitacaoRep.findByInstituicaoDona_IdOrderByCriadaEmDesc(usuario.getInstituicao().getId())
                .stream()
                .filter(s -> status == null || s.getStatus() == status)
                .map(s -> toDTO(s, usuario))
                .toList();
    }

    public SolicitacaoRespDTO buscar(Long id, Usuario usuario) {
        Solicitacao solicitacao = buscarOuFalhar(id);
        exigirParticipante(solicitacao, usuario);
        return toDTO(solicitacao, usuario);
    }

    public List<MovimentacaoRespDTO> historico(Long id, Usuario usuario) {
        Solicitacao solicitacao = buscarOuFalhar(id);
        exigirParticipante(solicitacao, usuario);
        return movimentacaoRep.findBySolicitacao_IdOrderByDataHoraAsc(id).stream()
                .map(m -> toDTO(m, usuario))
                .toList();
    }

    public List<MovimentacaoRespDTO> movimentacoesDaUnidade(Usuario usuario) {
        return movimentacaoRep.findDaInstituicao(usuario.getInstituicao().getId()).stream()
                .map(m -> toDTO(m, usuario))
                .toList();
    }

    private void validarRecursoDisponivel(Recurso recurso, Integer quantidadeDesejada) {
        if (recurso.getStatus() == StatusRecurso.INATIVO || !recurso.isDisponivel()
                || recurso.getQuantidade() == null || recurso.getQuantidade() <= 0) {
            throw new NegocioException("Recurso indisponível não pode gerar nova solicitação (RN02).");
        }
        if (quantidadeDesejada > recurso.getQuantidade()) {
            throw new NegocioException("Quantidade solicitada (" + quantidadeDesejada
                    + ") é maior que a disponível (" + recurso.getQuantidade() + ").");
        }
    }

    private Solicitacao buscarOuFalhar(Long id) {
        return solicitacaoRep.findById(id)
                .orElseThrow(() -> new NegocioException("Solicitação não encontrada."));
    }

    private void exigirResponsavelDaDona(Solicitacao solicitacao, Usuario usuario) {
        if (!ehGestorOuAdmin(usuario)
                || !mesmaInstituicao(solicitacao.getInstituicaoDona(), usuario.getInstituicao())) {
            throw new AccessDeniedException(
                    "Apenas o gestor ou administrador da instituição dona do recurso pode executar esta ação.");
        }
    }

    private void exigirParticipante(Solicitacao solicitacao, Usuario usuario) {
        Instituicao minha = usuario.getInstituicao();
        if (!mesmaInstituicao(solicitacao.getInstituicaoDona(), minha)
                && !mesmaInstituicao(solicitacao.getInstituicaoSolicitante(), minha)) {
            throw new AccessDeniedException("Você não participa desta solicitação.");
        }
    }

    private void exigirStatus(Solicitacao solicitacao, StatusSolicitacao esperado, String mensagem) {
        if (solicitacao.getStatus() != esperado) {
            throw new NegocioException(mensagem);
        }
    }

    private boolean ehGestorOuAdmin(Usuario usuario) {
        return usuario.getPerfil() == Perfil.GESTOR || usuario.getPerfil() == Perfil.ADMINISTRADOR;
    }

    private boolean mesmaInstituicao(Instituicao a, Instituicao b) {
        return a != null && b != null && Objects.equals(a.getId(), b.getId());
    }

    private String textoOuNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private void registrarMovimentacao(Solicitacao solicitacao, StatusSolicitacao anterior,
            Usuario usuario, String observacao) {
        MovimentacaoSolicitacao mov = new MovimentacaoSolicitacao();
        mov.setSolicitacao(solicitacao);
        mov.setStatusAnterior(anterior);
        mov.setStatusNovo(solicitacao.getStatus());
        mov.setUsuario(usuario);
        mov.setObservacao(observacao);
        mov.setDataHora(LocalDateTime.now());
        movimentacaoRep.save(mov);
    }

    private SolicitacaoRespDTO toDTO(Solicitacao s, Usuario quemConsulta) {
        boolean souDaSolicitante = mesmaInstituicao(s.getInstituicaoSolicitante(), quemConsulta.getInstituicao());
        Recurso recurso = s.getRecurso();
        return new SolicitacaoRespDTO(
                s.getId(),
                recurso.getId(),
                recurso.getNome(),
                recurso.getCategoria() != null ? recurso.getCategoria().name() : null,
                s.getQuantidade(),
                s.getJustificativa(),
                s.getMotivoRecusa(),
                s.getStatus().name(),
                souDaSolicitante ? s.getSolicitante().getNome() : null,
                s.getInstituicaoSolicitante().getId(),
                s.getInstituicaoSolicitante().getNome(),
                s.getInstituicaoDona().getId(),
                s.getInstituicaoDona().getNome(),
                s.getCriadaEm(),
                s.getRespondidaEm(),
                s.getRetiradaEm(),
                s.getDevolvidaEm());
    }

    private MovimentacaoRespDTO toDTO(MovimentacaoSolicitacao m, Usuario quemConsulta) {
        Usuario responsavel = m.getUsuario();
        Instituicao instResponsavel = responsavel.getInstituicao();
        boolean mesmaUnidade = mesmaInstituicao(instResponsavel, quemConsulta.getInstituicao());
        return new MovimentacaoRespDTO(
                m.getId(),
                m.getSolicitacao().getId(),
                m.getSolicitacao().getRecurso().getNome(),
                m.getStatusAnterior() != null ? m.getStatusAnterior().name() : null,
                m.getStatusNovo().name(),
                mesmaUnidade ? responsavel.getNome() : instResponsavel.getNome(),
                instResponsavel.getNome(),
                m.getObservacao(),
                m.getDataHora());
    }
}
