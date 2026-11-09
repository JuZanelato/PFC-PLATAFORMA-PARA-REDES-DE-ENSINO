import { Navigate } from "react-router-dom";
import authService from "../Services/authService";
import Navegacao from "./Navegacao";

export default function RotaProtegida({ children, perfisPermitidos }) {
  if (!authService.estaAutenticado()) {
    return <Navigate to="/login" replace />;
  }

  if (perfisPermitidos) {
    const usuario = authService.usuarioLogado();
    if (!usuario || !perfisPermitidos.includes(usuario.perfil)) {
      return <Navigate to="/rede" replace />;
    }
  }

  return (
    <>
      <Navegacao />
      {children}
    </>
  );
}
