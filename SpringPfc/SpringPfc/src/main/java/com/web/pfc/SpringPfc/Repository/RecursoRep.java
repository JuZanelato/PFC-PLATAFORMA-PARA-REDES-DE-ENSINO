package com.web.pfc.SpringPfc.Repository;

import com.web.pfc.SpringPfc.domain.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecursoRep extends JpaRepository<Recurso, Long> {
    List<Recurso> findByInstituicaoId(Long instituicaoId);
}
