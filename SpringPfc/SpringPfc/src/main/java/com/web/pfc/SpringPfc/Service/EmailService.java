package com.web.pfc.SpringPfc.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.web.pfc.SpringPfc.domain.Usuario;

@Service
public class EmailService {

    private static final String URL_ENVIO = "https://api.sendgrid.com/v3/mail/send";

    private final String apiKey;
    private final String remetente;
    private final String frontendUrl;
    private final RestClient http = RestClient.create();

    public EmailService(
            @Value("${sendgrid.api-key:}") String apiKey,
            @Value("${sendgrid.remetente:}") String remetente,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.apiKey = apiKey;
        this.remetente = remetente;
        this.frontendUrl = frontendUrl;
    }


    public boolean estaConfigurado() {
        return !apiKey.isBlank() && !remetente.isBlank();
    }

    public void enviarRecuperacaoSenha(Usuario usuario, String token) {
        String link = frontendUrl + "/recuperar-senha?token=" + token;
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:520px;color:#1f2430">
                  <h2 style="color:#7a1f3d;margin:0 0 16px">CompartilhaEdu</h2>
                  <p>Olá, %s.</p>
                  <p>Recebemos um pedido para redefinir a senha da sua conta.</p>
                  <p style="margin:24px 0">
                    <a href="%s" style="background:#7a1f3d;color:#ffffff;padding:12px 20px;border-radius:6px;text-decoration:none;font-weight:bold">Criar nova senha</a>
                  </p>
                  <p>Se o botão não funcionar, use este código na tela de recuperação:</p>
                  <p style="font-family:monospace;background:#f4f2f5;padding:10px;border-radius:6px">%s</p>
                  <p style="color:#6b6375;font-size:13px">O link e o código valem por 1 hora e só podem ser usados uma vez.
                  Se você não pediu a troca de senha, ignore este e-mail: sua senha continua a mesma.</p>
                </div>
                """.formatted(escaparHtml(usuario.getNome()), link, token);
        enviar(usuario.getEmail(), "Recuperação de senha – CompartilhaEdu", html);
    }

    void enviar(String para, String assunto, String html) {
        if (!estaConfigurado()) {
            throw new IllegalStateException("Envio de e-mail não configurado.");
        }
        http.post()
                .uri(URL_ENVIO)
                .header("Authorization", "Bearer " + apiKey)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(montarCorpo(remetente, para, assunto, html))
                .retrieve()
                .toBodilessEntity();
    }

    
    static String montarCorpo(String de, String para, String assunto, String html) {
        return "{"
                + "\"personalizations\":[{\"to\":[{\"email\":\"" + json(para) + "\"}]}],"
                + "\"from\":{\"email\":\"" + json(de) + "\",\"name\":\"CompartilhaEdu\"},"
                + "\"subject\":\"" + json(assunto) + "\","
                + "\"content\":[{\"type\":\"text/html\",\"value\":\"" + json(html) + "\"}]"
                + "}";
    }

    
    static String json(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private static String escaparHtml(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}