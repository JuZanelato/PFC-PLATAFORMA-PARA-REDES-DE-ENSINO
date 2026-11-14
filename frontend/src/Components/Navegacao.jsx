import { NavLink, useNavigate } from "react-router-dom";
import authService from "../Services/authService";
import { PERFIL_LABEL } from "../Services/usuarioService";
import "./Navegacao.css";

export default function Navegacao() {
    const navigate = useNavigate();
    const usuario = authService.usuarioLogado();
    const perfil = usuario?.perfil;
    const ehDiretor = perfil === "ADMINISTRADOR";
    const ehGestorOuDiretor = perfil === "GESTOR" || perfil === "ADMINISTRADOR";

    const handleSair = async () => {
        try {
            await authService.logout();
        } catch {
            // mesmo se o back-end não responder, a sessão local já foi limpa
        }
        navigate("/login");
    };

    const classe = ({ isActive }) => (isActive ? "nav-link ativo" : "nav-link");

    return (
        <nav className="nav-barra">
            <span className="nav-marca">CompartilhaEdu</span>

            <div className="nav-links">
                {ehGestorOuDiretor && (
                    <NavLink to="/dashboard" className={classe}>
                        Dashboard
                    </NavLink>
                )}
                <NavLink to="/rede" className={classe}>
                    Buscar na rede
                </NavLink>
                <NavLink to="/solicitacoes" className={classe}>
                    Solicitações
                </NavLink>
                <NavLink to="/historico" className={classe}>
                    Histórico
                </NavLink>
                <NavLink to="/recursos" className={classe}>
                    Meus recursos
                </NavLink>
                <NavLink to="/instituicoes" className={classe}>
                    Instituições
                </NavLink>
                {ehDiretor && (
                    <NavLink to="/usuarios" className={classe}>
                        Usuários
                    </NavLink>
                )}
                {ehDiretor && (
                    <NavLink to="/auditoria" className={classe}>
                        Auditoria
                    </NavLink>
                )}
            </div>

            <div className="nav-usuario">
                <span title={usuario?.email}>
                    {usuario?.nome} · {PERFIL_LABEL[perfil] || perfil}
                    {usuario?.instituicaoNome && <small> — {usuario.instituicaoNome}</small>}
                </span>
                <button type="button" onClick={handleSair}>
                    Sair
                </button>
            </div>
        </nav>
    );
}