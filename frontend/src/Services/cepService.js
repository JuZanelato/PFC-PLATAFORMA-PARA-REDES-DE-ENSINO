import axios from "axios";

const cepService = {
  buscar: async (cep) => {
    const cepLimpo = cep.replace(/\D/g, "");

    if (cepLimpo.length !== 8) {
      throw new Error("CEP deve ter 8 dígitos");
    }

    const { data } = await axios.get(`https://viacep.com.br/ws/${cepLimpo}/json/`);

    if (data.erro) {
      throw new Error("CEP não encontrado");
    }

    return {
      logradouro: data.logradouro,
      bairro: data.bairro,
      cidade: data.localidade,
      uf: data.uf,
    };
  },
};

export default cepService;