import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import usuarioService, { PERFIL_LABEL } from "../Services/usuarioService";
import authService from "../Services/authService";
import { mensagemDeErro } from "../Services/mensagemErro";
import "./UsuariosPage.css";

const foiAnonimizado = (usuario) => usuario.email?.endsWith("@anonimizado.local");

function situacaoDe(usuario) {
  if (foiAnonimizado(usuario)) return { texto: "Dados excluídos", classe: "usr-selo inativo" };
  if (usuario.aguardandoAprovacao) return { texto: "Aguardando aprovação", classe: "usr-selo pendente" };
  if (usuario.ativo) return { texto: "Ativo", classe: "usr-selo ativo" };
  return { texto: "Inativo", classe: "usr-selo inativo" };
}

function buscarListas() {
  return Promise.all([usuarioService.listarPendentes(), usuarioService.listar()]);
}

export default function UsuariosPage() {
  const [pendentes, setPendentes] = useState([]);
  const [usuarios, setUsuarios] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [processandoId, setProcessandoId] = useState(null);
  const eu = authService.usuarioLogado();

  useEffect(() => {
    let telaAberta = true;
    buscarListas()
      .then(([listaPendentes, listaUsuarios]) => {
        if (!telaAberta) return;
        setPendentes(listaPendentes);
        setUsuarios(listaUsuarios);
      })
      .catch((err) => {
        if (telaAberta) setErro(mensagemDeErro(err, "Não foi possível carregar os usuários."));
      })
      .finally(() => {
        if (telaAberta) setCarregando(false);
      });
    return () => {
      telaAberta = false;
    };
  }, []);

  const recarregar = async () => {
    const [listaPendentes, listaUsuarios] = await buscarListas();
    setPendentes(listaPendentes);
    setUsuarios(listaUsuarios);
  };

  const executar = async (id, acao, textoSucesso) => {
    setProcessandoId(id);
    setErro("");
    setMensagem("");
    try {
      await acao(id);
      setMensagem(textoSucesso);
      await recarregar();
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível concluir a ação."));
    } finally {
      setProcessandoId(null);
    }
  };

  const aprovar = (u) =>
    executar(u.id, usuarioService.aprovar, `${u.nome} foi aprovado(a) e já pode entrar no sistema.`);

  const recusar = (u) => {
    if (!window.confirm(`Recusar o cadastro de ${u.nome}? A conta ficará inativa.`)) return;
    executar(u.id, usuarioService.recusar, `O cadastro de ${u.nome} foi recusado.`);
  };

  const inativar = (u) => {
    if (!window.confirm(`Inativar ${u.nome}? Essa pessoa não conseguirá mais entrar no sistema.`)) return;
    executar(u.id, usuarioService.inativar, `${u.nome} foi inativado(a).`);
  };

  const excluirDados = (u) => {
    const confirmou = window.confirm(
      `Excluir os dados de ${u.nome}?\n\nNome, e-mail e senha serão apagados de forma definitiva. ` +
        "Os empréstimos e doações continuam no histórico como \"Usuário removido\". Esta ação não pode ser desfeita."
    );
    if (!confirmou) return;
    executar(u.id, usuarioService.excluirDados, `Os dados de ${u.nome} foram excluídos.`);
  };

  return (
    <div className="usr-page">
      <header className="usr-cabecalho">
        <div>
          <h1>Usuários da escola</h1>
          {eu?.instituicaoNome && <p className="usr-subtitulo">{eu.instituicaoNome}</p>}
        </div>
        <Link to="/cadastro" className="usr-btn">
          Cadastrar usuário
        </Link>
      </header>

      {erro && <p className="usr-erro">{erro}</p>}
      {mensagem && <p className="usr-sucesso">{mensagem}</p>}

      {carregando ? (
        <p>Carregando...</p>
      ) : (
        <>
          <section className="usr-secao">
            <h2>
              Gestores aguardando aprovação
              {pendentes.length > 0 && <span className="usr-contador">{pendentes.length}</span>}
            </h2>

            {pendentes.length === 0 ? (
              <p className="usr-vazio">Nenhum gestor aguardando aprovação.</p>
            ) : (
              <div className="usr-tabela-wrap">
                <table className="usr-tabela">
                  <thead>
                    <tr>
                      <th>Nome</th>
                      <th>E-mail</th>
                      <th>Cargo</th>
                      <th>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendentes.map((u) => (
                      <tr key={u.id}>
                        <td>{u.nome}</td>
                        <td>{u.email}</td>
                        <td>{PERFIL_LABEL[u.perfil] || u.perfil}</td>
                        <td className="usr-acoes">
                          <button
                            type="button"
                            className="usr-btn"
                            disabled={processandoId === u.id}
                            onClick={() => aprovar(u)}
                          >
                            Aprovar
                          </button>
                          <button
                            type="button"
                            className="usr-btn secundario"
                            disabled={processandoId === u.id}
                            onClick={() => recusar(u)}
                          >
                            Recusar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="usr-secao">
            <h2>Todos os usuários</h2>

            {usuarios.length === 0 ? (
              <p className="usr-vazio">Nenhum usuário cadastrado nesta escola.</p>
            ) : (
              <div className="usr-tabela-wrap">
                <table className="usr-tabela">
                  <thead>
                    <tr>
                      <th>Nome</th>
                      <th>E-mail</th>
                      <th>Cargo</th>
                      <th>Situação</th>
                      <th>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usuarios.map((u) => {
                      const situacao = situacaoDe(u);
                      const podeInativar = u.ativo && !u.aguardandoAprovacao && u.id !== eu?.id;
                      const podeExcluir = !foiAnonimizado(u) && u.id !== eu?.id;
                      return (
                        <tr key={u.id}>
                          <td>
                            {u.nome}
                            {u.id === eu?.id && <small className="usr-voce"> (você)</small>}
                          </td>
                          <td>{u.email}</td>
                          <td>{PERFIL_LABEL[u.perfil] || u.perfil}</td>
                          <td>
                            <span className={situacao.classe}>{situacao.texto}</span>
                          </td>
                          <td className="usr-acoes">
                            {podeInativar && (
                              <button
                                type="button"
                                className="usr-btn secundario"
                                disabled={processandoId === u.id}
                                onClick={() => inativar(u)}
                              >
                                Inativar
                              </button>
                            )}
                            {podeExcluir && (
                              <button
                                type="button"
                                className="usr-btn perigo"
                                disabled={processandoId === u.id}
                                onClick={() => excluirDados(u)}
                              >
                                Excluir dados
                              </button>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}