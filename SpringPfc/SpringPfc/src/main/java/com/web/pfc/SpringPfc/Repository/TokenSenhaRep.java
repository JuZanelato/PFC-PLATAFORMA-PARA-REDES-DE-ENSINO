package com.web.pfc.SpringPfc.Repository;
 
import com.web.pfc.SpringPfc.domain.TokenRecSenha;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface TokenSenhaRep extends JpaRepository<TokenRecSenha, Long> {
    Optional<TokenRecSenha> findByTokenAndUtilizadoFalse(String token);
}