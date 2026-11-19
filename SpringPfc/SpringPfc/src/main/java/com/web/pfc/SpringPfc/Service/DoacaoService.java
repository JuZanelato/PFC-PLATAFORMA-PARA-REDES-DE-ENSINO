package com.web.pfc.SpringPfc.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.web.pfc.SpringPfc.Repository.DoacaoRep;
import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.domain.Doacao;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.DoacaoCriarDTO;
import com.web.pfc.SpringPfc.dto.DoacaoRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoacaoService {

    private final DoacaoRep doacaoRep;
    private final InstituicaoRep instituicaoRep;
    private final AuditoriaService auditoriaService;


    public DoacaoRespDTO informar(Long instituicaoId, DoacaoCriarDTO dto, Usuario usuario) {
        Instituicao instituicao = instituicaoRep.findById(instituicaoId)
                .filter(i -> i.getStatus() != StatusInst.INATIVA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada"));

        if (Objects.equals(instituicao.getId(), usuario.getInstituicao().getId())) {
            throw new NegocioException("Não é possível informar uma doação para a sua própria instituição.");
        }
        if (instituicao.getChavePix() == null || instituicao.getChavePix().isBlank()) {
            throw new NegocioException("Esta instituição não cadastrou uma chave Pix.");
        }

        Doacao doacao = new Doacao();
        doacao.setInstituicao(instituicao);
        doacao.setDoador(usuario);
        doacao.setValor(dto.valor());
        doacao.setMensagem(dto.mensagem() == null || dto.mensagem().isBlank() ? null : dto.mensagem().trim());
        doacao.setDataHora(LocalDateTime.now());

        Doacao salva = doacaoRep.save(doacao);
        auditoriaService.registrarDoacaoInformada(usuario, salva.getId());
        return paraDTO(salva);
    }

    
    public List<DoacaoRespDTO> listarDaMinhaInstituicao(Usuario usuario) {
        return doacaoRep.findByInstituicao_IdOrderByDataHoraDesc(usuario.getInstituicao().getId()).stream()
                .map(this::paraDTO)
                .toList();
    }

    private DoacaoRespDTO paraDTO(Doacao d) {
        return new DoacaoRespDTO(d.getId(), d.getValor(), d.getMensagem(), d.getDoador().getNome(),
                d.getDoador().getInstituicao().getNome(), d.getDataHora());
    }
}