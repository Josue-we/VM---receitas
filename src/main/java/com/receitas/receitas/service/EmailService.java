package com.receitas.receitas.service;

import com.receitas.receitas.entity.Receita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.destinatario:admin@receitas.com}")
    private String destinatario;

    @Value("${app.email.remetente:noreply@receitas.com}")
    private String remetente;

    public void enviarEmailCriacao(Receita receita) {
        enviar(
            "✅ Nova receita criada: " + receita.getNome(),
            montarCorpo("CRIAÇÃO", receita)
        );
    }

    public void enviarEmailAtualizacao(Receita receita) {
        enviar(
            "✏️ Receita atualizada: " + receita.getNome(),
            montarCorpo("ATUALIZAÇÃO", receita)
        );
    }

    private void enviar(String assunto, String corpo) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(destinatario);
            msg.setSubject(assunto);
            msg.setText(corpo);
            mailSender.send(msg);
            log.info("E-mail enviado: {}", assunto);
        } catch (Exception ex) {
            // Não interrompe o fluxo principal se o e-mail falhar
            log.error("Falha ao enviar e-mail '{}': {}", assunto, ex.getMessage());
        }
    }

    private String montarCorpo(String operacao, Receita r) {
        return String.format("""
                Operação: %s
                -------------------------
                ID       : %s
                Nome     : %s
                Tipo     : %s
                Custo    : %s
                Status   : %s
                Data     : %s
                Descrição: %s
                """,
                operacao,
                r.getId(),
                r.getNome(),
                r.getTipoReceita(),
                r.getCusto(),
                r.getStatus(),
                r.getDataRegistro(),
                r.getDescricao());
    }

    // Setter para facilitar testes
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public void setRemetente(String remetente) { this.remetente = remetente; }
}