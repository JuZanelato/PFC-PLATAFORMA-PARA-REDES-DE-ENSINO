package com.web.pfc.SpringPfc.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.web.pfc.SpringPfc.domain.MovimentacaoSolicitacao;

public interface MovimentacaoSolicitacaoRep extends JpaRepository<MovimentacaoSolicitacao, Long> {

    List<MovimentacaoSolicitacao> findBySolicitacao_IdOrderByDataHoraAsc(Long solicitacaoId);

    @Query("""
            select m from MovimentacaoSolicitacao m
            where m.solicitacao.instituicaoSolicitante.id = :instituicaoId
               or m.solicitacao.instituicaoDona.id = :instituicaoId
            order by m.dataHora desc
            """)
    List<MovimentacaoSolicitacao> findDaInstituicao(@Param("instituicaoId") Long instituicaoId);
}
