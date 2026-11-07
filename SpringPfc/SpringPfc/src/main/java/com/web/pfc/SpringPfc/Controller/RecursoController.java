package com.web.pfc.SpringPfc.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Service.AuditoriaService;
import com.web.pfc.SpringPfc.domain.Recurso;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.AtualizarDisponibilidadeDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;


@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    private final RecursoRep recursoRep;
    private final AuditoriaService auditoriaService;

    public RecursoController(RecursoRep recursoRep, AuditoriaService auditoriaService) {
        this.recursoRep = recursoRep;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public Recurso salvar(@RequestBody @Valid Recurso recurso, @AuthenticationPrincipal Usuario usuarioLogado) {
        // O cliente não escolhe unidade, autor, data nem status: vêm do usuário logado
        recurso.setId(null);
        recurso.setInstituicao(usuarioLogado.getInstituicao());
        recurso.setCadastradoPor(usuarioLogado.getEmail());
        recurso.setCriadoEm(LocalDateTime.now());
        recurso.setStatus(StatusRecurso.ATIVO);

        Recurso salvo = recursoRep.save(recurso);
        auditoriaService.registrarCriacaoRecurso(usuarioLogado, salvo.getId());
        return salvo;
    }

    @GetMapping
    public List<Recurso> listar(@AuthenticationPrincipal Usuario usuarioLogado) {
        return recursoRep.findByInstituicaoId(usuarioLogado.getInstituicao().getId());
    }

    @GetMapping("{id}")
    public Recurso buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        Recurso recurso = buscarOuFalhar(id);
        validarMesmaInstituicao(recurso, usuarioLogado);
        return recurso;
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public Recurso atualizar(@PathVariable Long id, @RequestBody @Valid Recurso recurso,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        Recurso existente = buscarOuFalhar(id);
        validarMesmaInstituicao(existente, usuarioLogado);
        validarAtivo(existente);

        recurso.setId(existente.getId());
        recurso.setInstituicao(existente.getInstituicao());
        recurso.setCadastradoPor(existente.getCadastradoPor());
        recurso.setCriadoEm(existente.getCriadoEm());
        recurso.setStatus(existente.getStatus());

        Recurso atualizado = recursoRep.save(recurso);
        auditoriaService.registrarAlteracaoRecurso(usuarioLogado, id);
        return atualizado;
    }

    @PatchMapping("{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public void inativar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        Recurso recurso = buscarOuFalhar(id);
        validarMesmaInstituicao(recurso, usuarioLogado);

        if (recurso.getStatus() == StatusRecurso.INATIVO) {
            throw new NegocioException("Recurso já está inativo.");
        }

        recurso.setStatus(StatusRecurso.INATIVO);
        recurso.setDisponivel(false);
        recursoRep.save(recurso);
        auditoriaService.registrarInativacaoRecurso(usuarioLogado, id);
    }

    @PatchMapping("{id}/disponibilidade")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public Recurso atualizarDisponibilidade(@PathVariable Long id,
            @RequestBody AtualizarDisponibilidadeDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        Recurso recurso = buscarOuFalhar(id);
        validarMesmaInstituicao(recurso, usuarioLogado);
        validarAtivo(recurso);

        recurso.setDisponivel(dto.disponivel());
        Recurso atualizado = recursoRep.save(recurso);
        auditoriaService.registrarAlteracaoDisponibilidadeRecurso(usuarioLogado, id, dto.disponivel());
        return atualizado;
    }

    private Recurso buscarOuFalhar(Long id) {
        return recursoRep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado"));
    }

    private void validarMesmaInstituicao(Recurso recurso, Usuario usuarioLogado) {
        if (!recurso.getInstituicao().getId().equals(usuarioLogado.getInstituicao().getId())) {
            throw new AccessDeniedException("Você não tem permissão para gerenciar recursos de outra instituição.");
        }
    }

    private void validarAtivo(Recurso recurso) {
        if (recurso.getStatus() == StatusRecurso.INATIVO) {
            throw new NegocioException("Recurso inativo não pode ser alterado.");
        }
    }
}