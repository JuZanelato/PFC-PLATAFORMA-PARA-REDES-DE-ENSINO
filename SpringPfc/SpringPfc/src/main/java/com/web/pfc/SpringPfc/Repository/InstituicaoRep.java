package com.web.pfc.SpringPfc.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.pfc.SpringPfc.domain.Instituicao;

@Repository
public interface InstituicaoRep extends JpaRepository<Instituicao, Integer> {

}