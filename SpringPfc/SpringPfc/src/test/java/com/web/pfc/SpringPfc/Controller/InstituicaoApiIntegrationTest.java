package com.web.pfc.SpringPfc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.web.pfc.SpringPfc.domain.Instituicao;

class InstituicaoApiIntegrationTest extends IntegracaoBase {

    // ===== POST /api/Instituicao =====

    @Test
    void postInstituicao_adminComDadosValidos_retorna201ECorpo() {
        // Arrange
        String nome = "EscolaNova" + sufixo;
        String payload = payloadInstituicao(nome, cnpjUnico());

        // Act
        ResponseEntity<String> resposta = enviar(HttpMethod.POST, "/api/Instituicao", tokenAdmin, payload);

        // Assert
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertTrue(numero(resposta, "$.id") > 0);
        assertEquals(nome, texto(resposta, "$.nome"));
        assertEquals("ATIVA", texto(resposta, "$.status"));
        assertEquals(admin.getEmail(), texto(resposta, "$.cadastradoPor"));
    }

    @Test
    void postInstituicao_aposCriar_persisteEPodeSerConsultada() {
        // Arrange
        String nome = "Persistida" + sufixo;
        ResponseEntity<String> criada = enviar(HttpMethod.POST, "/api/Instituicao", tokenAdmin,
                payloadInstituicao(nome, cnpjUnico()));
        long id = numero(criada, "$.id");

        // Act
        ResponseEntity<String> consulta = enviar(HttpMethod.GET, "/api/Instituicao/" + id, tokenAdmin, null);

        // Assert
        assertEquals(HttpStatus.OK, consulta.getStatusCode());
        assertEquals(nome, texto(consulta, "$.nome"));
        assertTrue(instituicaoRep.findById(id).isPresent());
    }

    @Test
    void postInstituicao_semNome_retorna400ENaoPersiste() {
        // Arrange
        long antes = instituicaoRep.count();
        String payload = payloadInstituicao("", cnpjUnico());

        // Act
        ResponseEntity<String> resposta = enviar(HttpMethod.POST, "/api/Instituicao", tokenAdmin, payload);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(antes, instituicaoRep.count());
    }

    // ===== GET /api/Instituicao/{id} =====

    @Test
    void getInstituicaoPorId_idExistente_retorna200ECorpo() {
        // Arrange
        Instituicao existente = criarInstituicaoNoBanco("Existente" + sufixo);

        // Act
        ResponseEntity<String> resposta = enviar(HttpMethod.GET,
                "/api/Instituicao/" + existente.getId(), tokenFuncionario, null);

        // Assert
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(existente.getId(), numero(resposta, "$.id"));
        assertEquals("Existente" + sufixo, texto(resposta, "$.nome"));
        assertEquals(existente.getCnpj(), texto(resposta, "$.cnpj"));
    }

    @Test
    void getInstituicaoPorId_idInexistente_retorna404() {
        ResponseEntity<String> resposta = enviar(HttpMethod.GET,
                "/api/Instituicao/" + ID_INEXISTENTE, tokenAdmin, null);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }

    // ===== GET /api/Instituicao (lista com filtro) =====

    @Test
    void getInstituicoes_filtroPorNome_retorna200ListaFiltradaEContentTypeJson() {
        // Arrange
        String nome = "Filtro" + sufixo;
        criarInstituicaoNoBanco(nome);
        criarInstituicaoNoBanco("Outra" + sufixo);

        // Act
        ResponseEntity<String> resposta = enviar(HttpMethod.GET,
                "/api/Instituicao?nome=" + nome, tokenAdmin, null);

        // Assert
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(1, lista(resposta, "$").size());
        assertEquals(nome, texto(resposta, "$[0].nome"));
        MediaType tipo = resposta.getHeaders().getContentType();
        assertNotNull(tipo);
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(tipo));
    }

    // ===== GET /api/Instituicao/publicas =====

    @Test
    void getInstituicoesPublicas_semToken_retorna200SomenteComIdENome() {
        // Arrange
        Instituicao publica = criarInstituicaoNoBanco("Publica" + sufixo);

        // Act
        ResponseEntity<String> resposta = enviar(HttpMethod.GET, "/api/Instituicao/publicas", null, null);

        // Assert
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        String filtro = "$[?(@.id==" + publica.getId() + ")]";
        assertEquals(1, lista(resposta, filtro + ".nome").size());
        assertEquals("Publica" + sufixo, lista(resposta, filtro + ".nome").get(0));
        assertTrue(lista(resposta, filtro + ".cnpj").isEmpty());
    }

    // ===== PUT /api/Instituicao/{id} =====

    @Test
    void putInstituicao_gestorComDadosValidos_retorna204EAtualizaRegistro() {
        // Arrange
        Instituicao existente = criarInstituicaoNoBanco("Antiga" + sufixo);
        String novoNome = "Atualizada" + sufixo;

        // Act
        ResponseEntity<String> resposta = enviar(HttpMethod.PUT,
                "/api/Instituicao/" + existente.getId(), tokenGestor,
                payloadInstituicao(novoNome, existente.getCnpj()));

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertTrue(resposta.getBody() == null || resposta.getBody().isEmpty());
        ResponseEntity<String> consulta = enviar(HttpMethod.GET,
                "/api/Instituicao/" + existente.getId(), tokenGestor, null);
        assertEquals(novoNome, texto(consulta, "$.nome"));
        assertEquals("seed", texto(consulta, "$.cadastradoPor"));
    }

    @Test
    void putInstituicao_idInexistente_retorna404() {
        ResponseEntity<String> resposta = enviar(HttpMethod.PUT,
                "/api/Instituicao/" + ID_INEXISTENTE, tokenAdmin,
                payloadInstituicao("Qualquer" + sufixo, cnpjUnico()));

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }

    // ===== DELETE /api/Instituicao/{id} : trio de segurança =====

    @Test
    void deleteInstituicao_semToken_retorna401() {
        Instituicao alvo = criarInstituicaoNoBanco("Alvo" + sufixo);

        ResponseEntity<String> resposta = enviar(HttpMethod.DELETE,
                "/api/Instituicao/" + alvo.getId(), null, null);

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
        assertTrue(instituicaoRep.existsById(alvo.getId()));
    }

    @Test
    void deleteInstituicao_perfilGestor_retorna403() {
        Instituicao alvo = criarInstituicaoNoBanco("Alvo" + sufixo);

        ResponseEntity<String> resposta = enviar(HttpMethod.DELETE,
                "/api/Instituicao/" + alvo.getId(), tokenGestor, null);

        assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
        assertTrue(instituicaoRep.existsById(alvo.getId()));
    }

    @Test
    void deleteInstituicao_perfilAdministrador_retorna204ERemoveRegistro() {
        Instituicao alvo = criarInstituicaoNoBanco("Alvo" + sufixo);

        ResponseEntity<String> resposta = enviar(HttpMethod.DELETE,
                "/api/Instituicao/" + alvo.getId(), tokenAdmin, null);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertFalse(instituicaoRep.existsById(alvo.getId()));
        ResponseEntity<String> consulta = enviar(HttpMethod.GET,
                "/api/Instituicao/" + alvo.getId(), tokenAdmin, null);
        assertEquals(HttpStatus.NOT_FOUND, consulta.getStatusCode());
    }
}