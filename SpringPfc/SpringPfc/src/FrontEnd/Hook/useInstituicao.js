import { useCallback, useEffect, useState } from "react";
import InstituicaoService from "../Service/InstituicaoService";

export function useInstituicao() {
  const [instituicao, setInstituicao] = useState([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState(null);

  const carregar = useCallback(async (filtro = {}) => {
    setCarregando(true);
    setErro(null);
    try {
      const dados = await InstituicaoService.listar(filtro);
      setInstituicao(dados);
    } catch (err) {
      setErro("Não foi possível carregar as instituições.");
      console.error(err);
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const salvar = async (instituicao) => {
    if (instituicao.id) {
      await InstituicaoService.atualizar(instituicao.id, instituicao);
    } else {
      await InstituicaoService.criar(instituicao);
    }
    await carregar();
  };

  const excluir = async (id) => {
    await InstituicaoService.excluir(id);
    await carregar();
  };

  return { instituicao, carregando, erro, carregar, salvar, excluir };
}