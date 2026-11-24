package com.web.pfc.SpringPfc.Repository;

import com.web.pfc.SpringPfc.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.web.pfc.SpringPfc.domain.Perfil;

import java.util.List;
import java.util.Optional;

public interface UsuarioRep extends JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(TRIM(:email))")
    Optional<Usuario> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE LOWER(u.email) = LOWER(TRIM(:email))")
    boolean existsByEmail(@Param("email") String email);

    boolean existsByInstituicao_IdAndPerfilAndAtivoTrue(Long instituicaoId, Perfil perfil);

    long countByInstituicao_IdAndPerfilAndAtivoTrue(Long instituicaoId, Perfil perfil);

    List<Usuario> findByInstituicao_IdOrderByNomeAsc(Long instituicaoId);

    List<Usuario> findByInstituicao_IdAndAguardandoAprovacaoTrueOrderByCriadoEmAsc(Long instituicaoId);
}