import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import authService from "../Services/authService";
import instituicaoService from "../Services/instituicaoServices";
import { mensagemDeErro } from "../Services/mensagemErro";
import "./CadastroPage.css";

const PERFIL_OPTIONS = [
  { value: "FUNCIONARIO", label: "Funcionário" },
  { value: "GESTOR", label: "Gestor" },
  { value: "ADMINISTRADOR", label: "Administrador" },
];

const VAZIO = {
  nome: "",
  email: "",
  senha: "",
  confirmarSenha: "",
  instituicaoId: "",
  perfil: "FUNCIONARIO",
};

export default function CadastroPage() {
  const [form, setForm] = useState(VAZIO);
  const [instituicoes, setInstituicoes] = useState([]);
  const [erros, setErros] = useState({});
  const [cadastrando, setCadastrando] = useState(false);
  const [aceitouTermos, setAceitouTermos] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    instituicaoService
      .listarPublicas()
      .then(setInstituicoes)
      .catch(() => setErros((prev) => ({ ...prev, geral: "Não foi possível carregar as instituições." })));
  }, []);

  const handleChange = (campo) => (e) => {
    setForm((prev) => ({ ...prev, [campo]: e.target.value }));
  };

  const validar = () => {
    const novosErros = {};
    if (!form.nome.trim()) novosErros.nome = "Nome é obrigatório";
    else if (form.nome.trim().length < 3) novosErros.nome = "Informe o nome completo";
    if (!form.email.trim()) novosErros.email = "E-mail é obrigatório";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) novosErros.email = "E-mail inválido";
    if (!form.senha) novosErros.senha = "Senha é obrigatória";
    else if (form.senha.length < 6) novosErros.senha = "Senha deve ter ao menos 6 caracteres";
    if (form.senha !== form.confirmarSenha) novosErros.confirmarSenha = "As senhas não coincidem";
    if (!form.instituicaoId) novosErros.instituicaoId = "Selecione uma instituição";
    if (!aceitouTermos)
      novosErros.termos = "É necessário concordar com os Termos e a Política de Privacidade";
    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validar()) return;

    setCadastrando(true);
    try {
      await authService.cadastrar({
        nome: form.nome,
        email: form.email,
        senha: form.senha,
        instituicaoId: Number(form.instituicaoId),
        perfil: form.perfil,
      });
      navigate("/login");
    } catch (err) {
      setErros({ geral: mensagemDeErro(err, "Erro ao cadastrar usuário.") });
    } finally {
      setCadastrando(false);
    }
  };

  return (
    <div className="cadastro-page">
      <form className="cadastro-form" onSubmit={handleSubmit}>
        <h1>Criar conta</h1>

        {erros.geral && <p className="erro-geral">{erros.geral}</p>}

        <label>
          Nome
          <input type="text" value={form.nome} onChange={handleChange("nome")} />
          {erros.nome && <span className="erro-campo">{erros.nome}</span>}
        </label>

        <label>
          E-mail
          <input type="email" value={form.email} onChange={handleChange("email")} />
          {erros.email && <span className="erro-campo">{erros.email}</span>}
        </label>

        <label>
          Senha
          <input type="password" value={form.senha} onChange={handleChange("senha")} />
          {erros.senha && <span className="erro-campo">{erros.senha}</span>}
        </label>

        <label>
          Confirmar senha
          <input type="password" value={form.confirmarSenha} onChange={handleChange("confirmarSenha")} />
          {erros.confirmarSenha && <span className="erro-campo">{erros.confirmarSenha}</span>}
        </label>

        <label>
          Instituição
          <select value={form.instituicaoId} onChange={handleChange("instituicaoId")}>
            <option value="">Selecione...</option>
            {instituicoes.map((inst) => (
              <option key={inst.id} value={inst.id}>
                {inst.nome}
              </option>
            ))}
          </select>
          {erros.instituicaoId && <span className="erro-campo">{erros.instituicaoId}</span>}
        </label>

        <label>
          Perfil
          <select value={form.perfil} onChange={handleChange("perfil")}>
            {PERFIL_OPTIONS.map((opcao) => (
              <option key={opcao.value} value={opcao.value}>
                {opcao.label}
              </option>
            ))}
          </select>
        </label>

        <label className="cadastro-termos">
          <span>
            <input
              type="checkbox"
              checked={aceitouTermos}
              onChange={(e) => setAceitouTermos(e.target.checked)}
            />{" "}
            Li e concordo com os{" "}
            <Link to="/politica-privacidade" target="_blank" rel="noopener noreferrer">
              Termos de Uso e a Política de Privacidade
            </Link>
          </span>
          {erros.termos && <span className="erro-campo">{erros.termos}</span>}
        </label>

        <button type="submit" disabled={cadastrando}>
          {cadastrando ? "Cadastrando..." : "Cadastrar"}
        </button>

        <p>
          Já tem conta? <Link to="/login">Entrar</Link>
        </p>
      </form>
    </div>
  );
}