package com.web.pfc.SpringPfc.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.ExcluirInstituicaoDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class InstituicaoExclusaoService {

    private static final List<StatusSolicitacao> EM_ANDAMENTO =
            List.of(StatusSolicitacao.PENDENTE, StatusSolicitacao.APROVADA, StatusSolicitacao.RETIRADA);

    private final InstituicaoRep instituicaoRep;
    private final UsuarioRep usuarioRep;
    private final RecursoRep recursoRep;
    private final SolicitacaoRep solicitacaoRep;
    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void excluirMinhaInstituicao(Usuario diretorLogado, ExcluirInstituicaoDTO dto) {
        Usuario diretor = usuarioRep.findById(diretorLogado.getId())
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));
        Instituicao instituicao = instituicaoRep.findById(diretor.getInstituicao().getId())
                .orElseThrow(() -> new NegocioException("Instituição não encontrada."));

        // 1. Confirmações: senha, nome e CNPJ da escola
        if (!passwordEncoder.matches(dto.senhaAtual(), diretor.getSenhaHash())) {
            throw new NegocioException("A senha atual está incorreta.");
        }
        if (!instituicao.getNome().trim().equalsIgnoreCase(dto.nomeConfirmacao().trim())) {
            throw new NegocioException("O nome digitado não confere com o nome da instituição.");
        }
        if (!instituicao.getCnpj().equals(dto.cnpjConfirmacao().replaceAll("\\D", ""))) {
            throw new NegocioException("O CNPJ digitado não confere com o CNPJ da instituição.");
        }

    
        if (instituicao.getStatus() == StatusInst.INATIVA) {
            throw new NegocioException("Esta instituição já foi excluída.");
        }
        if (solicitacaoRep.existsByInstituicaoDona_IdAndStatusIn(instituicao.getId(), EM_ANDAMENTO)
                || solicitacaoRep.existsByInstituicaoSolicitante_IdAndStatusIn(instituicao.getId(), EM_ANDAMENTO)) {
            throw new NegocioException("A instituição tem empréstimos em andamento (pendentes, aprovados ou "
                    + "retirados), recebidos ou enviados. Conclua, recuse ou cancele esses empréstimos antes de "
                    + "excluir a instituição.");
        }

        
        auditoriaService.registrarExclusaoInstituicao(diretor, instituicao.getId());

        
        instituicao.setStatus(StatusInst.INATIVA);
        instituicao.setChavePix(null);
        instituicaoRep.save(instituicao);

    
        recursoRep.findByInstituicaoId(instituicao.getId()).forEach(recurso -> {
            recurso.setStatus(StatusRecurso.INATIVO);
            recurso.setDisponivel(false);
            recursoRep.save(recurso);
        });

        
        usuarioService.anonimizarTodosDaInstituicao(instituicao.getId());
    }
}