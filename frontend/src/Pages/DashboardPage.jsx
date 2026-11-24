import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import dashboardService from "../Services/dashboardService";
import { CATEGORIA_OPTIONS } from "../Services/recursoService";
import { STATUS_SOLICITACAO } from "../Services/SolicitacaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import GraficoBarras from "../Components/GraficoBarras";
import authService from "../Services/authService";
import Icone from "../Components/Icone";
import { IlustracaoRede } from "../Components/Ilustracao";
import "../Components/Visual.css";
import "./DashboardPage.css";

const rotuloCategoria = (c) => CATEGORIA_OPTIONS.find((o) => o.value === c)?.label || c;
const rotuloStatus = (s) => STATUS_SOLICITACAO[s]?.label || s;

const PERIODOS = [
  { value: "", label: "Todo o período" },
  { value: "7", label: "Últimos 7 dias" },
  { value: "30", label: "Últimos 30 dias" },
  { value: "90", label: "Últimos 90 dias" },
  { value: "365", label: "Último ano" },
];

const ESCOPOS = [
  { value: "REDE", label: "Toda a rede" },
  { value: "MINHA", label: "Minha escola" },
];

const VISOES = [
  { value: "TODAS", label: "Enviadas e recebidas" },
  { value: "RECEBIDAS", label: "Recebidas pela minha escola" },
  { value: "ENVIADAS", label: "Enviadas pela minha escola" },
];

const FILTROS_INICIAIS = { periodo: "", escopo: "REDE", categoria: "", visao: "TODAS" };

const rotuloDe = (lista, valor) => lista.find((o) => o.value === valor)?.label || valor;

function Indicador({ titulo, valor, detalhe, destaque, icone, para, textoLink }) {
  const conteudo = (
    <>
      {icone && (
        <span className="dash-indicador-icone">
          <Icone nome={icone} tamanho={20} />
        </span>
      )}
      <span className="dash-indicador-titulo">{titulo}</span>
      <span className="dash-indicador-valor">{valor}</span>
      {detalhe && <span className="dash-indicador-detalhe">{detalhe}</span>}
      {para && <span className="dash-indicador-link">{textoLink} →</span>}
    </>
  );
  const classe = `dash-card dash-indicador ${destaque ? "destaque" : ""} ${para ? "clicavel" : ""}`;
  return para ? (
    <Link to={para} className={classe}>
      {conteudo}
    </Link>
  ) : (
    <div className={classe}>{conteudo}</div>
  );
}

