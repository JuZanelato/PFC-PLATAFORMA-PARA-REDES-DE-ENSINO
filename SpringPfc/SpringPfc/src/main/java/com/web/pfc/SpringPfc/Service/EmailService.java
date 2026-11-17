package com.web.pfc.SpringPfc.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.web.pfc.SpringPfc.domain.Usuario;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class EmailService {

    private static final String URL_TOKEN = "https://oauth2.googleapis.com/token";
    private static final String URL_ENVIO = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";
    private static final Pattern ACCESS_TOKEN = Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String remetente;
    private final String frontendUrl;
    private final RestClient http = RestClient.create();

    public EmailService(
            @Value("${gmail.client-id:}") String clientId,
            @Value("${gmail.client-secret:}") String clientSecret,
            @Value("${gmail.refresh-token:}") String refreshToken,
            @Value("${gmail.remetente:}") String remetente,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.remetente = remetente;
        this.frontendUrl = frontendUrl;
    }

    
    public boolean estaConfigurado() {
        return !clientId.isBlank() && !clientSecret.isBlank() && !refreshToken.isBlank() && !remetente.isBlank();
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
                """.formatted(escapar(usuario.getNome()), link, token);
        enviar(usuario.getEmail(), "Recuperação de senha – CompartilhaEdu", html);
    }

    void enviar(String para, String assunto, String html) {
        if (!estaConfigurado()) {
            throw new IllegalStateException("Envio de e-mail não configurado.");
        }
        String accessToken = obterAccessToken();
        String raw = montarMensagem(remetente, para, assunto, html);
        http.post()
                .uri(URL_ENVIO)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"raw\":\"" + raw + "\"}")
                .retrieve()
                .toBodilessEntity();
    }


    private String obterAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        String resposta = http.post()
                .uri(URL_TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        Matcher m = ACCESS_TOKEN.matcher(resposta == null ? "" : resposta);
        if (!m.find()) {
            throw new IllegalStateException("O Google não devolveu o access token.");
        }
        return m.group(1);
    }

    static String montarMensagem(String de, String para, String assunto, String html) {
        String assuntoCodificado = "=?UTF-8?B?"
                + Base64.getEncoder().encodeToString(assunto.getBytes(StandardCharsets.UTF_8)) + "?=";
        String corpo = Base64.getMimeEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));
        String mensagem = "From: CompartilhaEdu <" + de + ">\r\n"
                + "To: " + para + "\r\n"
                + "Subject: " + assuntoCodificado + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "\r\n"
                + corpo;
        return Base64.getUrlEncoder().encodeToString(mensagem.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}