package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.web.pfc.SpringPfc.Repository.MovimentacaoSolicitacaoRep;
import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.domain.CategoriaRecurso;
import com.web.pfc.SpringPfc.domain.CondicaoRecurso;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.MovimentacaoSolicitacao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.Recurso;
import com.web.pfc.SpringPfc.domain.Solicitacao;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.SolicitacaoCriarDTO;
import com.web.pfc.SpringPfc.dto.SolicitacaoRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    @Mock
    SolicitacaoRep solicitacaoRep;
    @Mock
    MovimentacaoSolicitacaoRep movimentacaoRep;
    @Mock
    RecursoRep recursoRep;
    @Mock
    AuditoriaService auditoriaService;
    @InjectMocks
    SolicitacaoService service;

    private Instituicao escolaDona;
    private Instituicao escolaSolicitante;
    private Usuario gestorDona;
    private Usuario funcionarioDona;
    private Usuario funcionarioSolicitante;
    private Usuario gestorOutraEscola;
    private Recurso recurso;

    @BeforeEach
    void configurar() {
        escolaDona = instituicao(1L, "EMEF Dona");
        escolaSolicitante = instituicao(2L, "EMEF Solicitante");
        Instituicao outraEscola = instituicao(3L, "EMEF Outra");

        gestorDona = usuario(10L, "Gestor Dona", Perfil.GESTOR, escolaDona);
        funcionarioDona = usuario(11L, "Func Dona", Perfil.FUNCIONARIO, escolaDona);
        funcionarioSolicitante = usuario(20L, "Func Solicitante", Perfil.FUNCIONARIO, escolaSolicitante);
        gestorOutraEscola = usuario(30L, "Gestor Outra", Perfil.GESTOR, outraEscola);

        recurso = new Recurso();
        recurso.setId(100L);
        recurso.setNome("Carteiras escolares");
        recurso.setCategoria(CategoriaRecurso.MOBILIARIO);
        recurso.setCondicao(CondicaoRecurso.BOM);
        recurso.setQuantidade(10);
        recurso.setDisponivel(true);
        recurso.setStatus(StatusRecurso.ATIVO);
        recurso.setInstituicao(escolaDona);
    }

    private Instituicao instituicao(Long id, String nome) {
        Instituicao i = new Instituicao();
        i.setId(id);
        i.setNome(nome);
        return i;
    }

    private Usuario usuario(Long id, String nome, Perfil perfil, Instituicao instituicao) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(nome.toLowerCase().replace(' ', '.') + "@escola.com");
        u.setPerfil(perfil);
        u.setInstituicao(instituicao);
        return u;
    }

    private Solicitacao solicitacao(StatusSolicitacao status, int quantidade) {
        Solicitacao s = new Solicitacao();
        s.setId(500L);
        s.setRecurso(recurso);
        s.setSolicitante(funcionarioSolicitante);
        s.setInstituicaoSolicitante(escolaSolicitante);
        s.setInstituicaoDona(escolaDona);
        s.setQuantidade(quantidade);
        s.setStatus(status);
        s.setCriadaEm(LocalDateTime.now());
        return s;
    }

    private void salvarDevolveOProprioObjeto() {
        when(solicitacaoRep.save(any(Solicitacao.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveCriarSolicitacaoPendenteEPreservarHistoricoEAuditoria() {
        when(recursoRep.findById(100L)).thenReturn(Optional.of(recurso));
        when(solicitacaoRep.existsByRecurso_IdAndInstituicaoSolicitante_IdAndStatusIn(eq(100L), eq(2L), any()))
                .thenReturn(false);
        salvarDevolveOProprioObjeto();

        SolicitacaoRespDTO resp = service.criar(
                new SolicitacaoCriarDTO(100L, 3, "Turma do 5º ano"), funcionarioSolicitante);

        assertEquals("PENDENTE", resp.status());
        assertEquals(3, resp.quantidade().intValue());
        assertEquals("EMEF Dona", resp.instituicaoDonaNome());
        assertEquals("Func Solicitante", resp.solicitanteNome());

        ArgumentCaptor<MovimentacaoSolicitacao> mov = ArgumentCaptor.forClass(MovimentacaoSolicitacao.class);
        verify(movimentacaoRep).save(mov.capture());
        assertNull(mov.getValue().getStatusAnterior());
        assertEquals(StatusSolicitacao.PENDENTE, mov.getValue().getStatusNovo());
        verify(auditoriaService).registrarSolicitacao(eq(funcionarioSolicitante), any(), eq("CRIADA"));

        assertEquals(10, recurso.getQuantidade().intValue());
    }

    @Test
    void naoDeveSolicitarRecursoDaPropriaInstituicao() {
        when(recursoRep.findById(100L)).thenReturn(Optional.of(recurso));

        assertThrows(NegocioException.class,
                () -> service.criar(new SolicitacaoCriarDTO(100L, 1, null), funcionarioDona));
        verify(solicitacaoRep, never()).save(any());
    }

    @Test
    void recursoIndisponivelNaoGeraNovaSolicitacao_RN02() {
        recurso.setDisponivel(false);
        when(recursoRep.findById(100L)).thenReturn(Optional.of(recurso));

        NegocioException erro = assertThrows(NegocioException.class,
                () -> service.criar(new SolicitacaoCriarDTO(100L, 1, null), funcionarioSolicitante));
        assertTrue(erro.getMessage().contains("RN02"));
        verify(solicitacaoRep, never()).save(any());
    }

    @Test
    void recursoInativoNaoGeraNovaSolicitacao_RN02() {
        recurso.setStatus(StatusRecurso.INATIVO);
        when(recursoRep.findById(100L)).thenReturn(Optional.of(recurso));

        assertThrows(NegocioException.class,
                () -> service.criar(new SolicitacaoCriarDTO(100L, 1, null), funcionarioSolicitante));
    }

    @Test
    void naoDeveSolicitarQuantidadeMaiorQueADisponivel() {
        when(recursoRep.findById(100L)).thenReturn(Optional.of(recurso));

        assertThrows(NegocioException.class,
                () -> service.criar(new SolicitacaoCriarDTO(100L, 11, null), funcionarioSolicitante));
    }

    @Test
    void naoDevePermitirDuasSolicitacoesEmAndamentoParaOMesmoRecurso() {
        when(recursoRep.findById(100L)).thenReturn(Optional.of(recurso));
        when(solicitacaoRep.existsByRecurso_IdAndInstituicaoSolicitante_IdAndStatusIn(eq(100L), eq(2L), any()))
                .thenReturn(true);

        assertThrows(NegocioException.class,
                () -> service.criar(new SolicitacaoCriarDTO(100L, 1, null), funcionarioSolicitante));
        verify(solicitacaoRep, never()).save(any());
    }

    @Test
    void gestorDaDonaAprovaEReservaAQuantidade_RN04() {
        Solicitacao s = solicitacao(StatusSolicitacao.PENDENTE, 4);
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(s));
        salvarDevolveOProprioObjeto();

        SolicitacaoRespDTO resp = service.aprovar(500L, gestorDona);

        assertEquals("APROVADA", resp.status());
        assertEquals(6, recurso.getQuantidade().intValue());
        assertTrue(recurso.isDisponivel());
        assertEquals(gestorDona.getEmail(), s.getRespondidaPor());
        verify(recursoRep).save(recurso);
        verify(auditoriaService).registrarSolicitacao(gestorDona, 500L, "APROVADA");

        assertNull(resp.solicitanteNome());
    }

    @Test
    void aprovarTodaAQuantidadeDeixaORecursoIndisponivel_RN04() {
        Solicitacao s = solicitacao(StatusSolicitacao.PENDENTE, 10);
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(s));
        salvarDevolveOProprioObjeto();

        service.aprovar(500L, gestorDona);

        assertEquals(0, recurso.getQuantidade().intValue());
        assertFalse(recurso.isDisponivel());
    }

    @Test
    void gestorDeOutraInstituicaoNaoPodeAprovar_RN03() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.PENDENTE, 1)));

        assertThrows(AccessDeniedException.class, () -> service.aprovar(500L, gestorOutraEscola));
        verify(solicitacaoRep, never()).save(any());
    }

    @Test
    void funcionarioDaDonaNaoPodeAprovar_RN06() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.PENDENTE, 1)));

        assertThrows(AccessDeniedException.class, () -> service.aprovar(500L, funcionarioDona));
    }

    @Test
    void naoDeveAprovarSolicitacaoQueNaoEstaPendente() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.RECUSADA, 1)));

        assertThrows(NegocioException.class, () -> service.aprovar(500L, gestorDona));
    }

    @Test
    void naoDeveAprovarSeORecursoJaFoiEmprestadoParaOutraSolicitacao_RN04() {
        recurso.setQuantidade(2);
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.PENDENTE, 5)));

        assertThrows(NegocioException.class, () -> service.aprovar(500L, gestorDona));
        assertEquals(2, recurso.getQuantidade().intValue());
    }

    @Test
    void gestorDaDonaRecusaComMotivo() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.PENDENTE, 1)));
        salvarDevolveOProprioObjeto();

        SolicitacaoRespDTO resp = service.recusar(500L, "  Itens reservados para evento  ", gestorDona);

        assertEquals("RECUSADA", resp.status());
        assertEquals("Itens reservados para evento", resp.motivoRecusa());
        verify(auditoriaService).registrarSolicitacao(gestorDona, 500L, "RECUSADA");
    }

    @Test
    void registraRetiradaSomenteAposAprovacao() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.APROVADA, 2)));
        salvarDevolveOProprioObjeto();

        SolicitacaoRespDTO resp = service.registrarRetirada(500L, gestorDona);

        assertEquals("RETIRADA", resp.status());
    }

    @Test
    void devolucaoConcluiASolicitacaoELiberaORecurso_RN05() {
        recurso.setQuantidade(0);
        recurso.setDisponivel(false);
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.RETIRADA, 10)));
        salvarDevolveOProprioObjeto();

        SolicitacaoRespDTO resp = service.registrarDevolucao(500L, gestorDona);

        assertEquals("CONCLUIDA", resp.status());
        assertEquals(10, recurso.getQuantidade().intValue());
        assertTrue(recurso.isDisponivel());
        verify(auditoriaService).registrarSolicitacao(gestorDona, 500L, "CONCLUIDA");
    }

    @Test
    void naoConcluiSemRegistrarARetiradaAntes_RN05() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.APROVADA, 1)));

        assertThrows(NegocioException.class, () -> service.registrarDevolucao(500L, gestorDona));
    }

    @Test
    void solicitanteCancelaEnquantoPendente() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.PENDENTE, 1)));
        salvarDevolveOProprioObjeto();

        SolicitacaoRespDTO resp = service.cancelar(500L, funcionarioSolicitante);

        assertEquals("CANCELADA", resp.status());
    }

    @Test
    void naoCancelaSolicitacaoJaAprovada() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.APROVADA, 1)));

        assertThrows(NegocioException.class, () -> service.cancelar(500L, funcionarioSolicitante));
    }

    @Test
    void outraInstituicaoNaoPodeCancelar() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.PENDENTE, 1)));

        assertThrows(AccessDeniedException.class, () -> service.cancelar(500L, gestorDona));
        verify(auditoriaService, never()).registrarSolicitacao(any(), anyLong(), anyString());
    }

    @Test
    void listaRecebidasFiltrandoPorStatus() {
        Solicitacao pendente = solicitacao(StatusSolicitacao.PENDENTE, 1);
        Solicitacao concluida = solicitacao(StatusSolicitacao.CONCLUIDA, 1);
        concluida.setId(501L);
        when(solicitacaoRep.findByInstituicaoDona_IdOrderByCriadaEmDesc(1L)).thenReturn(List.of(pendente, concluida));

        List<SolicitacaoRespDTO> resultado = service.listarRecebidas(gestorDona, StatusSolicitacao.PENDENTE);

        assertEquals(1, resultado.size());
        assertEquals(500L, resultado.get(0).id().longValue());
    }

    @Test
    void historicoSoPodeSerVistoPorInstituicoesParticipantes() {
        when(solicitacaoRep.findById(500L)).thenReturn(Optional.of(solicitacao(StatusSolicitacao.PENDENTE, 1)));

        assertThrows(AccessDeniedException.class, () -> service.historico(500L, gestorOutraEscola));
    }
}
