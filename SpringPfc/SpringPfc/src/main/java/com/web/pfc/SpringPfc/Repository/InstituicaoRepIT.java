package com.web.pfc.SpringPfc.Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InstituicaoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


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


        mockMvc.perform(post("/api/Instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Escola Municipal Teste"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }


    @Test
    void deveRetornar400QuandoNomeNaoForInformado() throws Exception {
  
        String payload = """
            {
              "cnpj": "43144880000182",
              "endereco": "Rua das Flores, 100",
              "status": "ATIVA"
            }
            """;

     
        mockMvc.perform(post("/api/Instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

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

        String resposta = mockMvc.perform(post("/api/Instituicao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(resposta);
        int idCriado = json.get("id").asInt();

        mockMvc.perform(get("/api/Instituicao/" + idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idCriado))
                .andExpect(jsonPath("$.nome").value("Instituto Fluxo Completo"))
                .andExpect(jsonPath("$.cnpj").value("99988877000166"))
                .andExpect(jsonPath("$.endereco").value("Rua da Integração, 42"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }
}
