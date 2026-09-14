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
    public ResponseEntity<List<UsuarioRespDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        usuarioService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
