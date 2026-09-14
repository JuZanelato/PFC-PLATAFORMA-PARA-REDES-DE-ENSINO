package com.web.pfc.SpringPfc.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import com.web.pfc.SpringPfc.Repository.InstituicaoRep;
import com.web.pfc.SpringPfc.domain.Instituicao;

@RestController
@RequestMapping("api/Instituicao")
public class InstituicaoController {

    private InstituicaoRep instituicaorep;

    public InstituicaoController(InstituicaoRep instituicaorep) {
        this.instituicaorep = instituicaorep;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
        public Instituicao save(@RequestBody @Valid Instituicao instituicao) {
            return instituicaorep.save(instituicao);
        }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id) {

        instituicaorep.findById(id)
                .map(instituicao -> {
                    instituicaorep.delete(instituicao);
                    return Void.TYPE;
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro nao encontrado"
                ));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Integer id,
            @RequestBody @Valid Instituicao instituicao) {

        instituicaorep.findById(id)
                .map(instituicaoExistente -> {

                    instituicao.setId(instituicaoExistente.getId());
                    instituicaorep.save(instituicao);

                    return instituicao;

                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro nao encontrado"
                ));
    }

    @GetMapping
    public List<Instituicao> find(Instituicao filtro) {

        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withStringMatcher(
                        ExampleMatcher.StringMatcher.CONTAINING
                );

        Example<Instituicao> example = Example.of(filtro, matcher);

        return instituicaorep.findAll(example);
    }

    @GetMapping("{id}")
    public Instituicao getInstituicaoById(
            @PathVariable("id") Integer id) {

        return instituicaorep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instituicao nao encontrada"
                ));
    }
}