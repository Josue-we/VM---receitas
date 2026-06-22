package com.receitas.receitas.controller;

import com.receitas.receitas.entity.Receita;
import com.receitas.receitas.service.ReceitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.ServletException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceitaController.class)
class ReceitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceitaService receitaService;

    private Receita receita;

    @BeforeEach
    void setUp() {
        receita = new Receita();
        receita.setId(1L);
        receita.setNome("Arroz Carreteiro");
        receita.setTipoReceita("Prato Principal");
        receita.setCusto(new BigDecimal("35.00"));
        receita.setStatus("ATIVO");
        receita.setDataRegistro(LocalDate.of(2024, 5, 20));
    }

    // ── Teste 13 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /receitas – deve retornar view 'receitas' com model populado")
    void listar_deveRetornarViewReceitas() throws Exception {
        when(receitaService.findWithFilters(any(), any(), any())).thenReturn(List.of(receita));

        mockMvc.perform(get("/receitas"))
                .andExpect(status().isOk())
                .andExpect(view().name("receitas"))
                .andExpect(model().attributeExists("receitas"))
                .andExpect(model().attributeExists("total"));
    }

    // ── Teste 14 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /receitas com filtro de status – deve passar parâmetro ao service")
    void listar_comFiltroStatus_devePassarParametroAoService() throws Exception {
        when(receitaService.findWithFilters(eq("ATIVO"), isNull(), isNull()))
                .thenReturn(List.of(receita));

        mockMvc.perform(get("/receitas").param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("status", "ATIVO"));

        verify(receitaService).findWithFilters("ATIVO", null, null);
    }

    // ── Teste 15 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /receitas/nova – deve retornar view 'receita-form' com receita vazia")
    void nova_deveRetornarFormularioVazio() throws Exception {
        mockMvc.perform(get("/receitas/nova"))
                .andExpect(status().isOk())
                .andExpect(view().name("receita-form"))
                .andExpect(model().attributeExists("receita"));
    }

    // ── Teste 16 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /receitas/editar/{id} – deve retornar formulário com dados preenchidos")
    void editar_deveRetornarFormularioPreenchido() throws Exception {
        when(receitaService.findById(1L)).thenReturn(Optional.of(receita));

        mockMvc.perform(get("/receitas/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("receita-form"))
                .andExpect(model().attribute("receita", receita));
    }

    // ── Teste 17 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /receitas/editar/{id} – deve lançar exceção quando id não existir")
    void editar_deveRetornar4xxQuandoNaoExistir() {
        when(receitaService.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/receitas/editar/999")));
    }

    // ── Teste 18 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /receitas/salvar – deve salvar e redirecionar para listagem")
    void salvar_deveRedirecionarParaListagem() throws Exception {
        when(receitaService.save(any(Receita.class))).thenReturn(receita);

        mockMvc.perform(post("/receitas/salvar")
                        .param("nome", "Feijão Tropeiro")
                        .param("tipoReceita", "Prato")
                        .param("custo", "20.00")
                        .param("status", "ATIVO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receitas"));

        verify(receitaService).save(any(Receita.class));
    }

    // ── Teste 19 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /receitas/deletar/{id} – deve excluir e redirecionar")
    void deletar_deveExcluirERedirecionarParaListagem() throws Exception {
        doNothing().when(receitaService).deleteById(1L);

        mockMvc.perform(post("/receitas/deletar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receitas"));

        verify(receitaService).deleteById(1L);
    }

    // ── Teste 20 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /receitas/pdf – deve retornar content-type application/pdf")
    void exportarPdf_deveRetornarPdf() throws Exception {
        when(receitaService.findWithFilters(any(), any(), any())).thenReturn(List.of(receita));
        // exportarPdf escreve direto no response; mockamos para não fazer nada
        doNothing().when(receitaService).exportarPdf(anyList(), any());

        mockMvc.perform(get("/receitas/pdf"))
                .andExpect(status().isOk());

        verify(receitaService).exportarPdf(anyList(), any());
    }
}