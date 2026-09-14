import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api/Instituicao",
});


export const STATUS_OPTIONS = [
{ value: "ATIVA", label: "Ativa" },
{ value: "INATIVA", label: "Inativa" },
];

const InstituicaoService = {
  listar: async (filtro = {}) => {
    const { data } = await api.get("", { params: filtro });
    return data;
  },

  buscarPorId: async (id) => {
    const { data } = await api.get(`/${id}`);
    return data;
  },

  criar: async (instituicao) => {
  console.log("Enviando para o backend:", JSON.stringify(instituicao));
  const { data } = await api.post("", instituicao);
  return data;
},


  atualizar: async (id, instituicao) => {
    await api.put(`/${id}`, instituicao);
  },


  excluir: async (id) => {
    await api.delete(`/${id}`);
  },
};

export default InstituicaoService;