package com.web.pfc.SpringPfc.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailServiceTest {

    @Test
    void naoEstaConfiguradoSemAsCredenciais() {
        EmailService service = new EmailService("", "", "http://localhost:5173");

        assertFalse(service.estaConfigurado());
        assertThrows(IllegalStateException.class, () -> service.enviar("ana@teste.com", "Assunto", "<p>Oi</p>"));
    }

    @Test
    void estaConfiguradoComAChaveEORemetente() {
        EmailService service = new EmailService("chave-de-teste", "sistema@teste.com", "http://localhost:5173");

        assertTrue(service.estaConfigurado());
    }

    @Test
    void deveMontarOCorpoNoFormatoDoSendGrid() {
        String corpo = EmailService.montarCorpo("sistema@teste.com", "ana@teste.com", "Recuperação de senha",
                "<a href=\"http://x\">Link</a>\n");

        assertEquals("{\"personalizations\":[{\"to\":[{\"email\":\"ana@teste.com\"}]}],"
                + "\"from\":{\"email\":\"sistema@teste.com\",\"name\":\"CompartilhaEdu\"},"
                + "\"subject\":\"Recuperação de senha\","
                + "\"content\":[{\"type\":\"text/html\",\"value\":\"<a href=\\\"http://x\\\">Link</a>\\n\"}]}", corpo);
    }
}