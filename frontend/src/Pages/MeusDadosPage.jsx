import { useState } from "react";
import authService from "../Services/authService";
import usuarioService, { PERFIL_LABEL } from "../Services/usuarioService";
import { mensagemDeErro } from "../Services/mensagemErro";
import "./MeusDadosPage.css";

export default function MeusDadosPage() {
  const usuario = authService.usuarioLogado();

  const [nome, setNome] = useState(usuario?.nome || "");
  const [trocarSenha, setTrocarSenha] = useState(false);
  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erros, setErros] = useState({});
  const [mensagem, setMensagem] = useState("");
  const [salvando, setSalvando] = useState(false);

  const validar = () => {
    const novosErros = {};
    if (!nome.trim()) novosErros.nome = "Nome é obrigatório";
    else if (nome.trim().length < 3) novosErros.nome = "Informe o nome completo";

    if (trocarSenha) {
      if (!senhaAtual) novosErros.senhaAtual = "Informe a senha atual";
      if (!novaSenha) novosErros.novaSenha = "Informe a nova senha";
      else if (novaSenha.length < 6) novosErros.novaSenha = "A nova senha deve ter ao menos 6 caracteres";
      if (novaSenha !== confirmarSenha) novosErros.confirmarSenha = "As senhas não coincidem";
    }

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensagem("");
    if (!validar()) return;

    setSalvando(true);
    try {
      const atualizado = await usuarioService.atualizarMeusDados({
        nome: nome.trim(),
        senhaAtual: trocarSenha ? senhaAtual : null,
        novaSenha: trocarSenha ? novaSenha : null,
      });

      localStorage.setItem("usuario", JSON.stringify({ ...usuario, nome: atualizado.nome }));

      setMensagem(trocarSenha ? "Seus dados e sua senha foram atualizados." : "Seus dados foram atualizados.");
      setTrocarSenha(false);
      setSenhaAtual("");
      setNovaSenha("");
      setConfirmarSenha("");
    } catch (err) {
      setErros({ geral: mensagemDeErro(err, "Não foi possível salvar os seus dados.") });
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className="md-page">
      <h1>Meus dados</h1>
      <p className="md-dica">
        Confira e corrija os seus dados pessoais. Esse é um direito garantido pela Lei Geral de Proteção de Dados
        (LGPD).
      </p>

      <dl className="md-conta">
        <dt>E-mail (login)</dt>
        <dd>{usuario?.email}</dd>
        <dt>Cargo</dt>
        <dd>{PERFIL_LABEL[usuario?.perfil] || usuario?.perfil}</dd>
        <dt>Instituição</dt>
        <dd>{usuario?.instituicaoNome}</dd>
      </dl>

      <form className="md-form" onSubmit={handleSubmit}>
        {erros.geral && <p className="md-erro">{erros.geral}</p>}
        {mensagem && <p className="md-sucesso">{mensagem}</p>}

        <label>
          Nome
          <input type="text" value={nome} maxLength={120} onChange={(e) => setNome(e.target.value)} />
          {erros.nome && <span className="md-erro-campo">{erros.nome}</span>}
        </label>

        <label className="md-checkbox">
          <input type="checkbox" checked={trocarSenha} onChange={(e) => setTrocarSenha(e.target.checked)} />
          Quero trocar minha senha
        </label>

        {trocarSenha && (
          <fieldset className="md-senha">
            <label>
              Senha atual
              <input type="password" value={senhaAtual} onChange={(e) => setSenhaAtual(e.target.value)} />
              {erros.senhaAtual && <span className="md-erro-campo">{erros.senhaAtual}</span>}
            </label>
            <label>
              Nova senha
              <input type="password" value={novaSenha} onChange={(e) => setNovaSenha(e.target.value)} />
              {erros.novaSenha && <span className="md-erro-campo">{erros.novaSenha}</span>}
            </label>
            <label>
              Confirmar nova senha
              <input type="password" value={confirmarSenha} onChange={(e) => setConfirmarSenha(e.target.value)} />
              {erros.confirmarSenha && <span className="md-erro-campo">{erros.confirmarSenha}</span>}
            </label>
          </fieldset>
        )}

        <button type="submit" disabled={salvando}>
          {salvando ? "Salvando..." : "Salvar alterações"}
        </button>
      </form>

      <p className="md-rodape">
        O e-mail é usado para entrar no sistema e não pode ser alterado aqui. Para mudar o e-mail ou excluir a sua
        conta, fale com o diretor da sua escola.
      </p>
    </div>
  );
}