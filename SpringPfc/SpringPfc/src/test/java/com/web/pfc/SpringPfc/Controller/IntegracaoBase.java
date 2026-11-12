package com.web.pfc.SpringPfc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.jayway.jsonpath.JsonPath;
import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.Usuario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegracaoBase {

    protected static final String SENHA = "Senha@12345";
    protected static final long ID_INEXISTENTE = 999_999_999L;

    @Value("${local.server.port}")
    protected int porta;

    @Autowired protected InstituicaoRep instituicaoRep;
    @Autowired protected UsuarioRep usuarioRep;
    @Autowired protected PasswordEncoder passwordEncoder;

    protected RestClient http;
    protected String sufixo;
    protected Instituicao instituicao;
    protected Usuario admin;
    protected Usuario gestor;
    protected Usuario funcionario;
    protected String tokenAdmin;
    protected String tokenGestor;
    protected String tokenFuncionario;

    @BeforeEach
    void prepararAmbiente() {
        // Não lança exceção em 4xx/5xx: o teste confere o status
        http = RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (req, res) -> { })
                .build();

        sufixo = String.valueOf(System.nanoTime());   // dados únicos por teste
        instituicao = criarInstituicaoNoBanco("Base" + sufixo);

        admin = criarUsuario(Perfil.ADMINISTRADOR);
        gestor = criarUsuario(Perfil.GESTOR);
        funcionario = criarUsuario(Perfil.FUNCIONARIO);

        // Tokens pelo endpoint real de login
        tokenAdmin = login(admin.getEmail());
        tokenGestor = login(gestor.getEmail());
        tokenFuncionario = login(funcionario.getEmail());
    }

    // ---------- helpers de dados ----------

    protected String cnpjUnico() {
        return String.format("%014d", System.nanoTime() % 100_000_000_000_000L);
    }

    protected Instituicao criarInstituicaoNoBanco(String nome) {
        Instituicao i = new Instituicao();
        i.setNome(nome);
        i.setCnpj(cnpjUnico());
        i.setCep("08000000");
        i.setNumero("100");
        i.setEndereco("Rua dos Testes");
        i.setEmail("seed" + System.nanoTime() + "@teste.com");
        i.setStatus(StatusInst.ATIVA);
        i.setCadastradoPor("seed");
        i.setCriadoEm(LocalDateTime.now());
        return instituicaoRep.save(i);
    }

    protected Usuario criarUsuario(Perfil perfil) {
        Usuario u = new Usuario();
        u.setNome(perfil.name() + " " + sufixo);
        u.setEmail(perfil.name().toLowerCase() + sufixo + "@teste.com");
        u.setSenhaHash(passwordEncoder.encode(SENHA));
        u.setPerfil(perfil);
        u.setInstituicao(instituicao);
        u.setAtivo(true);
        u.setCriadoEm(LocalDateTime.now());
        return usuarioRep.save(u);
    }

    protected String payloadInstituicao(String nome, String cnpj) {
        return """
            {"nome":"%s","cnpj":"%s","cep":"08000000","numero":"100",
             "endereco":"Rua dos Testes","email":"contato%s@teste.com","status":"ATIVA"}
            """.formatted(nome, cnpj, sufixo);
    }

    // ---------- helpers HTTP ----------

    protected ResponseEntity<String> enviar(HttpMethod metodo, String uri, String token, String corpo) {
        RestClient.RequestBodySpec req = http.method(metodo).uri(uri);
        if (token != null) {
            req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (corpo != null) {
            req.contentType(MediaType.APPLICATION_JSON).body(corpo);
        }
        return req.retrieve().toEntity(String.class);
    }

    protected String login(String email) {
        ResponseEntity<String> resposta = enviar(HttpMethod.POST, "/api/auth/login", null,
                """
                {"email":"%s","senha":"%s"}
                """.formatted(email, SENHA));
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        return texto(resposta, "$.token");
    }

    // ---------- helpers de leitura do JSON ----------

    protected String texto(ResponseEntity<String> r, String caminho) {
        String valor = JsonPath.read(r.getBody(), caminho);
        return valor;
    }

    protected long numero(ResponseEntity<String> r, String caminho) {
        Number valor = JsonPath.read(r.getBody(), caminho);
        return valor.longValue();
    }

    protected List<Object> lista(ResponseEntity<String> r, String caminho) {
        List<Object> valor = JsonPath.read(r.getBody(), caminho);
        return valor;
    }
}