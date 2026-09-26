import { useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useInstituicao } from "../Hook/useInstituicao";
import authService from "../Services/authService";
import InstituicaoForm from "../Components/InstituicaoForm";
import InstituicaoTable from "../Components/InstituicaoTable";
import "./InstituicoesPages.css";

export default function InstituicaoPages() {
  const usuarioLogado = authService.usuarioLogado();

  const filtroInicial = useMemo(
    () => ({ cadastradoPor: usuarioLogado?.email }),
    [usuarioLogado?.email]
  );

  const { instituicoes, carregando, erro, salvar, excluir } = useInstituicao(filtroInicial);

  const [searchParams] = useSearchParams();
  const [selecionada, setSelecionada] = useState(null);
  const [mostrarForm, setMostrarForm] = useState(searchParams.get("novo") === "true");
  const [termoBusca, setTermoBusca] = useState("");

  const instituicoesFiltradas = useMemo(() => {
    if (!termoBusca.trim()) return instituicoes;
    const termo = termoBusca.trim().toLowerCase();
    return instituicoes.filter((inst) => inst.nome?.toLowerCase().includes(termo));
  }, [instituicoes, termoBusca]);

  const handleNovo = () => {
    setSelecionada(null);
    setMostrarForm(true);
  };

  const handleEditar = (instituicao) => {
    setSelecionada(instituicao);
    setMostrarForm(true);
  };

  const handleCancelar = () => {
    setSelecionada(null);
    setMostrarForm(false);
  };

  const handleSalvar = async (form) => {
    await salvar(form);
    setSelecionada(null);
    setMostrarForm(false);
  };

  const handleExcluir = async (instituicao) => {
    const confirmar = window.confirm(
      `Excluir a instituição "${instituicao.nome}"? Essa ação não pode ser desfeita.`
    );
    if (!confirmar) return;
    await excluir(instituicao.id);
  };

  return (
    <div className="instituicao-page">
      <header className="instituicao-page-header">
        <h1>Minhas instituições</h1>
        {!mostrarForm && (
          <button type="button" onClick={handleNovo}>
            + Nova instituição
          </button>
        )}
      </header>

      {mostrarForm && (
        <InstituicaoForm
          instituicaoSelecionada={selecionada}
          onSalvar={handleSalvar}
          onCancelar={handleCancelar}
        />
      )}

      {erro && <p className="erro-geral">{erro}</p>}

      {!mostrarForm && !carregando && (
        <input
          type="text"
          className="busca-instituicao"
          placeholder="Buscar instituição pelo nome..."
          value={termoBusca}
          onChange={(e) => setTermoBusca(e.target.value)}
        />
      )}

      {carregando ? (
        <p>Carregando...</p>
      ) : (
        <InstituicaoTable
          instituicoes={instituicoesFiltradas}
          onEditar={handleEditar}
          onExcluir={handleExcluir}
        />
      )}
    </div>
  );
}