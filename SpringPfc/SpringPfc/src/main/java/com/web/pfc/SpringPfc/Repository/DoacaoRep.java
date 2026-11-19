package com.web.pfc.SpringPfc.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.web.pfc.SpringPfc.domain.Doacao;

public interface DoacaoRep extends JpaRepository<Doacao, Long> {

    List<Doacao> findByInstituicao_IdOrderByDataHoraDesc(Long instituicaoId);
}