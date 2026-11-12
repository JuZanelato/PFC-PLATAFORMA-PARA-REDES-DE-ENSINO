import { CATEGORIA_OPTIONS, CONDICAO_OPTIONS } from "../Services/recursoService";

const labelDe = (opcoes, valor) => opcoes.find((o) => o.value === valor)?.label || valor;

export default function RecursoTable({ recursos, onEditar, onInativar, onAlternarDisponibilidade }) {
  if (recursos.length === 0) {
    return <p className="tabela-vazia">Nenhum recurso cadastrado ainda.</p>;
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
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        {recursos.map((recurso) => (
          <tr key={recurso.id}>
            <td>{recurso.nome}</td>
            <td>{labelDe(CATEGORIA_OPTIONS, recurso.categoria)}</td>
            <td>{recurso.quantidade}</td>
            <td>{labelDe(CONDICAO_OPTIONS, recurso.condicao)}</td>
            <td>
              <button
                type="button"
                className={`badge-disponibilidade ${recurso.disponivel ? "disponivel" : "indisponivel"}`}
                onClick={() => onAlternarDisponibilidade(recurso)}
                disabled={recurso.status === "INATIVO"}
              >
                {recurso.disponivel ? "Disponível" : "Indisponível"}
              </button>
            </td>
            <td>
              <span className={`status-badge status-${recurso.status?.toLowerCase()}`}>
                {recurso.status}
              </span>
            </td>
            <td className="acoes">
              <button onClick={() => onEditar(recurso)} disabled={recurso.status === "INATIVO"}>
                Editar
              </button>
              {recurso.status !== "INATIVO" && (
                <button className="btn-perigo" onClick={() => onInativar(recurso)}>
                  Inativar
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}