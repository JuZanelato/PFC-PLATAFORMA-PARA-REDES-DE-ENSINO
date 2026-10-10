import "./PoliticaPrivacidadePage.css";

export default function PoliticaPrivacidadePage() {
  return (
    <div className="politica-page">
      <header>
        <h1>Política de Privacidade</h1>
        <p className="politica-atualizacao">Última atualização: 27 de setembro de 2026</p>
      </header>

      <section>
        <h2>1. Introdução</h2>
        <p>
          Esta Política de Privacidade descreve como a Plataforma para Redes
          de Ensino coleta, usa, armazena e protege os dados pessoais dos
          usuários cadastrados, em conformidade com a Lei Geral de Proteção
          de Dados Pessoais (Lei nº 13.709/2018 — LGPD).
        </p>
      </section>

      <section>
        <h2>2. Dados que coletamos</h2>
        <p>Para o funcionamento da plataforma, coletamos e armazenamos:</p>
        <ul>
          <li><strong>Dados de usuários:</strong> nome completo, e-mail, senha (armazenada de forma criptografada, nunca em texto puro) e perfil de acesso.</li>
          <li><strong>Dados de instituições:</strong> nome, CNPJ, endereço, e-mail de contato e status.</li>
          <li><strong>Dados de uso:</strong> registros de login, criação, alteração e exclusão de cadastros, associados ao e-mail do usuário responsável, para fins de auditoria e segurança.</li>
        </ul>
      </section>

      <section>
        <h2>3. Como usamos os dados</h2>
        <p>Os dados coletados são utilizados exclusivamente para:</p>
        <ul>
          <li>Autenticar e autorizar o acesso à plataforma;</li>
          <li>Vincular usuários às suas respectivas instituições de ensino;</li>
          <li>Manter um histórico de auditoria de ações realizadas no sistema;</li>
          <li>Garantir a segurança e a integridade dos dados cadastrados.</li>
        </ul>
        <p>Não utilizamos os dados coletados para fins de marketing, publicidade ou venda a terceiros.</p>
      </section>

      <section>
        <h2>4. Armazenamento e segurança</h2>
        <p>
          As senhas são armazenadas exclusivamente na forma de hash
          criptográfico (BCrypt), nunca em texto legível. O acesso aos dados é
          restrito por autenticação e controle de perfis (Administrador,
          Gestor e Funcionário), garantindo que cada usuário só acesse as
          informações compatíveis com seu nível de permissão.
        </p>
      </section>

      <section>
  <h2>5. Compartilhamento de dados</h2>
  <p>
    Os dados cadastrados de instituições e os itens registrados por elas na
    plataforma são compartilhados entre as instituições integrantes da
    mesma rede de ensino, permitindo a gestão colaborativa e a visibilidade
    das informações dentro da rede. Os dados pessoais de usuários (nome,
    e-mail, senha) não são expostos a outras instituições — apenas os
    dados institucionais e os registros vinculados a elas.
  </p>
  <p>
    Fora do âmbito da rede de ensino, os dados não são compartilhados com
    terceiros, exceto quando exigido por obrigação legal ou ordem judicial.
  </p>
</section>

      <section>
        <h2>6. Direitos do titular dos dados</h2>
        <p>Nos termos da LGPD, o usuário tem direito a:</p>
        <ul>
          <li>Confirmar a existência de tratamento de seus dados;</li>
          <li>Acessar seus dados armazenados;</li>
          <li>Corrigir dados incompletos, inexatos ou desatualizados;</li>
          <li>Solicitar a exclusão de dados, quando aplicável;</li>
          <li>Revogar o consentimento, quando o tratamento for baseado nele.</li>
        </ul>
        <p>
          Para exercer esses direitos, entre em contato com o administrador
          responsável pela sua instituição na plataforma.
        </p>
      </section>

      <section>
        <h2>7. Retenção de dados</h2>
        <p>
          Os dados são mantidos enquanto a conta do usuário ou da instituição
          estiver ativa na plataforma, ou pelo período necessário para
          cumprir obrigações legais e de auditoria.
        </p>
      </section>

      <section>
        <h2>8. Alterações nesta política</h2>
        <p>
          Esta política pode ser atualizada periodicamente. Recomendamos a
          revisão deste documento sempre que a plataforma for atualizada.
        </p>
      </section>
    </div>
  );
}