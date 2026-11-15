# PFC-PLATAFORMA-PARA-REDES-DE-ENSINO
# CompartilhaEdu — Plataforma para Redes de Ensino

Projeto Final de Curso (PFC): plataforma digital para **compartilhamento e gestão de recursos entre instituições de ensino** de uma mesma rede. Uma escola cadastra os recursos que pode emprestar (materiais didáticos, equipamentos, mobiliário etc.), e as outras escolas da rede encontram esses recursos, pedem emprestado e acompanham o empréstimo até a devolução.

## Funcionalidades

| Módulo | O que faz |
|---|---|
| **Usuários e acesso (BF01)** | Cadastro e login com JWT; três cargos (Diretor, Gestor e Funcionário); o Gestor aguarda aprovação do Diretor; tela "Meus dados"; Política de Privacidade (LGPD) |
| **Instituições (BF02)** | O Diretor cadastra e edita a própria escola; todos consultam as escolas da rede e veem os detalhes de cada uma (dados e recursos) |
| **Recursos (BF03)** | Cadastro, edição, inativação e controle de disponibilidade dos recursos da escola |
| **Busca e solicitação (BF04)** | Busca de recursos de outras escolas por nome, categoria e instituição; pedido com quantidade, justificativa e **data prevista de devolução** |
| **Empréstimo e histórico (BF05)** | O Diretor aprova ou recusa; o Gestor registra a retirada e a devolução; histórico de cada pedido e destaque para devoluções **atrasadas** |
| **Dashboard e auditoria (BF06)** | Painel com indicadores da escola; registro de auditoria das ações dos usuários da instituição |

### Cargos

| Cargo | Pode |
|---|---|
| **Diretor** | Um por escola. Cadastra a escola, cadastra/aprova/recusa/inativa usuários, aprova ou recusa empréstimos e consulta a auditoria |
| **Gestor** | Precisa ser aprovado pelo Diretor. Cadastra recursos, acompanha as solicitações recebidas e registra retirada e devolução |
| **Funcionário** | Busca recursos na rede e faz solicitações de empréstimo |

## Tecnologias

**Back-end:** Java 17, Spring Boot 4, Spring Security com JWT (JJWT) e BCrypt, Spring Data JPA/Hibernate, PostgreSQL, Bean Validation, Lombok, springdoc (Swagger).
**Testes:** JUnit 5, Mockito, MockMvc, banco H2 em memória e JaCoCo (relatório de cobertura).
**Front-end:** React 19, React Router 7, Axios e Vite.

## Como rodar

### Pré-requisitos

- **JDK 17** (versões mais novas quebram o Lombok)
- **PostgreSQL** com um banco chamado `springpfc`
- **Node.js** 20 ou superior

### 1. Back-end

1. Crie o banco no PostgreSQL:
   ```sql
   CREATE DATABASE springpfc;
   ```
2. Ajuste usuário e senha do banco em `SpringPfc/SpringPfc/src/main/resources/application.properties`.
3. Na pasta `SpringPfc/SpringPfc`, rode:
   ```bash
   # Windows
   mvnw spring-boot:run
   # Linux / macOS
   ./mvnw spring-boot:run
   ```
4. A API sobe em `http://localhost:8080`. As tabelas são criadas automaticamente. A documentação fica em `http://localhost:8080/swagger-ui.html`.

### 2. Front-end

Na pasta `frontend`:
```bash
npm install
npm run dev
```
Abra `http://localhost:5173`.

### 3. Primeiro acesso

1. Em **Criar conta**, escolha o cargo **Diretor** e clique em **Cadastrar instituição** para cadastrar a escola.
2. Conclua o cadastro do Diretor e entre no sistema.
3. Funcionários e Gestores da escola se cadastram escolhendo a instituição na lista. O Gestor só entra depois que o Diretor aprovar em **Usuários**.

## Testes

Na pasta `SpringPfc/SpringPfc`:
```bash
mvnw test
```
O relatório de cobertura do JaCoCo fica em `target/site/jacoco/index.html`.

## Trabalhos futuros

- Envio de e-mails pela API do Gmail (recuperação de senha, avisos de aprovação e de solicitações) e lembretes automáticos antes do vencimento
- Filtros do histórico por usuário, recurso e escola
- Campo de descrição dos recursos e controle de concorrência na aprovação
- Feed de recursos com fotos
- Publicação na nuvem (Render)
