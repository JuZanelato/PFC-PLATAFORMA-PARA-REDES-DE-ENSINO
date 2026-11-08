package com.web.pfc.SpringPfc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;
import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import com.web.pfc.SpringPfc.config.JwtUtil;
import com.web.pfc.SpringPfc.domain.Instituicao;
import com.web.pfc.SpringPfc.domain.Perfil;
import com.web.pfc.SpringPfc.domain.StatusInst;
import com.web.pfc.SpringPfc.domain.Usuario;

import jakarta.servlet.Filter;

/**
 * Testes de integração da API de Instituições: Controller + Spring Security (JWT)
 * + Repository + banco H2 em memória (src/test/resources/application.properties).
 * Cada teste roda em transação revertida ao final, então nenhum depende de outro
 * nem de dados do banco de desenvolvimento.
 */
@SpringBootTest
@Transactional
class InstituicaoControllerIntegrationTest {

    private static final String URL = "/api/Instituicao";

    private static final String JSON_VALIDO = """
            {
              "nome": "Escola Estadual Integração",
              "cnpj": "12345678000199",
              "cep": "01001000",
              "numero": "100",
              "endereco": "Praça da Sé, 100 - Sé, São Paulo/SP",
              "email": "contato@integracao.edu.br",
              "status": "ATIVA",
              "chavePix": "contato@integracao.edu.br"
            }
            """;

    @Autowired WebApplicationContext context;
    @Autowired InstituicaoRep instituicaoRep;
    @Autowired UsuarioRep usuarioRep;
    @Autowired JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private Instituicao instituicaoBase;

    @BeforeEach
    void configurar() {
        // Inclui a cadeia de filtros do Spring Security (com o JwtAuthFilter real)
        Filter segurancaFilter = context.getBean("springSecurityFilterChain", Filter.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(segurancaFilter).build();

        Instituicao base = new Instituicao();
        base.setNome("Instituição Base");
        base.setCnpj("98765432000111");
        base.setNumero("1");
        base.setEndereco("Rua Base, 1");
        base.setEmail("base@escola.edu.br");
        base.setStatus(StatusInst.ATIVA);
        instituicaoBase = instituicaoRep.save(base);
    }

    /** Cria um usuário real no H2 e devolve um token JWT válido para ele. */
    private String tokenDe(Perfil perfil, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário " + perfil.name());
        usuario.setEmail(email);
        usuario.setSenhaHash("hash-irrelevante-no-teste");
        usuario.setPerfil(perfil);
        usuario.setInstituicao(instituicaoBase);
        usuario.setAtivo(true);
        usuarioRep.save(usuario);
        return "Bearer " + jwtUtil.gerarToken(usuario);
    }

    private Long criarInstituicaoViaApi(String token) throws Exception {
        MvcResult resultado = mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    @Test
    void deveRetornar201EAInstituicaoCriadaQuandoDadosValidos() throws Exception {
        // Arrange
        String token = tokenDe(Perfil.ADMINISTRADOR, "admin@integracao.com");

        // Act + Assert
        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Escola Estadual Integração"))
                .andExpect(jsonPath("$.cadastradoPor").value("admin@integracao.com"));
    }

    @Test
    void deveRetornar403ComMensagemQuandoFuncionarioTentaExcluir() throws Exception {
        // Arrange
        String tokenFuncionario = tokenDe(Perfil.FUNCIONARIO, "func@integracao.com");

        // Act + Assert
        mockMvc.perform(delete(URL + "/" + instituicaoBase.getId())
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Você não tem permissão para executar esta ação."));

        // A instituição continua no banco
        assertTrue(instituicaoRep.findById(instituicaoBase.getId()).isPresent());
    }

    @Test
    void deveCriarInstituicaoEDepoisConsultarPorId() throws Exception {
        // Arrange
        String token = tokenDe(Perfil.GESTOR, "gestor@integracao.com");

        // Act
        Long id = criarInstituicaoViaApi(token);

        // Assert
        mockMvc.perform(get(URL + "/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cnpj").value("12345678000199"))
                .andExpect(jsonPath("$.chavePix").value("contato@integracao.edu.br"));
        assertEquals("Escola Estadual Integração", instituicaoRep.findById(id).orElseThrow().getNome());
    }

    @Test
    void deveExcluirInstituicaoERetornar404AoConsultarDepois() throws Exception {
        // Arrange
        String token = tokenDe(Perfil.ADMINISTRADOR, "admin2@integracao.com");
        Long id = criarInstituicaoViaApi(token);

        // Act
        mockMvc.perform(delete(URL + "/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());

        // Assert
        mockMvc.perform(get(URL + "/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
        assertFalse(instituicaoRep.findById(id).isPresent());
    }
}