package com.web.pfc.SpringPfc.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.StatusInst;

import jakarta.persistence.EntityManager;

/**
 * Teste de persistência com banco real de teste (H2 em memória).
 * A transação é revertida ao final, mantendo o banco limpo.
 */
@SpringBootTest
@Transactional
class InstituicaoRepIntegrationTest {

    @Autowired InstituicaoRep instituicaoRep;
    @Autowired EntityManager entityManager;

    @Test
    void deveSalvarERecuperarInstituicaoPorId() {
        // Arrange
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Escola Persistência");
        instituicao.setCnpj("11222333000144");
        instituicao.setNumero("50");
        instituicao.setEndereco("Rua do Banco, 50");
        instituicao.setEmail("persistencia@escola.edu.br");
        instituicao.setStatus(StatusInst.ATIVA);
        instituicao.setChavePix("11222333000144");

        // Act
        Instituicao salva = instituicaoRep.save(instituicao);
        entityManager.flush();   // força o INSERT no H2
        entityManager.clear();   // descarta o cache para ler de verdade do banco
        Optional<Instituicao> recuperada = instituicaoRep.findById(salva.getId());

        // Assert
        assertNotNull(salva.getId());
        assertTrue(recuperada.isPresent());
        assertEquals("Escola Persistência", recuperada.get().getNome());
        assertEquals(StatusInst.ATIVA, recuperada.get().getStatus());
        assertEquals("11222333000144", recuperada.get().getChavePix());
    }
}