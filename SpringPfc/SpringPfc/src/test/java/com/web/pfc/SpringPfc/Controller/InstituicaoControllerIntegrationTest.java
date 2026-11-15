package com.web.pfc.SpringPfc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    WebApplicationContext context;
    @Autowired
    InstituicaoRep instituicaoRep;
    @Autowired
    UsuarioRep usuarioRep;
    @Autowired
    JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private Instituicao instituicaoBase;

    @BeforeEach
    void configurar() {

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

        String token = tokenDe(Perfil.ADMINISTRADOR, "admin@integracao.com");

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
    void deveCriarInstituicaoSemLoginNoCadastroDoDiretor() throws Exception {

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cadastradoPor").value("autocadastro"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void deveRetornar403QuandoFuncionarioTentaCadastrarInstituicao() throws Exception {

        String tokenFuncionario = tokenDe(Perfil.FUNCIONARIO, "func@integracao.com");

        mockMvc.perform(post(URL)
                .header("Authorization", tokenFuncionario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Apenas o diretor pode cadastrar instituições."));

        assertFalse(instituicaoRep.existsByCnpj("12345678000199"));
    }

    @Test
    void deveRetornarOsDadosDaPropriaInstituicaoParaODiretor() throws Exception {

        String token = tokenDe(Perfil.ADMINISTRADOR, "diretor@integracao.com");

        mockMvc.perform(get(URL + "/minha").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(instituicaoBase.getId()))
                .andExpect(jsonPath("$.cnpj").value("98765432000111"));
    }

    @Test
    void deveNegarAoDiretorOsDadosDeOutraInstituicao() throws Exception {

        String token = tokenDe(Perfil.ADMINISTRADOR, "diretor2@integracao.com");
        Long outraId = criarInstituicaoViaApi(token);

        mockMvc.perform(get(URL + "/" + outraId).header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Você só pode acessar os dados da sua instituição."));
    }

    @Test
    void deveRetornar403QuandoGestorTentaEditarInstituicao() throws Exception {

        String tokenGestor = tokenDe(Perfil.GESTOR, "gestor@integracao.com");

        mockMvc.perform(put(URL + "/" + instituicaoBase.getId())
                .header("Authorization", tokenGestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
                .andExpect(status().isForbidden());

        assertEquals("Instituição Base", instituicaoRep.findById(instituicaoBase.getId()).orElseThrow().getNome());
    }

    @Test
    void deveListarARedeSemDadosSensiveis() throws Exception {

        String tokenFuncionario = tokenDe(Perfil.FUNCIONARIO, "func2@integracao.com");

        mockMvc.perform(get(URL + "/rede").header("Authorization", tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Instituição Base')]").exists())
                .andExpect(jsonPath("$[0].cnpj").doesNotExist())
                .andExpect(jsonPath("$[0].chavePix").doesNotExist());
    }
}