package com.web.pfc.SpringPfc.Service;
 
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
 
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
 
    private final UsuarioRep usuarioRep;
 
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRep.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}