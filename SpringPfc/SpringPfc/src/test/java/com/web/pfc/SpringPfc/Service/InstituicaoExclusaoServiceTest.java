package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.Recurso;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.ExcluirInstituicaoDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

// NOVO: testes da exclusão de instituição
@ExtendWith(MockitoExtension.class)
class InstituicaoExclusaoServiceTest {

    @Mock
    InstituicaoRep instituicaoRep;
    @Mock
    UsuarioRep usuarioRep;
    @Mock
    RecursoRep recursoRep;
    @Mock
    SolicitacaoRep solicitacaoRep;
    @Mock
    UsuarioService usuarioService;
    @Mock
    AuditoriaService auditoriaService;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    InstituicaoExclusaoService service;

    private Instituicao escola;
    private Usuario diretor;

    @BeforeEach
    void preparar() {
        escola = new Instituicao();
        escola.setId(7L);
        escola.setNome("Escola Teste A");
        escola.setCnpj("12345678000199");
        escola.setChavePix("escola@teste.com");
        escola.setStatus(StatusInst.ATIVA);

        diretor = new Usuario();
        diretor.setId(1L);
        diretor.setEmail("diretor@teste.com");
        diretor.setSenhaHash("HASH");
        diretor.setPerfil(Perfil.ADMINISTRADOR);
        diretor.setInstituicao(escola);
    }

    private void comDiretorEEscola() {
        when(usuarioRep.findById(1L)).thenReturn(Optional.of(diretor));
        when(instituicaoRep.findById(7L)).thenReturn(Optional.of(escola));
    }

    @Test
    void deveExcluirAInstituicaoQuandoTudoConfere() {
        comDiretorEEscola();
        when(passwordEncoder.matches("senha123", "HASH")).thenReturn(true);
        Recurso carteira = new Recurso();
        carteira.setStatus(StatusRecurso.ATIVO);
        carteira.setDisponivel(true);
        when(recursoRep.findByInstituicaoId(7L)).thenReturn(List.of(carteira));

        service.excluirMinhaInstituicao(diretor,
                new ExcluirInstituicaoDTO("senha123", "  escola teste a ", "12.345.678/0001-99"));

        assertEquals(StatusInst.INATIVA, escola.getStatus());
        assertNull(escola.getChavePix());
        assertEquals(StatusRecurso.INATIVO, carteira.getStatus());
        assertFalse(carteira.isDisponivel());
        verify(auditoriaService).registrarExclusaoInstituicao(diretor, 7L);
        verify(usuarioService).anonimizarTodosDaInstituicao(7L);
    }

    @Test
    void naoDeveExcluirComSenhaErrada() {
        comDiretorEEscola();
        when(passwordEncoder.matches("errada", "HASH")).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class, () -> service.excluirMinhaInstituicao(diretor,
                new ExcluirInstituicaoDTO("errada", "Escola Teste A", "12345678000199")));

        assertEquals("A senha atual está incorreta.", ex.getMessage());
        assertEquals(StatusInst.ATIVA, escola.getStatus());
        verify(usuarioService, never()).anonimizarTodosDaInstituicao(anyLong());
    }

    @Test
    void naoDeveExcluirQuandoONomeNaoConfere() {
        comDiretorEEscola();
        when(passwordEncoder.matches("senha123", "HASH")).thenReturn(true);

        NegocioException ex = assertThrows(NegocioException.class, () -> service.excluirMinhaInstituicao(diretor,
                new ExcluirInstituicaoDTO("senha123", "Escola Teste B", "12345678000199")));

        assertEquals("O nome digitado não confere com o nome da instituição.", ex.getMessage());
        verify(instituicaoRep, never()).save(any());
    }

    @Test
    void naoDeveExcluirQuandoOCnpjNaoConfere() {
        comDiretorEEscola();
        when(passwordEncoder.matches("senha123", "HASH")).thenReturn(true);

        NegocioException ex = assertThrows(NegocioException.class, () -> service.excluirMinhaInstituicao(diretor,
                new ExcluirInstituicaoDTO("senha123", "Escola Teste A", "99999999000199")));

        assertEquals("O CNPJ digitado não confere com o CNPJ da instituição.", ex.getMessage());
        verify(instituicaoRep, never()).save(any());
    }

    @Test
    void naoDeveExcluirComEmprestimosEmAndamento() {
        comDiretorEEscola();
        when(passwordEncoder.matches("senha123", "HASH")).thenReturn(true);
        when(solicitacaoRep.existsByInstituicaoDona_IdAndStatusIn(any(), any())).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.excluirMinhaInstituicao(diretor,
                new ExcluirInstituicaoDTO("senha123", "Escola Teste A", "12345678000199")));

        assertEquals(StatusInst.ATIVA, escola.getStatus());
        verify(instituicaoRep, never()).save(any());
        verify(usuarioService, never()).anonimizarTodosDaInstituicao(anyLong());
    }
}