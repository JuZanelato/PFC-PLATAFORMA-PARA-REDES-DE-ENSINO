import { NavLink, useNavigate } from "react-router-dom";
import authService from "../Services/authService";
import { PERFIL_LABEL } from "../Services/usuarioService";
import Icone from "./Icone";
import Logo from "./Logo";
import "./Visual.css";
import "./Navegacao.css";


const iniciais = (nome = "") =>
    nome
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map((p) => p[0].toUpperCase())
        .join("");

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
            <NavLink to="/rede" className="nav-marca" aria-label="CompartilhaEdu - início">
                <Logo tamanho={34} claro />
            </NavLink>

            <div className="nav-links">
                {ehGestorOuDiretor && (
                    <NavLink to="/dashboard" className={classe}>
                        <Icone nome="painel" tamanho={18} />
                        Dashboard
                    </NavLink>
                )}
                <NavLink to="/rede" className={classe}>
                    <Icone nome="busca" tamanho={18} />
                    Buscar na rede
                </NavLink>
                <NavLink to="/solicitacoes" className={classe}>
                    <Icone nome="solicitacoes" tamanho={18} />
                    Solicitações
                </NavLink>
                <NavLink to="/historico" className={classe}>
                    <Icone nome="historico" tamanho={18} />
                    Histórico
                </NavLink>
                <NavLink to="/recursos" className={classe}>
                    <Icone nome="recursos" tamanho={18} />
                    Meus recursos
                </NavLink>
                <NavLink to="/instituicoes" className={classe}>
                    <Icone nome="instituicoes" tamanho={18} />
                    Instituições
                </NavLink>
                {ehDiretor && (
                    <NavLink to="/usuarios" className={classe}>
                        <Icone nome="usuarios" tamanho={18} />
                        Usuários
                    </NavLink>
                )}
                {ehDiretor && (
                    <NavLink to="/auditoria" className={classe}>
                        <Icone nome="auditoria" tamanho={18} />
                        Auditoria
                    </NavLink>
                )}
            </div>

            <div className="nav-usuario">
                <NavLink to="/meus-dados" className="nav-usuario-link" title="Meus dados">
                    <span className="nav-avatar">{iniciais(usuario?.nome)}</span>
                    <span className="nav-usuario-texto">
                        <span className="nav-usuario-nome">{usuario?.nome}</span>
                        <small>
                            {PERFIL_LABEL[perfil] || perfil}
                            {usuario?.instituicaoNome && ` · ${usuario.instituicaoNome}`}
                        </small>
                    </span>
                </NavLink>
                <button type="button" onClick={handleSair} title="Sair">
                    <Icone nome="sair" tamanho={18} />
                    <span>Sair</span>
                </button>
            </div>
        </nav>
    );
}