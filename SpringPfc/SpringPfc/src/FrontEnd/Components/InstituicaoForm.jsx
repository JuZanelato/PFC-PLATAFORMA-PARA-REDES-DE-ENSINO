import { useEffect, useState } from "react";
import { STATUS_OPTIONS } from "../Service/InstituicaoService";

const VAZIO = { nome: "", cnpj: "", endereco: "", status: "ATIVA" };

export default function InstituicaoForm({ instituicaoSelecionada, onSalvar, onCancelar }) {
  const [form, setForm] = useState(VAZIO);
  const [erros, setErros] = useState({});
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    setForm(instituicaoSelecionada || VAZIO);
    setErros({});
  }, [instituicaoSelecionada]);

  const validar = () => {
    const novosErros = {};
    if (!form.nome?.trim()) novosErros.nome = "Nome é obrigatório";
    if (!form.cnpj?.trim()) novosErros.cnpj = "CNPJ é obrigatório";
    else if (form.cnpj.replace(/\D/g, "").length !== 14)
      novosErros.cnpj = "CNPJ deve ter 14 dígitos";
    if (!form.endereco?.trim()) novosErros.endereco = "Endereço é obrigatório";
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
      await onSalvar(form);
      setForm(VAZIO);
    } catch (err) {
    
      const mensagem =
        err?.response?.data?.message || "Erro ao salvar instituição.";
      setErros({ geral: mensagem });
    } finally {
      setSalvando(false);
    }
  };

  return (
    <form className="instituicao-form" onSubmit={handleSubmit}>
      <h2>{form.id ? "Editar instituição" : "Nova instituição"}</h2>

      {erros.geral && <p className="erro-geral">{erros.geral}</p>}

      <label>
        Nome
        <input
          type="text"
          value={form.nome}
          onChange={handleChange("nome")}
          maxLength={120}
        />
        {erros.nome && <span className="erro-campo">{erros.nome}</span>}
      </label>

      <label>
        CNPJ
        <input
          type="text"
          value={form.cnpj}
          onChange={handleChange("cnpj")}
          maxLength={14}
          placeholder="Somente números"
        />
        {erros.cnpj && <span className="erro-campo">{erros.cnpj}</span>}
      </label>

      <label>
        Endereço
        <input
          type="text"
          value={form.endereco}
          onChange={handleChange("endereco")}
          maxLength={150}
        />
        {erros.endereco && <span className="erro-campo">{erros.endereco}</span>}
      </label>

      <label>
        Status
        <select value={form.status} onChange={handleChange("status")}>
          {STATUS_OPTIONS.map((opcao) => (
            <option key={opcao.value} value={opcao.value}>
              {opcao.label}
            </option>
          ))}
        </select>
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