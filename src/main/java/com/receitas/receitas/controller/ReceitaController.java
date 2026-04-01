package com.receitas.receitas.controller;

import com.receitas.receitas.repository.ReceitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReceitaController {

    @Autowired
    private ReceitaRepository receitaRepository;

    @GetMapping("/receitas")
    public String listar(Model model) {
        model.addAttribute("receitas", receitaRepository.findAll());
        return "receitas";
    }
}