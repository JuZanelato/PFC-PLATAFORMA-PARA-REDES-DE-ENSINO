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
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.StatusInst;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.transaction.annotation.Transactional;

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

       @Transactional
    public UsuarioRespDTO cadastrar(CadUsuarioDTO dto, Usuario responsavel) {
        validarEmailLivre(dto.email());
        Perfil perfil = converterPerfil(dto.perfil());

        Instituicao instituicao;
        boolean aguardandoAprovacao = false;

        if (responsavel == null) {
            
            instituicao = buscarInstituicaoAtiva(dto.instituicaoId());

            if (perfil == Perfil.ADMINISTRADOR) {
                validarInstituicaoSemDiretor(instituicao);
            } else if (perfil == Perfil.GESTOR) {
                aguardandoAprovacao = true;
            }
        } else {
            
            if (responsavel.getPerfil() != Perfil.ADMINISTRADOR) {
                throw new AccessDeniedException("Somente o diretor pode cadastrar usuários.");
            }
            if (perfil == Perfil.ADMINISTRADOR) {
                throw new NegocioException("Cada instituição possui apenas um diretor.");
            }
            instituicao = responsavel.getInstituicao();
        }

        Usuario salvo = criarUsuario(dto.nome(), dto.email(), dto.senha(), perfil, instituicao, aguardandoAprovacao);

        String emailResponsavel = (responsavel != null) ? responsavel.getEmail() : salvo.getEmail() + " (autocadastro)";
        auditoriaService.registrarCriacaoUsuario(emailResponsavel, salvo.getEmail());

        return toDTO(salvo);
    }

       public TokenRespDTO login(LoginDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));
        } catch (DisabledException e) {
            // Conta pendente (Gestor esperando o Diretor aprovar)
            auditoriaService.registrarTentativaLogin(dto.email(), false);
            boolean senhaCorreta = usuarioRep.findByEmail(dto.email())
                    .map(u -> passwordEncoder.matches(dto.senha(), u.getSenhaHash()))
                    .orElse(false);
            throw new CredInvalidaException(senhaCorreta
                    ? "Seu cadastro está aguardando a aprovação do diretor da instituição."
                    : "E-mail ou senha inválidos.");
        } catch (AuthenticationException e) {
            auditoriaService.registrarTentativaLogin(dto.email(), false);
            throw new CredInvalidaException("E-mail ou senha inválidos.");
        }

        Usuario usuario = usuarioRep.findByEmail(dto.email())
                .orElseThrow(() -> new CredInvalidaException("E-mail ou senha inválidos."));

        String token = jwtUtil.gerarToken(usuario);
        auditoriaService.registrarLogin(usuario);

        return new TokenRespDTO(token, "Bearer", toDTO(usuario));
    }

    public void logout(Usuario usuarioLogado) {
        auditoriaService.registrarLogout(usuarioLogado);
    }


    private Usuario criarUsuario(String nome, String email, String senha, Perfil perfil,
            Instituicao instituicao, boolean aguardandoAprovacao) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim());
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setInstituicao(instituicao);
        usuario.setAtivo(true);
        usuario.setAguardandoAprovacao(aguardandoAprovacao);
        usuario.setCriadoEm(LocalDateTime.now());
        return usuarioRep.save(usuario);
    }

    private void validarEmailLivre(String email) {
        if (usuarioRep.existsByEmail(email)) {
            throw new NegocioException("E-mail já cadastrado.");
        }
    }

    private Perfil converterPerfil(String perfil) {
        if (perfil == null || perfil.isBlank()) {
            return Perfil.FUNCIONARIO;
        }
        try {
            return Perfil.valueOf(perfil.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NegocioException("Perfil inválido.");
        }
    }

    private Instituicao buscarInstituicaoAtiva(Long instituicaoId) {
        String mensagemRn01 = "Instituição não encontrada — usuário deve estar associado a uma instituição (RN01).";
        if (instituicaoId == null) {
            throw new NegocioException(mensagemRn01);
        }
        Instituicao instituicao = instituicaoRep.findById(instituicaoId)
                .orElseThrow(() -> new NegocioException(mensagemRn01));
        if (instituicao.getStatus() == StatusInst.INATIVA) {
            throw new NegocioException("Esta instituição está inativa e não aceita novos cadastros.");
        }
        return instituicao;
    }

    private void validarInstituicaoSemDiretor(Instituicao instituicao) {
        if (usuarioRep.existsByInstituicao_IdAndPerfilAndAtivoTrue(instituicao.getId(), Perfil.ADMINISTRADOR)) {
            throw new NegocioException("Esta instituição já possui um diretor cadastrado.");
        }
    }

        private UsuarioRespDTO toDTO(Usuario usuario) {
        return new UsuarioRespDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil().name(), usuario.getInstituicao().getNome(),
                usuario.isAtivo(), usuario.isAguardandoAprovacao());
    }
}   