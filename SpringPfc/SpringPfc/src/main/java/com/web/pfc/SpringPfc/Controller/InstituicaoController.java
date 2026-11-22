package com.web.pfc.SpringPfc.Controller;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.RecursoRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.Service.AuditoriaService;
import com.web.pfc.SpringPfc.Service.InstituicaoExclusaoService;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.StatusRecurso;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.ExcluirInstituicaoDTO;
import com.web.pfc.SpringPfc.dto.InstituicaoDetalheDTO;
import com.web.pfc.SpringPfc.dto.InstituicaoRedeDTO;
import com.web.pfc.SpringPfc.dto.InstituicaoResumoDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;

@RestController
@RequestMapping("api/Instituicao")
public class InstituicaoController {

        private InstituicaoRep instituicaorep;
        private AuditoriaService auditoriaService;
        private UsuarioRep usuarioRep;
        private RecursoRep recursoRep;
        private InstituicaoExclusaoService instituicaoExclusaoService;

        public InstituicaoController(InstituicaoRep instituicaorep, AuditoriaService auditoriaService,
                        UsuarioRep usuarioRep, RecursoRep recursoRep,
                        InstituicaoExclusaoService instituicaoExclusaoService) {
                this.instituicaorep = instituicaorep;
                this.auditoriaService = auditoriaService;
                this.usuarioRep = usuarioRep;
                this.recursoRep = recursoRep;
                this.instituicaoExclusaoService = instituicaoExclusaoService;
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Instituicao save(
                        @RequestBody @Valid Instituicao instituicao,
                        @AuthenticationPrincipal Usuario usuarioLogado) {

                if (usuarioLogado != null && usuarioLogado.getPerfil() != Perfil.ADMINISTRADOR) {
                        throw new AccessDeniedException("Apenas o diretor pode cadastrar instituições.");
                }

                String cnpj = instituicao.getCnpj().replaceAll("\\D", "");
                if (instituicaorep.existsByCnpj(cnpj)) {
                        throw new NegocioException("Já existe uma instituição cadastrada com este CNPJ.");
                }

                String responsavel = (usuarioLogado != null) ? usuarioLogado.getEmail() : "autocadastro";

                instituicao.setId(null);
                instituicao.setCnpj(cnpj);
                instituicao.setChavePix(limparPix(instituicao.getChavePix()));
                instituicao.setStatus(StatusInst.ATIVA);
                instituicao.setCadastradoPor(responsavel);
                instituicao.setCriadoEm(LocalDateTime.now());

                Instituicao salva = instituicaorep.save(instituicao);
                auditoriaService.registrarCriacaoInstituicao(responsavel, salva.getId());

                return salva;
        }


        @GetMapping("/minha")
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        public Instituicao minha(@AuthenticationPrincipal Usuario usuarioLogado) {
                return buscarOuFalhar(usuarioLogado.getInstituicao().getId());
        }


        @GetMapping("{id}")
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        public Instituicao getInstituicaoById(
                        @PathVariable("id") Long id,
                        @AuthenticationPrincipal Usuario usuarioLogado) {
                validarPropriaInstituicao(id, usuarioLogado);
                return buscarOuFalhar(id);
        }


        @PutMapping("{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        public void update(
                        @PathVariable Long id,
                        @RequestBody @Valid Instituicao instituicao,
                        @AuthenticationPrincipal Usuario usuarioLogado) {

                validarPropriaInstituicao(id, usuarioLogado);
                Instituicao existente = buscarOuFalhar(id);

                String cnpj = instituicao.getCnpj().replaceAll("\\D", "");
                if (!cnpj.equals(existente.getCnpj()) && instituicaorep.existsByCnpj(cnpj)) {
                        throw new NegocioException("Já existe uma instituição cadastrada com este CNPJ.");
                }

                String pixAnterior = existente.getChavePix();

                instituicao.setId(existente.getId());
                instituicao.setCnpj(cnpj);
                instituicao.setChavePix(limparPix(instituicao.getChavePix()));
                instituicao.setStatus(existente.getStatus());
                instituicao.setCadastradoPor(existente.getCadastradoPor());
                instituicao.setCriadoEm(existente.getCriadoEm());

                instituicaorep.save(instituicao);
                auditoriaService.registrarAlteracaoInstituicao(usuarioLogado, id);

        
                if (!Objects.equals(pixAnterior, instituicao.getChavePix())) {
                        if (instituicao.getChavePix() == null) {
                                auditoriaService.registrarRemocaoPix(usuarioLogado, id);
                        } else {
                                auditoriaService.registrarAlteracaoPix(usuarioLogado, id);
                        }
                }
        }


        
        @PostMapping("/minha/excluir")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        public void excluirMinha(
                        @RequestBody @Valid ExcluirInstituicaoDTO dto,
                        @AuthenticationPrincipal Usuario usuarioLogado) {
                instituicaoExclusaoService.excluirMinhaInstituicao(usuarioLogado, dto);
        }


        @GetMapping("/rede")
        public List<InstituicaoRedeDTO> listarRede() {
                return instituicaorep.findAll().stream()
                                .filter(i -> i.getStatus() != StatusInst.INATIVA)
                                .map(i -> new InstituicaoRedeDTO(i.getId(), i.getNome(), i.getEndereco(), i.getEmail()))
                                .toList();
        }


        @GetMapping("{id}/detalhes")
        public InstituicaoDetalheDTO detalhes(
                        @PathVariable("id") Long id,
                        @AuthenticationPrincipal Usuario usuarioLogado) {

                Instituicao instituicao = buscarOuFalhar(id);
                boolean minha = Objects.equals(id, usuarioLogado.getInstituicao().getId());

                if (!minha && instituicao.getStatus() == StatusInst.INATIVA) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada");
                }

                List<InstituicaoDetalheDTO.UsuarioDaEscola> usuarios = List.of();
                if (minha) {
                        usuarios = usuarioRep.findByInstituicao_IdOrderByNomeAsc(id).stream()
                                        .filter(u -> u.isAtivo() && !u.isAguardandoAprovacao())
                                        .map(u -> new InstituicaoDetalheDTO.UsuarioDaEscola(u.getNome(), u.getPerfil().name()))
                                        .toList();
                }

                List<InstituicaoDetalheDTO.RecursoDaEscola> recursos = recursoRep.findByInstituicaoId(id).stream()
                                .filter(r -> r.getStatus() == StatusRecurso.ATIVO)
                                .sorted(Comparator.comparing(r -> r.getNome().toLowerCase()))
                                .map(r -> new InstituicaoDetalheDTO.RecursoDaEscola(r.getId(), r.getNome(),
                                                r.getCategoria().name(), r.getQuantidade(),
                                                r.getCondicao().name(), r.isDisponivel()))
                                .toList();

                return new InstituicaoDetalheDTO(instituicao.getId(), instituicao.getNome(), instituicao.getEndereco(),
                                instituicao.getNumero(), instituicao.getCep(), instituicao.getEmail(),
                                instituicao.getChavePix(), minha,
                                usuarios, recursos);
        }


        @GetMapping("/publicas")
        public List<InstituicaoResumoDTO> listarPublicas() {
                return instituicaorep.findAll().stream()
                                .filter(i -> i.getStatus() != StatusInst.INATIVA)
                                .map(i -> new InstituicaoResumoDTO(i.getId(), i.getNome()))
                                .toList();
        }

        
        private String limparPix(String chavePix) {
                return (chavePix == null || chavePix.isBlank()) ? null : chavePix.trim();
        }

        private Instituicao buscarOuFalhar(Long id) {
                return instituicaorep.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Instituição não encontrada"));
        }

        private void validarPropriaInstituicao(Long id, Usuario usuarioLogado) {
                if (!Objects.equals(id, usuarioLogado.getInstituicao().getId())) {
                        throw new AccessDeniedException("Você só pode acessar os dados da sua instituição.");
                }
        }
}