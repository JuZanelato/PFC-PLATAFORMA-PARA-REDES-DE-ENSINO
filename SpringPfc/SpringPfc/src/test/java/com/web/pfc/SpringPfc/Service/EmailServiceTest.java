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
    void deveMontarOCorpoNoFormatoDoBrevo() {
        String corpo = EmailService.montarCorpo("sistema@teste.com", "ana@teste.com", "Recuperação de senha",
                "<a href=\"http://x\">Link</a>\n");

        assertEquals("{\"sender\":{\"name\":\"CompartilhaEdu\",\"email\":\"sistema@teste.com\"},"
                + "\"to\":[{\"email\":\"ana@teste.com\"}],"
                + "\"subject\":\"Recuperação de senha\","
                + "\"htmlContent\":\"<a href=\\\"http://x\\\">Link</a>\\n\"}", corpo);
    }
}