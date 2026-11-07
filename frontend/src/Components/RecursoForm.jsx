import { useState } from "react";
import {
  CATEGORIA_OPTIONS,
  CONDICAO_OPTIONS,
  mensagemDeErro,
} from "../Services/recursoService";

const VAZIO = { nome: "", categoria: "", quantidade: "", condicao: "", disponivel: true };

export default function RecursoForm({ recursoSelecionado, onSalvar, onCancelar }) {
  const [form, setForm] = useState(recursoSelecionado || VAZIO);
  const [erros, setErros] = useState({});
  const [salvando, setSalvando] = useState(false);

  const validar = () => {
    const novosErros = {};

    if (!form.nome?.trim()) novosErros.nome = "Nome é obrigatório";
    if (!form.categoria) novosErros.categoria = "Categoria é obrigatória";

    if (form.quantidade === "" || form.quantidade === null || form.quantidade === undefined) {
      novosErros.quantidade = "Quantidade é obrigatória";
    } else if (!Number.isInteger(Number(form.quantidade)) || Number(form.quantidade) < 0) {
      novosErros.quantidade = "Informe um número inteiro maior ou igual a zero";
    }

    if (!form.condicao) novosErros.condicao = "Condição é obrigatória";

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const handleChange = (campo) => (e) => {
    setForm((prev) => ({ ...prev, [campo]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validar()) return;

    setSalvando(true);
    try {
      await onSalvar({ ...form, nome: form.nome.trim(), quantidade: Number(form.quantidade) });
    } catch (err) {
      setErros({ geral: mensagemDeErro(err, "Erro ao salvar recurso.") });
    } finally {
      setSalvando(false);
    }
  };

  return (
    <form className="recurso-form" onSubmit={handleSubmit}>
      <h2>{form.id ? "Editar recurso" : "Novo recurso"}</h2>

      {erros.geral && <p className="recurso-erro-geral">{erros.geral}</p>}

      <label>
        Nome
        <input
          type="text"
          value={form.nome}
          onChange={handleChange("nome")}
          maxLength={120}
          placeholder="Ex: Carteira escolar"
        />
        {erros.nome && <span className="recurso-erro-campo">{erros.nome}</span>}
      </label>

      <div className="recurso-linha-dupla">
        <label>
          Categoria
          <select value={form.categoria} onChange={handleChange("categoria")}>
            <option value="">Selecione...</option>
            {CATEGORIA_OPTIONS.map((opcao) => (
              <option key={opcao.value} value={opcao.value}>
                {opcao.label}
              </option>
            ))}
          </select>
          {erros.categoria && <span className="recurso-erro-campo">{erros.categoria}</span>}
        </label>

        <label>
          Condição
          <select value={form.condicao} onChange={handleChange("condicao")}>
            <option value="">Selecione...</option>
            {CONDICAO_OPTIONS.map((opcao) => (
              <option key={opcao.value} value={opcao.value}>
                {opcao.label}
              </option>
            ))}
          </select>
          {erros.condicao && <span className="recurso-erro-campo">{erros.condicao}</span>}
        </label>
      </div>

      <label>
        Quantidade
        <input
          type="number"
          min="0"
          step="1"
          value={form.quantidade}
          onChange={handleChange("quantidade")}
        />
        {erros.quantidade && <span className="recurso-erro-campo">{erros.quantidade}</span>}
      </label>

      <label className="recurso-checkbox">
        <input
          type="checkbox"
          checked={form.disponivel}
          onChange={(e) => setForm((prev) => ({ ...prev, disponivel: e.target.checked }))}
        />
        <span>Disponível para uso/empréstimo</span>
      </label>

      <div className="recurso-form-acoes">
        <button type="submit" disabled={salvando}>
          {salvando ? "Salvando..." : "Salvar"}
        </button>
        <button type="button" className="secundario" onClick={onCancelar} disabled={salvando}>
          Cancelar
        </button>
      </div>
    </form>
  );
}