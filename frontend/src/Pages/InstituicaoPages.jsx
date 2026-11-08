import { useMemo, useState } from "react";
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import { useInstituicao } from "../Hook/useInstituicao";
import authService from "../Services/authService";
import { mensagemDeErro } from "../Services/mensagemErro";
import InstituicaoForm from "../Components/InstituicaoForm";
import InstituicaoTable from "../Components/InstituicaoTable";
import "./InstituicoesPages.css";

export default function InstituicaoPages() {
  const usuarioLogado = authService.usuarioLogado();
  const navigate = useNavigate();

  const filtroInicial = useMemo(
    () => ({ cadastradoPor: usuarioLogado?.email }),
    [usuarioLogado?.email]
  );

  const { instituicoes, carregando, erro, salvar, inativar } = useInstituicao(filtroInicial);
  const [erroAcao, setErroAcao] = useState(null);

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

  const handleInativar = async (instituicao) => {
    const confirmar = window.confirm(
      `Inativar a instituição "${instituicao.nome}"? Ela deixará de aparecer na rede, mas o histórico será mantido.`
    );
    if (!confirmar) return;
    try {
      setErroAcao(null);
      await inativar(instituicao.id);
    } catch (err) {
      setErroAcao(mensagemDeErro(err, "Não foi possível inativar a instituição."));
    }
  };

  const handleSair = async () => {
    try {
      await authService.logout();
    } catch {
      // sessão já expirada no servidor: o token local é limpo mesmo assim
    }
    navigate("/login");
  };

  return (
    <div className="instituicao-page">
      <header className="instituicao-page-header">
        <h1>Minhas instituições</h1>

        <div className="instituicao-page-acoes">
          {!mostrarForm && (
            <button type="button" onClick={handleNovo}>
              + Nova instituição
            </button>
          )}
          <Link to="/cadastro" className="link-acao">
            Cadastrar usuário
          </Link>
          {usuarioLogado?.perfil === "ADMINISTRADOR" && (
            <Link to="/auditoria" className="link-acao">
              Log de auditoria
            </Link>
          )}
          <button type="button" className="btn-secundario" onClick={handleSair}>
            Sair
          </button>

                    <Link to="/recursos" className="link-acao">
            Gestão de recursos
          </Link>
        </div>
      </header>

      {mostrarForm && (
        <InstituicaoForm
          instituicaoSelecionada={selecionada}
          onSalvar={handleSalvar}
          onCancelar={handleCancelar}
        />
      )}

      {erro && <p className="erro-geral">{erro}</p>}
      {erroAcao && <p className="erro-geral">{erroAcao}</p>}

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
          podeInativar={usuarioLogado?.perfil === "ADMINISTRADOR"}
          onEditar={handleEditar}
          onInativar={handleInativar}
        />
      )}
    </div>
  );
}