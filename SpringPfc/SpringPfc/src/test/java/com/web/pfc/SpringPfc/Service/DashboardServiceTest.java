package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Repository.SolicitacaoRep;
import com.web.pfc.SpringPfc.domain.CategoriaRecurso;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Recurso;
import com.web.pfc.SpringPfc.domain.Solicitacao;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.StatusSolicitacao;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.DashboardRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    RecursoRep recursoRep;
    @Mock
    SolicitacaoRep solicitacaoRep;
    @Mock
    InstituicaoRep instituicaoRep;
    @InjectMocks
    DashboardService dashboardService;

    private Instituicao minha;
    private Instituicao outra;
    private Usuario diretor;
    private Recurso livroMinha;
    private Recurso cadeiraOutra;

    private Instituicao escola(Long id, String nome) {
        Instituicao i = new Instituicao();
        i.setId(id);
        i.setNome(nome);
        i.setStatus(StatusInst.ATIVA);
        return i;
    }

    private Recurso recurso(Long id, Instituicao dona, CategoriaRecurso categoria) {
        Recurso r = new Recurso();
        r.setId(id);
        r.setInstituicao(dona);
        r.setCategoria(categoria);
        r.setQuantidade(5);
        r.setDisponivel(true);
        r.setStatus(StatusRecurso.ATIVO);
        return r;
    }

    private Solicitacao pedido(Recurso recurso, Instituicao solicitante, StatusSolicitacao status, int diasAtras) {
        Solicitacao s = new Solicitacao();
        s.setRecurso(recurso);
        s.setInstituicaoDona(recurso.getInstituicao());
        s.setInstituicaoSolicitante(solicitante);
        s.setQuantidade(1);
        s.setStatus(status);
        s.setCriadaEm(LocalDateTime.now().minusDays(diasAtras));
        return s;
    }

    @BeforeEach
    void preparar() {
        minha = escola(1L, "Minha Escola");
        outra = escola(2L, "Outra Escola");
        diretor = new Usuario();
        diretor.setInstituicao(minha);
        livroMinha = recurso(10L, minha, CategoriaRecurso.MATERIAL_DIDATICO);
        cadeiraOutra = recurso(20L, outra, CategoriaRecurso.MOBILIARIO);

        lenient().when(recursoRep.findAll()).thenReturn(List.of(livroMinha, cadeiraOutra));
        lenient().when(instituicaoRep.findAll()).thenReturn(List.of(minha, outra));
        lenient().when(solicitacaoRep.findAll()).thenReturn(List.of(
                pedido(livroMinha, outra, StatusSolicitacao.PENDENTE, 2),
                pedido(cadeiraOutra, minha, StatusSolicitacao.CONCLUIDA, 10),
                pedido(cadeiraOutra, minha, StatusSolicitacao.RECUSADA, 60)));
    }

    @Test
    void semFiltrosConsideraTodaARede() {
        DashboardRespDTO r = dashboardService.resumo(diretor, null, null, null, null);

        assertEquals(2, r.totalRecursos());
        assertEquals(3, r.totalSolicitacoes());
        assertEquals(1, r.solicitacoesPendentes());
        assertEquals(1, r.solicitacoesConcluidas());
        assertEquals(50, r.taxaAprovacao());
    }

    @Test
    void filtroPorPeriodoIgnoraSolicitacoesMaisAntigas() {
        DashboardRespDTO r = dashboardService.resumo(diretor, 30, null, null, null);

        assertEquals(2, r.totalSolicitacoes());
        assertEquals(100, r.taxaAprovacao());
        assertEquals(2, r.totalRecursos());
    }

    @Test
    void filtroMinhaEscolaComVisaoRecebidas() {
        DashboardRespDTO r = dashboardService.resumo(diretor, null, "MINHA", null, "RECEBIDAS");

        assertEquals(1, r.totalRecursos());
        assertEquals(1, r.totalSolicitacoes());
        assertEquals(1, r.solicitacoesPendentes());
    }

    @Test
    void filtroMinhaEscolaComVisaoEnviadas() {
        DashboardRespDTO r = dashboardService.resumo(diretor, null, "MINHA", null, "ENVIADAS");

        assertEquals(2, r.totalSolicitacoes());
        assertEquals(1, r.solicitacoesConcluidas());
        assertEquals(0, r.solicitacoesPendentes());
    }

    @Test
    void filtroPorCategoriaValeParaRecursosESolicitacoes() {
        DashboardRespDTO r = dashboardService.resumo(diretor, null, null, "MOBILIARIO", null);

        assertEquals(1, r.totalRecursos());
        assertEquals(2, r.totalSolicitacoes());
        assertEquals(0, r.solicitacoesPendentes());
    }

    @Test
    void categoriaInexistenteGeraErro() {
        assertThrows(NegocioException.class,
                () -> dashboardService.resumo(diretor, null, null, "QUALQUER", null));
    }
}
