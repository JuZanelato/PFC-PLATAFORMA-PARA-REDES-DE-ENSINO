package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRep usuarioRep;
    @Mock
    AuditoriaService auditoriaService;
    @InjectMocks
    UsuarioService usuarioService;

    private Usuario usuario(Long id, String email) {
        Instituicao inst = new Instituicao();
        inst.setNome("Escola Modelo");
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Ana");
        u.setEmail(email);
        u.setPerfil(Perfil.FUNCIONARIO);
        u.setInstituicao(inst);
        u.setAtivo(true);
        return u;
    }

    @Test
    void deveInativarUsuarioERegistrarAuditoria() {

        Usuario alvo = usuario(5L, "func@escola.com");
        Usuario gestor = usuario(1L, "gestor@escola.com");
        when(usuarioRep.findById(5L)).thenReturn(Optional.of(alvo));

        usuarioService.inativar(5L, gestor);

        assertFalse(alvo.isAtivo());
        verify(usuarioRep).save(alvo);
        verify(auditoriaService).registrarInativacaoUsuario(gestor, 5L);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoAoInativar() {

        when(usuarioRep.findById(99L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarioService.inativar(99L, usuario(1L, "gestor@escola.com")));

        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(usuarioRep, never()).save(any());
        verify(auditoriaService, never()).registrarInativacaoUsuario(any(), anyLong());
    }

    @Test
    void deveListarUsuariosComNomeDaInstituicao() {

        when(usuarioRep.findAll()).thenReturn(List.of(usuario(5L, "func@escola.com")));

        List<UsuarioRespDTO> lista = usuarioService.listarTodos();

        assertEquals(1, lista.size());
        assertEquals("func@escola.com", lista.get(0).email());
        assertEquals("FUNCIONARIO", lista.get(0).perfil());
        assertEquals("Escola Modelo", lista.get(0).instituicaoNome());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {

        when(usuarioRep.findAll()).thenReturn(List.of());

        List<UsuarioRespDTO> lista = usuarioService.listarTodos();

        assertTrue(lista.isEmpty());
    }
}