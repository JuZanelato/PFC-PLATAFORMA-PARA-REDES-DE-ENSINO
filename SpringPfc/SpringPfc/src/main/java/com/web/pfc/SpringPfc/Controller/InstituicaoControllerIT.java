package com.web.pfc.SpringPfc.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.StatusInst;

@DataJpaTest
@ActiveProfiles("test")
class InstituicaoRepIT {

    @Autowired
    private InstituicaoRep instituicaoRep;

    @Test
    void deveSalvarERecuperarInstituicaoPorId() {
        // Arrange
        Instituicao nova = new Instituicao();
        nova.setNome("Colégio Teste");
        nova.setCnpj("12345678000199");
        nova.setEndereco("Av. Central, 500");
        nova.setStatus(StatusInst.ATIVA);

        // Act
        Instituicao salva = instituicaoRep.save(nova);
        Optional<Instituicao> encontrada = instituicaoRep.findById(salva.getId());

        // Assert
        assertTrue(encontrada.isPresent());
        assertEquals("Colégio Teste", encontrada.get().getNome());
        assertEquals("12345678000199", encontrada.get().getCnpj());
