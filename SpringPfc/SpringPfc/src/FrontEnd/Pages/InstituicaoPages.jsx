import { useState } from "react";
import { useInstituicao } from "../Hook/useInstituicao";
import InstituicaoForm from "../Components/InstituicaoForm";
import InstituicaoTable from "../Components/InstituicaoTable";
import "./InstituicoesPage.css";

export default function InstituicaoPage() {
  const { instituicoes, carregando, erro, salvar, excluir } = useInstituicao();
  const [selecionada, setSelecionada] = useState(null);

  const handleExcluir = async (instituicao) => {
    const confirmar = window.confirm(
      `Excluir a instituição "${instituicao.nome}"? Essa ação não pode ser desfeita.`
    );
    if (!confirmar) return;
    await excluir(instituicao.id);
  };

  const handleSalvar = async (form) => {
    await salvar(form);
    setSelecionada(null);
  };

  return (
    <div className="instituicao-page">
      <header>
        <h1>Gestão de Instituições</h1>
      </header>

      <InstituicaoForm
        instituicaoSelecionada={selecionada}
        onSalvar={handleSalvar}
        onCancelar={() => setSelecionada(null)}
      />

      {erro && <p className="erro-geral">{erro}</p>}
      {carregando ? (
        <p>Carregando...</p>
      ) : (
        <InstituicaoTable
          instituicoes={instituicoes}
          onEditar={setSelecionada}
          onExcluir={handleExcluir}
        />
      )}
    </div>
  );
}