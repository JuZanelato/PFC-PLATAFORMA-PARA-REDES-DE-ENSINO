package com.web.pfc.SpringPfc.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@ActiveProfiles("test")
class InstituicaoControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        // Substitui o @AutoConfigureMockMvc: monta o MockMvc manualmente
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // Teste 1: endpoint, caminho feliz (status + corpo)
    @Test
    void deveRetornar201EAInstituicaoCriadaQuandoDadosValidos() throws Exception {
        // Arrange
        String payload = """
            {
              "nome": "Escola Municipal Teste",
              "cnpj": "43144880000182",
              "endereco": "Rua das Flores, 100",
              "status": "ATIVA"
            }
            """;

        // Act + Assert
        mockMvc.perform(post("/api/Instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Escola Municipal Teste"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    // Teste 2: endpoint, erro 4xx (validação do @NotEmpty no nome)
    @Test
    void deveRetornar400QuandoNomeNaoForInformado() throws Exception {
        // Arrange (payload sem o campo "nome")
        String payload = """
            {
              "cnpj": "43144880000182",
              "endereco": "Rua das Flores, 100",
              "status": "ATIVA"
            }
            """;

        // Act + Assert
        mockMvc.perform(post("/api/Instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Exception erro = result.getResolvedException();
                    assertInstanceOf(MethodArgumentNotValidException.class, erro);

                    BindingResult bindingResult =
                            ((MethodArgumentNotValidException) erro).getBindingResult();
                    assertEquals(1, bindingResult.getErrorCount());
                    assertEquals("Obrigatório nome ser preenchido",
                            bindingResult.getFieldError("nome").getDefaultMessage());
                });
    }

    // Teste 4: fluxo completo (criar e depois consultar)
    @Test
    void deveCriarInstituicaoEDepoisConsultarPorId() throws Exception {
        // Arrange
        String payload = """
            {
              "nome": "Instituto Fluxo Completo",
              "cnpj": "99988877000166",
              "endereco": "Rua da Integração, 42",
              "status": "ATIVA"
            }
            """;

        // Act 1: cria e captura o id gerado pelo banco
        String resposta = mockMvc.perform(post("/api/Instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer idCriado = JsonPath.read(resposta, "$.id");

        // Act 2 + Assert: consulta pelo id e confere os dados
        mockMvc.perform(get("/api/Instituicao/" + idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idCriado))
                .andExpect(jsonPath("$.nome").value("Instituto Fluxo Completo"))
                .andExpect(jsonPath("$.cnpj").value("99988877000166"))
                .andExpect(jsonPath("$.endereco").value("Rua da Integração, 42"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    // Teste 5: exclusão (criar, excluir e confirmar que sumiu)
    @Test
    void deveExcluirInstituicaoERetornar404AoConsultarDepois() throws Exception {
        // Arrange
        String payload = """
            {
              "nome": "Instituto Para Excluir",
              "cnpj": "11122233000144",
              "endereco": "Rua da Exclusão, 7",
              "status": "INATIVA"
            }
            """;

        String resposta = mockMvc.perform(post("/api/Instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer idCriado = JsonPath.read(resposta, "$.id");

        // Act: exclui
        mockMvc.perform(delete("/api/Instituicao/" + idCriado))
                .andExpect(status().isNoContent());

        // Assert: o registro não existe mais
        mockMvc.perform(get("/api/Instituicao/" + idCriado))
                .andExpect(status().isNotFound());
    }
}