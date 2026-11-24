import api from "./api";

export const PERFIL_LABEL = {
  FUNCIONARIO: "Funcionário",
  GESTOR: "Gestor",
  ADMINISTRADOR: "Diretor",
};

const usuarioService = {
  listar: async () => {
    const { data } = await api.get("/usuarios");
    return data;
  },

  listarPendentes: async () => {
    const { data } = await api.get("/usuarios/pendentes");
    return data;
  },

  aprovar: async (id) => {
    const { data } = await api.patch(`/usuarios/${id}/aprovar`);
    return data;
  },

  recusar: async (id) => {
    await api.patch(`/usuarios/${id}/recusar`);
  },

  inativar: async (id) => {
    await api.patch(`/usuarios/${id}/inativar`);
  },

  atualizarMeusDados: async (dados) => {
    const { data } = await api.put("/usuarios/me", dados);
    return data;
  },

  excluirMinhaConta: async (senhaAtual) => {
    await api.post("/usuarios/me/excluir", { senhaAtual });
  },

  excluirDados: async (id) => {
    await api.patch(`/usuarios/${id}/excluir-dados`);
  },

  desativarMinhaConta: async (senhaAtual) => {
    await api.post("/usuarios/me/desativar", { senhaAtual });
  },

  reativar: async (id) => {
    const { data } = await api.patch(`/usuarios/${id}/reativar`);
    return data;
  },
};

export default usuarioService;