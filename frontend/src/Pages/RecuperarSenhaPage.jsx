import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import authService from "../Services/authService";
import { mensagemDeErro } from "../Services/mensagemErro";
import "./LoginPage.css";

// Recuperacao de senha em duas etapas (BF01):
// 1) o usuario informa o e-mail e o back-end gera um token valido por 1 hora;
// 2) o usuario informa o token recebido e a nova senha.
// Na 1a versao nao ha envio de e-mail: o token aparece no console do back-end.
export default function RecuperarSenhaPage() {
  const navigate = useNavigate();
  const [etapa, setEtapa] = useState(1);
  const [email, setEmail] = useState("");
  const [token, setToken] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");
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
      // Mensagem neutra: nao revela se o e-mail existe (protecao de dados)
      setMensagem("Se o e-mail estiver cadastrado, um código de recuperação foi gerado.");
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
      alert("Senha redefinida com sucesso. Faça login com a nova senha.");
      navigate("/login");
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível redefinir a senha."));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login-page">
      {etapa === 1 ? (
        <form className="login-form" onSubmit={handleSolicitar}>
          <h1>Recuperar senha</h1>
          {erro && <p className="erro-geral">{erro}</p>}
          <label>
            E-mail cadastrado
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          </label>
          <button type="submit" disabled={enviando}>
            {enviando ? "Enviando..." : "Gerar código"}
          </button>
          <p>
            <Link to="/login">Voltar ao login</Link>
          </p>
        </form>
      ) : (
        <form className="login-form" onSubmit={handleRedefinir}>
          <h1>Nova senha</h1>
          {mensagem && <p>{mensagem}</p>}
          {erro && <p className="erro-geral">{erro}</p>}
          <label>
            Código de recuperação
            <input type="text" value={token} onChange={(e) => setToken(e.target.value)} />
          </label>
          <label>
            Nova senha
            <input type="password" value={novaSenha} onChange={(e) => setNovaSenha(e.target.value)} />
          </label>
          <label>
            Confirmar nova senha
            <input
              type="password"
              value={confirmarSenha}
              onChange={(e) => setConfirmarSenha(e.target.value)}
            />
          </label>
          <button type="submit" disabled={enviando}>
            {enviando ? "Salvando..." : "Redefinir senha"}
          </button>
          <p>
            <Link to="/login">Voltar ao login</Link>
          </p>
        </form>
      )}
    </div>
  );
}
