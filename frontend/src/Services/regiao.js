export const normalizar = (texto) =>
  (texto || "")
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase()
    .trim();

export function regiaoDe(endereco) {
  const texto = (endereco || "").trim();
  const encontrado = texto.match(/(?:,|\s-\s)\s*([^,]+?)\s*\/\s*([A-Za-z]{2})\s*$/);
  if (encontrado) return `${encontrado[1].trim()}/${encontrado[2].toUpperCase()}`;
  const semRua = texto.match(/^([^,\d]+?)\s*\/\s*([A-Za-z]{2})\s*$/);
  return semRua ? `${semRua[1].trim()}/${semRua[2].toUpperCase()}` : "";
}

export function regioesDe(instituicoes) {
  const contagem = new Map();
  instituicoes.forEach((inst) => {
    const regiao = regiaoDe(inst.endereco);
    if (regiao) contagem.set(regiao, (contagem.get(regiao) || 0) + 1);
  });
  return [...contagem.entries()]
    .map(([nome, total]) => ({ nome, total }))
    .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"));
}

export const combinaRegiao = (endereco, termo) => !normalizar(termo) || normalizar(regiaoDe(endereco)).includes(normalizar(termo));
