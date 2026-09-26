import { useEffect, useState } from "react";
import { STATUS_OPTIONS } from "../Services/instituicaoService";
import cepService from "../Services/cepService";

const VAZIO = {
  nome: "",
  cnpj: "",
  cep: "",
  numero: "",
  endereco: "",
  email: "",
  status: "ATIVA",
};

export default function InstituicaoForm({ instituicaoSelecionada, onSalvar, onCancelar }) {
  const [form, setForm] = useState(VAZIO);
  const [erros, setErros] = useState({});
  const [salvando, setSalvando] = useState(false);
  const [buscandoCep, setBuscandoCep] = useState(false);
  const [enderecoManual, setEnderecoManual] = useState(false);

  useEffect(() => {
    setForm(instituicaoSelecionada || VAZIO);
    setErros({});
    setEnderecoManual(false);
  }, [instituicaoSelecionada]);

  const handleCepBlur = async () => {
    const cepLimpo = form.cep.replace(/\D/g, "");
    if (cepLimpo.length !== 8) return;

    setBuscandoCep(true);
    setErros((prev) => ({ ...prev, cep: undefined }));

    try {
      const endereco = await cepService.buscar(cepLimpo);
      setForm((prev) => ({
        ...prev,
        endereco: montarEndereco(endereco, prev.numero),
      }));
    } catch (err) {
      setErros((prev) => ({ ...prev, cep: err.message }));
    } finally {
      setBuscandoCep(false);
    }
  };

  const montarEndereco = ({ logradouro, bairro, cidade, uf }, numero) => {
    const numeroTexto = numero?.trim() ? `, ${numero}` : "";
    return `${logradouro}${numeroTexto} - ${bairro}, ${cidade}/${uf}`;
  };

  const alternarModoEndereco = () => {
    setEnderecoManual((modoAtual) => {
      const novoModo = !modoAtual;

      if (!novoModo) {
        setForm((prev) => ({ ...prev, endereco: "" }));
      }

      setErros((prev) => ({ ...prev, cep: undefined, endereco: undefined }));

      return novoModo;
    });
  };

  const validar = () => {
    const novosErros = {};

    if (!form.nome?.trim()) novosErros.nome = "Nome é obrigatório";

    if (!form.cnpj?.trim()) novosErros.cnpj = "CNPJ é obrigatório";
    else if (form.cnpj.replace(/\D/g, "").length !== 14)
      novosErros.cnpj = "CNPJ deve ter 14 dígitos";

    if (!enderecoManual) {
      if (!form.cep?.trim()) novosErros.cep = "CEP é obrigatório";
      else if (form.cep.replace(/\D/g, "").length !== 8)
        novosErros.cep = "CEP deve ter 8 dígitos";
    }

    if (!form.numero?.trim()) novosErros.numero = "Número é obrigatório";

    if (!form.endereco?.trim())
      novosErros.endereco = enderecoManual
        ? "Endereço é obrigatório"
        : "Endereço não encontrado — confira o CEP digitado";

    if (!form.email?.trim()) novosErros.email = "E-mail é obrigatório";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
      novosErros.email = "E-mail inválido";

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
      setEnderecoManual(false);
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
          placeholder="Escola Municipal João de Barro"
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
          placeholder="Somente números — ex: 12345678000199"
        />
        {erros.cnpj && <span className="erro-campo">{erros.cnpj}</span>}
      </label>

      {!enderecoManual && (
        <div className="linha-dupla">
          <label>
            CEP
            <input
              type="text"
              value={form.cep}
              onChange={handleChange("cep")}
              onBlur={handleCepBlur}
              maxLength={9}
              placeholder="00000-000"
            />
            {buscandoCep && <span className="cep-buscando">Buscando endereço...</span>}
            {erros.cep && <span className="erro-campo">{erros.cep}</span>}
          </label>

          <label>
            Número
            <input
              type="text"
              value={form.numero}
              onChange={(e) => {
                const numero = e.target.value;
                setForm((prev) => ({
                  ...prev,
                  numero,
                  endereco: prev.endereco
                    ? prev.endereco.replace(/^([^,]+)(,.*)?( -.*)$/, (_, rua, _num, resto) => `${rua}, ${numero}${resto}`)
                    : prev.endereco,
                }));
              }}
              maxLength={10}
              placeholder="120"
            />
            {erros.numero && <span className="erro-campo">{erros.numero}</span>}
          </label>
        </div>
      )}

      {enderecoManual && (
        <label>
          Número
          <input
            type="text"
            value={form.numero}
            onChange={handleChange("numero")}
            maxLength={10}
            placeholder="120"
          />
          {erros.numero && <span className="erro-campo">{erros.numero}</span>}
        </label>
      )}

      <label>
        Endereço
        <input
          type="text"
          value={form.endereco}
          onChange={enderecoManual ? handleChange("endereco") : undefined}
          readOnly={!enderecoManual}
          maxLength={150}
          placeholder={
            enderecoManual
              ? "Rua das Acácias, 340 - Centro, São Paulo/SP"
              : "Preenchido automaticamente pelo CEP"
          }
        />
        {erros.endereco && <span className="erro-campo">{erros.endereco}</span>}
      </label>

      <button
        type="button"
        className="link-alternar-endereco"
        onClick={alternarModoEndereco}
      >
        {enderecoManual
          ? "Voltar a buscar por CEP"
          : "Não sei o CEP — digitar endereço manualmente"}
      </button>

      <label>
        E-mail
        <input
          type="email"
          value={form.email}
          onChange={handleChange("email")}
          maxLength={150}
          placeholder="contato@suainstituicao.edu.br"
        />
        {erros.email && <span className="erro-campo">{erros.email}</span>}
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