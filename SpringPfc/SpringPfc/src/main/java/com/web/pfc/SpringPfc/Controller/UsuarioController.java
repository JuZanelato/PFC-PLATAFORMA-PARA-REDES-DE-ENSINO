package com.web.pfc.SpringPfc.Controller;

import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.Service.UsuarioService;
import com.web.pfc.SpringPfc.domain.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioRespDTO> meuPerfil(@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(usuarioService.toRespDTO(usuarioLogado));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioRespDTO>> listar(@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(usuarioService.listarDaInstituicao(usuarioLogado));
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioRespDTO>> pendentes(@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(usuarioService.listarPendentes(usuarioLogado));
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioRespDTO> aprovar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(usuarioService.aprovar(id, usuarioLogado));
    }

    @PatchMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> recusar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        usuarioService.recusar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        usuarioService.inativar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
    
}
