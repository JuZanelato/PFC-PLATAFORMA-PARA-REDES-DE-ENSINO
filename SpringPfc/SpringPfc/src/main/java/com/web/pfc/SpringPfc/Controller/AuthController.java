package com.web.pfc.SpringPfc.Controller;

import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.CadUsuarioDTO;
import com.web.pfc.SpringPfc.dto.LoginDTO;
import com.web.pfc.SpringPfc.dto.RedefSenhaDTO;
import com.web.pfc.SpringPfc.dto.SolicSenhaDTO;
import com.web.pfc.SpringPfc.dto.TokenRespDTO;
import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.Service.AuthService;
import com.web.pfc.SpringPfc.Service.SenhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SenhaService senhaService;

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioRespDTO> cadastrar(@RequestBody @Valid CadUsuarioDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(dto, usuarioLogado));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenRespDTO> login(@RequestBody @Valid LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Usuario usuarioLogado) {
        authService.logout(usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Void> solicitarRecuperacao(@RequestBody @Valid SolicSenhaDTO dto) {
        senhaService.solicitar(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody @Valid RedefSenhaDTO dto) {
        senhaService.redefinir(dto);
        return ResponseEntity.ok().build();
    }
}