function Seletor({ rotulo, valor, opcoes, onChange, desabilitado }) {
  return (
    <label className="dash-filtro">
      <span>{rotulo}</span>
      <select value={valor} onChange={(e) => onChange(e.target.value)} disabled={desabilitado}>
        {opcoes.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
    </label>
  );
}

export default function DashboardPage() {
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState("");
  const [versao, setVersao] = useState(0);
  const [atualizadoEm, setAtualizadoEm] = useState(null);
  const [filtros, setFiltros] = useState(FILTROS_INICIAIS);
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    let ativo = true;
    setCarregando(true);
    dashboardService
      .resumo(filtros)
      .then((resp) => {
        if (!ativo) return;
        setDados(resp);
        setErro("");
        setAtualizadoEm(new Date());
      })
      .catch((err) => ativo && setErro(mensagemDeErro(err, "Não foi possível carregar o dashboard.")))
      .finally(() => ativo && setCarregando(false));
    return () => {
      ativo = false;
    };
  }, [versao, filtros]);

  const mudarFiltro = (campo) => (valor) => setFiltros((atual) => ({ ...atual, [campo]: valor }));
  const limparFiltro = (campo) => setFiltros((atual) => ({ ...atual, [campo]: FILTROS_INICIAIS[campo] }));

  const minhaEscola = filtros.escopo === "MINHA";
  const ativos = [
    filtros.periodo && { campo: "periodo", texto: rotuloDe(PERIODOS, filtros.periodo) },
    minhaEscola && { campo: "escopo", texto: "Minha escola" },
    filtros.categoria && { campo: "categoria", texto: rotuloCategoria(filtros.categoria) },
    minhaEscola && filtros.visao !== "TODAS" && { campo: "visao", texto: rotuloDe(VISOES, filtros.visao) },
  ].filter(Boolean);
  const onde = minhaEscola ? "da minha escola" : "da rede";
  const quando = filtros.periodo ? ` · ${rotuloDe(PERIODOS, filtros.periodo).toLowerCase()}` : "";

  if (erro && !dados) return <div className="dash-page"><p className="dash-erro">{erro}</p></div>;
  if (!dados) return <div className="dash-page"><p>Carregando indicadores...</p></div>;

  return (
    <div className="dash-page">
      <header className="dash-topo">
        <div>
          <h1>Dashboard gerencial</h1>
          <p>
            {minhaEscola ? "Visão da minha escola" : "Visão da rede de ensino"}
            {atualizadoEm && ` · atualizado às ${atualizadoEm.toLocaleTimeString("pt-BR")}`}
          </p>
        </div>
        <button type="button" className="dash-botao" onClick={() => setVersao((v) => v + 1)}>
          Atualizar
        </button>
      </header>

      <section className="dash-boasvindas">
        <div>
          <p className="dash-boasvindas-ola">Olá, {authService.usuarioLogado()?.nome?.split(" ")[0] || "bem-vindo(a)"}!</p>
          <p>
            Acompanhe como os recursos estão circulando entre as escolas da rede. Cada empréstimo é um material a
            mais chegando a quem precisa.
          </p>
          <Link to="/rede" className="dash-boasvindas-link">
            <Icone nome="busca" tamanho={16} /> Buscar recursos na rede
          </Link>
        </div>
        <IlustracaoRede className="dash-boasvindas-img" />
      </section>

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

      <section className="dash-card dash-filtros" aria-label="Filtros do dashboard">
        <div className="dash-filtros-topo">
          <h2>Filtros</h2>
          {ativos.length > 0 && (
            <button type="button" className="dash-filtros-limpar" onClick={() => setFiltros(FILTROS_INICIAIS)}>
              Limpar filtros
            </button>
          )}
        </div>
        <div className="dash-filtros-campos">
          <Seletor rotulo="Período das solicitações" valor={filtros.periodo} opcoes={PERIODOS} onChange={mudarFiltro("periodo")} />
          <Seletor rotulo="Abrangência" valor={filtros.escopo} opcoes={ESCOPOS} onChange={mudarFiltro("escopo")} />
          <Seletor
            rotulo="Categoria do recurso"
            valor={filtros.categoria}
            opcoes={[{ value: "", label: "Todas as categorias" }, ...CATEGORIA_OPTIONS]}
            onChange={mudarFiltro("categoria")}
          />
          <Seletor
            rotulo="Solicitações"
            valor={filtros.visao}
            opcoes={VISOES}
            onChange={mudarFiltro("visao")}
            desabilitado={!minhaEscola}
          />
        </div>
        {ativos.length > 0 && (
          <div className="dash-filtros-ativos">
            <span>Filtrando por:</span>
            {ativos.map((a) => (
              <button key={a.campo} type="button" className="dash-chip" onClick={() => limparFiltro(a.campo)}>
                {a.texto} <span aria-hidden="true">×</span>
                <span className="dash-sr">remover filtro</span>
              </button>
            ))}
          </div>
        )}
        {carregando && <p className="dash-filtros-status">Atualizando indicadores...</p>}
        {erro && <p className="dash-erro">{erro}</p>}
      </section>

      <div className="dash-indicadores">
        <Indicador
          titulo={minhaEscola ? "Recursos ativos da minha escola" : "Recursos ativos na rede"}
          icone="recursos"
          valor={dados.totalRecursos}
          detalhe={minhaEscola ? "cadastrados pela sua escola" : `${dados.instituicoesAtivas} instituição(ões) ativa(s)`}
          destaque
          para={minhaEscola ? "/recursos" : "/rede"}
          textoLink={minhaEscola ? "Ver meus recursos" : "Buscar na rede"}
        />
        <Indicador
          titulo="Disponíveis"
          icone="check"
          valor={dados.recursosDisponiveis}
          detalhe={`${dados.recursosIndisponiveis} indisponível(is)`}
          para={minhaEscola ? "/recursos" : "/rede"}
          textoLink={minhaEscola ? "Ver meus recursos" : "Buscar na rede"}
        />
        <Indicador
          titulo="Unidades emprestadas"
          icone="solicitacoes"
          valor={dados.unidadesEmprestadas}
          detalhe="aprovadas ou retiradas"
          para="/solicitacoes"
          textoLink="Ver solicitações"
        />
        <Indicador
          titulo="Solicitações pendentes"
          icone="relogio"
          valor={dados.solicitacoesPendentes}
          detalhe={`de ${dados.totalSolicitacoes} no total`}
          para="/solicitacoes"
          textoLink="Ver solicitações"
        />
        <Indicador
          titulo="Empréstimos concluídos"
          icone="historico"
          valor={dados.solicitacoesConcluidas}
          detalhe="com devolução registrada"
          para="/historico"
          textoLink="Ver histórico"
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
          subtitulo={`Recursos ativos ${onde}`}
          itens={dados.recursosPorCategoria}
          rotulo={rotuloCategoria}
          unidade="recurso(s)"
        />
        <GraficoBarras
          titulo="Solicitações por status"
          subtitulo={`Solicitações ${onde}${quando}`}
          itens={dados.solicitacoesPorStatus}
          rotulo={rotuloStatus}
          unidade="solicitação(ões)"
        />
        <GraficoBarras
          titulo="Instituições que mais emprestam"
          subtitulo={`Empréstimos aprovados, retirados ou concluídos (top 5)${quando}`}
          itens={dados.emprestimosPorInstituicao}
          unidade="empréstimo(s)"
        />
      </div>
    </div>
  );
}