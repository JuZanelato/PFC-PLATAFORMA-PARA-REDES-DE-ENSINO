package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class EmailServiceTest {

    @Test
    void naoEstaConfiguradoSemAsCredenciais() {
        EmailService service = new EmailService("", "", "", "", "http://localhost:5173");

        assertFalse(service.estaConfigurado());
        assertThrows(IllegalStateException.class, () -> service.enviar("ana@teste.com", "Assunto", "<p>Oi</p>"));
    }

    @Test
    void estaConfiguradoComAsQuatroCredenciais() {
        EmailService service = new EmailService("id", "segredo", "refresh", "sistema@teste.com", "http://localhost:5173");

        assertTrue(service.estaConfigurado());
    }

    @Test
    void deveMontarAMensagemNoFormatoDoGmail() {
        String raw = EmailService.montarMensagem("sistema@teste.com", "ana@teste.com", "Recuperação de senha", "<p>Olá</p>");

        String mensagem = new String(Base64.getUrlDecoder().decode(raw), StandardCharsets.UTF_8);

        assertTrue(mensagem.contains("From: CompartilhaEdu <sistema@teste.com>"));
        assertTrue(mensagem.contains("To: ana@teste.com"));
        assertTrue(mensagem.contains("Subject: =?UTF-8?B?"));
        assertTrue(mensagem.contains("Content-Type: text/html; charset=UTF-8"));
    }
}