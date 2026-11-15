import { useEffect, useMemo, useState } from "react";
import authService from "../Services/authService";
import instituicaoService from "../Services/instituicaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import InstituicaoForm from "../Components/InstituicaoForm";
import "./InstituicoesPages.css";

function MinhaInstituicao() {
  const [instituicao, setInstituicao] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [editando, setEditando] = useState(false);
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    let telaAberta = true;
    instituicaoService
      .minha()
      .then((dados) => {
        if (telaAberta) setInstituicao(dados);
      })
      .catch((err) => {
        if (telaAberta) setErro(mensagemDeErro(err, "Não foi possível carregar a sua instituição."));
      })
      .finally(() => {
        if (telaAberta) setCarregando(false);
      });
    return () => {
      telaAberta = false;
    };
  }, []);

  const handleSalvar = async (form) => {
    await instituicaoService.atualizar(instituicao.id, form);
    const atualizada = await instituicaoService.minha();
    setInstituicao(atualizada);
    setEditando(false);
    setMensagem("Dados da instituição atualizados.");

    const usuario = authService.usuarioLogado();
    if (usuario) {
      localStorage.setItem("usuario", JSON.stringify({ ...usuario, instituicaoNome: atualizada.nome }));
    }
  };

  const iniciarEdicao = () => {
    setMensagem("");
    setEditando(true);
  };

  return (
    <section className="inst-secao">
      <div className="inst-secao-cabecalho">
        <h2>Minha instituição</h2>
        {instituicao && !editando && (
          <button type="button" onClick={iniciarEdicao}>
            Editar dados
          </button>
        )}
      </div>

      {erro && <p className="erro-geral">{erro}</p>}
      {mensagem && <p className="inst-sucesso">{mensagem}</p>}

      {carregando && <p>Carregando...</p>}

      {instituicao && editando && (
        <InstituicaoForm
          instituicaoSelecionada={instituicao}
          onSalvar={handleSalvar}
          onCancelar={() => setEditando(false)}
          esconderStatus
        />
      )}

      {instituicao && !editando && (
        <dl className="inst-dados">
          <dt>Nome</dt>
          <dd>{instituicao.nome}</dd>
          <dt>CNPJ</dt>
          <dd>{instituicao.cnpj}</dd>
          <dt>Endereço</dt>
          <dd>{instituicao.endereco}</dd>
          <dt>CEP</dt>
          <dd>{instituicao.cep || "Não informado"}</dd>
          <dt>E-mail</dt>
          <dd>{instituicao.email}</dd>
          <dt>Chave PIX</dt>
          <dd>{instituicao.chavePix || "Não informada"}</dd>
          <dt>Cadastrada em</dt>
          <dd>{instituicao.criadoEm ? new Date(instituicao.criadoEm).toLocaleDateString("pt-BR") : "—"}</dd>
        </dl>
      )}
    </section>
  );
}

function InstituicoesDaRede() {
  const [instituicoes, setInstituicoes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [busca, setBusca] = useState("");
  const minhaEscola = authService.usuarioLogado()?.instituicaoNome;

  useEffect(() => {
    let telaAberta = true;
    instituicaoService
      .listarRede()
      .then((dados) => {
        if (telaAberta) setInstituicoes(dados);
      })
      .catch((err) => {
        if (telaAberta) setErro(mensagemDeErro(err, "Não foi possível carregar as instituições da rede."));
      })
      .finally(() => {
        if (telaAberta) setCarregando(false);
      });
    return () => {
      telaAberta = false;
    };
  }, []);

  const filtradas = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    if (!termo) return instituicoes;
    return instituicoes.filter((inst) => inst.nome?.toLowerCase().includes(termo));
  }, [instituicoes, busca]);

  return (
    <section className="inst-secao">
      <div className="inst-secao-cabecalho">
        <h2>Instituições da rede</h2>
      </div>
      <p className="inst-dica">Consulte as escolas que participam da rede de compartilhamento.</p>

      {erro && <p className="erro-geral">{erro}</p>}

      <input
        type="text"
        className="busca-instituicao"
        placeholder="Buscar instituição pelo nome..."
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
      />

      {carregando ? (
        <p>Carregando...</p>
      ) : filtradas.length === 0 ? (
        <p className="inst-vazio">Nenhuma instituição encontrada.</p>
      ) : (
        <div className="inst-tabela-wrap">
          <table className="instituicao-tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Endereço</th>
                <th>E-mail</th>
              </tr>
            </thead>
            <tbody>
              {filtradas.map((inst) => (
                <tr key={inst.id}>
                  <td>
                    {inst.nome}
                    {inst.nome === minhaEscola && <span className="inst-sua"> (sua escola)</span>}
                  </td>
                  <td>{inst.endereco}</td>
                  <td>{inst.email}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default function InstituicaoPages() {
  const ehDiretor = authService.usuarioLogado()?.perfil === "ADMINISTRADOR";

  return (
    <div className="instituicao-page">
      <header className="instituicao-page-header">
        <h1>Instituições</h1>
      </header>

      {ehDiretor && <MinhaInstituicao />}
      <InstituicoesDaRede />
    </div>
  );
}