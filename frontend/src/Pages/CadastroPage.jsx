import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import authService from "../Services/authService";
import instituicaoService from "../Services/instituicaoService";
import { mensagemDeErro } from "../Services/mensagemErro";
import { PERFIL_LABEL } from "../Services/usuarioService";
import InstituicaoForm from "../Components/InstituicaoForm";
import "./InstituicoesPages.css";
import "./CadastroPage.css";

const CARGOS_AUTOCADASTRO = [
  { value: "FUNCIONARIO", label: "Funcionário" },
  { value: "GESTOR", label: "Gestor (coordenador)" },
  { value: "ADMINISTRADOR", label: "Diretor" },
];

const CARGOS_DIRETOR = [
  { value: "FUNCIONARIO", label: "Funcionário" },
  { value: "GESTOR", label: "Gestor (coordenador)" },
];

const VAZIO = {
  nome: "",
  email: "",
  senha: "",
  confirmarSenha: "",
  perfil: "FUNCIONARIO",
};

export default function CadastroPage() {
  const navigate = useNavigate();
  const usuarioLogado = authService.usuarioLogado();
  const diretorLogado = usuarioLogado?.perfil === "ADMINISTRADOR";
  const outroPerfilLogado = !!usuarioLogado && !diretorLogado;

  const [form, setForm] = useState(VAZIO);
  const [instituicoes, setInstituicoes] = useState([]);
  const [busca, setBusca] = useState("");
  const [instituicaoEscolhida, setInstituicaoEscolhida] = useState(null);
  const [mostrarFormInstituicao, setMostrarFormInstituicao] = useState(false);
  const [aviso, setAviso] = useState("");
  const [erros, setErros] = useState({});
  const [cadastrando, setCadastrando] = useState(false);
  const [aceitouTermos, setAceitouTermos] = useState(false);
  const [cadastroPendente, setCadastroPendente] = useState(false);

  useEffect(() => {
    if (diretorLogado) return;
    instituicaoService
      .listarPublicas()
      .then(setInstituicoes)
      .catch(() => setErros((prev) => ({ ...prev, geral: "Não foi possível carregar as instituições." })));
  }, [diretorLogado]);

  const termo = busca.trim().toLowerCase();
  const sugestoes =
    !instituicaoEscolhida && termo
      ? instituicoes.filter((inst) => inst.nome.toLowerCase().includes(termo)).slice(0, 6)
      : [];
  const naoEncontrada = !instituicaoEscolhida && termo.length >= 3 && sugestoes.length === 0;
  const cargos = diretorLogado ? CARGOS_DIRETOR : CARGOS_AUTOCADASTRO;

  const handleChange = (campo) => (e) => {
    setForm((prev) => ({ ...prev, [campo]: e.target.value }));
  };

  const handleBusca = (e) => {
    setBusca(e.target.value);
    setInstituicaoEscolhida(null);
    setAviso("");
  };

  const escolherInstituicao = (inst) => {
    setInstituicaoEscolhida(inst);
    setBusca(inst.nome);
    setErros((prev) => ({ ...prev, instituicao: undefined }));
  };

  const salvarInstituicao = async (dados) => {
    const nova = await instituicaoService.criar(dados);
    const resumo = { id: nova.id, nome: nova.nome };
    setInstituicoes((prev) => [...prev, resumo]);
    escolherInstituicao(resumo);
    setMostrarFormInstituicao(false);
    setAviso(`A instituição "${nova.nome}" foi cadastrada. Agora conclua o seu cadastro como Diretor.`);
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
    if (!diretorLogado && !instituicaoEscolhida)
      novosErros.instituicao = "Digite o nome da escola e selecione na lista";
    if (!diretorLogado && !aceitouTermos)
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
        instituicaoId: diretorLogado ? null : instituicaoEscolhida.id,
        perfil: form.perfil,
      });

      if (diretorLogado) {
        navigate("/usuarios");
      } else if (form.perfil === "GESTOR") {
        setCadastroPendente(true);
      } else {
        navigate("/login");
      }
    } catch (err) {
      setErros({ geral: mensagemDeErro(err, "Erro ao cadastrar usuário.") });
    } finally {
      setCadastrando(false);
    }
  };

  if (outroPerfilLogado) {
    return (
      <div className="cadastro-page">
        <div className="cadastro-form">
          <h1>Cadastrar usuário</h1>
          <p className="cad-aviso">
            Você está logado como {PERFIL_LABEL[usuarioLogado.perfil] || usuarioLogado.perfil}. Apenas o diretor
            da escola pode cadastrar novos usuários.
          </p>
          <Link to="/rede">Voltar ao sistema</Link>
        </div>
      </div>
    );
  }

  if (cadastroPendente) {
    return (
      <div className="cadastro-page">
        <div className="cadastro-form">
          <h1>Cadastro enviado</h1>
          <p className="cad-sucesso">
            Sua conta de Gestor foi criada e está aguardando a aprovação do diretor da{" "}
            <strong>{instituicaoEscolhida?.nome}</strong>. Assim que ele aprovar, você poderá entrar no sistema.
          </p>
          <Link to="/login">Ir para o login</Link>
        </div>
      </div>
    );
  }

  if (mostrarFormInstituicao) {
    return (
      <div className="cadastro-page">
        <div className="cad-instituicao">
          <h1>Cadastrar instituição</h1>
          <p className="cad-dica">
            Preencha os dados da sua escola. Depois de salvar, você volta para concluir o seu cadastro como Diretor.
          </p>
          <InstituicaoForm onSalvar={salvarInstituicao} esconderStatus />
          <button type="button" className="cad-voltar" onClick={() => setMostrarFormInstituicao(false)}>
            ← Voltar ao cadastro
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="cadastro-page">
      <form className="cadastro-form" onSubmit={handleSubmit}>
        <h1>{diretorLogado ? "Cadastrar usuário" : "Criar conta"}</h1>

        {erros.geral && <p className="erro-geral">{erros.geral}</p>}
        {aviso && <p className="cad-sucesso">{aviso}</p>}

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
          Cargo na escola
          <select value={form.perfil} onChange={handleChange("perfil")}>
            {cargos.map((opcao) => (
              <option key={opcao.value} value={opcao.value}>
                {opcao.label}
              </option>
            ))}
          </select>
          {!diretorLogado && form.perfil === "GESTOR" && (
            <span className="cad-dica">Contas de Gestor precisam ser aprovadas pelo diretor da escola.</span>
          )}
        </label>

        {diretorLogado ? (
          <p className="cad-info">
            Instituição: <strong>{usuarioLogado.instituicaoNome}</strong>
          </p>
        ) : (
          <div className="cad-campo-instituicao">
            <label>
              Instituição
              <input
                type="text"
                value={busca}
                onChange={handleBusca}
                placeholder="Digite o nome da escola"
                autoComplete="off"
              />
            </label>

            {instituicaoEscolhida && <span className="cad-selecionada">✓ Instituição selecionada</span>}

            {sugestoes.length > 0 && (
              <ul className="cad-sugestoes">
                {sugestoes.map((inst) => (
                  <li key={inst.id}>
                    <button type="button" onClick={() => escolherInstituicao(inst)}>
                      {inst.nome}
                    </button>
                  </li>
                ))}
              </ul>
            )}

            {form.perfil === "ADMINISTRADOR" && !instituicaoEscolhida && (
              <div className="cad-aviso">
                <strong>{naoEncontrada ? "Instituição não cadastrada." : "A sua escola ainda não está no sistema?"}</strong>
                <span>Como diretor, você pode cadastrar a instituição agora e depois concluir a sua conta.</span>
                <button type="button" onClick={() => setMostrarFormInstituicao(true)}>
                  Cadastrar instituição
                </button>
              </div>
            )}

            {form.perfil !== "ADMINISTRADOR" && naoEncontrada && (
              <div className="cad-aviso">
                <strong>Instituição não cadastrada.</strong>
                <span>Peça ao diretor da sua escola para cadastrá-la primeiro.</span>
              </div>
            )}

            {erros.instituicao && <span className="erro-campo">{erros.instituicao}</span>}
          </div>
        )}

        {!diretorLogado && (
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
        )}

        <button type="submit" disabled={cadastrando}>
          {cadastrando ? "Cadastrando..." : "Cadastrar"}
        </button>

        {diretorLogado ? (
          <p>
            <Link to="/usuarios">← Voltar para usuários</Link>
          </p>
        ) : (
          <p>
            Já tem conta? <Link to="/login">Entrar</Link>
          </p>
        )}
      </form>
    </div>
  );
}