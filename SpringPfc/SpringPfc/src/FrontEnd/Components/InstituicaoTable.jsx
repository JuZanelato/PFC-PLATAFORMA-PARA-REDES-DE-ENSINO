export default function InstituicaoTable({ instituicao, onEditar, onExcluir }) {
  if (instituicao.length === 0) {
    return <p className="tabela-vazia">Nenhuma instituição cadastrada ainda.</p>;
  }

  return (
    <table className="instituicao-tabela">
      <thead>
        <tr>
          <th>Nome</th>
          <th>CNPJ</th>
          <th>Endereço</th>
          <th>Status</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        {instituicao.map((inst) => (
          <tr key={inst.id}>
            <td>{inst.nome}</td>
            <td>{inst.cnpj}</td>
            <td>{inst.endereco}</td>
            <td>
              <span className={`status-badge status-${inst.status?.toLowerCase()}`}>
                {inst.status}
              </span>
            </td>
            <td className="acoes">
              <button onClick={() => onEditar(inst)}>Editar</button>
              <button className="btn-perigo" onClick={() => onExcluir(inst)}>
                Excluir
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}