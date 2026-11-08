package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.pfc.SpringPfc.Repository.TokenSenhaRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.TokenRecSenha;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.RedefSenhaDTO;
import com.web.pfc.SpringPfc.dto.SolicSenhaDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

@ExtendWith(MockitoExtension.class)
class SenhaServiceTest {

    @Mock UsuarioRep usuarioRep;
    @Mock TokenSenhaRep tokenSenhaRep;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks SenhaService senhaService;

    private TokenRecSenha token(Usuario usuario, LocalDateTime expiraEm) {
        TokenRecSenha t = new TokenRecSenha();
        t.setUsuario(usuario);
        t.setToken("abc-123");
        t.setExpiraEm(expiraEm);
        return t;
    }

    

    @Test
    void deveGerarTokenValidoPorUmaHoraQuandoEmailCadastrado() {
        
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@escola.com");
        when(usuarioRep.findByEmail("ana@escola.com")).thenReturn(Optional.of(usuario));
        ArgumentCaptor<TokenRecSenha> captor = ArgumentCaptor.forClass(TokenRecSenha.class);
        LocalDateTime antes = LocalDateTime.now();

        
        senhaService.solicitar(new SolicSenhaDTO("ana@escola.com"));

        
        verify(tokenSenhaRep).save(captor.capture());
        TokenRecSenha gerado = captor.getValue();
        assertNotNull(gerado.getToken());
        assertEquals(usuario, gerado.getUsuario());
        assertTrue(gerado.getExpiraEm().isAfter(antes.plusMinutes(59)));
        assertTrue(gerado.getExpiraEm().isBefore(antes.plusMinutes(61)));
    }

    @Test
    void naoDeveGerarTokenQuandoEmailNaoCadastrado() {
        
        when(usuarioRep.findByEmail("nao@existe.com")).thenReturn(Optional.empty());

        
        senhaService.solicitar(new SolicSenhaDTO("nao@existe.com"));

        
        verify(tokenSenhaRep, never()).save(any());
    }

    

    @Test
    void deveRedefinirSenhaEMarcarTokenComoUtilizado() {
        
        Usuario usuario = new Usuario();
        TokenRecSenha t = token(usuario, LocalDateTime.now().plusMinutes(30));
        when(tokenSenhaRep.findByTokenAndUtilizadoFalse("abc-123")).thenReturn(Optional.of(t));
        when(passwordEncoder.encode("novaSenha1")).thenReturn("NOVO_HASH");

        
        senhaService.redefinir(new RedefSenhaDTO("abc-123", "novaSenha1"));

        
        assertEquals("NOVO_HASH", usuario.getSenhaHash());
        assertTrue(t.isUtilizado());
        verify(usuarioRep).save(usuario);
        verify(tokenSenhaRep).save(t);
    }

    @Test
    void deveLancarExcecaoQuandoTokenInexistenteOuJaUtilizado() {
        
        when(tokenSenhaRep.findByTokenAndUtilizadoFalse("invalido")).thenReturn(Optional.empty());

        
        NegocioException ex = assertThrows(NegocioException.class,
                () -> senhaService.redefinir(new RedefSenhaDTO("invalido", "novaSenha1")));

        
        assertEquals("Token inválido ou expirado.", ex.getMessage());
        verify(usuarioRep, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 60, 1440})
    void deveRejeitarTokenExpiradoNaFronteiraDaValidade(long minutosAposExpirar) {
        
        Usuario usuario = new Usuario();
        TokenRecSenha t = token(usuario, LocalDateTime.now().minusMinutes(minutosAposExpirar));
        when(tokenSenhaRep.findByTokenAndUtilizadoFalse("abc-123")).thenReturn(Optional.of(t));

        
        NegocioException ex = assertThrows(NegocioException.class,
                () -> senhaService.redefinir(new RedefSenhaDTO("abc-123", "novaSenha1")));

        
        assertEquals("Token inválido ou expirado.", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
    }
}