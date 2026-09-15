import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import authService from "../Services/authService";
import "./LoginPage.css";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [erro, setErro] = useState("");
    const [entrando, setEntrando] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErro("");
        setEntrando(true);
        try {
            await authService.login(email, senha);
            navigate("/instituicoes");
        } catch (err) {
            const mensagem =
                err?.response?.data?.message || "E-mail ou senha inválidos.";
            setErro(mensagem);
        } finally {
            setEntrando(false);
        }
    };

    return (
        <div className="login-page">
            <form className="login-form" onSubmit={handleSubmit}>
                <h1>Entrar</h1>

                {erro && <p className="erro-geral">{erro}</p>}

                <label>
                    E-mail
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </label>

                <label>
                    Senha
                    <input
                        type="password"
                        value={senha}
                        onChange={(e) => setSenha(e.target.value)}
                        required
                    />
                </label>

                <button type="submit" disabled={entrando}>
                    {entrando ? "Entrando..." : "Entrar"}
                </button>


            </form>
        </div>
    );
}