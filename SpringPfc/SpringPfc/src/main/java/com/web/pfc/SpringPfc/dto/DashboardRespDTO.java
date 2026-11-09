package com.web.pfc.SpringPfc.dto;

import java.util.List;

public record DashboardRespDTO(

        long totalRecursos,
        long recursosDisponiveis,
        long recursosIndisponiveis,
        long unidadesEmprestadas,
        long solicitacoesPendentes,
        long solicitacoesConcluidas,
        long totalSolicitacoes,
        long instituicoesAtivas,

        int taxaAprovacao,

        long pendentesParaMinhaAprovacao,
        long minhasSolicitacoesEmAndamento,

        List<ItemGraficoDTO> recursosPorCategoria,
        List<ItemGraficoDTO> solicitacoesPorStatus,
        List<ItemGraficoDTO> emprestimosPorInstituicao) {
}
