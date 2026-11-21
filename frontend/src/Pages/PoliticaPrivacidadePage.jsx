import "./PoliticaPrivacidadePage.css";

export default function PoliticaPrivacidadePage() {
  return (
    <div className="politica-page">
      <header>
        <h1>Política de Privacidade</h1>
        <p className="politica-atualizacao">Última atualização: 19 de novembro de 2026</p>
      </header>

      <section>
        <h2>1. Introdução</h2>
        <p>
          Esta Política de Privacidade descreve como a plataforma CompartilhaEdu (Plataforma para Redes de Ensino)
          coleta, usa, armazena e protege os dados pessoais dos usuários cadastrados, em conformidade com a Lei
          Geral de Proteção de Dados Pessoais (Lei nº 13.709/2018 — LGPD).
        </p>
        <p>
          Cada instituição de ensino é responsável pelos dados dos usuários que fazem parte dela. Na plataforma,
          essa responsabilidade é exercida pelo diretor da instituição.
        </p>
      </section>

      <section>
        <h2>2. Dados que coletamos</h2>
        <p>Para o funcionamento da plataforma, coletamos e armazenamos:</p>
        <ul>
          <li>
            <strong>Dados de usuários:</strong> nome completo, e-mail, senha (armazenada de forma criptografada,
            nunca em texto puro), cargo (Diretor, Gestor ou Funcionário), instituição a que o usuário pertence e
            situação da conta (ativa, inativa ou aguardando aprovação).
          </li>
          <li>
            <strong>Dados de instituições:</strong> nome, CNPJ, endereço, e-mail de contato e status.
          </li>
          <li>
            <strong>Dados de recursos e solicitações:</strong> recursos cadastrados pela instituição e pedidos de
            empréstimo, com o nome de quem solicitou, a justificativa, as datas (pedido, resposta, retirada e
            devolução) e o motivo, em caso de recusa.
          </li>
          <li>
            <strong>Dados de uso:</strong> registros de login, saída, tentativas de login sem sucesso, criação,
            aprovação, recusa, inativação e alteração de cadastros, associados ao e-mail do usuário responsável,
            para fins de auditoria e segurança.
          </li>
        </ul>
      </section>

      <section>
        <h2>3. Como usamos os dados</h2>
        <p>Os dados coletados são utilizados exclusivamente para:</p>
        <ul>
          <li>Autenticar e autorizar o acesso à plataforma;</li>
          <li>Vincular usuários às suas respectivas instituições de ensino;</li>
          <li>Permitir que o diretor aprove ou recuse o cadastro de gestores da sua instituição;</li>
          <li>Registrar e acompanhar o empréstimo de recursos entre as instituições da rede;</li>
          <li>Enviar e-mails sobre a conta e as solicitações, conforme a seção 4;</li>
          <li>Manter um histórico de auditoria de ações realizadas no sistema;</li>
          <li>Garantir a segurança e a integridade dos dados cadastrados.</li>
        </ul>
        <p>Não utilizamos os dados coletados para fins de marketing, publicidade ou venda a terceiros.</p>
      </section>

      <section>
        <h2>4. Envio de e-mails</h2>
        <p>
          A plataforma usa o e-mail cadastrado para enviar mensagens relacionadas ao uso do sistema, como:
        </p>
        <ul>
          <li>Link para redefinição de senha, quando solicitado pelo próprio usuário;</li>
          <li>Aviso ao diretor quando um gestor se cadastra e aguarda aprovação;</li>
          <li>Aviso ao gestor quando o seu cadastro é aprovado;</li>
          <li>Avisos sobre solicitações de empréstimo: nova solicitação, aprovação, recusa e lembretes de devolução.</li>
        </ul>
        <p>
          Os e-mails são enviados pelo serviço SendGrid, da empresa Twilio, sediada nos Estados Unidos, que recebe
          apenas o endereço do destinatário e o conteúdo da mensagem. Não enviamos propagandas.
        </p>
      </section>

      <section>
        <h2>5. Armazenamento e segurança</h2>
        <p>
          As senhas são armazenadas exclusivamente na forma de hash criptográfico (BCrypt), nunca em texto legível.
          Para trocar a senha pela tela "Meus dados", o usuário precisa informar a senha atual.
        </p>
        <p>
          Após o login, o navegador guarda um token de acesso e os dados básicos do usuário (nome, cargo e
          instituição) para manter a sessão aberta. Essas informações são apagadas ao clicar em "Sair". A
          plataforma não usa cookies de rastreamento.
        </p>
        <p>O acesso aos dados é restrito por autenticação e controle de cargos:</p>
        <ul>
          <li>
            <strong>Diretor:</strong> um por instituição. Gerencia os dados da instituição, cadastra, aprova,
            recusa e inativa usuários da sua instituição e consulta a auditoria;
          </li>
          <li>
            <strong>Gestor:</strong> o cadastro precisa ser aprovado pelo diretor. Acompanha as solicitações
            recebidas e o painel da instituição;
          </li>
          <li>
            <strong>Funcionário:</strong> consulta os recursos da rede e faz solicitações de empréstimo.
          </li>
        </ul>
        <p>Cada usuário só acessa as informações compatíveis com o seu cargo e com a sua instituição.</p>
      </section>

      <section>
        <h2>6. Compartilhamento de dados</h2>
        <p>
          Os dados das instituições (nome, endereço e e-mail de contato) e os recursos cadastrados por elas são
          visíveis para as instituições da mesma rede de ensino, para permitir o empréstimo entre escolas.
        </p>
        <p>
          Quando um usuário faz uma solicitação de empréstimo, a instituição dona do recurso vê o nome da escola
          solicitante e a justificativa informada, para que possa responder ao pedido. O nome, o e-mail e a senha
          de quem fez o pedido não são mostrados a outras instituições.
        </p>
        <p>
          Fora da rede de ensino, os dados não são compartilhados com terceiros, exceto com o serviço de envio de
          e-mails descrito na seção 4 ou quando exigido por obrigação legal ou ordem judicial.
        </p>
      </section>

      <section>
        <h2>7. Direitos do titular dos dados</h2>
        <p>Nos termos da LGPD, o usuário tem direito a:</p>
        <ul>
          <li>Confirmar a existência de tratamento de seus dados;</li>
          <li>Acessar seus dados armazenados;</li>
          <li>Corrigir dados incompletos, inexatos ou desatualizados;</li>
          <li>Solicitar a exclusão de dados, quando aplicável;</li>
          <li>Revogar o consentimento, quando o tratamento for baseado nele.</li>
        </ul>
        <p>
          O usuário pode consultar os seus dados e corrigir o nome e a senha a qualquer momento pela tela "Meus
          dados", acessada clicando no próprio nome no menu. Na mesma tela, pode pedir a exclusão da conta,
          confirmando com a senha. A exclusão também pode ser feita pelo diretor da instituição, a pedido do
          usuário. Para alterar o e-mail ou exercer os demais direitos, entre em contato com o diretor da sua
          instituição.
        </p>
      </section>

      <section>
        <h2>8. Retenção de dados</h2>
        <p>
          Os dados são mantidos enquanto a conta do usuário ou da instituição estiver ativa na plataforma. Contas
          inativadas ou com cadastro recusado não são apagadas na hora: ficam desativadas, sem acesso ao sistema,
          para preservar o histórico de empréstimos e a auditoria, pelo período necessário para cumprir
          obrigações legais.
        </p>
        <p>
          Quando a exclusão da conta é pedida, os dados pessoais são <strong>anonimizados</strong>: o nome, o
          e-mail e a senha são apagados de forma definitiva, e os empréstimos, doações e registros de auditoria
          passam a aparecer como "Usuário removido", sem identificar a pessoa. A exclusão não é feita enquanto
          houver empréstimos em andamento solicitados pelo usuário.
        </p>
      </section>

      <section>
        <h2>9. Alterações nesta política</h2>
        <p>
          Esta política pode ser atualizada periodicamente. A data da última atualização aparece no início desta
          página. Recomendamos a revisão deste documento sempre que a plataforma for atualizada.
        </p>
      </section>
    </div>
  );
}