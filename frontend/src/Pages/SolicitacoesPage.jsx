import { Fragment, useEffect, useState } from "react";
import authService from "../Services/authService";
import solicitacaoService, {
    STATUS_SOLICITACAO,
    formatarDataHora,
} from "../Services/solicitacaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import StatusBadge from "../Components/StatusBadge";
import "./RecursosPage.css";
import "./SolicitacoesPage.css";


export default function SolicitacoesPage() {
    const usuario = authService.usuarioLogado();
    const ehGestorOuAdmin = usuario?.perfil === "GESTOR" || usuario?.perfil === "ADMINISTRADOR";

    const [aba, setAba] = useState(ehGestorOuAdmin ? "recebidas" : "enviadas");
    const [statusFiltro, setStatusFiltro] = useState("");
    const [solicitacoes, setSolicitacoes] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");
    const [mensagem, setMensagem] = useState("");
    const [versao, setVersao] = useState(0);
    const [processando, setProcessando] = useState(null);
    const [historicoAberto, setHistoricoAberto] = useState(null);
    const [historico, setHistorico] = useState([]);

    useEffect(() => {
        let ativo = true;
        const buscar =
            aba === "recebidas" ? solicitacaoService.listarRecebidas : solicitacaoService.listarEnviadas;
        buscar(statusFiltro || undefined)
            .then((dados) => {
                if (!ativo) return;
                setSolicitacoes(dados);
                setErro("");
            })
            .catch((err) => ativo && setErro(mensagemDeErro(err, "Não foi possível carregar as solicitações.")))
            .finally(() => ativo && setCarregando(false));
        return () => {
            ativo = false;
        };
    }, [aba, statusFiltro, versao]);

    const trocarAba = (novaAba) => {
        setAba(novaAba);
        setHistoricoAberto(null);
        setMensagem("");
        setErro("");
    };

    const executar = async (solicitacao, acao, textoSucesso) => {
        setProcessando(solicitacao.id);
        setErro("");
        setMensagem("");
        try {
            await acao();
            setMensagem(textoSucesso);
            setHistoricoAberto(null);
            setVersao((v) => v + 1);
        } catch (err) {
            setErro(mensagemDeErro(err, "Não foi possível concluir a ação."));
        } finally {
            setProcessando(null);
        }
    };

    const handleAprovar = (s) =>
        executar(s, () => solicitacaoService.aprovar(s.id), `Solicitação #${s.id} aprovada. A quantidade foi reservada.`);

    const handleRecusar = (s) => {
        const motivo = window.prompt(`Motivo da recusa da solicitação #${s.id} (opcional):`, "");
        if (motivo === null) return;
        if (motivo.length > 500) {
            setErro("O motivo deve ter no máximo 500 caracteres.");
            return;
        }
        executar(s, () => solicitacaoService.recusar(s.id, motivo), `Solicitação #${s.id} recusada.`);
    };

    const handleCancelar = (s) => {
        if (!window.confirm(`Cancelar a solicitação #${s.id} de "${s.recursoNome}"?`)) return;
        executar(s, () => solicitacaoService.cancelar(s.id), `Solicitação #${s.id} cancelada.`);
    };

    const handleRetirada = (s) =>
        executar(s, () => solicitacaoService.registrarRetirada(s.id), `Retirada da solicitação #${s.id} registrada.`);

    const handleDevolucao = (s) => {
        if (!window.confirm(`Confirmar a devolução de ${s.quantidade} unidade(s) de "${s.recursoNome}"?`)) return;
        executar(
            s,
            () => solicitacaoService.registrarDevolucao(s.id),
            `Devolução registrada. Solicitação #${s.id} concluída e recurso liberado.`
        );
    };

    const alternarHistorico = async (s) => {
        if (historicoAberto === s.id) {
            setHistoricoAberto(null);
            return;
        }
        try {
            const dados = await solicitacaoService.historico(s.id);
            setHistorico(dados);
            setHistoricoAberto(s.id);
        } catch (err) {
            setErro(mensagemDeErro(err, "Não foi possível carregar o histórico."));
        }
    };

    const acoesDaLinha = (s) => {
        const ocupado = processando === s.id;
        const botoes = [];
        if (aba === "recebidas" && ehGestorOuAdmin) {
            if (s.status === "PENDENTE") {
                botoes.push(
                    <button key="aprovar" type="button" disabled={ocupado} onClick={() => handleAprovar(s)}>
                        Aprovar
                    </button>,
                    <button key="recusar" type="button" className="perigo" disabled={ocupado} onClick={() => handleRecusar(s)}>
                        Recusar
                    </button>
                );
            }
            if (s.status === "APROVADA") {
                botoes.push(
                    <button key="retirada" type="button" disabled={ocupado} onClick={() => handleRetirada(s)}>
                        Registrar retirada
                    </button>
                );
            }
            if (s.status === "RETIRADA") {
                botoes.push(
                    <button key="devolucao" type="button" disabled={ocupado} onClick={() => handleDevolucao(s)}>
                        Registrar devolução
                    </button>
                );
            }
        }
        if (aba === "enviadas" && s.status === "PENDENTE") {
            botoes.push(
                <button key="cancelar" type="button" className="perigo" disabled={ocupado} onClick={() => handleCancelar(s)}>
                    Cancelar
                </button>
            );
        }
        botoes.push(
            <button key="historico" type="button" onClick={() => alternarHistorico(s)}>
                {historicoAberto === s.id ? "Ocultar histórico" : "Histórico"}
            </button>
        );
        return botoes;
    };

    return (
        <div className="recurso-page sol-page">
            <header className="recurso-page-header">
                <h1>Solicitações</h1>
            </header>

            <div className="sol-abas">
                <button
                    type="button"
                    className={aba === "recebidas" ? "ativa" : ""}
                    onClick={() => trocarAba("recebidas")}
                >
                    Recebidas (para minha instituição)
                </button>
                <button
                    type="button"
                    className={aba === "enviadas" ? "ativa" : ""}
                    onClick={() => trocarAba("enviadas")}
                >
                    Enviadas (pela minha instituição)
                </button>
            </div>

            {aba === "recebidas" && !ehGestorOuAdmin && (
                <p className="recurso-aviso">
                    Apenas gestores e administradores podem aprovar, recusar e registrar retirada ou devolução.
                </p>
            )}

            <div className="recurso-filtros">
                <select value={statusFiltro} onChange={(e) => setStatusFiltro(e.target.value)}>
                    <option value="">Todos os status</option>
                    {Object.entries(STATUS_SOLICITACAO).map(([valor, info]) => (
                        <option key={valor} value={valor}>
                            {info.label}
                        </option>
                    ))}
                </select>
            </div>

            {mensagem && <p className="sol-sucesso">{mensagem}</p>}
            {erro && <p className="recurso-erro-geral">{erro}</p>}

            {carregando ? (
                <p>Carregando...</p>
            ) : solicitacoes.length === 0 ? (
                <p className="recurso-vazio">Nenhuma solicitação encontrada.</p>
            ) : (
                <table className="recurso-tabela">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Recurso</th>
                            <th>Qtd.</th>
                            <th>{aba === "recebidas" ? "Solicitante" : "Instituição dona"}</th>
                            <th>Status</th>
                            <th>Criada em</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        {solicitacoes.map((s) => (
                            <Fragment key={s.id}>
                                <tr>
                                    <td>{s.id}</td>
                                    <td>
                                        {s.recursoNome}
                                        {s.justificativa && <div className="sol-detalhe">“{s.justificativa}”</div>}
                                        {s.motivoRecusa && <div className="sol-detalhe">Motivo da recusa: {s.motivoRecusa}</div>}
                                    </td>
                                    <td>{s.quantidade}</td>
                                    <td>
                                        {aba === "recebidas" ? s.instituicaoSolicitanteNome : s.instituicaoDonaNome}
                                        {aba === "enviadas" && s.solicitanteNome && (
                                            <div className="sol-detalhe">por {s.solicitanteNome}</div>
                                        )}
                                    </td>
                                    <td>
                                        <StatusBadge status={s.status} />
                                    </td>
                                    <td>{formatarDataHora(s.criadaEm)}</td>
                                    <td className="recurso-acoes sol-acoes">{acoesDaLinha(s)}</td>
                                </tr>
                                {historicoAberto === s.id && (
                                    <tr className="sol-historico-linha">
                                        <td colSpan={7}>
                                            <ol className="sol-linha-tempo">
                                                {historico.map((m) => (
                                                    <li key={m.id}>
                                                        <StatusBadge status={m.statusNovo} />
                                                        <span>{formatarDataHora(m.dataHora)}</span>
                                                        <span>
                                                            {m.observacao} — <em>{m.responsavel}</em>
                                                        </span>
                                                    </li>
                                                ))}
                                            </ol>
                                        </td>
                                    </tr>
                                )}
                            </Fragment>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
