import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import CadastroPage from "./Pages/CadastroPage";
import InstituicaoPages from "./Pages/InstituicaoPages";
import AuditoriaPage from "./Pages/AuditoriaPage";
import RotaProtegida from "./Components/RotaProtegida";
import RecursosPage from "./Pages/RecursosPage";
import PoliticaPrivacidadePage from "./Pages/PoliticaPrivacidadePage";
import RecuperarSenhaPage from "./Pages/RecuperarSenhaPage";
import BuscarRecursosPage from "./Pages/BuscarRecursoPage";
import SolicitacoesPage from "./Pages/SolicitacoesPage";
import HistoricoPage from "./Pages/HistoricoPage";
import DashboardPage from "./Pages/DashboardPage";
import UsuariosPage from "./Pages/UsuariosPage";
import MeusDadosPage from "./Pages/MeusDadosPage";
import InstituicaoDetalhePage from "./Pages/InstituicaoDetalhePage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rotas publicas */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route path="/recuperar-senha" element={<RecuperarSenhaPage />} />
        <Route path="/politica-privacidade" element={<PoliticaPrivacidadePage />} />

        {/* Rotas protegidas */}
        <Route
          path="/auditoria"
          element={
            <RotaProtegida perfisPermitidos={["ADMINISTRADOR"]}>
              <AuditoriaPage />
            </RotaProtegida>
          }
        />
        <Route
          path="/usuarios"
          element={
            <RotaProtegida perfisPermitidos={["ADMINISTRADOR"]}>
              <UsuariosPage />
            </RotaProtegida>
          }
        />
        <Route
          path="/instituicoes"
          element={
            <RotaProtegida>
              <InstituicaoPages />
            </RotaProtegida>
          }
        />

        <Route
          path="/instituicoes/:id"
          element={
            <RotaProtegida>
              <InstituicaoDetalhePage />
            </RotaProtegida>
          }
        />
        <Route
          path="/meus-dados"
          element={
            <RotaProtegida>
              <MeusDadosPage />
            </RotaProtegida>
          }
        />

        <Route
          path="/recursos"
          element={
            <RotaProtegida>
              <RecursosPage />
            </RotaProtegida>
          }
        />
        <Route
          path="/rede"
          element={
            <RotaProtegida>
              <BuscarRecursosPage />
            </RotaProtegida>
          }
        />
        <Route
          path="/solicitacoes"
          element={
            <RotaProtegida>
              <SolicitacoesPage />
            </RotaProtegida>
          }
        />
        <Route
          path="/historico"
          element={
            <RotaProtegida>
              <HistoricoPage />
            </RotaProtegida>
          }
        />
        <Route
          path="/dashboard"
          element={
            <RotaProtegida perfisPermitidos={["GESTOR", "ADMINISTRADOR"]}>
              <DashboardPage />
            </RotaProtegida>
          }
        />
        <Route path="*" element={<Navigate to="/rede" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;