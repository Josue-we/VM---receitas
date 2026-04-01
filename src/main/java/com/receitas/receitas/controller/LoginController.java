package com.receitas.receitas.controller;

import com.receitas.receitas.entity.Usuario;
import com.receitas.receitas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String autenticar(String login, String senha, Model model) {
        Usuario usuario = usuarioRepository.findByLoginAndSenha(login, senha);

        if (usuario != null) {
            return "redirect:/receitas";
        }

        model.addAttribute("erro", "Login inválido");
        return "login";
    }
}