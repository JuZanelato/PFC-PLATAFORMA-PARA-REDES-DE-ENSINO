package com.web.pfc.SpringPfc.Service;

import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.config.JwtUtil;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.*;
import com.web.pfc.SpringPfc.exception.CredInvalidaException;
import com.web.pfc.SpringPfc.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRep usuarioRep;
    private final InstituicaoRep instituicaoRep;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditoriaService auditoriaService;

    public UsuarioRespDTO cadastrar(CadUsuarioDTO dto, Usuario responsavel) {
    if (usuarioRep.existsByEmail(dto.email())) {
        throw new NegocioException("E-mail já cadastrado.");
    }

    var instituicao = instituicaoRep.findById(dto.instituicaoId())
        .orElseThrow(() -> new NegocioException(
            "Instituição não encontrada — usuário deve estar associado a uma instituição (RN01)."));

    Usuario usuario = new Usuario();
    usuario.setNome(dto.nome());
    usuario.setEmail(dto.email());
    usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
    usuario.setPerfil(Perfil.valueOf(dto.perfil() != null ? dto.perfil() : "FUNCIONARIO"));
    usuario.setInstituicao(instituicao);
    usuario.setAtivo(true);
    usuario.setCriadoEm(LocalDateTime.now());

    Usuario salvo = usuarioRep.save(usuario);
    auditoriaService.registrarCriacaoUsuario(responsavel.getEmail(), salvo.getEmail());

    return new UsuarioRespDTO(salvo.getId(), salvo.getNome(), salvo.getEmail(),
        salvo.getPerfil().name(), instituicao.getNome());
}

    public TokenRespDTO login(LoginDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));
        } catch (AuthenticationException e) {
            auditoriaService.registrarTentativaLogin(dto.email(), false);
            throw new CredInvalidaException("E-mail ou senha inválidos.");
        }

        Usuario usuario = usuarioRep.findByEmail(dto.email())
                .orElseThrow(() -> new CredInvalidaException("E-mail ou senha inválidos."));

        String token = jwtUtil.gerarToken(usuario);
        auditoriaService.registrarLogin(usuario);

        UsuarioRespDTO usuarioDTO = new UsuarioRespDTO(usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getPerfil().name(), usuario.getInstituicao().getNome());
        return new TokenRespDTO(token, "Bearer", usuarioDTO);
    }

    public void logout(Usuario usuarioLogado) {
        auditoriaService.registrarLogout(usuarioLogado);
    }
}