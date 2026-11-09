import { NavLink, useNavigate } from "react-router-dom";
import authService from "../Services/authService";
import "./Navegacao.css";

const PERFIL_LABEL = {
    FUNCIONARIO: "Funcionário",
    GESTOR: "Gestor",
    ADMINISTRADOR: "Administrador",
};


export default function Navegacao() {
    const navigate = useNavigate();
    const usuario = authService.usuarioLogado();
    const perfil = usuario?.perfil;
    const ehGestorOuAdmin = perfil === "GESTOR" || perfil === "ADMINISTRADOR";

    const handleSair = async () => {
        try {
            await authService.logout();
        } catch {

        }
        navigate("/login");
    };

    const classe = ({ isActive }) => (isActive ? "nav-link ativo" : "nav-link");

    return (
        <nav className="nav-barra">
            <span className="nav-marca">CompartilhaEdu</span>

            <div className="nav-links">
                {ehGestorOuAdmin && (
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
                {perfil === "ADMINISTRADOR" && (
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
