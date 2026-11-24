// CompartilhaEdu - cadastro automatico de dados de teste (todos ficticios)
// Uso:  node popular-dados.mjs --teste          (5 escolas, para conferir)
//       node popular-dados.mjs                  (500 escolas)
//       node popular-dados.mjs --instituicoes 200 --solicitacoes 150
// A senha das contas de teste NAO fica no arquivo: o script pergunta ao iniciar
// (ou le a variavel de ambiente SENHA_TESTE).

import fs from "node:fs";
import readline from "node:readline/promises";

const API = (process.env.API_URL || "https://compartilhaedu-api.onrender.com/api").replace(/\/$/, "");
const DOMINIO = "compartilhaedu.test"; // dominio reservado para testes: nenhum e-mail real
const ARQ_PROGRESSO = "progresso-cadastro.json";
const ARQ_CONTAS = "contas-de-teste.txt";
const PARALELO = 4;

// ---------- argumentos ----------
const args = process.argv.slice(2);
const arg = (nome, padrao) => {
  const i = args.indexOf(nome);
  return i >= 0 ? Number(args[i + 1]) : padrao;
};
const MODO_TESTE = args.includes("--teste");
const N_INST = MODO_TESTE ? 5 : arg("--instituicoes", Number(process.env.QTD_ESCOLAS || 500));
const N_SOL = MODO_TESTE ? 5 : arg("--solicitacoes", Number(process.env.QTD_SOLICITACOES || 300));
const RECURSOS_POR_ESCOLA = arg("--recursos", 4);
const FUNCIONARIOS_POR_ESCOLA = arg("--funcionarios", 2);

// ---------- dados ficticios ----------
const NOMES_F = ["Ana", "Beatriz", "Camila", "Daniela", "Fernanda", "Helena", "Juliana", "Mariana", "Natália", "Paula", "Renata", "Tatiane", "Yasmin"];
const NOMES_M = ["Bruno", "Carlos", "Eduardo", "Gabriel", "Igor", "Lucas", "Mateus", "Otávio", "Rafael", "Sérgio", "Vinícius"];
const NOMES = [...NOMES_F, ...NOMES_M];
const SOBRENOMES = ["Almeida", "Barbosa", "Cardoso", "Costa", "Dias", "Ferreira", "Gomes", "Lima", "Martins", "Melo", "Moreira", "Nunes", "Oliveira", "Pereira", "Ramos", "Rocha", "Santos", "Silva", "Souza", "Teixeira"];
const TIPOS = ["Escola Estadual", "Escola Municipal", "EMEF", "Colégio Estadual"];
const RUAS = ["Rua das Acácias", "Rua dos Ipês", "Avenida Central", "Rua Sete de Setembro", "Rua da Paz", "Avenida das Flores", "Rua São João", "Rua Primavera", "Travessa do Sol", "Rua Boa Esperança"];
const CIDADES = ["Mogi das Cruzes", "Suzano", "Itaquaquecetuba", "Poá", "Ferraz de Vasconcelos", "Arujá", "Guararema", "Biritiba-Mirim"];
const RECURSOS = [
  ["Kit de geometria", "MATERIAL_DIDATICO"], ["Atlas geográfico", "MATERIAL_DIDATICO"], ["Microscópio escolar", "MATERIAL_DIDATICO"],
  ["Coleção de livros paradidáticos", "MATERIAL_DIDATICO"], ["Globo terrestre", "MATERIAL_DIDATICO"], ["Jogo de tabuleiro educativo", "MATERIAL_DIDATICO"],
  ["Carteira escolar", "MOBILIARIO"], ["Cadeira escolar", "MOBILIARIO"], ["Armário de aço", "MOBILIARIO"], ["Estante de livros", "MOBILIARIO"],
  ["Camiseta do uniforme", "VESTUARIO"], ["Agasalho do uniforme", "VESTUARIO"], ["Projetor multimídia", "OUTRO"], ["Caixa de som", "OUTRO"],
  ["Kit de material escolar", "DOACAO"], ["Cesta de livros para doação", "DOACAO"],
];
const CONDICOES = ["NOVO", "BOM", "BOM", "REGULAR"];

const gerador = (semente) => () => ((semente = (semente * 1103515245 + 12345) % 2147483648) / 2147483648);
let rnd = gerador(20261123);
const pickR = (r, lista) => lista[Math.floor(r() * lista.length)];
const pick = (lista) => pickR(rnd, lista);
const pessoaR = (r) => `${pickR(r, NOMES)} ${pickR(r, SOBRENOMES)} ${pickR(r, SOBRENOMES)}`;
const pessoa = () => pessoaR(rnd);
const patronoR = (r) => (r() < 0.5
  ? `${pickR(r, ["Profa.", "Dra.", "Dona", ""])} ${pickR(r, NOMES_F)}`
  : `${pickR(r, ["Prof.", "Dr.", ""])} ${pickR(r, NOMES_M)}`) + ` ${pickR(r, SOBRENOMES)} ${pickR(r, SOBRENOMES)}`;
