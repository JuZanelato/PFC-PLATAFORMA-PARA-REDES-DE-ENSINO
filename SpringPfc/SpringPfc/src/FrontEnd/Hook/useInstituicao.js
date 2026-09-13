import { useCallback, useEffect, useState } from "react";
import instituicaoService from "../services/instituicaoService";

export function useInstituicao() {
  const [instituicaos, setInstituicao] = useState([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState(null);

  const carregar = useCallback(async (filtro = {}) => {
    setCarregando(true);
    setErro(null);
    try {
      const dados = await instituicaoService.listar(filtro);
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
      await instituicaoService.atualizar(instituicao.id, instituicao);
    } else {
      await instituicaoService.criar(instituicao);
    }
    await carregar();
  };

  const excluir = async (id) => {
    await instituicaoService.excluir(id);
    await carregar();
  };

  return { instituicao, carregando, erro, carregar, salvar, excluir };
}