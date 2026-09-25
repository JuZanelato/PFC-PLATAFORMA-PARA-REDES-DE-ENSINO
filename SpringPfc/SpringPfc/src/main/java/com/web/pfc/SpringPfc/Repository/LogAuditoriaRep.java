package com.web.pfc.SpringPfc.Repository;

import com.web.pfc.SpringPfc.domain.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaRep extends JpaRepository<LogAuditoria, Long> {
    Page<LogAuditoria> findAllByOrderByDataHoraDesc(Pageable pageable);
}