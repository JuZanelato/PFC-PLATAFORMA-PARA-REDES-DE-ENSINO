import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import recursoService, { CATEGORIA_OPTIONS, CONDICAO_OPTIONS } from "../Services/recursoService";
import instituicaoService from "../Services/instituicaoService";
import solicitacaoService from "../Services/SolicitacaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import SolicitacaoForm from "../Components/SolicitacaoForm";
import Icone, { VISUAL_CATEGORIA } from "../Components/Icone";
import EstadoVazio from "../Components/EstadoVazio";
import CampoRegiao from "../Components/CampoRegiao";
import { combinaRegiao, regioesDe } from "../Services/regiao";
import "./RecursosPage.css";
import "./SolicitacoesPage.css";

const labelDe = (opcoes, valor) => opcoes.find((o) => o.value === valor)?.label || valor;
const FILTRO_VAZIO = { nome: "", categoria: "", instituicaoId: "", regiao: "" };
const LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
const inicialDe = (nome) =>
    (nome || "").trim().charAt(0).normalize("NFD").replace(/[\u0300-\u036f]/g, "").toUpperCase();


const POR_PAGINA = 24;

export default function BuscarRecursosPage() {
    const [filtro, setFiltro] = useState(FILTRO_VAZIO);
    const [filtroAplicado, setFiltroAplicado] = useState(FILTRO_VAZIO);
    const [recursos, setRecursos] = useState([]);
    const [instituicoes, setInstituicoes] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");
    const [sucesso, setSucesso] = useState("");
    const [selecionado, setSelecionado] = useState(null);
    const [versao, setVersao] = useState(0);
    const [limite, setLimite] = useState(POR_PAGINA);
    const [letra, setLetra] = useState("");

    useEffect(() => {
        instituicaoService.listarRede().then(setInstituicoes).catch(() => setInstituicoes([]));
    }, []);

    useEffect(() => {
        let ativo = true;
        recursoService
            .listarRede(filtroAplicado)
            .then((dados) => {
                if (!ativo) return;
                setRecursos(dados);
                setLimite(POR_PAGINA);
                setLetra("");
                setErro("");
            })
            .catch((err) => ativo && setErro(mensagemDeErro(err, "Não foi possível buscar os recursos.")))
            .finally(() => ativo && setCarregando(false));
        return () => {
            ativo = false;
        };
    }, [filtroAplicado, versao]);

    const regioes = regioesDe(instituicoes);
    const enderecoPorEscola = new Map(instituicoes.map((i) => [i.id, i.endereco]));
    const instituicoesDaRegiao = instituicoes.filter((i) => combinaRegiao(i.endereco, filtro.regiao));
    const daRegiao = filtroAplicado.regiao
        ? recursos.filter((r) => combinaRegiao(enderecoPorEscola.get(r.instituicaoId), filtroAplicado.regiao))
        : recursos;

    const contagemPorLetra = daRegiao.reduce((acc, r) => {
        const l = inicialDe(r.nome);
        acc[l] = (acc[l] || 0) + 1;
        return acc;
    }, {});
    const visiveis = letra ? daRegiao.filter((r) => inicialDe(r.nome) === letra) : daRegiao;

    const escolherLetra = (l) => {
        setLetra(l);
        setLimite(POR_PAGINA);
    };

    const handleFiltro = (campo) => (e) => setFiltro((prev) => ({ ...prev, [campo]: e.target.value }));

    const comRegiao = (base, valor) => {
        const escolaForaDaRegiao =
            base.instituicaoId && !combinaRegiao(enderecoPorEscola.get(Number(base.instituicaoId)), valor);
        return { ...base, regiao: valor, instituicaoId: escolaForaDaRegiao ? "" : base.instituicaoId };
    };

    const handleRegiao = (valor) => setFiltro((prev) => comRegiao(prev, valor));

    const escolherRegiao = (valor) => {
        const novo = comRegiao(filtro, valor);
        setFiltro(novo);
        setSucesso("");
        setFiltroAplicado(novo);
    };

    const handleBuscar = (e) => {
        e.preventDefault();
        setSucesso("");
        setFiltroAplicado({ ...filtro });
    };

    const handleLimpar = () => {
        setFiltro(FILTRO_VAZIO);
        setFiltroAplicado(FILTRO_VAZIO);
    };

    const handleEnviar = async (dados) => {
        await solicitacaoService.criar(dados);
        setSucesso(`Solicitação de "${selecionado.nome}" enviada para ${selecionado.instituicaoNome}.`);
        setSelecionado(null);
        setVersao((v) => v + 1);
    };

    return (
        <div className="recurso-page">
            <header className="recurso-page-header">
                <h1>
                    <Icone nome="busca" tamanho={28} className="titulo-icone" /> Buscar recursos na rede
                </h1>
            </header>

            <p className="recurso-aviso">
                Aqui aparecem apenas recursos <strong>ativos e disponíveis</strong> de outras instituições.
                Acompanhe seus pedidos em <Link to="/solicitacoes">Solicitações</Link>.
            </p>

            {sucesso && <p className="sol-sucesso">{sucesso}</p>}
            {erro && <p className="recurso-erro-geral">{erro}</p>}

            {selecionado && (
                <SolicitacaoForm
                    key={selecionado.id}
                    recurso={selecionado}
                    onEnviar={handleEnviar}
                    onCancelar={() => setSelecionado(null)}
                />
            )}

            <form className="recurso-filtros" onSubmit={handleBuscar}>
                <input
                    type="text"
                    placeholder="Nome do recurso..."
                    value={filtro.nome}
                    onChange={handleFiltro("nome")}
                />
                <select value={filtro.categoria} onChange={handleFiltro("categoria")}>
                    <option value="">Todas as categorias</option>
                    {CATEGORIA_OPTIONS.map((o) => (
                        <option key={o.value} value={o.value}>
                            {o.label}
                        </option>
                    ))}
                </select>
                <CampoRegiao
                    valor={filtro.regiao}
                    onChange={handleRegiao}
                    onEscolher={escolherRegiao}
                    regioes={regioes}
                />
                <select value={filtro.instituicaoId} onChange={handleFiltro("instituicaoId")}>
                    <option value="">
                        {filtro.regiao ? "Todas as instituições da região" : "Todas as instituições"}
                    </option>
                    {instituicoesDaRegiao.map((i) => (
                        <option key={i.id} value={i.id}>
                            {i.nome}
                        </option>
                    ))}
                </select>
                <button type="submit" className="sol-btn">
                    Buscar
                </button>
                <button type="button" className="sol-btn secundario" onClick={handleLimpar}>
                    Limpar
                </button>
            </form>

            {carregando ? (
                <p>Carregando...</p>
            ) : recursos.length === 0 ? (
                <EstadoVazio
                    titulo="Nenhum recurso encontrado"
                    texto="Tente outro nome, categoria ou instituição. Novos recursos aparecem aqui assim que as escolas da rede os cadastram."
                />
            ) : (
                
                <>
                <nav className="rede-letras" aria-label="Filtrar pela letra inicial">
                    <button
                        type="button"
                        className={`rede-letra todas ${letra === "" ? "ativa" : ""}`}
                        aria-pressed={letra === ""}
                        onClick={() => escolherLetra("")}
                    >
                        Todas
                    </button>
                    {LETRAS.map((l) => (
                        <button
                            key={l}
                            type="button"
                            className={`rede-letra ${letra === l ? "ativa" : ""}`}
                            aria-pressed={letra === l}
                            disabled={!contagemPorLetra[l]}
                            title={contagemPorLetra[l] ? `${contagemPorLetra[l]} recurso(s) com ${l}` : `Nenhum recurso com ${l}`}
                            onClick={() => escolherLetra(l)}
                        >
                            {l}
                        </button>
                    ))}
                </nav>
                <p className="rede-total">
                    {visiveis.length === 1 ? "1 recurso encontrado" : `${visiveis.length.toLocaleString("pt-BR")} recursos encontrados`}
                    {letra && ` com a letra ${letra}`}
                    {visiveis.length > limite && ` · mostrando ${limite.toLocaleString("pt-BR")}`}
                </p>
                <div className="rede-grade">
                    {visiveis.slice(0, limite).map((r) => {
                        const visual = VISUAL_CATEGORIA[r.categoria] || VISUAL_CATEGORIA.OUTRO;
                        return (
                            <article key={r.id} className="rede-cartao">
                                <div className="rede-cartao-topo">
                                    <span
                                        className="selo-categoria rede-selo"
                                        style={{ background: visual.fundo, color: visual.cor }}
                                    >
                                        <Icone nome={visual.icone} tamanho={24} />
                                    </span>
                                    <span className="rede-categoria" style={{ color: visual.cor }}>
                                        {labelDe(CATEGORIA_OPTIONS, r.categoria)}
                                    </span>
                                </div>
                                <h3>{r.nome}</h3>
                                <p className="rede-escola">
                                    <Icone nome="instituicoes" tamanho={15} /> {r.instituicaoNome}
                                </p>
                                <div className="rede-info">
                                    <span>
                                        <strong>{r.quantidade}</strong> disponível(is)
                                    </span>
                                    <span className="rede-condicao">{labelDe(CONDICAO_OPTIONS, r.condicao)}</span>
                                </div>
                                <button
                                    type="button"
                                    className="rede-solicitar"
                                    onClick={() => {
                                        setSucesso("");
                                        setSelecionado(r);
                                        window.scrollTo({ top: 0, behavior: "smooth" });
                                    }}
                                >
                                    Solicitar <Icone nome="seta" tamanho={16} />
                                </button>
                            </article>
                        );
                    })}
                </div>
                {visiveis.length > limite && (
                    <button type="button" className="rede-mais" onClick={() => setLimite((l) => l + POR_PAGINA)}>
                        Mostrar mais recursos
                    </button>
                )}
                </>
            )}
        </div>
    );
}