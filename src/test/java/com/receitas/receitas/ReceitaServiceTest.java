package com.receitas.receitas.service;

import com.receitas.receitas.entity.Receita;
import com.receitas.receitas.repository.ReceitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceitaServiceTest {

    @Mock
    private ReceitaRepository receitaRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReceitaService receitaService;

    private Receita receita;

    @BeforeEach
    void setUp() {
        receita = new Receita();
        receita.setNome("Bolo de Cenoura");
        receita.setTipoReceita("Bolo");
        receita.setCusto(new BigDecimal("25.50"));
        receita.setStatus("ATIVO");
        receita.setDataRegistro(LocalDate.of(2024, 6, 15));
    }

    // ── Teste 1 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("save – deve persistir e retornar receita criada")
    void save_deveRetornarReceitaSalva() {
        Receita salva = new Receita();
        salva.setId(1L);
        salva.setNome(receita.getNome());
        when(receitaRepository.save(receita)).thenReturn(salva);

        Receita resultado = receitaService.save(receita);

        assertThat(resultado.getId()).isEqualTo(1L);
        verify(receitaRepository).save(receita);
    }

    // ── Teste 2 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("save – deve disparar e-mail de CRIAÇÃO quando id for nulo")
    void save_deveChamarEmailCriacaoParaNovaReceita() {
        receita.setId(null);                              // nova entidade
        Receita salva = new Receita();
        salva.setId(99L);
        when(receitaRepository.save(receita)).thenReturn(salva);

        receitaService.save(receita);

        verify(emailService).enviarEmailCriacao(salva);
        verify(emailService, never()).enviarEmailAtualizacao(any());
    }

    // ── Teste 3 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("save – deve disparar e-mail de ATUALIZAÇÃO quando id existir")
    void save_deveChamarEmailAtualizacaoParaReceitaExistente() {
        receita.setId(10L);                               // entidade existente
        when(receitaRepository.save(receita)).thenReturn(receita);

        receitaService.save(receita);

        verify(emailService).enviarEmailAtualizacao(receita);
        verify(emailService, never()).enviarEmailCriacao(any());
    }

    // ── Teste 4 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("findById – deve retornar receita quando existir")
    void findById_deveRetornarReceita() {
        receita.setId(5L);
        when(receitaRepository.findById(5L)).thenReturn(Optional.of(receita));

        Optional<Receita> resultado = receitaService.findById(5L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Bolo de Cenoura");
    }

    // ── Teste 5 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("findById – deve retornar Optional.empty quando não existir")
    void findById_deveRetornarVazioQuandoNaoExiste() {
        when(receitaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Receita> resultado = receitaService.findById(999L);

        assertThat(resultado).isEmpty();
    }

    // ── Teste 6 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("deleteById – deve chamar repositório com o id correto")
    void deleteById_deveDelegarAoRepositorio() {
        doNothing().when(receitaRepository).deleteById(7L);

        receitaService.deleteById(7L);

        verify(receitaRepository).deleteById(7L);
    }

    // ── Teste 7 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("findWithFilters – sem parâmetros deve retornar findAll")
    void findWithFilters_semParametrosDeveUsarFindAll() {
        when(receitaRepository.findAll()).thenReturn(List.of(receita));

        List<Receita> resultado = receitaService.findWithFilters(null, null, null);

        assertThat(resultado).hasSize(1);
        verify(receitaRepository).findAll();
        verify(receitaRepository, never()).findByStatus(any());
        verify(receitaRepository, never()).findByDataRegistroBetween(any(), any());
    }

    // ── Teste 8 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("findWithFilters – com status E datas deve usar método combinado")
    void findWithFilters_comStatusEDataDeveUsarMetodoCombinado() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim    = LocalDate.of(2024, 12, 31);
        when(receitaRepository.findByStatusAndDataRegistroBetween("ATIVO", inicio, fim))
                .thenReturn(List.of(receita));

        List<Receita> resultado = receitaService.findWithFilters("ATIVO", inicio, fim);

        assertThat(resultado).hasSize(1);
        verify(receitaRepository).findByStatusAndDataRegistroBetween("ATIVO", inicio, fim);
        verify(receitaRepository, never()).findAll();
    }
}