import api from "./api";

export const CATEGORIA_OPTIONS = [
  { value: "MOBILIARIO", label: "Mobiliário" },
  { value: "MATERIAL_DIDATICO", label: "Material didático" },
  { value: "VESTUARIO", label: "Vestuário" },
  { value: "DOACAO", label: "Doação" },
  { value: "OUTRO", label: "Outro" },
];

export const CONDICAO_OPTIONS = [
  { value: "NOVO", label: "Novo" },
  { value: "BOM", label: "Bom" },
  { value: "REGULAR", label: "Regular" },
  { value: "RUIM", label: "Ruim" },
];

export { mensagemDeErro } from "./mensagemErro";

const recursoService = {
  listar: async () => {
    const { data } = await api.get("/recursos");
    return data;
  },


  listarRede: async ({ nome, categoria, instituicaoId } = {}) => {
    const params = {};
    if (nome?.trim()) params.nome = nome.trim();
    if (categoria) params.categoria = categoria;
    if (instituicaoId) params.instituicaoId = instituicaoId;
    const { data } = await api.get("/recursos/rede", { params });
    return data;
  },

  buscarPorId: async (id) => {
    const { data } = await api.get(`/recursos/${id}`);
    return data;
  },

  criar: async (recurso) => {
    const { data } = await api.post("/recursos", recurso);
    return data;
  },

  atualizar: async (id, recurso) => {
    const { data } = await api.put(`/recursos/${id}`, recurso);
    return data;
  },

  inativar: async (id) => {
    await api.patch(`/recursos/${id}/inativar`);
  },

  atualizarDisponibilidade: async (id, disponivel) => {
    const { data } = await api.patch(`/recursos/${id}/disponibilidade`, { disponivel });
    return data;
  },
};

export default recursoService;