package com.web.pfc.SpringPfc.Repository;

import com.web.pfc.SpringPfc.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import com.web.pfc.SpringPfc.domain.Perfil;

import java.util.List;
import java.util.Optional;

public interface UsuarioRep extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByInstituicao_IdAndPerfilAndAtivoTrue(Long instituicaoId, Perfil perfil);

    List<Usuario> findByInstituicao_IdOrderByNomeAsc(Long instituicaoId);

    List<Usuario> findByInstituicao_IdAndAguardandoAprovacaoTrueOrderByCriadoEmAsc(Long instituicaoId);
}

