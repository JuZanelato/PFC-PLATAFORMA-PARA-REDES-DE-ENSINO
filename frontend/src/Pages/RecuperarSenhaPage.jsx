import { useState } from "react";
import { Link } from "react-router-dom";
import authService from "../Services/authService";
import { mensagemDeErro } from "../Services/mensagemErro";
import "./LoginPage.css";


const SUBTITULOS = {
  1: "Recuperar senha",
  2: "Criar nova senha",
  3: "Senha redefinida",
};

export default function RecuperarSenhaPage() {
  const [etapa, setEtapa] = useState(1);
  const [email, setEmail] = useState("");
  const [token, setToken] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  const handleSolicitar = async (e) => {
    e.preventDefault();
    setErro("");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setErro("Informe um e-mail válido.");
      return;
    }
    setEnviando(true);
    try {
      await authService.solicitarRecuperacao(email);
      setEtapa(2);
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível solicitar a recuperação."));
    } finally {
      setEnviando(false);
    }
  };

  const handleRedefinir = async (e) => {
    e.preventDefault();
    setErro("");
    if (!token.trim()) return setErro("Informe o código de recuperação.");
    if (novaSenha.length < 6) return setErro("A nova senha deve ter ao menos 6 caracteres.");
    if (novaSenha !== confirmarSenha) return setErro("As senhas não coincidem.");

    setEnviando(true);
    try {
      await authService.redefinirSenha(token.trim(), novaSenha);
      setEtapa(3);
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível redefinir a senha."));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <header className="login-cabecalho">
          <h1 className="login-marca">CompartilhaEdu</h1>
          <p className="login-subtitulo">{SUBTITULOS[etapa]}</p>
        </header>

        {etapa === 1 && (
          <form className="login-form" onSubmit={handleSolicitar}>
            <p className="login-dica">
              Informe o e-mail da sua conta. Vamos gerar um código para você criar uma nova senha.
            </p>
            {erro && <p className="login-erro">{erro}</p>}
            <label>
              E-mail cadastrado
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="seu.email@escola.com"
                autoComplete="username"
              />
            </label>
            <button type="submit" className="login-entrar" disabled={enviando}>
              {enviando ? "Enviando..." : "Gerar código"}
            </button>
          </form>
        )}

        {etapa === 2 && (
          <form className="login-form" onSubmit={handleRedefinir}>
            <p className="login-info">
              Se o e-mail estiver cadastrado, um código de recuperação foi gerado. Ele vale por 1 hora e só pode ser
              usado uma vez.
            </p>
            {erro && <p className="login-erro">{erro}</p>}
            <label>
              Código de recuperação
              <input type="text" value={token} onChange={(e) => setToken(e.target.value)} autoComplete="off" />
            </label>
            <label>
              Nova senha
              <input
                type="password"
                value={novaSenha}
                onChange={(e) => setNovaSenha(e.target.value)}
                autoComplete="new-password"
              />
            </label>
            <label>
              Confirmar nova senha
              <input
                type="password"
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                autoComplete="new-password"
              />
            </label>
            <button type="submit" className="login-entrar" disabled={enviando}>
              {enviando ? "Salvando..." : "Redefinir senha"}
            </button>
          </form>
        )}

        {etapa === 3 && (
          <div className="login-form">
            <p className="login-info">Sua senha foi redefinida. Entre com a nova senha.</p>
            <Link to="/login" className="login-entrar login-botao-link">
              Ir para o login
            </Link>
          </div>
        )}

        {etapa !== 3 && (
          <nav className="login-links">
            <Link to="/login">Voltar ao login</Link>
          </nav>
        )}
      </div>
    </div>
  );
}