package com.web.pfc.SpringPfc.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web.pfc.SpringPfc.Service.DashboardService;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.DashboardRespDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public DashboardRespDTO resumo(@AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam(required = false) Integer periodo,
            @RequestParam(required = false) String escopo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String visao) {
        return dashboardService.resumo(usuarioLogado, periodo, escopo, categoria, visao);
    }
}
