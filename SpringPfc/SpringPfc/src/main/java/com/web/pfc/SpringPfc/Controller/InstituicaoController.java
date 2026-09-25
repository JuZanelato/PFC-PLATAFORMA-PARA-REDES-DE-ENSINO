package com.web.pfc.SpringPfc.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Service.AuditoriaService;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Usuario;

@RestController
@RequestMapping("api/Instituicao")
public class InstituicaoController {

    private InstituicaoRep instituicaorep;
    private AuditoriaService auditoriaService;

    public InstituicaoController(InstituicaoRep instituicaorep, AuditoriaService auditoriaService) {
        this.instituicaorep = instituicaorep;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public Instituicao save(@RequestBody @Valid Instituicao instituicao,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        Instituicao salva = instituicaorep.save(instituicao);
        auditoriaService.registrarCriacaoInstituicao(usuarioLogado, salva.getId());
        return salva;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void delete(@PathVariable("id") Long id, @AuthenticationPrincipal Usuario usuarioLogado) {

        instituicaorep.findById(id)
                .map(instituicao -> {
                    instituicaorep.delete(instituicao);
                    auditoriaService.registrarExclusaoInstituicao(usuarioLogado, id);
                    return Void.TYPE;
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro nao encontrado"));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GESTOR')")
    public void update(
            @PathVariable Long id,
            @RequestBody @Valid Instituicao instituicao,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        instituicaorep.findById(id)
                .map(instituicaoExistente -> {

                    instituicao.setId(instituicaoExistente.getId());
                    instituicaorep.save(instituicao);
                    auditoriaService.registrarAlteracaoInstituicao(usuarioLogado, id);

                    return instituicao;

                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro nao encontrado"));
    }

    @GetMapping
    public List<Instituicao> find(Instituicao filtro) {

        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withStringMatcher(
                        ExampleMatcher.StringMatcher.CONTAINING);

        Example<Instituicao> example = Example.of(filtro, matcher);

        return instituicaorep.findAll(example);
    }

    @GetMapping("{id}")
    public Instituicao getInstituicaoById(
            @PathVariable("id") Long id) {

        return instituicaorep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituicao nao encontrada"));
    }

    @GetMapping("{id}/pix")
    @PreAuthorize("hasAnyRole('FUNCIONARIO', 'GESTOR', 'ADMINISTRADOR')")
    public ResponseEntity<String> consultarChavePix(@PathVariable("id") Long id) {

        Instituicao instituicao = instituicaorep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituição não encontrada"));

        return ResponseEntity.ok(instituicao.getChavePix());
    }

    @PutMapping("{id}/pix")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMINISTRADOR')")
    public ResponseEntity<String> alterarChavePix(
            @PathVariable("id") Long id,
            @RequestBody String chavePix,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        if (chavePix == null || chavePix.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("A chave PIX não pode estar vazia.");
        }
        if (chavePix.length() > 77) {
            return ResponseEntity.badRequest().body("A chave PIX deve possuir no máximo 77 caracteres.");
        }

        Instituicao instituicao = instituicaorep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituição não encontrada"));

        instituicao.setChavePix(chavePix.trim());
        instituicaorep.save(instituicao);
        auditoriaService.registrarAlteracaoPix(usuarioLogado, id);

        return ResponseEntity.ok("Chave PIX alterada com sucesso.");
    }

    @DeleteMapping("{id}/pix")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMINISTRADOR')")
    public ResponseEntity<String> removerChavePix(@PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        Instituicao instituicao = instituicaorep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituição não encontrada"));

        instituicao.setChavePix(null);
        instituicaorep.save(instituicao);
        auditoriaService.registrarRemocaoPix(usuarioLogado, id);

        return ResponseEntity.ok("Chave PIX removida com sucesso.");
    }
}