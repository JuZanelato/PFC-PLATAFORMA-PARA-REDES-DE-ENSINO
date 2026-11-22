import { useState } from "react";
import { mensagemDeErro } from "../Services/mensagemErro";
import { hojeISO } from "../Services/SolicitacaoService";


export default function SolicitacaoForm({ recurso, onEnviar, onCancelar }) {
    const [quantidade, setQuantidade] = useState("1");
    const [justificativa, setJustificativa] = useState("");
    const [dataPrevista, setDataPrevista] = useState("");
    const [erros, setErros] = useState({});
    const [enviando, setEnviando] = useState(false);

    const validar = () => {
        const novosErros = {};
        const qtd = Number(quantidade);
        if (quantidade === "" || quantidade === null) {
            novosErros.quantidade = "Quantidade é obrigatória";
        } else if (!Number.isInteger(qtd) || qtd < 1) {
            novosErros.quantidade = "Informe um número inteiro maior que zero";
        } else if (qtd > recurso.quantidade) {
            novosErros.quantidade = `Máximo disponível: ${recurso.quantidade}`;
        }
        if (!dataPrevista) {
            novosErros.dataPrevista = "Informe a data prevista de devolução";
        } else if (dataPrevista < hojeISO()) {
            novosErros.dataPrevista = "A data não pode estar no passado";
        }
        if (justificativa.length > 500) {
            novosErros.justificativa = "A justificativa deve ter no máximo 500 caracteres";
        }
        setErros(novosErros);
        return Object.keys(novosErros).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validar()) return;
        setEnviando(true);
        try {
            await onEnviar({
                recursoId: recurso.id,
                quantidade: Number(quantidade),
                justificativa: justificativa.trim(),
                dataPrevistaDevolucao: dataPrevista,
            });
        } catch (err) {
            setErros({ geral: mensagemDeErro(err, "Não foi possível enviar a solicitação.") });
        } finally {
            setEnviando(false);
        }
    };

    return (
        <form className="recurso-form" onSubmit={handleSubmit} noValidate>
            <h2>Solicitar “{recurso.nome}”</h2>
            <p className="recurso-aviso">
                Oferecido por <strong>{recurso.instituicaoNome}</strong> · {recurso.quantidade} unidade(s)
                disponível(is). A solicitação será analisada pelo diretor dessa instituição.
            </p>

            {erros.geral && <p className="recurso-erro-geral">{erros.geral}</p>}

            <label>
                Quantidade
                <input
                    type="number"
                    min="1"
                    max={recurso.quantidade}
                    step="1"
                    value={quantidade}
                    onChange={(e) => setQuantidade(e.target.value)}
                />
                {erros.quantidade && <span className="recurso-erro-campo">{erros.quantidade}</span>}
            </label>

            <label>
                Data prevista de devolução
                <input
                    type="date"
                    min={hojeISO()}
                    value={dataPrevista}
                    onChange={(e) => setDataPrevista(e.target.value)}
                />
                {erros.dataPrevista && <span className="recurso-erro-campo">{erros.dataPrevista}</span>}
            </label>

            <label>
                Justificativa (opcional)
                <textarea
                    rows={3}
                    maxLength={500}
                    value={justificativa}
                    onChange={(e) => setJustificativa(e.target.value)}
                    placeholder="Ex.: atender a turma do 5º ano durante o mês de novembro"
                />
                <small>{justificativa.length}/500</small>
                {erros.justificativa && <span className="recurso-erro-campo">{erros.justificativa}</span>}
            </label>

            <div className="recurso-form-acoes">
                <button type="submit" disabled={enviando}>
                    {enviando ? "Enviando..." : "Enviar solicitação"}
                </button>
                <button type="button" className="secundario" onClick={onCancelar} disabled={enviando}>
                    Cancelar
                </button>
            </div>
        </form>
    );
}