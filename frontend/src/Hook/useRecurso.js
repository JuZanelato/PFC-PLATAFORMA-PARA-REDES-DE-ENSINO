import { useCallback, useEffect, useState } from "react";
import recursoService from "../Services/recursoService";

export function useRecurso() {
  const [recursos, setRecursos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const dados = await recursoService.listar();
      setRecursos(dados);
    } catch (err) {
      setErro("Não foi possível carregar os recursos.");
      console.error(err);
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    let ativo = true;

    recursoService
      .listar()
      .then((dados) => {
        if (ativo) setRecursos(dados);
      })
      .catch((err) => {
        if (ativo) setErro("Não foi possível carregar os recursos.");
        console.error(err);
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, []);

  const salvar = async (recurso) => {
    if (recurso.id) await recursoService.atualizar(recurso.id, recurso);
    else await recursoService.criar(recurso);
    await carregar();
  };

  const inativar = async (id) => {
    await recursoService.inativar(id);
    await carregar();
  };

  const alternarDisponibilidade = async (recurso) => {
    await recursoService.atualizarDisponibilidade(recurso.id, !recurso.disponivel);
    await carregar();
  };

  return { recursos, carregando, erro, carregar, salvar, inativar, alternarDisponibilidade };
}