import api from "./api";

const auditoriaService = {
  listar: async (pagina = 0, tamanho = 20) => {
    const { data } = await api.get("/auditoria", {
      params: { pagina, tamanho },
    });
    return data; // { content, totalPages, totalElements, number, size, ... }
  },
};

export default auditoriaService;