package com.web.pfc.SpringPfc.Controller;

import com.web.pfc.SpringPfc.Repository.LogAuditoriaRep;
import com.web.pfc.SpringPfc.dto.LogAuditoriaRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final LogAuditoriaRep logAuditoriaRep;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<LogAuditoriaRespDTO>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {

        Page<LogAuditoriaRespDTO> logs = logAuditoriaRep
                .findAllByOrderByDataHoraDesc(PageRequest.of(pagina, tamanho))
                .map(log -> new LogAuditoriaRespDTO(log.getId(), log.getEmailUsuario(), log.getAcao(), log.getDataHora()));

        return ResponseEntity.ok(logs);
    }
}