const CAMINHOS = {
  painel: (
    <>
      <rect x="3" y="3" width="7.5" height="9" rx="1.5" />
      <rect x="13.5" y="3" width="7.5" height="5" rx="1.5" />
      <rect x="13.5" y="11" width="7.5" height="10" rx="1.5" />
      <rect x="3" y="15" width="7.5" height="6" rx="1.5" />
    </>
  ),
  busca: (
    <>
      <circle cx="10.5" cy="10.5" r="6.5" />
      <path d="M15.5 15.5 21 21" />
    </>
  ),
  solicitacoes: (
    <>
      <path d="M4 8h13l-3.5-3.5" />
      <path d="M20 16H7l3.5 3.5" />
    </>
  ),
  historico: (
    <>
      <path d="M3.5 12a8.5 8.5 0 1 0 2.6-6.1" />
      <path d="M3.5 4v4.5H8" />
      <path d="M12 7.5V12l3 2" />
    </>
  ),
  recursos: (
    <>
      <path d="M12 2.8 20.5 7.4v9.2L12 21.2 3.5 16.6V7.4Z" />
      <path d="M3.8 7.6 12 12l8.2-4.4M12 12v9" />
    </>
  ),
  instituicoes: (
    <>
      <path d="M3 10.5 12 4l9 6.5" />
      <path d="M5 9.5V20h14V9.5" />
      <path d="M10 20v-5h4v5" />
      <circle cx="12" cy="10.5" r="1.6" />
    </>
  ),
  usuarios: (
    <>
      <circle cx="9" cy="8" r="3.5" />
      <path d="M2.8 20c.6-3.5 3.1-5.5 6.2-5.5s5.6 2 6.2 5.5" />
      <path d="M16 4.8a3.3 3.3 0 0 1 0 6.4M17.8 14.8c1.9.6 3.1 2.4 3.4 5.2" />
    </>
  ),
  auditoria: (
    <>
      <path d="M12 2.8 19.5 5.6v5.8c0 4.6-3.1 8.1-7.5 9.8-4.4-1.7-7.5-5.2-7.5-9.8V5.6Z" />
      <path d="m8.8 12 2.3 2.3 4.3-4.6" />
    </>
  ),
  sair: (
    <>
      <path d="M14 4h4.5A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5H14" />
      <path d="M10 8l-4 4 4 4M6 12h10" />
    </>
  ),
  mobiliario: (
    <>
      <path d="M7 3.5h10v8H7z" />
      <path d="M5 11.5h14v3H5z" />
      <path d="M6.5 14.5 6 20.5M17.5 14.5l.5 6" />
    </>
  ),
  material: (
    <>
      <path d="M12 6.5C10 5 7 4.5 3.5 5v13c3.5-.5 6.5 0 8.5 1.5 2-1.5 5-2 8.5-1.5V5C17 4.5 14 5 12 6.5Z" />
      <path d="M12 6.5v13" />
    </>
  ),
  vestuario: (
    <>
      <path d="M8.5 3.5 3.5 6.5l2 4 2-1V20.5h9V9.5l2 1 2-4-5-3c-.5 1.6-1.9 2.6-3.5 2.6S9 5.1 8.5 3.5Z" />
    </>
  ),
  doacao: (
    <>
      <rect x="3.5" y="9" width="17" height="4" rx="1" />
      <path d="M5 13v7.5h14V13M12 9v11.5" />
      <path d="M12 9C10.5 9 7.5 8.5 7.5 6.3 7.5 4.8 9 4 10 4.6 11.2 5.3 12 7.5 12 9Zm0 0c1.5 0 4.5-.5 4.5-2.7 0-1.5-1.5-2.3-2.5-1.7C12.8 5.3 12 7.5 12 9Z" />
    </>
  ),
  outro: (
    <>
      <path d="M4 7.5 12 3.5l8 4v9l-8 4-8-4Z" />
      <path d="M8 5.5l8 4v4" />
    </>
  ),
  coracao: (
    <path d="M12 20s-7.5-4.6-8.6-9.6C2.7 7 4.9 4.5 7.7 4.5c1.8 0 3.3 1 4.3 2.6 1-1.6 2.5-2.6 4.3-2.6 2.8 0 5 2.5 4.3 5.9C19.5 15.4 12 20 12 20Z" />
  ),
  copiar: (
    <>
      <rect x="8.5" y="8.5" width="11.5" height="11.5" rx="2" />
      <path d="M15.5 8.5V5.5A1.5 1.5 0 0 0 14 4H5.5A1.5 1.5 0 0 0 4 5.5V14a1.5 1.5 0 0 0 1.5 1.5h3" />
    </>
  ),
  atualizar: (
    <>
      <path d="M20 11a8 8 0 0 0-14.5-4.6L4 8" />
      <path d="M4 3.5V8h4.5" />
      <path d="M4 13a8 8 0 0 0 14.5 4.6L20 16" />
      <path d="M20 20.5V16h-4.5" />
    </>
  ),
  check: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="m8.3 12.2 2.5 2.5 5-5.2" />
    </>
  ),
  relogio: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </>
  ),
  grafico: (
    <>
      <path d="M4 20h16" />
      <path d="M7 16v-5M12 16V7M17 16v-8" />
    </>
  ),
  seta: <path d="M5 12h14M13 6l6 6-6 6" />,
};

export default function Icone({ nome, tamanho = 20, className = "", titulo }) {
  return (
    <svg
      className={`icone ${className}`}
      width={tamanho}
      height={tamanho}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      role={titulo ? "img" : undefined}
      aria-label={titulo}
      aria-hidden={titulo ? undefined : true}
    >
      {CAMINHOS[nome] || CAMINHOS.outro}
    </svg>
  );
}


export const VISUAL_CATEGORIA = {
  MOBILIARIO: { icone: "mobiliario", cor: "#8a5a2b", fundo: "#f6ece1" },
  MATERIAL_DIDATICO: { icone: "material", cor: "#7a1f3d", fundo: "#f7e6ec" },
  VESTUARIO: { icone: "vestuario", cor: "#2d6a8a", fundo: "#e3f0f6" },
  DOACAO: { icone: "doacao", cor: "#1e7a4f", fundo: "#e2f3ea" },
  OUTRO: { icone: "outro", cor: "#5e5a7a", fundo: "#ecebf3" },
};