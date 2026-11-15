import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import instituicaoService from "../Services/instituicaoService";
import { CATEGORIA_OPTIONS, CONDICAO_OPTIONS } from "../Services/recursoService";
import { PERFIL_LABEL } from "../Services/usuarioService";
import { mensagemDeErro } from "../Services/mensagemErro";
import "./InstituicoesPages.css";

const rotulo = (opcoes, valor) => opcoes.find((o) => o.value === valor)?.label || valor;

export default function InstituicaoDetalhePage() {
  const { id } = useParams();
  const [detalhe, setDetalhe] = useState(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let telaAberta = true;
    instituicaoService
      .detalhes(id)
      .then((dados) => {
        if (telaAberta) {
          setDetalhe(dados);
          setErro("");
        }
      })
      .catch((err) => {
        if (telaAberta) setErro(mensagemDeErro(err, "Não foi possível carregar a instituição."));
      });
    return () => {
      telaAberta = false;
    };
  }, [id]);

  return (
    <div className="instituicao-page">
      <p className="inst-voltar">
        <Link to="/instituicoes">← Voltar para Instituições</Link>
      </p>

      {erro && <p className="erro-geral">{erro}</p>}
      {!detalhe && !erro && <p>Carregando...</p>}

      {detalhe && (
        <>
          <header className="instituicao-page-header">
            <h1>
              {detalhe.nome}
              {detalhe.minhaInstituicao && <span className="inst-sua"> (sua escola)</span>}
            </h1>
          </header>

          <section className="inst-secao">
            <h2>Dados da unidade</h2>
            <dl className="inst-dados">
              <dt>Endereço</dt>
              <dd>
                {detalhe.endereco}
                {detalhe.numero && !detalhe.endereco?.includes(detalhe.numero) && `, ${detalhe.numero}`}
              </dd>
              <dt>CEP</dt>
              <dd>{detalhe.cep || "Não informado"}</dd>
              <dt>Contato</dt>
              <dd>{detalhe.email}</dd>
            </dl>
          </section>

          {detalhe.minhaInstituicao && (
            <section className="inst-secao">
              <h2>Usuários vinculados ({detalhe.usuarios.length})</h2>
              {detalhe.usuarios.length === 0 ? (
                <p className="inst-vazio">Nenhum usuário ativo.</p>
              ) : (
                <div className="inst-tabela-wrap">
                  <table className="instituicao-tabela">
                    <thead>
                      <tr>
                        <th>Nome</th>
                        <th>Cargo</th>
                      </tr>
                    </thead>
                    <tbody>
                      {detalhe.usuarios.map((u) => (
                        <tr key={`${u.nome}-${u.perfil}`}>
                          <td>{u.nome}</td>
                          <td>{PERFIL_LABEL[u.perfil] || u.perfil}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          )}

          <section className="inst-secao">
            <h2>Recursos vinculados ({detalhe.recursos.length})</h2>
            {detalhe.recursos.length === 0 ? (
              <p className="inst-vazio">Esta unidade ainda não cadastrou recursos.</p>
            ) : (
              <div className="inst-tabela-wrap">
                <table className="instituicao-tabela">
                  <thead>
                    <tr>
                      <th>Recurso</th>
                      <th>Categoria</th>
                      <th>Quantidade</th>
                      <th>Condição</th>
                      <th>Situação</th>
                    </tr>
                  </thead>
                  <tbody>
                    {detalhe.recursos.map((r) => (
                      <tr key={r.id}>
                        <td>{r.nome}</td>
                        <td>{rotulo(CATEGORIA_OPTIONS, r.categoria)}</td>
                        <td>{r.quantidade}</td>
                        <td>{rotulo(CONDICAO_OPTIONS, r.condicao)}</td>
                        <td>
                          <span className={r.disponivel ? "inst-tag inst-tag-ok" : "inst-tag inst-tag-off"}>
                            {r.disponivel ? "Disponível" : "Indisponível"}
                          </span>
                        </td>
                      </tr>
                    ))}
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