import api from "./api";

export const STATUS_SOLICITACAO = {
    PENDENTE: { label: "Pendente", classe: "pendente" },
    APROVADA: { label: "Aprovada", classe: "aprovada" },
    RECUSADA: { label: "Recusada", classe: "recusada" },
    CANCELADA: { label: "Cancelada", classe: "cancelada" },
    RETIRADA: { label: "Retirada", classe: "retirada" },
    CONCLUIDA: { label: "Concluída", classe: "concluida" },
};

export const formatarDataHora = (valor) =>
    valor ? new Date(valor).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" }) : "—";

export const formatarData = (valor) => {
    if (!valor) return "—";
    const [ano, mes, dia] = valor.split("-");
    return `${dia}/${mes}/${ano}`;
};

export const hojeISO = () => {
    const hoje = new Date();
    const mes = String(hoje.getMonth() + 1).padStart(2, "0");
    const dia = String(hoje.getDate()).padStart(2, "0");
    return `${hoje.getFullYear()}-${mes}-${dia}`;
};

const solicitacaoService = {
    criar: async ({ recursoId, quantidade, justificativa, dataPrevistaDevolucao }) => {
        const { data } = await api.post("/solicitacoes", {
            recursoId,
            quantidade,
            justificativa,
            dataPrevistaDevolucao,
        });
        return data;
    },

    listarEnviadas: async (status) => {
        const { data } = await api.get("/solicitacoes/enviadas", { params: status ? { status } : {} });
        return data;
    },

    listarRecebidas: async (status) => {
        const { data } = await api.get("/solicitacoes/recebidas", { params: status ? { status } : {} });
        return data;
    },

    historico: async (id) => {
        const { data } = await api.get(`/solicitacoes/${id}/historico`);
        return data;
    },

    movimentacoes: async () => {
        const { data } = await api.get("/solicitacoes/movimentacoes");
        return data;
    },

    aprovar: async (id) => (await api.patch(`/solicitacoes/${id}/aprovar`)).data,
    recusar: async (id, motivo) => (await api.patch(`/solicitacoes/${id}/recusar`, { motivo })).data,
    cancelar: async (id) => (await api.patch(`/solicitacoes/${id}/cancelar`)).data,
    registrarRetirada: async (id) => (await api.patch(`/solicitacoes/${id}/retirada`)).data,
    registrarDevolucao: async (id) => (await api.patch(`/solicitacoes/${id}/devolucao`)).data,
};

export default solicitacaoService;