package com.receitas.receitas.controller;

import com.lowagie.text.DocumentException;
import com.receitas.receitas.entity.Receita;
import com.receitas.receitas.service.ReceitaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ReceitaController {

    @Autowired
    private ReceitaService receitaService;

    // -------------------------------------------------------------------------
    // Listagem + filtros
    // -------------------------------------------------------------------------

    @GetMapping("/receitas")
    public String listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Model model) {

        List<Receita> receitas = receitaService.findWithFilters(status, dataInicio, dataFim);

        model.addAttribute("receitas",    receitas);
        model.addAttribute("status",      status);
        model.addAttribute("dataInicio",  dataInicio);
        model.addAttribute("dataFim",     dataFim);
        model.addAttribute("total",       receitas.size());
        return "receitas";
    }

    // -------------------------------------------------------------------------
    // Formulário – criar
    // -------------------------------------------------------------------------

    @GetMapping("/receitas/nova")
    public String nova(Model model) {
        model.addAttribute("receita", new Receita());
        model.addAttribute("titulo", "Nova Receita");
        return "receita-form";
    }

    // -------------------------------------------------------------------------
    // Formulário – editar
    // -------------------------------------------------------------------------

    @GetMapping("/receitas/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Receita receita = receitaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receita não encontrada: " + id));
        model.addAttribute("receita", receita);
        model.addAttribute("titulo", "Editar Receita");
        return "receita-form";
    }

    // -------------------------------------------------------------------------
    // Salvar (criar e atualizar)
    // -------------------------------------------------------------------------

    @PostMapping("/receitas/salvar")
    public String salvar(@ModelAttribute Receita receita) {
        receitaService.save(receita);
        return "redirect:/receitas";
    }

    // -------------------------------------------------------------------------
    // Excluir
    // -------------------------------------------------------------------------

    @PostMapping("/receitas/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        receitaService.deleteById(id);
        return "redirect:/receitas";
    }

    // -------------------------------------------------------------------------
    // Exportar PDF com os mesmos filtros da tela
    // -------------------------------------------------------------------------

    @GetMapping("/receitas/pdf")
    public void exportarPdf(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletResponse response) throws IOException, DocumentException {

        List<Receita> receitas = receitaService.findWithFilters(status, dataInicio, dataFim);
        receitaService.exportarPdf(receitas, response);
    }
}