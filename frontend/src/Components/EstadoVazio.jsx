import { IlustracaoVazio } from "./Ilustracao";
import "./Visual.css";

export default function EstadoVazio({ titulo, texto, children }) {
  return (
    <div className="estado-vazio">
      <IlustracaoVazio className="estado-vazio-img" />
      <p className="estado-vazio-titulo">{titulo}</p>
      {texto && <p className="estado-vazio-texto">{texto}</p>}
      {children}
    </div>
  );
}