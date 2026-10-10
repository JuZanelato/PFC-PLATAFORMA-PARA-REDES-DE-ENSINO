import api from "./api";
 
const authService = {
  login: async (email, senha) => {
    const { data } = await api.post("/auth/login", { email, senha });
    
    localStorage.setItem("token", data.token);
    localStorage.setItem("usuario", JSON.stringify(data.usuario));
    return data.usuario;
  },
 
  cadastrar: async ({ nome, email, senha, instituicaoId, perfil }) => {
    const { data } = await api.post("/auth/cadastro", {
      nome,
      email,
      senha,
      instituicaoId,
      perfil,
    });
    
    return data;
  },
 
  logout: async () => {
    try {
      await api.post("/auth/logout");
    } finally {
      localStorage.removeItem("token");
      localStorage.removeItem("usuario");
    }
  },
 
  usuarioLogado: () => {
    const raw = localStorage.getItem("usuario");
    return raw ? JSON.parse(raw) : null;
  },
 
  estaAutenticado: () => {
    return !!localStorage.getItem("token");
  },
};
 
export default authService;