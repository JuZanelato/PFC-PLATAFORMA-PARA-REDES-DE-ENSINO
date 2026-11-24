import api from "./api";

const dashboardService = {
  resumo: async (filtros = {}) => {
    const params = {};
    if (filtros.periodo) params.periodo = filtros.periodo;
    if (filtros.escopo && filtros.escopo !== "REDE") params.escopo = filtros.escopo;
    if (filtros.categoria) params.categoria = filtros.categoria;
    if (filtros.escopo === "MINHA" && filtros.visao && filtros.visao !== "TODAS") params.visao = filtros.visao;
    const { data } = await api.get("/dashboard", { params });
    return data;
  },
};

export default dashboardService;
