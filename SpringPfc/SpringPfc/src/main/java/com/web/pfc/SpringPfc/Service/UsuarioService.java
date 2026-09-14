package com.web.pfc.SpringPfc.Service;

import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.UsuarioRespDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRep usuarioRep;

    public UsuarioRespDTO toRespDTO(Usuario usuario) {
        return new UsuarioRespDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name(),
                usuario.getInstituicao().getNome());
    }

    public List<UsuarioRespDTO> listarTodos() {
        return usuarioRep.findAll().stream().map(this::toRespDTO).toList();
    }

    public void inativar(Long id) {
        Usuario usuario = usuarioRep.findById(id).orElseThrow(() -> new NegocioException("Usuário não encontrado."));
        usuario.setAtivo(false);
        usuarioRep.save(usuario);
    }
}
