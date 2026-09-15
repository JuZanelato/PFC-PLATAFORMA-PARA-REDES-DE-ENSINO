import { Navigate } from "react-router-dom";
import authService from "../Services/authService";
 
export default function RotaProtegida({ children, perfisPermitidos }) {
  if (!authService.estaAutenticado()) {
    return <Navigate to="/login" replace />;
  }
 
  if (perfisPermitidos) {
    const usuario = authService.usuarioLogado();
    if (!usuario || !perfisPermitidos.includes(usuario.perfil)) {
      return <Navigate to="/instituicoes" replace />;
    }
  }
 
  return children;
}