const num = (i) => String(i).padStart(4, "0");
const email = (papel, i) => `${papel}.${num(i)}@${DOMINIO}`;

// ---------- HTTP ----------
const esperar = (ms) => new Promise((r) => setTimeout(r, ms));
async function chamar(metodo, caminho, corpo, token, tentativas = 4) {
  for (let t = 1; ; t++) {
    try {
      const resp = await fetch(API + caminho, {
        method: metodo,
        headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        body: corpo === undefined ? undefined : JSON.stringify(corpo),
      });
      const texto = await resp.text();
      let dados = null;
      try { dados = texto ? JSON.parse(texto) : null; } catch { dados = texto; }
      if (resp.status >= 500 && t < tentativas) { await esperar(2000 * t); continue; }
      if (!resp.ok) {
        const msg = typeof dados === "string" ? dados : JSON.stringify(dados);
        const erro = new Error(`${metodo} ${caminho} -> ${resp.status}: ${msg}`);
        erro.status = resp.status; erro.msg = msg;
        throw erro;
      }
      return dados;
    } catch (e) {
      if (e.status) throw e;
      if (t >= tentativas) throw e;
      await esperar(3000 * t);
    }
  }
}

let SENHA = "";
const tokens = new Map(); // email -> { token, em }
async function token(emailUsuario) {
  const c = tokens.get(emailUsuario);
  if (c && Date.now() - c.em < 45 * 60 * 1000) return c.token;
  const r = await chamar("POST", "/auth/login", { email: emailUsuario, senha: SENHA });
  tokens.set(emailUsuario, { token: r.token, em: Date.now() });
  return r.token;
}

// ---------- progresso (permite continuar se parar no meio) ----------
let prog = { instituicoes: {}, solicitacoes: 0 };
if (fs.existsSync(ARQ_PROGRESSO)) prog = JSON.parse(fs.readFileSync(ARQ_PROGRESSO, "utf8"));
const salvar = () => fs.writeFileSync(ARQ_PROGRESSO, JSON.stringify(prog, null, 1));

// ---------- etapa 1: escolas, usuarios e recursos ----------
let publicas = null;
async function criarEscola(i) {
  const p = (prog.instituicoes[i] ||= { recursos: [] });
  const r = gerador(1000 + i * 7919);
  const nome = `${pickR(r, TIPOS)} ${patronoR(r)}`.replace(/\s+/g, " ").trim();
  const temPix = r() < 0.5;
  let retomada = false;

  if (!p.id) {
    try {
      const inst = await chamar("POST", "/Instituicao", {
        nome,
        cnpj: `99${String(i).padStart(8, "0")}0001`,
        cep: `08${String(700 + (i % 99)).padStart(3, "0")}-${String(i % 1000).padStart(3, "0")}`,
        endereco: `${pickR(r, RUAS)} - ${pickR(r, CIDADES)}/SP`,
        numero: String(10 + Math.floor(r() * 990)),
        email: email("contato", i),
        chavePix: temPix ? email("pix", i) : null,
      });
      p.id = inst.id;
    } catch (e) {
      if (!/CNPJ/i.test(e.msg || "")) throw e;
      publicas ||= await chamar("GET", "/Instituicao/publicas");
      const achada = publicas.find((x) => x.nome === nome);
      if (!achada) throw e;
      p.id = achada.id; retomada = true;
    }
    p.nome = nome; salvar();
  }

  const cadastro = async (papel, perfil, tok) => {
    if (p[papel]) return;
    try {
      await chamar("POST", "/auth/cadastro", { nome: pessoaR(r), email: email(papel, i), senha: SENHA, instituicaoId: p.id, perfil }, tok);
    } catch (e) {
      if (!/já cadastrado/i.test(e.msg || "")) throw e;
    }
    p[papel] = true; salvar();
  };

  await cadastro("diretor", "ADMINISTRADOR");
  const tokDiretor = await token(email("diretor", i));
  await cadastro("gestor", "GESTOR", tokDiretor);
  for (let f = 1; f <= FUNCIONARIOS_POR_ESCOLA; f++) await cadastro(`funcionario${f}`, "FUNCIONARIO", tokDiretor);

  if (retomada && p.recursos.length === 0) {
    const existentes = await chamar("GET", "/recursos", undefined, tokDiretor);
    p.recursos = (existentes || []).map((x) => x.id);
  }
  while (p.recursos.length < RECURSOS_POR_ESCOLA) {
    const [nomeRecurso, categoria] = pick(RECURSOS);
    const rec = await chamar("POST", "/recursos", {
      nome: nomeRecurso, categoria, quantidade: 2 + Math.floor(rnd() * 19), condicao: pick(CONDICOES), disponivel: true,
    }, tokDiretor);
    p.recursos.push(rec.id); salvar();
  }
  p.ok = true; salvar();
}

