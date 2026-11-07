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

// Transforma o erro do axios em um texto seguro para exibir na tela
export function mensagemDeErro(err, padrao) {
  const data = err?.response?.data;
  if (typeof data === "string" && data.trim()) return data;
  if (Array.isArray(data?.errors) && data.errors.length > 0) {
    return data.errors.map((e) => e.defaultMessage || e.message).join(" ");
  }
  if (typeof data?.detail === "string") return data.detail;
  if (typeof data?.message === "string" && data.message) return data.message;
  if (err?.response?.status === 403) return "Você não tem permissão para executar esta ação.";
  return padrao;
}

const recursoService = {
  listar: async () => {
    const { data } = await api.get("/recursos");
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