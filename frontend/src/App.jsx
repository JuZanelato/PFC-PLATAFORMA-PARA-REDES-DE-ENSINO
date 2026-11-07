import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import CadastroPage from "./Pages/CadastroPage";
import InstituicaoPages from "./Pages/InstituicaoPages";
import AuditoriaPage from "./Pages/AuditoriaPage";
import RotaProtegida from "./Components/RotaProtegida";
import RecursosPage from "./Pages/RecursosPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route
          path="/auditoria"
          element={
            <RotaProtegida perfisPermitidos={["ADMINISTRADOR"]}>
              <AuditoriaPage />
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
          path="/recursos"
          element={
            <RotaProtegida>
              <RecursosPage />
            </RotaProtegida>
          }
        />
        <Route path="*" element={<Navigate to="/instituicoes" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;