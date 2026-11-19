package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.web.server.ResponseStatusException;

import com.web.pfc.SpringPfc.Repository.DoacaoRep;
import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.domain.Doacao;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.DoacaoCriarDTO;
import com.web.pfc.SpringPfc.dto.DoacaoRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceTest {

    @Mock
    DoacaoRep doacaoRep;
    @Mock
    InstituicaoRep instituicaoRep;
    @Mock
    AuditoriaService auditoriaService;
    @InjectMocks
    DoacaoService service;

    private Instituicao escolaRecebedora;
    private Instituicao escolaDoadora;
    private Usuario doador;

    @BeforeEach
    void configurar() {
        escolaRecebedora = instituicao(1L, "Escola Recebedora", "contato@recebedora.edu.br");
        escolaDoadora = instituicao(2L, "Escola Doadora", null);

        doador = new Usuario();
        doador.setId(20L);
        doador.setNome("Diretor Doador");
        doador.setEmail("diretor@doadora.edu.br");
        doador.setPerfil(Perfil.ADMINISTRADOR);
        doador.setInstituicao(escolaDoadora);
    }

    private Instituicao instituicao(Long id, String nome, String chavePix) {
        Instituicao i = new Instituicao();
        i.setId(id);
        i.setNome(nome);
        i.setChavePix(chavePix);
        i.setStatus(StatusInst.ATIVA);
        return i;
    }

    @Test
    void deveRegistrarDoacaoParaOutraInstituicao() {
        when(instituicaoRep.findById(1L)).thenReturn(Optional.of(escolaRecebedora));
        when(doacaoRep.save(any(Doacao.class))).thenAnswer(inv -> {
            Doacao d = inv.getArgument(0);
            d.setId(100L);
            return d;
        });
        ArgumentCaptor<Doacao> captor = ArgumentCaptor.forClass(Doacao.class);

        DoacaoRespDTO resp = service.informar(1L, new DoacaoCriarDTO(new BigDecimal("50.00"), "  Para a biblioteca  "), doador);

        verify(doacaoRep).save(captor.capture());
        Doacao salva = captor.getValue();
        assertEquals(escolaRecebedora, salva.getInstituicao());
        assertEquals(doador, salva.getDoador());
        assertEquals("Para a biblioteca", salva.getMensagem());
        assertEquals(new BigDecimal("50.00"), resp.valor());
        assertEquals("Escola Doadora", resp.instituicaoDoadora());
        verify(auditoriaService).registrarDoacaoInformada(doador, 100L);
    }

    @Test
    void naoDevePermitirDoacaoParaAPropriaInstituicao() {
        escolaDoadora.setChavePix("11222333000144");
        when(instituicaoRep.findById(2L)).thenReturn(Optional.of(escolaDoadora));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.informar(2L, new DoacaoCriarDTO(BigDecimal.TEN, null), doador));

        assertEquals("Não é possível informar uma doação para a sua própria instituição.", ex.getMessage());
        verify(doacaoRep, never()).save(any());
    }

    @Test
    void naoDevePermitirDoacaoQuandoAInstituicaoNaoTemChavePix() {
        escolaRecebedora.setChavePix(null);
        when(instituicaoRep.findById(1L)).thenReturn(Optional.of(escolaRecebedora));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.informar(1L, new DoacaoCriarDTO(BigDecimal.TEN, null), doador));

        assertEquals("Esta instituição não cadastrou uma chave Pix.", ex.getMessage());
        verify(doacaoRep, never()).save(any());
    }

    @Test
    void naoDevePermitirDoacaoParaInstituicaoInativa() {
        escolaRecebedora.setStatus(StatusInst.INATIVA);
        when(instituicaoRep.findById(1L)).thenReturn(Optional.of(escolaRecebedora));

        assertThrows(ResponseStatusException.class,
                () -> service.informar(1L, new DoacaoCriarDTO(BigDecimal.TEN, null), doador));

        verify(doacaoRep, never()).save(any());
        verify(auditoriaService, never()).registrarDoacaoInformada(any(), anyLong());
    }

    @Test
    void deveListarAsDoacoesRecebidasPelaInstituicaoDoUsuario() {
        Usuario diretorRecebedora = new Usuario();
        diretorRecebedora.setInstituicao(escolaRecebedora);

        Doacao doacao = new Doacao();
        doacao.setId(5L);
        doacao.setInstituicao(escolaRecebedora);
        doacao.setDoador(doador);
        doacao.setValor(new BigDecimal("120.00"));
        doacao.setDataHora(LocalDateTime.now());
        when(doacaoRep.findByInstituicao_IdOrderByDataHoraDesc(1L)).thenReturn(List.of(doacao));

        List<DoacaoRespDTO> lista = service.listarDaMinhaInstituicao(diretorRecebedora);

        assertEquals(1, lista.size());
        assertEquals("Diretor Doador", lista.get(0).doadorNome());
        assertEquals("Escola Doadora", lista.get(0).instituicaoDoadora());
        assertNull(lista.get(0).mensagem());
    }
}