import { useState } from "react";

export default function GraficoBarras({ titulo, subtitulo, itens, rotulo = (c) => c, unidade = "" }) {
  const [verTabela, setVerTabela] = useState(false);
  const [foco, setFoco] = useState(null);
  const maximo = Math.max(1, ...itens.map((i) => i.valor));
  const total = itens.reduce((soma, i) => soma + i.valor, 0);

  return (
    <section className="dash-card dash-grafico">
      <header className="dash-grafico-topo">
        <div>
          <h2>{titulo}</h2>
          {subtitulo && <p>{subtitulo}</p>}
        </div>
        <button type="button" className="dash-link" onClick={() => setVerTabela((v) => !v)}>
          {verTabela ? "Ver gráfico" : "Ver tabela"}
        </button>
      </header>

      {itens.length === 0 || total === 0 ? (
        <p className="dash-vazio">Ainda não há dados para este gráfico.</p>
      ) : verTabela ? (
        <table className="dash-tabela">
          <thead>
            <tr>
              <th>Item</th>
              <th>Quantidade</th>
            </tr>
          </thead>
          <tbody>
            {itens.map((i) => (
              <tr key={i.chave}>
                <td>{rotulo(i.chave)}</td>
                <td>{i.valor}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <ul className="dash-barras" role="list">
          {itens.map((i) => {
            const pct = total ? Math.round((i.valor * 100) / total) : 0;
            return (
              <li
                key={i.chave}
                className={foco === i.chave ? "foco" : ""}
                onMouseEnter={() => setFoco(i.chave)}
                onMouseLeave={() => setFoco(null)}
              >
                <span className="dash-barra-rotulo">{rotulo(i.chave)}</span>
                <span className="dash-barra-trilho">
                  <span
                    className="dash-barra"
                    style={{ width: i.valor === 0 ? "0" : `max(4px, ${(i.valor / maximo) * 100}%)` }}
                  />
                  {foco === i.chave && (
                    <span className="dash-tooltip" role="tooltip">
                      <strong>{i.valor}</strong> {unidade} · {pct}% do total
                    </span>
                  )}
                </span>
                <span className="dash-barra-valor">{i.valor}</span>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
