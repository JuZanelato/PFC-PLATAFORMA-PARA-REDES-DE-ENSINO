package com.web.pfc.SpringPfc.Repository;

import com.web.pfc.SpringPfc.domain.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogAuditoriaRep extends JpaRepository<LogAuditoria, Long> {
    Page<LogAuditoria> findAllByOrderByDataHoraDesc(Pageable pageable);

    @Query(value = "SELECT l FROM LogAuditoria l WHERE l.emailUsuario IN "
            + "(SELECT u.email FROM Usuario u WHERE u.instituicao.id = :instituicaoId) "
            + "ORDER BY l.dataHora DESC",
            countQuery = "SELECT COUNT(l) FROM LogAuditoria l WHERE l.emailUsuario IN "
            + "(SELECT u.email FROM Usuario u WHERE u.instituicao.id = :instituicaoId)")
    Page<LogAuditoria> buscarDaInstituicao(@Param("instituicaoId") Long instituicaoId, Pageable pageable);
}