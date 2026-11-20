export function IlustracaoRede({ className = "" }) {
  return (
    <svg className={className} viewBox="0 0 420 280" role="img" aria-label="Duas escolas compartilhando materiais">
      <circle cx="210" cy="150" r="128" fill="#fbe9ef" />
      <ellipse cx="96" cy="58" rx="26" ry="10" fill="#ffffff" />
      <ellipse cx="118" cy="52" rx="18" ry="9" fill="#ffffff" />
      <ellipse cx="330" cy="70" rx="22" ry="9" fill="#ffffff" />
      <circle cx="350" cy="40" r="14" fill="#f2b84b" />

      {/* caminho de troca */}
      <path d="M135 112 Q 222 6 306 118" fill="none" stroke="#a8395f" strokeWidth="3" strokeDasharray="7 9" strokeLinecap="round" />
      <path d="M296 108 306 120 310 104" fill="none" stroke="#a8395f" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />

      {/* livro voando */}
      <g transform="translate(178 46) rotate(-12)">
        <path d="M0 4C7-1 15-1 22 4v20c-7-5-15-5-22 0Z" fill="#ffffff" stroke="#7a1f3d" strokeWidth="2.5" strokeLinejoin="round" />
        <path d="M22 4c7-5 15-5 22 0v20c-7-5-15-5-22 0Z" fill="#f7e6ec" stroke="#7a1f3d" strokeWidth="2.5" strokeLinejoin="round" />
      </g>
      {/* caixa voando */}
      <g transform="translate(246 46) rotate(10)">
        <rect width="30" height="24" rx="3" fill="#f2b84b" stroke="#7a1f3d" strokeWidth="2.5" />
        <path d="M0 8h30M15 0v8" stroke="#7a1f3d" strokeWidth="2.5" />
      </g>

      {/* chão */}
      <rect x="40" y="228" width="340" height="6" rx="3" fill="#e8cdd6" />

      {/* árvores */}
      <rect x="49" y="198" width="6" height="30" rx="2" fill="#8a5a2b" />
      <circle cx="52" cy="190" r="18" fill="#3f8f6b" />
      <rect x="371" y="204" width="6" height="24" rx="2" fill="#8a5a2b" />
      <circle cx="374" cy="198" r="14" fill="#56a37f" />

      {/* escola A */}
      <rect x="74" y="146" width="110" height="82" fill="#ffffff" stroke="#7a1f3d" strokeWidth="3" />
      <path d="M64 148 129 106 194 148Z" fill="#7a1f3d" />
      <path d="M129 106V82" stroke="#7a1f3d" strokeWidth="3" strokeLinecap="round" />
      <path d="M129 82l20 6-20 6Z" fill="#f2b84b" />
      <circle cx="129" cy="132" r="7" fill="#ffffff" />
      <rect x="86" y="162" width="22" height="18" rx="2" fill="#f7e6ec" stroke="#7a1f3d" strokeWidth="2.5" />
      <rect x="150" y="162" width="22" height="18" rx="2" fill="#f7e6ec" stroke="#7a1f3d" strokeWidth="2.5" />
      <rect x="116" y="192" width="26" height="36" rx="3" fill="#f2b84b" stroke="#7a1f3d" strokeWidth="2.5" />

      {/* escola B */}
      <rect x="252" y="160" width="100" height="68" fill="#ffffff" stroke="#7a1f3d" strokeWidth="3" />
      <path d="M242 162 302 126 362 162Z" fill="#a8395f" />
      <rect x="264" y="174" width="20" height="16" rx="2" fill="#f7e6ec" stroke="#7a1f3d" strokeWidth="2.5" />
      <rect x="320" y="174" width="20" height="16" rx="2" fill="#f7e6ec" stroke="#7a1f3d" strokeWidth="2.5" />
      <rect x="290" y="196" width="24" height="32" rx="3" fill="#7a1f3d" />

      {/* brilhos */}
      <circle cx="222" cy="120" r="4" fill="#f2b84b" />
      <circle cx="206" cy="136" r="2.5" fill="#a8395f" />
      <circle cx="238" cy="140" r="3" fill="#7a1f3d" opacity="0.5" />
    </svg>
  );
}


export function IlustracaoVazio({ className = "" }) {
  return (
    <svg className={className} viewBox="0 0 160 110" aria-hidden="true">
      <ellipse cx="80" cy="98" rx="50" ry="7" fill="#f1e4e9" />
      <path d="M40 50 80 64 120 50V88L80 100 40 88Z" fill="#f7e6ec" stroke="#7a1f3d" strokeWidth="2.5" strokeLinejoin="round" />
      <path d="M80 64v36" stroke="#7a1f3d" strokeWidth="2.5" />
      <path d="M40 50 26 36 66 24 80 38Z" fill="#ffffff" stroke="#7a1f3d" strokeWidth="2.5" strokeLinejoin="round" />
      <path d="M120 50l14-14-40-12-14 14Z" fill="#ffffff" stroke="#7a1f3d" strokeWidth="2.5" strokeLinejoin="round" />
      <path d="M80 38 40 50l40 14 40-14Z" fill="#e8cdd6" />
      <circle cx="80" cy="14" r="3.5" fill="#f2b84b" />
      <path d="M58 10l3 3M102 10l-3 3" stroke="#a8395f" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  );
}


export function IlustracaoDoacao({ className = "" }) {
  return (
    <svg className={className} viewBox="0 0 120 100" aria-hidden="true">
      <circle cx="60" cy="52" r="44" fill="#ffffff" opacity="0.7" />
      <path d="M60 82S28 63 24 42c-3-14 6-24 17-24 8 0 14 4 19 11 5-7 11-11 19-11 11 0 20 10 17 24-4 21-36 40-36 40Z" fill="#a8395f" />
      <path d="M44 34c-4 0-7 3-7 8" fill="none" stroke="#ffffff" strokeWidth="3" strokeLinecap="round" opacity="0.7" />
      <circle cx="94" cy="76" r="11" fill="#f2b84b" stroke="#7a1f3d" strokeWidth="2.5" />
      <path d="M94 70v12" stroke="#7a1f3d" strokeWidth="2.5" strokeLinecap="round" />
      <circle cx="22" cy="78" r="7" fill="#f2b84b" stroke="#7a1f3d" strokeWidth="2.5" />
    </svg>
  );
}