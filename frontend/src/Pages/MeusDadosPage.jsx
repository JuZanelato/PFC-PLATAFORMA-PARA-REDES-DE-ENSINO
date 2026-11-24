import { useState } from "react";
import { Link } from "react-router-dom";
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

  // NOVO: exclusão da própria conta
  const [abrirExclusao, setAbrirExclusao] = useState(false);
  const [senhaExclusao, setSenhaExclusao] = useState("");
  const [entendi, setEntendi] = useState(false);
  const [erroExclusao, setErroExclusao] = useState("");
  const [excluindo, setExcluindo] = useState(false);

  const [abrirDesativar, setAbrirDesativar] = useState(false);
  const [senhaDesativar, setSenhaDesativar] = useState("");
  const [erroDesativar, setErroDesativar] = useState("");
  const [desativando, setDesativando] = useState(false);

  const sairDoSistema = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    window.location.href = "/login";
  };

  const handleDesativar = async (e) => {
    e.preventDefault();
    setErroDesativar("");
    if (!senhaDesativar) return setErroDesativar("Informe a sua senha para confirmar.");

    setDesativando(true);
    try {
      await usuarioService.desativarMinhaConta(senhaDesativar);
      sairDoSistema();
    } catch (err) {
      setErroDesativar(mensagemDeErro(err, "Não foi possível desativar a sua conta."));
      setDesativando(false);
    }
  };

  const handleExcluir = async (e) => {
    e.preventDefault();
    setErroExclusao("");
    if (!senhaExclusao) return setErroExclusao("Informe a sua senha para confirmar.");
    if (!entendi) return setErroExclusao("Marque a confirmação de que entendeu que a exclusão é definitiva.");

    setExcluindo(true);
    try {
      await usuarioService.excluirMinhaConta(senhaExclusao);
      sairDoSistema();
    } catch (err) {
      setErroExclusao(mensagemDeErro(err, "Não foi possível excluir a sua conta."));
      setExcluindo(false);
    }
  };

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
        O e-mail é usado para entrar no sistema e não pode ser alterado aqui. Para mudar o e-mail, fale com o
        diretor da sua escola.
      </p>

      <section className="md-perigo md-desativar">
        <h2>Desativar minha conta</h2>
        <p>
          Você deixa de entrar no sistema, mas seus dados continuam guardados. Um diretor da escola pode reativar a
          sua conta depois, na tela Usuários.
          {usuario?.perfil === "ADMINISTRADOR" &&
            " Como você é diretor, a escola precisa ter outro diretor ativo antes. Cadastre-o na tela Usuários."}
        </p>

        {!abrirDesativar ? (
          <button type="button" className="md-btn-desativar" onClick={() => setAbrirDesativar(true)}>
            Quero desativar minha conta
          </button>
        ) : (
          <form className="md-form" onSubmit={handleDesativar}>
            {erroDesativar && <p className="md-erro">{erroDesativar}</p>}
            <label>
              Confirme com a sua senha
              <input
                type="password"
                value={senhaDesativar}
                onChange={(e) => setSenhaDesativar(e.target.value)}
                autoComplete="current-password"
              />
            </label>
            <div className="md-perigo-acoes">
              <button type="submit" className="md-btn-desativar" disabled={desativando}>
                {desativando ? "Desativando..." : "Desativar conta"}
              </button>
              <button
                type="button"
                className="md-btn-cancelar"
                onClick={() => {
                  setAbrirDesativar(false);
                  setSenhaDesativar("");
                  setErroDesativar("");
                }}
              >
                Cancelar
              </button>
            </div>
          </form>
        )}
      </section>

      <section className="md-perigo">
        <h2>Excluir minha conta</h2>
        <p>
          Seu nome, e-mail e senha serão apagados de forma definitiva e você não poderá mais entrar no sistema. Os
          empréstimos e doações já registrados continuam no histórico das escolas, mas aparecem como{" "}
          <strong>"Usuário removido"</strong>, sem identificar você.
        </p>
        {usuario?.perfil === "ADMINISTRADOR" && (
          <p className="md-aviso-diretor">
            Como você é diretor, a escola não pode ficar sem diretor. Antes de excluir a sua conta,{" "}
            <Link to="/cadastro">cadastre outro diretor</Link> (cargo "Diretor" na tela de cadastro de usuários).
          </p>
        )}

        {!abrirExclusao ? (
          <button type="button" className="md-btn-perigo" onClick={() => setAbrirExclusao(true)}>
            Quero excluir minha conta
          </button>
        ) : (
          <form className="md-form" onSubmit={handleExcluir}>
            {erroExclusao && <p className="md-erro">{erroExclusao}</p>}
            <label>
              Confirme com a sua senha
              <input
                type="password"
                value={senhaExclusao}
                onChange={(e) => setSenhaExclusao(e.target.value)}
                autoComplete="current-password"
              />
            </label>
            <label className="md-checkbox">
              <input type="checkbox" checked={entendi} onChange={(e) => setEntendi(e.target.checked)} />
              Entendo que a exclusão é definitiva e não pode ser desfeita.
            </label>
            <div className="md-perigo-acoes">
              <button type="submit" className="md-btn-perigo" disabled={excluindo}>
                {excluindo ? "Excluindo..." : "Excluir definitivamente"}
              </button>
              <button
                type="button"
                className="md-btn-cancelar"
                onClick={() => {
                  setAbrirExclusao(false);
                  setSenhaExclusao("");
                  setEntendi(false);
                  setErroExclusao("");
                }}
              >
                Cancelar
              </button>
            </div>
          </form>
        )}
      </section>
    </div>
  );
}