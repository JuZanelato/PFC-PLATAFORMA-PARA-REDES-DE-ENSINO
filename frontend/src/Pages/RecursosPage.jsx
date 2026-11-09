import { useMemo, useState } from "react";
import { useRecurso } from "../Hook/useRecurso";
import authService from "../Services/authService";
import { CATEGORIA_OPTIONS, mensagemDeErro } from "../Services/recursoService";
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
  const [categoriaFiltro, setCategoriaFiltro] = useState("");
  const [erroAcao, setErroAcao] = useState(null);

  const recursosFiltrados = useMemo(() => {
    const termo = termoBusca.trim().toLowerCase();
    return recursos.filter((recurso) => {
      const bateNome = !termo || recurso.nome?.toLowerCase().includes(termo);
      const bateCategoria = !categoriaFiltro || recurso.categoria === categoriaFiltro;
      return bateNome && bateCategoria;
    });
  }, [recursos, termoBusca, categoriaFiltro]);

  const handleNovo = () => {
    setErroAcao(null);
    setSelecionado(null);
    setMostrarForm(true);
  };

  const handleEditar = (recurso) => {
    setErroAcao(null);
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
      `Inativar o recurso "${recurso.nome}"? Ele ficará indisponível e não poderá mais ser editado.`
    );
    if (!confirmar) return;
    try {
      setErroAcao(null);
      await inativar(recurso.id);
    } catch (err) {
      setErroAcao(mensagemDeErro(err, "Não foi possível inativar o recurso."));
    }
  };

  const handleAlternarDisponibilidade = async (recurso) => {
    try {
      setErroAcao(null);
      await alternarDisponibilidade(recurso);
    } catch (err) {
      setErroAcao(mensagemDeErro(err, "Não foi possível atualizar a disponibilidade."));
    }
  };

  return (
    <div className="recurso-page">
      <header className="recurso-page-header">
        <h1>Recursos da minha instituição</h1>

        <div className="recurso-page-acoes">
          {podeGerenciar && !mostrarForm && (
            <button type="button" onClick={handleNovo}>
              + Novo recurso
            </button>
          )}
        </div>
      </header>


      {!podeGerenciar && (
        <p className="recurso-aviso">
          Você pode consultar os recursos da sua unidade. Apenas gestores e administradores podem
          cadastrar, editar ou inativar.
        </p>
      )}

      {mostrarForm && (
        <RecursoForm
          key={selecionado?.id ?? "novo"}
          recursoSelecionado={selecionado}
          onSalvar={handleSalvar}
          onCancelar={handleCancelar}
        />
      )}

      {erro && <p className="recurso-erro-geral">{erro}</p>}
      {erroAcao && <p className="recurso-erro-geral">{erroAcao}</p>}

      {!mostrarForm && !carregando && (
        <div className="recurso-filtros">
          <input
            type="text"
            placeholder="Buscar recurso pelo nome..."
            value={termoBusca}
            onChange={(e) => setTermoBusca(e.target.value)}
          />
          <select value={categoriaFiltro} onChange={(e) => setCategoriaFiltro(e.target.value)}>
            <option value="">Todas as categorias</option>
            {CATEGORIA_OPTIONS.map((opcao) => (
              <option key={opcao.value} value={opcao.value}>
                {opcao.label}
              </option>
            ))}
          </select>
        </div>
      )}

      {carregando ? (
        <p>Carregando...</p>
      ) : (
        <RecursoTable
          recursos={recursosFiltrados}
          podeGerenciar={podeGerenciar}
          onEditar={handleEditar}
          onInativar={handleInativar}
          onAlternarDisponibilidade={handleAlternarDisponibilidade}
        />
      )}
    </div>
  );
}