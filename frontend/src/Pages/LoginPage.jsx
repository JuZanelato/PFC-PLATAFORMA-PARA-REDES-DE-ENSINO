import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import authService from "../Services/authService";
import { mensagemDeErro } from "../Services/mensagemErro";
import { LogoMarca } from "../Components/Logo";
import { IlustracaoRede } from "../Components/Ilustracao";
import Icone from "../Components/Icone";
import "../Components/Visual.css";
import "./LoginPage.css";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [mostrarSenha, setMostrarSenha] = useState(false);
    const [erro, setErro] = useState("");
    const [entrando, setEntrando] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErro("");
        setEntrando(true);
        try {
            await authService.login(email, senha);
            navigate("/rede");
        } catch (err) {
            setErro(mensagemDeErro(err, "E-mail ou senha inválidos."));
        } finally {
            setEntrando(false);
        }
    };

    return (
        <div className="login-page">
            <div className="login-card login-card-duplo">
                {}
                <aside className="login-painel">
                    <p className="login-painel-marca">
                        <LogoMarca tamanho={30} claro /> CompartilhaEdu
                    </p>
                    <h2>Compartilhar recursos é multiplicar oportunidades.</h2>
                    <IlustracaoRede className="login-ilustracao" />
                    <ul className="login-beneficios">
                        <li>
                            <Icone nome="busca" tamanho={18} /> Encontre materiais em outras escolas da rede
                        </li>
                        <li>
                            <Icone nome="solicitacoes" tamanho={18} /> Peça emprestado e acompanhe a devolução
                        </li>
                        <li>
                            <Icone nome="coracao" tamanho={18} /> Apoie escolas com doações via Pix
                        </li>
                    </ul>
                </aside>

                <div className="login-lado-form">
                    <header className="login-cabecalho">
                        <LogoMarca tamanho={48} />
                        <h1 className="login-marca">CompartilhaEdu</h1>
                        <p className="login-subtitulo">Entre para continuar</p>
                    </header>

                    <form className="login-form" onSubmit={handleSubmit}>
                        {erro && <p className="login-erro">{erro}</p>}

                        <label>
                            E-mail
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="seu.email@escola.com"
                                autoComplete="username"
                                required
                            />
                        </label>

                        <label>
                            Senha
                            <div className="login-senha">
                                <input
                                    type={mostrarSenha ? "text" : "password"}
                                    value={senha}
                                    onChange={(e) => setSenha(e.target.value)}
                                    autoComplete="current-password"
                                    required
                                />
                                <button
                                    type="button"
                                    className="login-mostrar"
                                    onClick={() => setMostrarSenha((v) => !v)}
                                >
                                    {mostrarSenha ? "Ocultar" : "Mostrar"}
                                </button>
                            </div>
                        </label>

                        <button type="submit" className="login-entrar" disabled={entrando}>
                            {entrando ? "Entrando..." : "Entrar"}
                        </button>
                    </form>

                    <nav className="login-links">
                        <Link to="/recuperar-senha">Esqueci minha senha</Link>
                        <span aria-hidden="true">·</span>
                        <Link to="/cadastro">Criar conta</Link>
                    </nav>
                </div>
            </div>

            <p className="login-rodape">
                Plataforma para Redes de Ensino ·{" "}
                <Link to="/politica-privacidade">Política de Privacidade</Link>
            </p>
        </div>
    );
}