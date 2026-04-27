package com.receitas.receitas.repository;

import com.receitas.receitas.entity.Receita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ReceitaRepositoryTest {

    @Autowired
    private ReceitaRepository receitaRepository;

    @BeforeEach
    void popularBanco() {
        receitaRepository.deleteAll();

        Receita r1 = new Receita();
        r1.setNome("Coxinha");
        r1.setStatus("ATIVO");
        r1.setDataRegistro(LocalDate.of(2024, 3, 1));
        r1.setCusto(new BigDecimal("5.50"));

        Receita r2 = new Receita();
        r2.setNome("Pastel");
        r2.setStatus("INATIVO");
        r2.setDataRegistro(LocalDate.of(2024, 7, 15));
        r2.setCusto(new BigDecimal("4.00"));

        Receita r3 = new Receita();
        r3.setNome("Brigadeiro");
        r3.setStatus("ATIVO");
        r3.setDataRegistro(LocalDate.of(2024, 11, 20));
        r3.setCusto(new BigDecimal("2.00"));

        receitaRepository.saveAll(List.of(r1, r2, r3));
    }

    // ── Teste 17 (repo 1) ────────────────────────────────────────────────────
    @Test
    @DisplayName("findByStatus – deve retornar apenas receitas com status ATIVO")
    void findByStatus_deveRetornarApenasAtivos() {
        List<Receita> ativos = receitaRepository.findByStatus("ATIVO");

        assertThat(ativos).hasSize(2);
        assertThat(ativos).allMatch(r -> "ATIVO".equals(r.getStatus()));
    }

    // ── Teste 18 (repo 2) ────────────────────────────────────────────────────
    @Test
    @DisplayName("findByDataRegistroBetween – deve retornar receitas no intervalo de datas")
    void findByDataRegistroBetween_deveRetornarIntervalo() {
        LocalDate inicio = LocalDate.of(2024, 6, 1);
        LocalDate fim    = LocalDate.of(2024, 12, 31);

        List<Receita> resultado = receitaRepository.findByDataRegistroBetween(inicio, fim);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Receita::getNome)
                .containsExactlyInAnyOrder("Pastel", "Brigadeiro");
    }

    // ── Teste 19 (repo 3) ────────────────────────────────────────────────────
    @Test
    @DisplayName("findByStatusAndDataRegistroBetween – deve cruzar status e período")
    void findByStatusAndData_deveFiltrarCombinado() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim    = LocalDate.of(2024, 12, 31);

        List<Receita> resultado = receitaRepository
                .findByStatusAndDataRegistroBetween("INATIVO", inicio, fim);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Pastel");
    }

    // ── Teste 20 (repo 4) ────────────────────────────────────────────────────
    @Test
    @DisplayName("findAll – deve retornar todas as receitas salvas")
    void findAll_deveRetornarTodasAsReceitas() {
        List<Receita> todas = receitaRepository.findAll();

        assertThat(todas).hasSize(3);
    }
}