// ---------- etapa 2: emprestimos entre escolas (para o dashboard ter dados) ----------
async function criarSolicitacao(k) {
  const prontas = Object.entries(prog.instituicoes).filter(([, v]) => v.ok);
  const [iA] = pick(prontas);                 // escola que pede
  let [iB, dona] = pick(prontas);             // escola dona do recurso
  if (iB === iA) [iB, dona] = prontas[(prontas.findIndex(([x]) => x === iA) + 1) % prontas.length];
  const recursoId = pick(dona.recursos);
  const dias = 7 + Math.floor(rnd() * 40);
  const data = new Date(Date.now() + dias * 864e5).toISOString().slice(0, 10);

  const tokF = await token(email("funcionario1", Number(iA)));
  let sol;
  try {
    sol = await chamar("POST", "/solicitacoes", { recursoId, quantidade: 1, justificativa: "Uso em atividade pedagógica da turma.", dataPrevistaDevolucao: data }, tokF);
  } catch (e) {
    if (e.status === 400) return false; // ex.: recurso sem estoque ou pedido repetido; tenta outro
    throw e;
  }
  const destino = rnd();
  if (destino < 0.3) return true;                                   // fica PENDENTE
  const tokD = await token(email("diretor", Number(iB)));
  if (destino < 0.38) { await chamar("PATCH", `/solicitacoes/${sol.id}/recusar`, { motivo: "Recurso reservado para uso interno." }, tokD); return true; }
  await chamar("PATCH", `/solicitacoes/${sol.id}/aprovar`, undefined, tokD);
  if (destino < 0.55) return true;                                  // fica APROVADA
  const tokG = await token(email("gestor", Number(iB)));
  await chamar("PATCH", `/solicitacoes/${sol.id}/retirada`, undefined, tokG);
  if (destino < 0.75) return true;                                  // fica RETIRADA
  await chamar("PATCH", `/solicitacoes/${sol.id}/devolucao`, undefined, tokG);
  return true;                                                      // CONCLUIDA
}

// ---------- execucao ----------
async function emParalelo(lista, fn, rotulo) {
  let feitos = 0, erros = 0, idx = 0;
  const trabalhador = async () => {
    while (idx < lista.length) {
      const item = lista[idx++];
      try { await fn(item); feitos++; } catch (e) { erros++; console.error(`\n  erro em ${rotulo} ${item}: ${e.message}`); }
      if ((feitos + erros) % 25 === 0 || feitos + erros === lista.length) console.log(`  ${rotulo}: ${feitos}/${lista.length} prontos, ${erros} com erro`);
    }
  };
  await Promise.all(Array.from({ length: PARALELO }, trabalhador));
  console.log();
  return erros;
}

async function main() {
  console.log(`API: ${API}`);
  console.log(`Plano: ${N_INST} escolas, ${2 + FUNCIONARIOS_POR_ESCOLA} usuários e ${RECURSOS_POR_ESCOLA} recursos por escola, ${N_SOL} solicitações.`);
  SENHA = process.env.SENHA_TESTE || "";
  if (!SENHA) {
    const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
    SENHA = await rl.question("Senha para TODAS as contas de teste (mín. 6 caracteres, não use uma senha sua): ");
    rl.close();
  }
  if (SENHA.length < 6) { console.log("Senha muito curta."); process.exit(1); }

  process.stdout.write("Acordando o servidor do Render (pode levar 1 minuto)... ");
  await chamar("GET", "/Instituicao/publicas", undefined, undefined, 10);
  console.log("ok");

  const pendentes = [];
  for (let i = 1; i <= N_INST; i++) if (!prog.instituicoes[i]?.ok) pendentes.push(i);
  console.log(`Etapa 1 - escolas, usuários e recursos (${pendentes.length} a fazer)`);
  await emParalelo(pendentes, criarEscola, "escolas");

  const faltam = Math.max(0, N_SOL - prog.solicitacoes);
  console.log(`Etapa 2 - empréstimos entre escolas (${faltam} a fazer)`);
  let tentativas = 0;
  const lista = Array.from({ length: faltam }, (_, k) => k);
  await emParalelo(lista, async (k) => {
    while (!(await criarSolicitacao(k))) { if (++tentativas > faltam * 3) return; }
    prog.solicitacoes++; salvar();
  }, "solicitações");

  const linhas = [
    "Contas de teste do CompartilhaEdu (dados fictícios). A senha é a que você digitou ao rodar o script.",
    "",
    ...[1, 2, 3].filter((i) => prog.instituicoes[i]).flatMap((i) => [
      `${prog.instituicoes[i].nome}`,
      `  Diretor:     ${email("diretor", i)}`,
      `  Gestor:      ${email("gestor", i)}`,
      `  Funcionário: ${email("funcionario1", i)}`,
      "",
    ]),
    `Padrão para as demais escolas: diretor.NNNN@${DOMINIO}, gestor.NNNN@${DOMINIO}, funcionario1.NNNN@${DOMINIO} (NNNN de 0001 a ${num(N_INST)}).`,
  ];
  fs.writeFileSync(ARQ_CONTAS, linhas.join("\n"));
  console.log(`\nConcluído. Lista de contas para a demonstração em ${ARQ_CONTAS}.`);
}

main().catch((e) => { console.error("\nParou com erro:", e.message, "\nRode o mesmo comando de novo: o script continua de onde parou."); process.exit(1); });
