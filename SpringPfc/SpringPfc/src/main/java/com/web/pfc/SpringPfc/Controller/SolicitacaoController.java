package com.web.pfc.SpringPfc.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.web.pfc.SpringPfc.Service.SolicitacaoService;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.MovimentacaoRespDTO;
import com.web.pfc.SpringPfc.dto.RecusarSolicitacaoDTO;
import com.web.pfc.SpringPfc.dto.SolicitacaoCriarDTO;
import com.web.pfc.SpringPfc.dto.SolicitacaoRespDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoRespDTO criar(@RequestBody @Valid SolicitacaoCriarDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.criar(dto, usuarioLogado);
    }

    @GetMapping("/enviadas")
    public List<SolicitacaoRespDTO> enviadas(@RequestParam(required = false) StatusSolicitacao status,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.listarEnviadas(usuarioLogado, status);
    }

    @GetMapping("/recebidas")
    public List<SolicitacaoRespDTO> recebidas(@RequestParam(required = false) StatusSolicitacao status,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.listarRecebidas(usuarioLogado, status);
    }

    @GetMapping("/movimentacoes")
    public List<MovimentacaoRespDTO> movimentacoes(@AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.movimentacoesDaUnidade(usuarioLogado);
    }

    @GetMapping("/{id}")
    public SolicitacaoRespDTO buscar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.buscar(id, usuarioLogado);
    }

    @GetMapping("/{id}/historico")
    public List<MovimentacaoRespDTO> historico(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.historico(id, usuarioLogado);
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public SolicitacaoRespDTO aprovar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.aprovar(id, usuarioLogado);
    }

    @PatchMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public SolicitacaoRespDTO recusar(@PathVariable Long id,
            @RequestBody(required = false) @Valid RecusarSolicitacaoDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.recusar(id, dto != null ? dto.motivo() : null, usuarioLogado);
    }

    @PatchMapping("/{id}/cancelar")
    public SolicitacaoRespDTO cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.cancelar(id, usuarioLogado);
    }

    @PatchMapping("/{id}/retirada")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public SolicitacaoRespDTO registrarRetirada(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.registrarRetirada(id, usuarioLogado);
    }

    @PatchMapping("/{id}/devolucao")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public SolicitacaoRespDTO registrarDevolucao(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return solicitacaoService.registrarDevolucao(id, usuarioLogado);
    }
}
