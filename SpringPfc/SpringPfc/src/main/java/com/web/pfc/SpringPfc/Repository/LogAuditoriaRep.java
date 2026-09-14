package com.web.pfc.SpringPfc.Repository;

import com.web.pfc.SpringPfc.domain.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaRep extends JpaRepository<LogAuditoria, Long> {
}