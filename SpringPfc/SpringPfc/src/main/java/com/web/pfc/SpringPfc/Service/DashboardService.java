package com.web.pfc.SpringPfc.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.domain.CategoriaRecurso;
import com.web.pfc.SpringPfc.domain.Recurso;
import com.web.pfc.SpringPfc.domain.Solicitacao;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.DashboardRespDTO;
import com.web.pfc.SpringPfc.dto.ItemGraficoDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIMITE_INSTITUICOES_NO_GRAFICO = 5;

    private final RecursoRep recursoRep;
    private final SolicitacaoRep solicitacaoRep;
    private final InstituicaoRep instituicaoRep;

    public DashboardRespDTO resumo(Usuario usuario) {
        return resumo(usuario, null, null, null, null);
    }

    public DashboardRespDTO resumo(Usuario usuario, Integer periodoDias, String escopo, String categoria,
            String visao) {
        Long minhaInstituicaoId = usuario.getInstituicao().getId();
        boolean soMinhaEscola = "MINHA".equalsIgnoreCase(escopo);
        CategoriaRecurso categoriaFiltro = converterCategoria(categoria);
        LocalDateTime desde = converterPeriodo(periodoDias);

        List<Recurso> recursosAtivos = recursoRep.findAll().stream()
                .filter(r -> r.getStatus() == StatusRecurso.ATIVO)
                .filter(r -> !soMinhaEscola || Objects.equals(r.getInstituicao().getId(), minhaInstituicaoId))
                .filter(r -> categoriaFiltro == null || r.getCategoria() == categoriaFiltro)
                .toList();

        List<Solicitacao> solicitacoesDoPeriodo = solicitacaoRep.findAll().stream()
                .filter(s -> desde == null || (s.getCriadaEm() != null && !s.getCriadaEm().isBefore(desde)))
                .filter(s -> categoriaFiltro == null || s.getRecurso().getCategoria() == categoriaFiltro)
                .toList();

        List<Solicitacao> solicitacoes = solicitacoesDoPeriodo.stream()
                .filter(s -> !soMinhaEscola || participa(s, minhaInstituicaoId, visao))
                .toList();

        long disponiveis = recursosAtivos.stream()
                .filter(r -> r.isDisponivel() && r.getQuantidade() != null && r.getQuantidade() > 0)
                .count();

        long unidadesEmprestadas = solicitacoes.stream()
                .filter(s -> s.getStatus() == StatusSolicitacao.APROVADA || s.getStatus() == StatusSolicitacao.RETIRADA)
                .mapToLong(Solicitacao::getQuantidade)
                .sum();

        long pendentes = contar(solicitacoes, StatusSolicitacao.PENDENTE);
        long concluidas = contar(solicitacoes, StatusSolicitacao.CONCLUIDA);

        long aprovadas = contar(solicitacoes, StatusSolicitacao.APROVADA)
                + contar(solicitacoes, StatusSolicitacao.RETIRADA)
                + concluidas;
        long respondidas = aprovadas + contar(solicitacoes, StatusSolicitacao.RECUSADA);
        int taxaAprovacao = respondidas == 0 ? 0 : (int) Math.round(aprovadas * 100.0 / respondidas);

        long instituicoesAtivas = instituicaoRep.findAll().stream()
                .filter(i -> i.getStatus() != StatusInst.INATIVA)
                .count();

        long pendentesParaMim = solicitacoesDoPeriodo.stream()
                .filter(s -> s.getStatus() == StatusSolicitacao.PENDENTE)
                .filter(s -> Objects.equals(s.getInstituicaoDona().getId(), minhaInstituicaoId))
                .count();

        long minhasEmAndamento = solicitacoesDoPeriodo.stream()
                .filter(s -> SolicitacaoService.EM_ANDAMENTO.contains(s.getStatus()))
                .filter(s -> Objects.equals(s.getInstituicaoSolicitante().getId(), minhaInstituicaoId))
                .count();

        List<ItemGraficoDTO> porCategoria = Arrays.stream(CategoriaRecurso.values())
                .map(c -> new ItemGraficoDTO(c.name(),
                        recursosAtivos.stream().filter(r -> r.getCategoria() == c).count()))
                .toList();

        List<ItemGraficoDTO> porStatus = Arrays.stream(StatusSolicitacao.values())
                .map(st -> new ItemGraficoDTO(st.name(), contar(solicitacoes, st)))
                .toList();

        Map<String, Long> porInstituicao = solicitacoes.stream()
                .filter(s -> s.getStatus() == StatusSolicitacao.APROVADA
                        || s.getStatus() == StatusSolicitacao.RETIRADA
                        || s.getStatus() == StatusSolicitacao.CONCLUIDA)
                .collect(Collectors.groupingBy(s -> s.getInstituicaoDona().getNome(), Collectors.counting()));
        List<ItemGraficoDTO> emprestimosPorInstituicao = porInstituicao.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(LIMITE_INSTITUICOES_NO_GRAFICO)
                .map(e -> new ItemGraficoDTO(e.getKey(), e.getValue()))
                .toList();

        return new DashboardRespDTO(
                recursosAtivos.size(),
                disponiveis,
                recursosAtivos.size() - disponiveis,
                unidadesEmprestadas,
                pendentes,
                concluidas,
                solicitacoes.size(),
                instituicoesAtivas,
                taxaAprovacao,
                pendentesParaMim,
                minhasEmAndamento,
                porCategoria,
                porStatus,
                emprestimosPorInstituicao);
    }

    private boolean participa(Solicitacao s, Long instituicaoId, String visao) {
        boolean recebida = Objects.equals(s.getInstituicaoDona().getId(), instituicaoId);
        boolean enviada = Objects.equals(s.getInstituicaoSolicitante().getId(), instituicaoId);
        if ("RECEBIDAS".equalsIgnoreCase(visao)) {
            return recebida;
        }
        if ("ENVIADAS".equalsIgnoreCase(visao)) {
            return enviada;
        }
        return recebida || enviada;
    }

    private CategoriaRecurso converterCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }
        try {
            return CategoriaRecurso.valueOf(categoria.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NegocioException("Categoria inválida.");
        }
    }

    private LocalDateTime converterPeriodo(Integer periodoDias) {
        if (periodoDias == null || periodoDias <= 0) {
            return null;
        }
        if (periodoDias > 3650) {
            throw new NegocioException("Período inválido.");
        }
        return LocalDate.now().minusDays(periodoDias - 1L).atStartOfDay();
    }

    private long contar(List<Solicitacao> solicitacoes, StatusSolicitacao status) {
        return solicitacoes.stream().filter(s -> s.getStatus() == status).count();
    }
}
