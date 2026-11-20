export function LogoMarca({ tamanho = 36, claro = false }) {
  const fundo = claro ? "#ffffff" : "#7a1f3d";
  const traco = claro ? "#7a1f3d" : "#ffffff";
  return (
    <svg width={tamanho} height={tamanho} viewBox="0 0 48 48" aria-hidden="true">
      <rect width="48" height="48" rx="12" fill={fundo} />
      <path d="M24 15c-3.2-2.4-7.6-3-12-2.2v20c4.4-.8 8.8-.2 12 2.2" fill="none" stroke={traco} strokeWidth="2.6" strokeLinejoin="round" />
      <path d="M24 15c3.2-2.4 7.6-3 12-2.2v20c-4.4-.8-8.8-.2-12 2.2V15Z" fill={traco} opacity="0.95" />
      <path d="M28 21.5h5.5l-1.8-1.8M33.5 26.5H28l1.8 1.8" fill="none" stroke={fundo} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="38" cy="11" r="3.2" fill="#f2b84b" />
    </svg>
  );
}

export default function Logo({ tamanho = 36, claro = false, className = "" }) {
  return (
    <span className={`logo ${className}`}>
      <LogoMarca tamanho={tamanho} claro={claro} />
      <span className="logo-nome">
        Compartilha<strong>Edu</strong>
      </span>
    </span>
  );
}