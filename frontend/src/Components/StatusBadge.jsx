import { STATUS_SOLICITACAO } from "../Services/solicitacaoService";

export default function StatusBadge({ status }) {
    if (!status) return <span className="sol-status">—</span>;
    const info = STATUS_SOLICITACAO[status] || { label: status, classe: "" };
    return <span className={`sol-status ${info.classe}`}>{info.label}</span>;
}
