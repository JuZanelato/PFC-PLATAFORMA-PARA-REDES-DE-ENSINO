import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import recursoService, { CATEGORIA_OPTIONS, CONDICAO_OPTIONS } from "../Services/recursoService";
import instituicaoService from "../Services/instituicaoService";
import solicitacaoService from "../Services/solicitacaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import SolicitacaoForm from "../Components/SolicitacaoForm";
import "./RecursosPage.css";
import "./SolicitacoesPage.css";

const labelDe = (opcoes, valor) => opcoes.find((o) => o.value === valor)?.label || valor;
const FILTRO_VAZIO = { nome: "", categoria: "", instituicaoId: "" };


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

    useEffect(() => {
        instituicaoService.listarPublicas().then(setInstituicoes).catch(() => setInstituicoes([]));
    }, []);

    useEffect(() => {
        let ativo = true;
        recursoService
            .listarRede(filtroAplicado)
            .then((dados) => {
                if (!ativo) return;
                setRecursos(dados);
                setErro("");
            })
            .catch((err) => ativo && setErro(mensagemDeErro(err, "Não foi possível buscar os recursos.")))
            .finally(() => ativo && setCarregando(false));
        return () => {
            ativo = false;
        };
    }, [filtroAplicado, versao]);

    const handleFiltro = (campo) => (e) => setFiltro((prev) => ({ ...prev, [campo]: e.target.value }));

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
                <h1>Buscar recursos na rede</h1>
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
                <select value={filtro.instituicaoId} onChange={handleFiltro("instituicaoId")}>
                    <option value="">Todas as instituições</option>
                    {instituicoes.map((i) => (
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
                <p className="recurso-vazio">Nenhum recurso disponível encontrado com esses filtros.</p>
            ) : (
                <table className="recurso-tabela">
                    <thead>
                        <tr>
                            <th>Recurso</th>
                            <th>Categoria</th>
                            <th>Condição</th>
                            <th>Disponível</th>
                            <th>Instituição</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {recursos.map((r) => (
                            <tr key={r.id}>
                                <td>{r.nome}</td>
                                <td>{labelDe(CATEGORIA_OPTIONS, r.categoria)}</td>
                                <td>{labelDe(CONDICAO_OPTIONS, r.condicao)}</td>
                                <td>{r.quantidade}</td>
                                <td>{r.instituicaoNome}</td>
                                <td className="recurso-acoes">
                                    <button
                                        type="button"
                                        onClick={() => {
                                            setSucesso("");
                                            setSelecionado(r);
                                            window.scrollTo({ top: 0, behavior: "smooth" });
                                        }}
                                    >
                                        Solicitar
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
