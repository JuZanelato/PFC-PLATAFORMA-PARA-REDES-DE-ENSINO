# CompartilhaEdu — Plataforma para Redes de Ensino

Projeto Final de Curso (PFC): plataforma digital para **compartilhamento e gestão de recursos entre instituições de ensino** de uma mesma rede. Uma escola cadastra os recursos que pode emprestar (materiais didáticos, equipamentos, mobiliário etc.), e as outras escolas da rede encontram esses recursos, pedem emprestado e acompanham o empréstimo até a devolução.

## Sistema online

| Parte | Endereço |
|---|---|
| Site (front-end) | https://compartilhaedu.onrender.com |
| API (back-end) | https://compartilhaedu-api.onrender.com |
| Documentação da API (Swagger) | https://compartilhaedu-api.onrender.com/swagger-ui.html |

> O sistema está hospedado no plano gratuito do Render. Depois de 15 minutos sem uso o servidor "dorme", e o primeiro acesso seguinte pode levar cerca de 1 minuto para responder.

## Funcionalidades

| Módulo | O que faz |
|---|---|
| **Usuários e acesso (BF01)** | Cadastro e login com JWT; três cargos (Diretor, Gestor e Funcionário); o Gestor que se cadastra sozinho aguarda aprovação do Diretor; o Diretor pode cadastrar outros usuários, inclusive outro Diretor, e inativar ou reativar contas; recuperação de senha por e-mail; tela "Meus dados", onde o usuário corrige os dados e pode desativar a própria conta; Política de Privacidade (LGPD) |
| **Instituições (BF02)** | O Diretor cadastra e edita a própria escola (com preenchimento do endereço pelo CEP); todos consultam as escolas da rede e veem os detalhes de cada uma (dados e recursos) |
| **Recursos (BF03)** | Cadastro, edição, inativação e controle de disponibilidade dos recursos da escola |
| **Busca e solicitação (BF04)** | Busca de recursos de outras escolas por nome, categoria e instituição; pedido com quantidade, justificativa e **data prevista de devolução** |
| **Empréstimo e histórico (BF05)** | O Diretor aprova ou recusa; o Gestor registra a retirada e a devolução; histórico de cada pedido e destaque para devoluções **atrasadas** |
| **Dashboard e auditoria (BF06)** | Painel com indicadores da rede; auditoria das ações dos usuários, restrita à instituição de cada Diretor |
| **Doações via Pix** | A escola cadastra uma chave Pix; usuários de outras escolas veem a chave, copiam e informam a doação feita; o Diretor acompanha as doações recebidas |
| **Exclusão de dados (LGPD)** | O usuário exclui a própria conta em "Meus dados" e o Diretor pode excluir os dados de um usuário da escola. Os dados pessoais são **anonimizados** e o histórico é mantido. Para excluir ou desativar a própria conta, o Diretor precisa antes cadastrar outro Diretor, pois a escola nunca fica sem Diretor ativo |
| **Exclusão de instituição** | O Diretor encerra a escola confirmando senha, nome e CNPJ. A escola sai da rede, os recursos são inativados e os usuários são anonimizados |

### Cargos

| Cargo | Pode |
|---|---|
| **Diretor** | Um ou mais por escola. O primeiro se cadastra junto com a escola; os demais são cadastrados por um Diretor já existente. Cadastra e edita a escola, cadastra/aprova/recusa/inativa/reativa usuários, aprova ou recusa empréstimos, consulta a auditoria e as doações, exclui dados de usuários e a instituição |
| **Gestor** | Precisa ser aprovado pelo Diretor. Cadastra recursos, acompanha as solicitações recebidas e registra retirada e devolução |
| **Funcionário** | Busca recursos na rede, faz solicitações de empréstimo e informa doações |

## Tecnologias

- **Back-end:** Java 17, Spring Boot 4, Spring Security com JWT (JJWT) e BCrypt, Spring Data JPA/Hibernate, PostgreSQL, Bean Validation, Lombok e springdoc (Swagger).
- **Front-end:** React 19, React Router 7, Axios e Vite.
- **Testes:** JUnit 5, Mockito, MockMvc, banco H2 em memória e JaCoCo (cobertura).
- **Integrações:** API ViaCEP (endereço pelo CEP) e SendGrid (envio de e-mails).
- **Publicação:** Render (PostgreSQL, Web Service com Docker e Static Site).

## Estrutura do repositório

```
PFC-PLATAFORMA-PARA-REDES-DE-ENSINO/
├── SpringPfc/SpringPfc/     # Back-end (Spring Boot)
│   ├── src/main/java/...    # Controller, Service, Repository, domain, dto, config
│   ├── src/test/java/...    # Testes unitários e de integração
│   └── Dockerfile           # Montagem do back-end para o Render
└── frontend/                # Front-end (React + Vite)
    └── src/                 # Pages, Components e Services
```

