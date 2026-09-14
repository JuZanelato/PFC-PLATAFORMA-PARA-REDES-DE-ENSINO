package com.web.pfc.SpringPfc.Service;
 
import com.web.pfc.SpringPfc.Repository.TokenSenhaRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.TokenRecSenha;
import com.web.pfc.SpringPfc.domain.Usuario;
import com.web.pfc.SpringPfc.dto.RedefSenhaDTO;
import com.web.pfc.SpringPfc.dto.SolicSenhaDTO;
import com.web.pfc.SpringPfc.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
 
import java.time.LocalDateTime;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
public class SenhaService {
 
    private final UsuarioRep usuarioRep;
    private final TokenSenhaRep tokenSenhaRep;
    private final PasswordEncoder passwordEncoder;
 
    public void solicitar(SolicSenhaDTO dto) {
        usuarioRep.findByEmail(dto.email()).ifPresent(usuario -> {
            TokenRecSenha token = new TokenRecSenha();
            token.setUsuario(usuario);
            token.setToken(UUID.randomUUID().toString());
            token.setExpiraEm(LocalDateTime.now().plusHours(1));
            tokenSenhaRep.save(token);
            // enviarEmail(usuario.getEmail(), token.getToken()); // implementar depois
        });
    }
 
    public void redefinir(RedefSenhaDTO dto) {
        TokenRecSenha token = tokenSenhaRep.findByTokenAndUtilizadoFalse(dto.token())
            .filter(t -> t.getExpiraEm().isAfter(LocalDateTime.now()))
            .orElseThrow(() -> new NegocioException("Token inválido ou expirado."));
 
        Usuario usuario = token.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
        usuarioRep.save(usuario);
 
        token.setUtilizado(true);
        tokenSenhaRep.save(token);
    }
}