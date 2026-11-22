package com.web.pfc.SpringPfc.Repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.web.pfc.SpringPfc.domain.Solicitacao;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;

public interface SolicitacaoRep extends JpaRepository<Solicitacao, Long> {

    List<Solicitacao> findByInstituicaoSolicitante_IdOrderByCriadaEmDesc(Long instituicaoId);

    List<Solicitacao> findByInstituicaoDona_IdOrderByCriadaEmDesc(Long instituicaoId);

    boolean existsByRecurso_IdAndInstituicaoSolicitante_IdAndStatusIn(
            Long recursoId, Long instituicaoId, Collection<StatusSolicitacao> status);

    boolean existsByRecurso_IdAndStatusIn(Long recursoId, Collection<StatusSolicitacao> status);

    boolean existsBySolicitante_IdAndStatusIn(Long usuarioId, Collection<StatusSolicitacao> status);

        // NOVO: a escola tem empréstimos em andamento, como dona do recurso ou como solicitante?
    boolean existsByInstituicaoDona_IdAndStatusIn(Long instituicaoId, Collection<StatusSolicitacao> status);

    boolean existsByInstituicaoSolicitante_IdAndStatusIn(Long instituicaoId, Collection<StatusSolicitacao> status);
}