## Como rodar localmente

### Pré-requisitos

- **JDK 17** (versões mais novas quebram o Lombok)
- **PostgreSQL** 14 ou superior
- **Node.js** 20 ou superior

### 1. Banco de dados

Crie o banco no PostgreSQL:
```sql
CREATE DATABASE springpfc;
```
As tabelas são criadas automaticamente na primeira execução do back-end.

### 2. Back-end

1. Em `SpringPfc/SpringPfc/src/main/resources/application.properties`, ajuste o usuário e a senha do seu PostgreSQL local.
2. Se o computador tiver outra versão do Java como padrão, aponte o terminal para o JDK 17 antes de rodar:
   ```cmd
   :: Prompt de Comando (cmd) do Windows — ajuste o caminho para a sua instalação
   set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot"
   set "PATH=%JAVA_HOME%\bin;%PATH%"
   ```
3. Na pasta `SpringPfc/SpringPfc`, rode:
   ```bash
   # Windows
   mvnw spring-boot:run
   # Linux / macOS
   ./mvnw spring-boot:run
   ```
4. A API sobe em `http://localhost:8080` e a documentação fica em `http://localhost:8080/swagger-ui.html`.

### 3. Front-end

Na pasta `frontend`:
```bash
npm install
npm run dev
```
Abra `http://localhost:5173`.

### 4. Envio de e-mails (opcional)

O e-mail de recuperação de senha é enviado pelo SendGrid. As credenciais **não ficam no código**: são lidas de variáveis de ambiente.

| Variável | Valor |
|---|---|
| `SENDGRID_API_KEY` | chave de API criada na conta do SendGrid |
| `SENDGRID_REMETENTE` | e-mail remetente verificado no SendGrid |

Sem essas variáveis, o sistema funciona normalmente e mostra o código de recuperação de senha no console do back-end (modo de demonstração).

### 5. Primeiro acesso

1. Em **Criar conta**, escolha o cargo **Diretor** e clique em **Cadastrar instituição** para cadastrar a escola.
2. Conclua o cadastro do Diretor e entre no sistema.
3. Funcionários e Gestores se cadastram escolhendo a instituição na lista. O Gestor só entra depois que o Diretor aprovar em **Usuários**.
4. Para ter mais de um Diretor na escola, o Diretor logado cadastra o novo Diretor em **Usuários → Cadastrar usuário**.

## Testes automatizados

Na pasta `SpringPfc/SpringPfc`:
```bash
mvnw test
```
São **95 testes** (unitários com Mockito e de integração com H2 e MockMvc). O relatório de cobertura do JaCoCo fica em `target/site/jacoco/index.html`.

| Classe de teste | Testes |
|---|---|
| SolicitacaoServiceTest | 25 |
| UsuarioServiceTest | 17 |
| AuthServiceTest | 17 |
| SenhaServiceTest | 10 |
| InstituicaoControllerIntegrationTest | 10 |
| DoacaoServiceTest | 5 |
| InstituicaoExclusaoServiceTest | 5 |
| EmailServiceTest | 3 |
| InstituicaoRepIntegrationTest | 1 |
| LogAuditoriaRepIntegrationTest | 1 |
| UsuarioRepIntegrationTest | 1 |

## Publicação (Render)

O sistema é publicado a partir da branch `entrega1611`. Cada push nessa branch atualiza o sistema online automaticamente.

| Serviço | Tipo | Configuração |
|---|---|---|
| Banco | PostgreSQL (Free) | Criado no painel do Render |
| Back-end | Web Service (Docker) | Root Directory `SpringPfc/SpringPfc`, usa o `Dockerfile` |
| Front-end | Static Site | Root Directory `frontend`, build `npm install && npm run build`, publish `dist`, regra de rewrite `/*` → `/index.html` |

Variáveis de ambiente (os valores ficam apenas no painel do Render):

| Serviço | Variáveis |
|---|---|
| Back-end | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `SENDGRID_API_KEY`, `SENDGRID_REMETENTE`, `APP_FRONTEND_URL`, `SPRING_JPA_SHOW_SQL` |
| Front-end | `VITE_API_URL` |

## Segurança e privacidade

- Senhas armazenadas apenas como hash BCrypt.
- Nenhuma senha, token ou chave de API fica no repositório: as credenciais de produção são variáveis de ambiente.
- Cada usuário acessa apenas os dados compatíveis com o seu cargo e a sua instituição.
- Exclusão de dados por anonimização, conforme o direito de eliminação da LGPD (art. 18, VI).

## Trabalhos futuros

- Avisos por e-mail de aprovação de cadastro e de solicitações, e lembretes antes do vencimento da devolução
- Filtros do histórico por usuário, recurso e escola
- Campo de descrição dos recursos e controle de concorrência na aprovação
- Feed de recursos com fotos
