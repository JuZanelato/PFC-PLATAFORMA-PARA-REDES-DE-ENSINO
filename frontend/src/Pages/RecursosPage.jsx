import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useRecurso } from "../Hook/useRecurso";
import authService from "../Services/authService";
import RecursoForm from "../Components/RecursoForm";
import RecursoTable from "../Components/RecursoTable";
import "./RecursosPage.css";

export default function RecursosPage() {
  const usuarioLogado = authService.usuarioLogado();
  const podeGerenciar =
    usuarioLogado?.perfil === "ADMINISTRADOR" || usuarioLogado?.perfil === "GESTOR";

  const { recursos, carregando, erro, salvar, inativar, alternarDisponibilidade } = useRecurso();

  const [selecionado, setSelecionado] = useState(null);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [termoBusca, setTermoBusca] = useState("");

  const recursosFiltrados = useMemo(() => {
    if (!termoBusca.trim()) return recursos;
    const termo = termoBusca.trim().toLowerCase();
    return recursos.filter((recurso) => recurso.nome?.toLowerCase().includes(termo));
  }, [recursos, termoBusca]);

  const handleNovo = () => {
    setSelecionado(null);
    setMostrarForm(true);
  };

  const handleEditar = (recurso) => {
    setSelecionado(recurso);
    setMostrarForm(true);
  };

  const handleCancelar = () => {
    setSelecionado(null);
    setMostrarForm(false);
  };

  const handleSalvar = async (form) => {
    await salvar(form);
    setSelecionado(null);
    setMostrarForm(false);
  };

  const handleInativar = async (recurso) => {
    const confirmar = window.confirm(
      `Inativar o recurso "${recurso.nome}"? Ele deixará de aparecer como disponível.`
    );
    if (!confirmar) return;
    await inativar(recurso.id);
  };

  return (
    <div className="recurso-page">
      <header className="recurso-page-header">
        <h1>Gestão de Recursos</h1>

        <div className="recurso-page-acoes">
          {podeGerenciar && !mostrarForm && (
            <button type="button" onClick={handleNovo}>
              + Novo recurso
            </button>
          )}
          <Link to="/instituicoes" className="link-acao">
            Minhas instituições
          </Link>
        </div>
      </header>

      {mostrarForm && (
        <RecursoForm
          recursoSelecionado={selecionado}
          onSalvar={handleSalvar}
          onCancelar={handleCancelar}
        />
      )}

      {erro && <p className="erro-geral">{erro}</p>}

      {!mostrarForm && !carregando && (
        <input
          type="text"
          className="busca-recurso"
          placeholder="Buscar recurso pelo nome..."
          value={termoBusca}
          onChange={(e) => setTermoBusca(e.target.value)}
        />
      )}

      {carregando ? (
        <p>Carregando...</p>
      ) : (
        <RecursoTable
          recursos={recursosFiltrados}
          onEditar={handleEditar}
          onInativar={handleInativar}
          onAlternarDisponibilidade={alternarDisponibilidade}
        />
      )}
    </div>
  );
}