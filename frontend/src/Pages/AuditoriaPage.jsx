import { useEffect, useState } from "react";
import auditoriaService from "../Services/auditoriaService";
import "./AuditoriaPage.css";

export default function AuditoriaPage() {
  const [pagina, setPagina] = useState(0);
  const [dados, setDados] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState("");

  useEffect(() => {
    setCarregando(true);
    setErro("");
    auditoriaService
      .listar(pagina, 20)
      .then(setDados)
      .catch(() => setErro("Não foi possível carregar o log de auditoria."))
      .finally(() => setCarregando(false));
  }, [pagina]);

  return (
    <div className="auditoria-page">
      <header>
        <h1>Log de Auditoria</h1>
      </header>

      {erro && <p className="erro-geral">{erro}</p>}

      {carregando ? (
        <p>Carregando...</p>
      ) : (
        <>
          <table className="auditoria-tabela">
            <thead>
              <tr>
                <th>Data/Hora</th>
                <th>Usuário</th>
                <th>Ação</th>
              </tr>
            </thead>
            <tbody>
              {dados.content.length === 0 ? (
                <tr>
                  <td colSpan={3} className="tabela-vazia">
                    Nenhum registro encontrado.
                  </td>
                </tr>
              ) : (
                dados.content.map((log) => (
                  <tr key={log.id}>
                    <td>{new Date(log.dataHora).toLocaleString("pt-BR")}</td>
                    <td>{log.emailUsuario}</td>
                    <td>{log.acao}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          <div className="paginacao">
            <button
              type="button"
              onClick={() => setPagina((p) => Math.max(p - 1, 0))}
              disabled={pagina === 0}
            >
              Anterior
            </button>
            <span>
              Página {pagina + 1} de {Math.max(dados.totalPages, 1)}
            </span>
            <button
              type="button"
              onClick={() => setPagina((p) => p + 1)}
              disabled={pagina + 1 >= dados.totalPages}
            >
              Próxima
            </button>
          </div>
        </>
      )}
    </div>
  );
}