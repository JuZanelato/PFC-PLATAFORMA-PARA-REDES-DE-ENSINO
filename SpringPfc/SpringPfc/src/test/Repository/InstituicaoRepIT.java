package com.web.pfc.SpringPfc.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.StatusInst;

@SpringBootTest
@ActiveProfiles("test")
@Transactional 
class InstituicaoRepIT {

    @Autowired
    private InstituicaoRep instituicaoRep;

    
    @Test
    void deveSalvarERecuperarInstituicaoPorId() {
        
        Instituicao nova = new Instituicao();
        nova.setNome("Colégio Teste");
        nova.setCnpj("12345678000199");
        nova.setEndereco("Av. Central, 500");
        nova.setStatus(StatusInst.ATIVA);

        
        Instituicao salva = instituicaoRep.save(nova);
        Optional<Instituicao> encontrada = instituicaoRep.findById(salva.getId());

        
        assertTrue(encontrada.isPresent());
        assertEquals("Colégio Teste", encontrada.get().getNome());
        assertEquals("12345678000199", encontrada.get().getCnpj());
        assertEquals("Av. Central, 500", encontrada.get().getEndereco());
        assertEquals(StatusInst.ATIVA, encontrada.get().getStatus());
    }
}