package com.receitas.receitas.service;

import com.receitas.receitas.entity.Receita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private Receita receita;

    @BeforeEach
    void setUp() {
        emailService.setDestinatario("dest@test.com");
        emailService.setRemetente("from@test.com");

        receita = new Receita();
        receita.setId(1L);
        receita.setNome("Torta de Frango");
        receita.setTipoReceita("Torta");
        receita.setCusto(new BigDecimal("45.00"));
        receita.setStatus("ATIVO");
        receita.setDataRegistro(LocalDate.of(2024, 3, 10));
    }

    // ── Teste 9 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("enviarEmailCriacao – assunto deve conter 'criada' e nome da receita")
    void enviarEmailCriacao_assuntoDeveConterNomeEOperacao() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.enviarEmailCriacao(receita);

        verify(mailSender).send(captor.capture());
        String assunto = captor.getValue().getSubject();
        assertThat(assunto).containsIgnoringCase("criada");
        assertThat(assunto).contains("Torta de Frango");
    }

    // ── Teste 10 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("enviarEmailAtualizacao – assunto deve conter 'atualizada' e nome da receita")
    void enviarEmailAtualizacao_assuntoDeveConterNomeEOperacao() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.enviarEmailAtualizacao(receita);

        verify(mailSender).send(captor.capture());
        String assunto = captor.getValue().getSubject();
        assertThat(assunto).containsIgnoringCase("atualizada");
        assertThat(assunto).contains("Torta de Frango");
    }

    // ── Teste 11 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("enviarEmailCriacao – destinatário deve ser o configurado")
    void enviarEmailCriacao_deveEnviarParaDestinatarioCorreto() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.enviarEmailCriacao(receita);

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).contains("dest@test.com");
    }

    // ── Teste 12 ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("enviarEmailCriacao – falha no envio não deve lançar exceção")
    void enviarEmail_falhaDeveSerAbsorvidaSemLancarExcecao() {
        doThrow(new RuntimeException("SMTP indisponível")).when(mailSender).send(any(SimpleMailMessage.class));

        // Não deve propagar a exceção
        assertThatNoException().isThrownBy(() -> emailService.enviarEmailCriacao(receita));
    }
}