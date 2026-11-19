package com.web.pfc.SpringPfc.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.web.pfc.SpringPfc.Service.DoacaoService;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.DoacaoCriarDTO;
import com.web.pfc.SpringPfc.dto.DoacaoRespDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/Instituicao")
@RequiredArgsConstructor
public class DoacaoController {

    private final DoacaoService doacaoService;


    @PostMapping("{id}/doacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public DoacaoRespDTO informar(
            @PathVariable("id") Long id,
            @RequestBody @Valid DoacaoCriarDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return doacaoService.informar(id, dto, usuarioLogado);
    }


    @GetMapping("/minha/doacoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR')")
    public List<DoacaoRespDTO> recebidas(@AuthenticationPrincipal Usuario usuarioLogado) {
        return doacaoService.listarDaMinhaInstituicao(usuarioLogado);
    }
}