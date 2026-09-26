export default function InstituicaoTable({ instituicoes, onEditar, onExcluir }) {
  if (instituicoes.length === 0) {
    return <p className="tabela-vazia">Nenhuma instituição cadastrada ainda.</p>;
  }

  return (
    <table className="instituicao-tabela">
      <thead>
        <tr>
          <th>Nome</th>
          <th>CNPJ</th>
          <th>Endereço</th>
          <th>E-mail</th>
          <th>Status</th>
          <th>Cadastrado por</th>
          <th>Criado em</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        {instituicoes.map((inst) => (
          <tr key={inst.id}>
            <td>{inst.nome}</td>
            <td>{inst.cnpj}</td>
            <td>{inst.endereco}</td>
            <td>{inst.email}</td>
            <td>
              <span className={`status-badge status-${inst.status?.toLowerCase()}`}>
                {inst.status}
              </span>
            </td>
            <td>{inst.cadastradoPor}</td>
            <td>
              {inst.criadoEm && new Date(inst.criadoEm).toLocaleDateString("pt-BR")}
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