import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import instituicaoService from "../Services/instituicaoService";
import { CATEGORIA_OPTIONS, CONDICAO_OPTIONS } from "../Services/recursoService";
import { PERFIL_LABEL } from "../Services/usuarioService";
import { mensagemDeErro } from "../Services/mensagemErro";
import "./InstituicoesPages.css";

const rotulo = (opcoes, valor) => opcoes.find((o) => o.value === valor)?.label || valor;

function DoacaoPix({ instituicaoId, nomeEscola, chavePix, minhaInstituicao }) {
  const [copiado, setCopiado] = useState(false);
  const [abrirForm, setAbrirForm] = useState(false);
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [enviando, setEnviando] = useState(false);

  const copiar = () => {
    navigator.clipboard
      .writeText(chavePix)
      .then(() => {
        setCopiado(true);
        setTimeout(() => setCopiado(false), 2000);
      })
      .catch(() => setErro("Não foi possível copiar. Selecione a chave e copie manualmente."));
  };

  const enviar = async (e) => {
    e.preventDefault();
    setErro("");
    const numero = Number(valor.replace(",", "."));
    if (!numero || numero <= 0) {
      setErro("Informe um valor maior que zero.");
      return;
    }
    setEnviando(true);
    try {
      await instituicaoService.informarDoacao(instituicaoId, { valor: numero, mensagem });
      setSucesso("Obrigado! A sua doação foi registrada e a escola poderá vê-la.");
      setAbrirForm(false);
      setValor("");
      setMensagem("");
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível registrar a doação."));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <section className="inst-secao">
      <div className="inst-pix">
        <h2>{minhaInstituicao ? "Chave Pix da sua escola" : `Apoie a ${nomeEscola}`}</h2>
        <p className="inst-pix-texto">
          {minhaInstituicao
            ? "É assim que as outras escolas da rede veem a sua chave Pix para doações."
            : "Esta escola recebe doações via Pix. Use a chave abaixo no aplicativo do seu banco e, depois, informe a doação aqui."}
        </p>
        <div className="inst-pix-chave">
          <code>{chavePix}</code>
          <button type="button" className="btn-copiar" onClick={copiar}>
            {copiado ? "Copiada!" : "Copiar chave"}
          </button>
        </div>

        {sucesso && <p className="inst-sucesso">{sucesso}</p>}
        {erro && <p className="erro-geral">{erro}</p>}

        {!minhaInstituicao && !abrirForm && (
          <button
            type="button"
            className="inst-pix-botao"
            onClick={() => {
              setSucesso("");
              setAbrirForm(true);
            }}
          >
            Informar doação
          </button>
        )}

        {!minhaInstituicao && abrirForm && (
          <form className="inst-pix-form" onSubmit={enviar}>
            <label>
              Valor doado (R$)
              <input
                type="text"
                inputMode="decimal"
                value={valor}
                onChange={(e) => setValor(e.target.value)}
                placeholder="Ex.: 50,00"
              />
            </label>
            <label>
              Mensagem (opcional)
              <textarea
                value={mensagem}
                onChange={(e) => setMensagem(e.target.value)}
                maxLength={300}
                rows={3}
                placeholder="Ex.: Para a compra de livros da biblioteca"
              />
            </label>
            <div className="inst-pix-acoes">
              <button type="submit" disabled={enviando}>
                {enviando ? "Enviando..." : "Registrar doação"}
              </button>
              <button type="button" className="inst-pix-cancelar" onClick={() => setAbrirForm(false)}>
                Cancelar
              </button>
            </div>
            <p className="inst-pix-aviso">
              O pagamento é feito pelo seu banco. Aqui você apenas registra a doação para a escola acompanhar.
            </p>
          </form>
        )}
      </div>
    </section>
  );
}

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

          {detalhe.chavePix && (
            <DoacaoPix
              instituicaoId={detalhe.id}
              nomeEscola={detalhe.nome}
              chavePix={detalhe.chavePix}
              minhaInstituicao={detalhe.minhaInstituicao}
            />
          )}

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