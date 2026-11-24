import { useId, useRef, useState } from "react";
import { normalizar } from "../Services/regiao";
import "./CampoRegiao.css";

export default function CampoRegiao({ valor, onChange, onEscolher, regioes, placeholder = "Região (cidade)..." }) {
  const [aberto, setAberto] = useState(false);
  const [destaque, setDestaque] = useState(-1);
  const listaId = useId();
  const campo = useRef(null);

  const termo = normalizar(valor);
  const opcoes = termo ? regioes.filter((r) => normalizar(r.nome).includes(termo)) : regioes;

  const escolher = (nome) => {
    if (onEscolher) onEscolher(nome);
    else onChange(nome);
    setAberto(false);
    setDestaque(-1);
  };

  const handleTecla = (e) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setAberto(true);
      setDestaque((d) => Math.min(d + 1, opcoes.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setDestaque((d) => Math.max(d - 1, 0));
    } else if (e.key === "Enter" && aberto && destaque >= 0 && opcoes[destaque]) {
      e.preventDefault();
      escolher(opcoes[destaque].nome);
    } else if (e.key === "Escape") {
      setAberto(false);
    }
  };

  return (
    <div className="campo-regiao">
      <input
        ref={campo}
        type="text"
        className="campo-regiao-input"
        role="combobox"
        aria-expanded={aberto}
        aria-controls={listaId}
        aria-autocomplete="list"
        autoComplete="off"
        placeholder={placeholder}
        value={valor}
        onChange={(e) => {
          onChange(e.target.value);
          setAberto(true);
          setDestaque(-1);
        }}
        onClick={() => setAberto(true)}
        onFocus={() => setAberto(true)}
        onBlur={() => setAberto(false)}
        onKeyDown={handleTecla}
      />
      {valor && (
        <button
          type="button"
          className="campo-regiao-limpar"
          aria-label="Limpar região"
          onMouseDown={(e) => e.preventDefault()}
          onClick={() => {
            if (onEscolher) onEscolher("");
            else onChange("");
            campo.current?.focus();
          }}
        >
          ×
        </button>
      )}
      {aberto && (
        <ul id={listaId} role="listbox" className="campo-regiao-lista">
          {opcoes.length === 0 ? (
            <li className="campo-regiao-vazio">Nenhuma região encontrada</li>
          ) : (
            opcoes.map((r, i) => (
              <li
                key={r.nome}
                role="option"
                aria-selected={i === destaque}
                className={i === destaque ? "destaque" : ""}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => escolher(r.nome)}
              >
                <span>{r.nome}</span>
                <small>
                  {r.total} {r.total === 1 ? "escola" : "escolas"}
                </small>
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  );
}
