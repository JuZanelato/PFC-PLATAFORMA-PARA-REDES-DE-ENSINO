import api from "./api";

export const STATUS_OPTIONS = [
  { value: "ATIVA", label: "Ativa" },
  { value: "INATIVA", label: "Inativa" },
];

const instituicaoService = {
  listar: async (filtro = {}) => {
    const { data } = await api.get("/Instituicao", { params: filtro });
    return data;
  },

  buscarPorId: async (id) => {
    const { data } = await api.get(`/Instituicao/${id}`);
    return data;
  },

  criar: async (instituicao) => {
    const { data } = await api.post("/Instituicao", instituicao);
    return data;
  },

  atualizar: async (id, instituicao) => {
    await api.put(`/Instituicao/${id}`, instituicao);
  },

  excluir: async (id) => {
    await api.delete(`/Instituicao/${id}`);
  },
};

export default instituicaoService;