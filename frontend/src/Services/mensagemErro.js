export function mensagemDeErro(err, padrao) {
  const data = err?.response?.data;
  if (typeof data === "string" && data.trim()) return data;
  if (Array.isArray(data?.errors) && data.errors.length > 0) {
    return data.errors.map((e) => e.defaultMessage || e.message).join(" ");
  }
  if (typeof data?.detail === "string") return data.detail;
  if (typeof data?.message === "string" && data.message) return data.message;
  if (err?.response?.status === 403) return "Você não tem permissão para executar esta ação.";
  if (!err?.response) return "Não foi possível conectar ao servidor. Verifique se o back-end está rodando.";
  return padrao;
}
