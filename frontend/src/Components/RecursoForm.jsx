import { useState } from "react";
import { CATEGORIA_OPTIONS, CONDICAO_OPTIONS } from "../Services/recursoService";

const VAZIO = { nome: "", categoria: "", quantidade: "", condicao: "", disponivel: true };

export default function RecursoForm({ recursoSelecionado, onSalvar, onCancelar }) {
  const [form, setForm] = useState(recursoSelecionado || VAZIO);
  const [erros, setErros] = useState({});
  const [salvando, setSalvando] = useState(false);

  const validar = () => {
    const novosErros = {};

    if (!form.nome?.trim()) novosErros.nome = "Nome é obrigatório";
    if (!form.categoria) novosErros.categoria = "Categoria é obrigatória";

    if (form.quantidade === "" || form.quantidade === null || form.quantidade === undefined)
      novosErros.quantidade = "Quantidade é obrigatória";
    else if (Number(form.quantidade) < 0)
      novosErros.quantidade = "Quantidade não pode ser negativa";

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
      await onSalvar({ ...form, quantidade: Number(form.quantidade) });
      setForm(VAZIO);
    } catch (err) {
      const mensagem = err?.response?.data?.message || err?.response?.data || "Erro ao salvar recurso.";
      setErros({ geral: mensagem });
    } finally {
      setSalvando(false);
    }
  };

  return (
    <form className="recurso-form" onSubmit={handleSubmit}>
      <h2>{form.id ? "Editar recurso" : "Novo recurso"}</h2>

      {erros.geral && <p className="erro-geral">{erros.geral}</p>}

      <label>
        Nome
        <input
          type="text"
          value={form.nome}
          onChange={handleChange("nome")}
          maxLength={120}
          placeholder="Ex: Carteira escolar"
        />
        {erros.nome && <span className="erro-campo">{erros.nome}</span>}
      </label>

      <div className="linha-dupla">
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
          {erros.categoria && <span className="erro-campo">{erros.categoria}</span>}
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
          {erros.condicao && <span className="erro-campo">{erros.condicao}</span>}
        </label>
      </div>

      <label>
        Quantidade
        <input
          type="number"
          min="0"
          value={form.quantidade}
          onChange={handleChange("quantidade")}
        />
        {erros.quantidade && <span className="erro-campo">{erros.quantidade}</span>}
      </label>

      <label className="checkbox-disponivel">
        <input
          type="checkbox"
          checked={form.disponivel}
          onChange={(e) => setForm((prev) => ({ ...prev, disponivel: e.target.checked }))}
        />
        <span>Disponível para uso/empréstimo</span>
      </label>

      <div className="form-acoes">
        <button type="submit" disabled={salvando}>
          {salvando ? "Salvando..." : "Salvar"}
        </button>
        {form.id && (
          <button type="button" onClick={onCancelar} disabled={salvando}>
            Cancelar edição
          </button>
        )}
      </div>
    </form>
  );
}