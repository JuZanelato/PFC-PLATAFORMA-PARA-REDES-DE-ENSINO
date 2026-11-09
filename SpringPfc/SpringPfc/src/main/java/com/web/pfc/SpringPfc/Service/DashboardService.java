package com.web.pfc.SpringPfc.Service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIMITE_INSTITUICOES_NO_GRAFICO = 5;

    private final RecursoRep recursoRep;
    private final SolicitacaoRep solicitacaoRep;
    private final InstituicaoRep instituicaoRep;

    public DashboardRespDTO resumo(Usuario usuario) {
        Long minhaInstituicaoId = usuario.getInstituicao().getId();

        List<Recurso> recursosAtivos = recursoRep.findAll().stream()
                .filter(r -> r.getStatus() == StatusRecurso.ATIVO)
                .toList();
        List<Solicitacao> solicitacoes = solicitacaoRep.findAll();

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

        long pendentesParaMim = solicitacoes.stream()
                .filter(s -> s.getStatus() == StatusSolicitacao.PENDENTE)
                .filter(s -> Objects.equals(s.getInstituicaoDona().getId(), minhaInstituicaoId))
                .count();

        long minhasEmAndamento = solicitacoes.stream()
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

    private long contar(List<Solicitacao> solicitacoes, StatusSolicitacao status) {
        return solicitacoes.stream().filter(s -> s.getStatus() == status).count();
    }
}
