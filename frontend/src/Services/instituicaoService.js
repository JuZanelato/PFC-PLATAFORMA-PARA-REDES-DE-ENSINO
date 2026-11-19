import api from "./api";

export const STATUS_OPTIONS = [
  { value: "ATIVA", label: "Ativa" },
  { value: "INATIVA", label: "Inativa" },
];

const instituicaoService = {
  minha: async () => {
    const { data } = await api.get("/Instituicao/minha");
    return data;
  },

  listarRede: async () => {
    const { data } = await api.get("/Instituicao/rede");
    return data;
  },

  detalhes: async (id) => {
    const { data } = await api.get(`/Instituicao/${id}/detalhes`);
    return data;
  },

  listarPublicas: async () => {
    const { data } = await api.get("/Instituicao/publicas");
    return data;
  },

  criar: async (instituicao) => {
    const { data } = await api.post("/Instituicao", instituicao);
    return data;
  },

  atualizar: async (id, instituicao) => {
    await api.put(`/Instituicao/${id}`, instituicao);
  },


  informarDoacao: async (id, doacao) => {
    const { data } = await api.post(`/Instituicao/${id}/doacoes`, doacao);
    return data;
  },

  doacoesRecebidas: async () => {
    const { data } = await api.get("/Instituicao/minha/doacoes");
    return data;
  },
};

export default instituicaoService;