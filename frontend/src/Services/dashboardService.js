import api from "./api";

const dashboardService = {
  resumo: async () => {
    const { data } = await api.get("/dashboard");
    return data;
  },
};

export default dashboardService;
