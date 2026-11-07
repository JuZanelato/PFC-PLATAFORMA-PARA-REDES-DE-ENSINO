import { CATEGORIA_OPTIONS, CONDICAO_OPTIONS } from "../Services/recursoService";

const labelDe = (opcoes, valor) => opcoes.find((o) => o.value === valor)?.label || valor;

export default function RecursoTable({
  recursos,
  podeGerenciar,
  onEditar,
  onInativar,
  onAlternarDisponibilidade,
}) {
  if (recursos.length === 0) {
    return <p className="recurso-vazio">Nenhum recurso encontrado.</p>;
  }

  return (
    <table className="recurso-tabela">
      <thead>
        <tr>
          <th>Nome</th>
          <th>Categoria</th>
          <th>Quantidade</th>
          <th>Condição</th>
          <th>Disponibilidade</th>
          <th>Status</th>
          {podeGerenciar && <th>Ações</th>}
        </tr>
      </thead>
      <tbody>
        {recursos.map((recurso) => {
          const inativo = recurso.status === "INATIVO";
          return (
            <tr key={recurso.id} className={inativo ? "recurso-linha-inativa" : ""}>
              <td>{recurso.nome}</td>
              <td>{labelDe(CATEGORIA_OPTIONS, recurso.categoria)}</td>
              <td>{recurso.quantidade}</td>
              <td>{labelDe(CONDICAO_OPTIONS, recurso.condicao)}</td>
              <td>
                {podeGerenciar ? (
                  <button
                    type="button"
                    className={`recurso-disponibilidade ${recurso.disponivel ? "sim" : "nao"}`}
                    onClick={() => onAlternarDisponibilidade(recurso)}
                    disabled={inativo}
                    title={inativo ? "Recurso inativo" : "Clique para alternar"}
                  >
                    {recurso.disponivel ? "Disponível" : "Indisponível"}
                  </button>
                ) : (
                  <span className={`recurso-disponibilidade ${recurso.disponivel ? "sim" : "nao"}`}>
                    {recurso.disponivel ? "Disponível" : "Indisponível"}
                  </span>
                )}
              </td>
              <td>
                <span className={`recurso-status ${inativo ? "inativo" : "ativo"}`}>
                  {inativo ? "Inativo" : "Ativo"}
                </span>
              </td>
              {podeGerenciar && (
                <td className="recurso-acoes">
                  <button type="button" onClick={() => onEditar(recurso)} disabled={inativo}>
                    Editar
                  </button>
                  {!inativo && (
                    <button type="button" className="perigo" onClick={() => onInativar(recurso)}>
                      Inativar
                    </button>
                  )}
                </td>
              )}
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}