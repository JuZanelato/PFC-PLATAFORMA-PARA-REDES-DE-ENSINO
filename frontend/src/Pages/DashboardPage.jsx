import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import dashboardService from "../Services/dashboardService";
import { CATEGORIA_OPTIONS } from "../Services/recursoService";
import { STATUS_SOLICITACAO } from "../Services/SolicitacaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import GraficoBarras from "../Components/GraficoBarras";
import "./DashboardPage.css";

const rotuloCategoria = (c) => CATEGORIA_OPTIONS.find((o) => o.value === c)?.label || c;
const rotuloStatus = (s) => STATUS_SOLICITACAO[s]?.label || s;

function Indicador({ titulo, valor, detalhe, destaque }) {
  return (
    <div className={`dash-card dash-indicador ${destaque ? "destaque" : ""}`}>
      <span className="dash-indicador-titulo">{titulo}</span>
      <span className="dash-indicador-valor">{valor}</span>
      {detalhe && <span className="dash-indicador-detalhe">{detalhe}</span>}
    </div>
  );
}


export default function DashboardPage() {
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState("");
  const [versao, setVersao] = useState(0);
  const [atualizadoEm, setAtualizadoEm] = useState(null);

  useEffect(() => {
    let ativo = true;
    dashboardService
      .resumo()
      .then((resp) => {
        if (!ativo) return;
        setDados(resp);
        setErro("");
        setAtualizadoEm(new Date());
      })
      .catch((err) => ativo && setErro(mensagemDeErro(err, "Não foi possível carregar o dashboard.")));
    return () => {
      ativo = false;
    };
  }, [versao]);

  if (erro) return <div className="dash-page"><p className="dash-erro">{erro}</p></div>;
  if (!dados) return <div className="dash-page"><p>Carregando indicadores...</p></div>;

  return (
    <div className="dash-page">
      <header className="dash-topo">
        <div>
          <h1>Dashboard gerencial</h1>
          <p>
            Visão da rede de ensino
            {atualizadoEm && ` · atualizado às ${atualizadoEm.toLocaleTimeString("pt-BR")}`}
          </p>
        </div>
        <button type="button" className="dash-botao" onClick={() => setVersao((v) => v + 1)}>
          Atualizar
        </button>
      </header>

      {(dados.pendentesParaMinhaAprovacao > 0 || dados.minhasSolicitacoesEmAndamento > 0) && (
        <div className="dash-alerta">
          <span>
            Minha instituição: <strong>{dados.pendentesParaMinhaAprovacao}</strong> solicitação(ões)
            aguardando minha aprovação · <strong>{dados.minhasSolicitacoesEmAndamento}</strong> pedido(s)
            enviado(s) em andamento.
          </span>
          <Link to="/solicitacoes">Ver solicitações</Link>
        </div>
      )}

      <div className="dash-indicadores">
        <Indicador
          titulo="Recursos ativos na rede"
          valor={dados.totalRecursos}
          detalhe={`${dados.instituicoesAtivas} instituição(ões) ativa(s)`}
          destaque
        />
        <Indicador
          titulo="Disponíveis"
          valor={dados.recursosDisponiveis}
          detalhe={`${dados.recursosIndisponiveis} indisponível(is)`}
        />
        <Indicador
          titulo="Unidades emprestadas"
          valor={dados.unidadesEmprestadas}
          detalhe="aprovadas ou retiradas"
        />
        <Indicador
          titulo="Solicitações pendentes"
          valor={dados.solicitacoesPendentes}
          detalhe={`de ${dados.totalSolicitacoes} no total`}
        />
        <Indicador
          titulo="Empréstimos concluídos"
          valor={dados.solicitacoesConcluidas}
          detalhe="com devolução registrada"
        />
      </div>

      <section className="dash-card dash-medidor">
        <div className="dash-medidor-topo">
          <h2>Taxa de aprovação</h2>
          <span className="dash-medidor-valor">{dados.taxaAprovacao}%</span>
        </div>
        <div
          className="dash-medidor-trilho"
          role="meter"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={dados.taxaAprovacao}
          aria-label="Taxa de aprovação"
        >
          <span className="dash-medidor-barra" style={{ width: `${dados.taxaAprovacao}%` }} />
        </div>
        <p>Solicitações aprovadas sobre o total de solicitações respondidas (aprovadas + recusadas).</p>
      </section>

      <div className="dash-graficos">
        <GraficoBarras
          titulo="Recursos por categoria"
          subtitulo="Recursos ativos cadastrados na rede"
          itens={dados.recursosPorCategoria}
          rotulo={rotuloCategoria}
          unidade="recurso(s)"
        />
        <GraficoBarras
          titulo="Solicitações por status"
          subtitulo="Todas as solicitações da rede"
          itens={dados.solicitacoesPorStatus}
          rotulo={rotuloStatus}
          unidade="solicitação(ões)"
        />
        <GraficoBarras
          titulo="Instituições que mais emprestam"
          subtitulo="Empréstimos aprovados, retirados ou concluídos (top 5)"
          itens={dados.emprestimosPorInstituicao}
          unidade="empréstimo(s)"
        />
      </div>
    </div>
  );
}
