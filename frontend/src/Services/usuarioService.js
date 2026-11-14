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
};

export default usuarioService;