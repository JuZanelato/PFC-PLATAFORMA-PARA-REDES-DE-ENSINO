package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.web.pfc.SpringPfc.Repository.LogAuditoriaRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.Repository.TokenSenhaRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.web.pfc.SpringPfc.dto.AtualizarMeusDadosDTO;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRep usuarioRep;
    @Mock
    AuditoriaService auditoriaService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    SolicitacaoRep solicitacaoRep;
    @Mock
    LogAuditoriaRep logAuditoriaRep;
    @Mock
    TokenSenhaRep tokenSenhaRep;
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
    void deveListarUsuariosDaInstituicaoDoDiretor() {

        Usuario diretor = usuario(1L, "diretor@escola.com");
        diretor.getInstituicao().setId(7L);
        when(usuarioRep.findByInstituicao_IdOrderByNomeAsc(7L)).thenReturn(List.of(usuario(5L, "func@escola.com")));

        List<UsuarioRespDTO> lista = usuarioService.listarDaInstituicao(diretor);

        assertEquals(1, lista.size());
        assertEquals("func@escola.com", lista.get(0).email());
        assertEquals("FUNCIONARIO", lista.get(0).perfil());
        assertEquals("Escola Modelo", lista.get(0).instituicaoNome());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {

        Usuario diretor = usuario(1L, "diretor@escola.com");
        diretor.getInstituicao().setId(7L);
        when(usuarioRep.findByInstituicao_IdOrderByNomeAsc(7L)).thenReturn(List.of());

        List<UsuarioRespDTO> lista = usuarioService.listarDaInstituicao(diretor);

        assertTrue(lista.isEmpty());
    }

    @Test
    void deveAlterarONomeSemTrocarASenha() {

        Usuario eu = usuario(1L, "ana@escola.com");
        when(usuarioRep.findById(1L)).thenReturn(Optional.of(eu));

        UsuarioRespDTO resposta = usuarioService.atualizarMeusDados(eu,
                new AtualizarMeusDadosDTO("  Ana Souza  ", null, null));

        assertEquals("Ana Souza", resposta.nome());
        verify(passwordEncoder, never()).encode(any());
        verify(auditoriaService).registrarAlteracaoDadosProprios(eu, false);
    }

    @Test
    void deveTrocarASenhaQuandoSenhaAtualEstaCorreta() {

        Usuario eu = usuario(1L, "ana@escola.com");
        eu.setSenhaHash("HASH_ANTIGO");
        when(usuarioRep.findById(1L)).thenReturn(Optional.of(eu));
        when(passwordEncoder.matches("antiga123", "HASH_ANTIGO")).thenReturn(true);
        when(passwordEncoder.encode("nova123")).thenReturn("HASH_NOVO");

        usuarioService.atualizarMeusDados(eu, new AtualizarMeusDadosDTO("Ana", "antiga123", "nova123"));

        assertEquals("HASH_NOVO", eu.getSenhaHash());
        verify(auditoriaService).registrarAlteracaoDadosProprios(eu, true);
    }

    @Test
    void deveRecusarTrocaDeSenhaQuandoSenhaAtualEstaErrada() {

        Usuario eu = usuario(1L, "ana@escola.com");
        eu.setSenhaHash("HASH_ANTIGO");
        when(usuarioRep.findById(1L)).thenReturn(Optional.of(eu));
        when(passwordEncoder.matches("errada", "HASH_ANTIGO")).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarioService.atualizarMeusDados(eu, new AtualizarMeusDadosDTO("Ana", "errada", "nova123")));

        assertEquals("A senha atual está incorreta.", ex.getMessage());
        assertEquals("HASH_ANTIGO", eu.getSenhaHash());
        verify(usuarioRep, never()).save(any());
    }


    @Test
    void deveAnonimizarAPropriaContaQuandoASenhaEstaCorreta() {
        Usuario ana = usuario(5L, "ana@escola.com");
        ana.setSenhaHash("HASH_ATUAL");
        when(usuarioRep.findById(5L)).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("senha123", "HASH_ATUAL")).thenReturn(true);
        when(solicitacaoRep.existsBySolicitante_IdAndStatusIn(any(), any())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH_ALEATORIO");

        usuarioService.excluirMinhaConta(ana, "senha123");

        assertEquals("Usuário removido", ana.getNome());
        assertEquals("removido-5@anonimizado.local", ana.getEmail());
        assertEquals("HASH_ALEATORIO", ana.getSenhaHash());
        assertFalse(ana.isAtivo());
        verify(logAuditoriaRep).anonimizarEmail("ana@escola.com", "removido-5@anonimizado.local");
        verify(tokenSenhaRep).deleteByUsuario_Id(5L);
        verify(auditoriaService).registrarAnonimizacaoUsuario("removido-5@anonimizado.local", 5L);
    }

    @Test
    void naoDeveExcluirAContaComSenhaErrada() {
        Usuario ana = usuario(5L, "ana@escola.com");
        ana.setSenhaHash("HASH_ATUAL");
        when(usuarioRep.findById(5L)).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("errada", "HASH_ATUAL")).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> usuarioService.excluirMinhaConta(ana, "errada"));

        assertEquals("A senha atual está incorreta.", ex.getMessage());
        assertEquals("ana@escola.com", ana.getEmail());
        verify(usuarioRep, never()).save(any());
    }

    @Test
    void unicoDiretorDaEscolaNaoPodeExcluirAPropriaConta() {
        Usuario diretor = usuario(1L, "diretor@escola.com");
        diretor.setPerfil(Perfil.ADMINISTRADOR);
        diretor.setSenhaHash("HASH");
        diretor.getInstituicao().setId(7L);
        when(usuarioRep.findById(1L)).thenReturn(Optional.of(diretor));
        when(passwordEncoder.matches("senha123", "HASH")).thenReturn(true);
        when(usuarioRep.countByInstituicao_IdAndPerfilAndAtivoTrue(7L, Perfil.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(NegocioException.class, () -> usuarioService.excluirMinhaConta(diretor, "senha123"));

        verify(usuarioRep, never()).save(any());
    }

    @Test
    void diretorDeveExcluirOsDadosDeUmUsuarioDaEscola() {
        Usuario diretor = usuario(1L, "diretor@escola.com");
        Usuario func = usuario(5L, "func@escola.com");
        when(usuarioRep.findById(5L)).thenReturn(Optional.of(func));
        when(solicitacaoRep.existsBySolicitante_IdAndStatusIn(any(), any())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH_ALEATORIO");

        usuarioService.excluirDadosDeUsuario(5L, diretor);

        assertEquals("Usuário removido", func.getNome());
        assertEquals("removido-5@anonimizado.local", func.getEmail());
        verify(auditoriaService).registrarAnonimizacaoUsuario("diretor@escola.com", 5L);
    }

    @Test
    void naoDeveExcluirDadosComEmprestimoEmAndamento() {
        Usuario diretor = usuario(1L, "diretor@escola.com");
        Usuario func = usuario(5L, "func@escola.com");
        when(usuarioRep.findById(5L)).thenReturn(Optional.of(func));
        when(solicitacaoRep.existsBySolicitante_IdAndStatusIn(any(), any())).thenReturn(true);

        assertThrows(NegocioException.class, () -> usuarioService.excluirDadosDeUsuario(5L, diretor));

        assertEquals("func@escola.com", func.getEmail());
        verify(usuarioRep, never()).save(any());
        verify(logAuditoriaRep, never()).anonimizarEmail(anyString(), anyString());
    }
}