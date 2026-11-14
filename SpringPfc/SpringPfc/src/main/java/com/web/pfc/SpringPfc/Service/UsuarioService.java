package com.web.pfc.SpringPfc.Service;

import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRep usuarioRep;
    private final AuditoriaService auditoriaService;

        public UsuarioRespDTO toRespDTO(Usuario usuario) {
        return new UsuarioRespDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name(),
                usuario.getInstituicao().getNome(), usuario.isAtivo(), usuario.isAguardandoAprovacao());
    }
    
        public List<UsuarioRespDTO> listarDaInstituicao(Usuario diretor) {
        return usuarioRep.findByInstituicao_IdOrderByNomeAsc(diretor.getInstituicao().getId())
                .stream().map(this::toRespDTO).toList();
    }

    public List<UsuarioRespDTO> listarPendentes(Usuario diretor) {
        return usuarioRep.findByInstituicao_IdAndAguardandoAprovacaoTrueOrderByCriadoEmAsc(diretor.getInstituicao().getId())
                .stream().map(this::toRespDTO).toList();
    }

        public void inativar(Long id, Usuario responsavel) {
        Usuario usuario = buscarDaMesmaInstituicao(id, responsavel);
        if (Objects.equals(usuario.getId(), responsavel.getId())) {
            throw new NegocioException("Você não pode inativar a própria conta.");
        }
        usuario.setAtivo(false);
        usuarioRep.save(usuario);
        auditoriaService.registrarInativacaoUsuario(responsavel, id);
    }
        private Usuario buscarDaMesmaInstituicao(Long id, Usuario diretor) {
        Usuario usuario = usuarioRep.findById(id)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));
        if (!Objects.equals(usuario.getInstituicao().getId(), diretor.getInstituicao().getId())) {
            throw new AccessDeniedException("Você só pode gerenciar usuários da sua instituição.");
        }
        return usuario;
    }

    @Transactional
    public UsuarioRespDTO aprovar(Long id, Usuario diretor) {
        Usuario usuario = buscarDaMesmaInstituicao(id, diretor);
        if (!usuario.isAguardandoAprovacao()) {
            throw new NegocioException("Este usuário não está aguardando aprovação.");
        }
        usuario.setAguardandoAprovacao(false);
        usuarioRep.save(usuario);
        auditoriaService.registrarAprovacaoUsuario(diretor, id);
        return toRespDTO(usuario);
    }

    @Transactional
    public void recusar(Long id, Usuario diretor) {
        Usuario usuario = buscarDaMesmaInstituicao(id, diretor);
        if (!usuario.isAguardandoAprovacao()) {
            throw new NegocioException("Este usuário não está aguardando aprovação.");
        }
        usuario.setAguardandoAprovacao(false);
        usuario.setAtivo(false);
        usuarioRep.save(usuario);
        auditoriaService.registrarRecusaUsuario(diretor, id);
    }
}