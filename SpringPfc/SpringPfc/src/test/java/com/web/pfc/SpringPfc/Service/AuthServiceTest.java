package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.config.JwtUtil;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.CadUsuarioDTO;
import com.web.pfc.SpringPfc.dto.LoginDTO;
import com.web.pfc.SpringPfc.dto.TokenRespDTO;
import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.exception.CredInvalidaException;
import com.web.pfc.SpringPfc.exception.NegocioException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UsuarioRep usuarioRep;
    @Mock
    InstituicaoRep instituicaoRep;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    AuditoriaService auditoriaService;
    @InjectMocks
    AuthService authService;

    private Instituicao instituicao() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        inst.setNome("Escola Modelo");
        return inst;
    }

    private void prepararCadastroValido() {
        when(usuarioRep.existsByEmail("ana@escola.com")).thenReturn(false);
        when(instituicaoRep.findById(1L)).thenReturn(Optional.of(instituicao()));
        when(usuarioRep.existsByInstituicao_IdAndPerfilAndAtivoTrue(1L, Perfil.ADMINISTRADOR)).thenReturn(true);
        when(passwordEncoder.encode("senha123")).thenReturn("HASH_BCRYPT");
        when(usuarioRep.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(10L);
            return u;
        });
    }

    @Test
    void deveCadastrarUsuarioComSenhaCriptografadaQuandoDadosValidos() {

        prepararCadastroValido();
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 1L, "GESTOR");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        UsuarioRespDTO resposta = authService.cadastrar(dto, null);

        verify(usuarioRep).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertEquals("HASH_BCRYPT", salvo.getSenhaHash());
        assertEquals(Perfil.GESTOR, salvo.getPerfil());
        assertTrue(salvo.isAguardandoAprovacao());
        assertEquals(10L, resposta.id());
        assertEquals("Escola Modelo", resposta.instituicaoNome());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {

        when(usuarioRep.existsByEmail("ana@escola.com")).thenReturn(true);
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 1L, "FUNCIONARIO");

        NegocioException ex = assertThrows(NegocioException.class, () -> authService.cadastrar(dto, null));

        assertEquals("E-mail já cadastrado.", ex.getMessage());
        verify(usuarioRep, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoInstituicaoNaoExiste() {

        when(usuarioRep.existsByEmail("ana@escola.com")).thenReturn(false);
        when(instituicaoRep.findById(99L)).thenReturn(Optional.empty());
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 99L, "FUNCIONARIO");

        NegocioException ex = assertThrows(NegocioException.class, () -> authService.cadastrar(dto, null));

        assertEquals("Instituição não encontrada — usuário deve estar associado a uma instituição (RN01).",
                ex.getMessage());
        verify(usuarioRep, never()).save(any());
    }

    @Test
    void deveAtribuirPerfilFuncionarioQuandoPerfilNaoInformado() {

        prepararCadastroValido();
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 1L, null);

        UsuarioRespDTO resposta = authService.cadastrar(dto, null);

        assertEquals("FUNCIONARIO", resposta.perfil());
    }

    @Test
    void deveRegistrarAutocadastroNaAuditoriaQuandoNaoHaResponsavel() {

        prepararCadastroValido();
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 1L, "FUNCIONARIO");

        authService.cadastrar(dto, null);

        verify(auditoriaService).registrarCriacaoUsuario("ana@escola.com (autocadastro)", "ana@escola.com");
    }

    @Test
    void deveRetornarTokenJwtQuandoCredenciaisValidas() {

        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Ana");
        usuario.setEmail("ana@escola.com");
        usuario.setPerfil(Perfil.ADMINISTRADOR);
        usuario.setInstituicao(instituicao());
        when(usuarioRep.findByEmail("ana@escola.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.gerarToken(usuario)).thenReturn("token-jwt");

        TokenRespDTO resposta = authService.login(new LoginDTO("ana@escola.com", "senha123"));

        assertEquals("token-jwt", resposta.token());
        assertEquals("Bearer", resposta.tipo());
        assertEquals("ADMINISTRADOR", resposta.usuario().perfil());
        verify(auditoriaService).registrarLogin(usuario);
    }

    @Test
    void deveLancarExcecaoERegistrarFalhaQuandoSenhaIncorreta() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        CredInvalidaException ex = assertThrows(CredInvalidaException.class,
                () -> authService.login(new LoginDTO("ana@escola.com", "errada")));

        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
        verify(auditoriaService).registrarTentativaLogin("ana@escola.com", false);
        verify(jwtUtil, never()).gerarToken(any());
    }

    @Test
    void deveCadastrarFuncionarioAtivoNoAutocadastro() {
        prepararCadastroValido();
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 1L, "FUNCIONARIO");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        authService.cadastrar(dto, null);

        verify(usuarioRep).save(captor.capture());
        assertFalse(captor.getValue().isAguardandoAprovacao());
        assertTrue(captor.getValue().isEnabled());
    }

    @Test
    void deveRecusarSegundoDiretorNaMesmaInstituicao() {
        when(usuarioRep.existsByEmail("ana@escola.com")).thenReturn(false);
        when(instituicaoRep.findById(1L)).thenReturn(Optional.of(instituicao()));
        when(usuarioRep.existsByInstituicao_IdAndPerfilAndAtivoTrue(1L, Perfil.ADMINISTRADOR)).thenReturn(true);
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 1L, "ADMINISTRADOR");

        NegocioException ex = assertThrows(NegocioException.class, () -> authService.cadastrar(dto, null));

        assertEquals("Esta instituição já possui um diretor cadastrado.", ex.getMessage());
        verify(usuarioRep, never()).save(any());
    }

    @Test
    void deveNegarCadastroFeitoPorFuncionarioLogado() {
        when(usuarioRep.existsByEmail("novo@escola.com")).thenReturn(false);
        Usuario funcionario = new Usuario();
        funcionario.setPerfil(Perfil.FUNCIONARIO);
        CadUsuarioDTO dto = new CadUsuarioDTO("Novo", "novo@escola.com", "senha123", 1L, "FUNCIONARIO");

        assertThrows(AccessDeniedException.class, () -> authService.cadastrar(dto, funcionario));
        verify(usuarioRep, never()).save(any());
    }

    @Test
    void deveCadastrarGestorAtivoNaInstituicaoDoDiretor() {
        when(usuarioRep.existsByEmail("gestor@escola.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("HASH_BCRYPT");
        when(usuarioRep.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        Usuario diretor = new Usuario();
        diretor.setEmail("diretor@escola.com");
        diretor.setPerfil(Perfil.ADMINISTRADOR);
        diretor.setInstituicao(instituicao());
        // instituicaoId de outra escola é ignorado: vale a escola do Diretor
        CadUsuarioDTO dto = new CadUsuarioDTO("Gestor", "gestor@escola.com", "senha123", 99L, "GESTOR");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        authService.cadastrar(dto, diretor);

        verify(usuarioRep).save(captor.capture());
        assertEquals(1L, captor.getValue().getInstituicao().getId());
        assertFalse(captor.getValue().isAguardandoAprovacao());
        verify(instituicaoRep, never()).findById(any());
    }

    @Test
    void deveCadastrarOutroDiretorQuandoODiretorLogadoCadastra() {
        when(usuarioRep.existsByEmail("vice@escola.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("HASH_BCRYPT");
        when(usuarioRep.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        Usuario diretor = new Usuario();
        diretor.setEmail("diretor@escola.com");
        diretor.setPerfil(Perfil.ADMINISTRADOR);
        diretor.setInstituicao(instituicao());
        CadUsuarioDTO dto = new CadUsuarioDTO("Vice", "vice@escola.com", "senha123", null, "ADMINISTRADOR");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        authService.cadastrar(dto, diretor);

        verify(usuarioRep).save(captor.capture());
        assertEquals(Perfil.ADMINISTRADOR, captor.getValue().getPerfil());
        assertEquals(1L, captor.getValue().getInstituicao().getId());
        assertTrue(captor.getValue().isEnabled());
    }

    @Test
    void deveAvisarQueAContaEstaDesativadaQuandoSenhaCorreta() {
        Usuario desativado = new Usuario();
        desativado.setSenhaHash("HASH");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("inativo"));
        when(usuarioRep.findByEmail("ana@escola.com")).thenReturn(Optional.of(desativado));
        when(passwordEncoder.matches("senha123", "HASH")).thenReturn(true);

        CredInvalidaException ex = assertThrows(CredInvalidaException.class,
                () -> authService.login(new LoginDTO("ana@escola.com", "senha123")));

        assertEquals("Sua conta está desativada. Fale com o diretor da sua escola para reativá-la.", ex.getMessage());
        verify(jwtUtil, never()).gerarToken(any());
    }

    @Test
    void deveGuardarOEmailEmMinusculasNoCadastro() {
        prepararCadastroValido();
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "  Ana@Escola.COM ", "senha123", 1L, "FUNCIONARIO");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        authService.cadastrar(dto, null);

        verify(usuarioRep).existsByEmail("ana@escola.com");
        verify(usuarioRep).save(captor.capture());
        assertEquals("ana@escola.com", captor.getValue().getEmail());
    }

    @Test
    void deveEntrarComOEmailEmMaiusculas() {
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@escola.com");
        usuario.setPerfil(Perfil.FUNCIONARIO);
        usuario.setInstituicao(instituicao());
        when(usuarioRep.findByEmail("ana@escola.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.gerarToken(usuario)).thenReturn("TOKEN");

        TokenRespDTO resp = authService.login(new LoginDTO("ANA@Escola.com", "senha123"));

        assertEquals("TOKEN", resp.token());
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("ana@escola.com", "senha123"));
    }

    @Test
    void deveAvisarQueCadastroEstaPendenteQuandoSenhaCorreta() {
        Usuario pendente = new Usuario();
        pendente.setSenhaHash("HASH");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("pendente"));
        when(usuarioRep.findByEmail("gestor@escola.com")).thenReturn(Optional.of(pendente));
        when(passwordEncoder.matches("senha123", "HASH")).thenReturn(true);

        CredInvalidaException ex = assertThrows(CredInvalidaException.class,
                () -> authService.login(new LoginDTO("gestor@escola.com", "senha123")));

        assertEquals("Seu cadastro está aguardando a aprovação do diretor da instituição.", ex.getMessage());
        verify(jwtUtil, never()).gerarToken(any());
    }

    @Test
    void deveRecusarFuncionarioQuandoInstituicaoNaoTemDiretor() {

        when(usuarioRep.existsByEmail("ana@escola.com")).thenReturn(false);
        when(instituicaoRep.findById(1L)).thenReturn(Optional.of(instituicao()));
        when(usuarioRep.existsByInstituicao_IdAndPerfilAndAtivoTrue(1L, Perfil.ADMINISTRADOR)).thenReturn(false);
        CadUsuarioDTO dto = new CadUsuarioDTO("Ana", "ana@escola.com", "senha123", 1L, "FUNCIONARIO");

        NegocioException ex = assertThrows(NegocioException.class, () -> authService.cadastrar(dto, null));

        assertEquals("Esta instituição ainda não tem um diretor cadastrado. "
                + "Peça ao diretor da sua escola para se cadastrar no sistema e tente novamente depois.",
                ex.getMessage());
        verify(usuarioRep, never()).save(any());
    }
}