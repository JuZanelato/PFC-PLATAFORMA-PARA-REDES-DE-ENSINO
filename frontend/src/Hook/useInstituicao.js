import { useCallback, useEffect, useState } from "react";
import instituicaoService from "../Services/instituicaoService";
 
export function useInstituicao() {
  const [instituicoes, setInstituicoes] = useState([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState(null);
 
  const carregar = useCallback(async (filtro = {}) => {
    setCarregando(true);
    setErro(null);
    try {
      const dados = await instituicaoService.listar(filtro);
      setInstituicoes(dados);
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
 
  return { instituicoes, carregando, erro, carregar, salvar, excluir };
}