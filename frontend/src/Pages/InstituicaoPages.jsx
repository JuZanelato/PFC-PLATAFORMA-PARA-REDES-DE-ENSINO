import { useState } from "react";
import { Link } from "react-router-dom";
import { useInstituicao } from "../Hook/useInstituicao";
import authService from "../Services/authService";
import InstituicaoForm from "../Components/InstituicaoForm";
import InstituicaoTable from "../Components/InstituicaoTable";
import "./InstituicaoPages.css";
 
export default function InstituicaoPage() {
  const { instituicoes, carregando, erro, salvar, excluir } = useInstituicao();
  const [selecionada, setSelecionada] = useState(null);
  const usuario = authService.usuarioLogado();
 
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
        {usuario?.perfil === "ADMINISTRADOR" && (
<Link to="/cadastro">Cadastrar novo usuário</Link>
        )}
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