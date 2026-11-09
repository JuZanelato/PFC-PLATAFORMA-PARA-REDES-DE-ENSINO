import { useEffect, useMemo, useState } from "react";
import solicitacaoService, {
    STATUS_SOLICITACAO,
    formatarDataHora,
} from "../Services/solicitacaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import StatusBadge from "../Components/StatusBadge";
import "./RecursosPage.css";
import "./SolicitacoesPage.css";


export default function HistoricoPage() {
    const [movimentacoes, setMovimentacoes] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");
    const [termo, setTermo] = useState("");
    const [statusFiltro, setStatusFiltro] = useState("");

    useEffect(() => {
        let ativo = true;
        solicitacaoService
            .movimentacoes()
            .then((dados) => ativo && setMovimentacoes(dados))
            .catch((err) => ativo && setErro(mensagemDeErro(err, "Não foi possível carregar o histórico.")))
            .finally(() => ativo && setCarregando(false));
        return () => {
            ativo = false;
        };
    }, []);

    const filtradas = useMemo(() => {
        const t = termo.trim().toLowerCase();
        return movimentacoes.filter((m) => {
            const bateTexto =
                !t ||
                m.recursoNome?.toLowerCase().includes(t) ||
                m.responsavel?.toLowerCase().includes(t) ||
                m.instituicaoResponsavel?.toLowerCase().includes(t);
            const bateStatus = !statusFiltro || m.statusNovo === statusFiltro;
            return bateTexto && bateStatus;
        });
    }, [movimentacoes, termo, statusFiltro]);

    return (
        <div className="recurso-page">
            <header className="recurso-page-header">
                <h1>Histórico de movimentações</h1>
            </header>

            <p className="recurso-aviso">
                Todas as movimentações das solicitações em que sua instituição participa, como solicitante ou
                como dona do recurso.
            </p>

            <div className="recurso-filtros">
                <input
                    type="text"
                    placeholder="Filtrar por recurso, usuário ou instituição..."
                    value={termo}
                    onChange={(e) => setTermo(e.target.value)}
                />
                <select value={statusFiltro} onChange={(e) => setStatusFiltro(e.target.value)}>
                    <option value="">Todos os status</option>
                    {Object.entries(STATUS_SOLICITACAO).map(([valor, info]) => (
                        <option key={valor} value={valor}>
                            {info.label}
                        </option>
                    ))}
                </select>
            </div>

            {erro && <p className="recurso-erro-geral">{erro}</p>}

            {carregando ? (
                <p>Carregando...</p>
            ) : filtradas.length === 0 ? (
                <p className="recurso-vazio">Nenhuma movimentação encontrada.</p>
            ) : (
                <table className="recurso-tabela">
                    <thead>
                        <tr>
                            <th>Data</th>
                            <th>Solicitação</th>
                            <th>Recurso</th>
                            <th>De</th>
                            <th>Para</th>
                            <th>Responsável</th>
                            <th>Observação</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtradas.map((m) => (
                            <tr key={m.id}>
                                <td>{formatarDataHora(m.dataHora)}</td>
                                <td>#{m.solicitacaoId}</td>
                                <td>{m.recursoNome}</td>
                                <td>
                                    <StatusBadge status={m.statusAnterior} />
                                </td>
                                <td>
                                    <StatusBadge status={m.statusNovo} />
                                </td>
                                <td>
                                    {m.responsavel}
                                    {m.responsavel !== m.instituicaoResponsavel && (
                                        <div className="sol-detalhe">{m.instituicaoResponsavel}</div>
                                    )}
                                </td>
                                <td>{m.observacao}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
