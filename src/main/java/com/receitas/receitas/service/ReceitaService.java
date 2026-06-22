package com.receitas.receitas.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.receitas.receitas.entity.Receita;
import com.receitas.receitas.repository.ReceitaRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReceitaService {

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private EmailService emailService;

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    public List<Receita> findAll() {
        return receitaRepository.findAll();
    }

    public Optional<Receita> findById(Long id) {
        return receitaRepository.findById(id);
    }

    /**
     * Aplica filtros opcionais. Se nenhum parâmetro for informado, retorna tudo.
     */
    public List<Receita> findWithFilters(String status, LocalDate dataInicio, LocalDate dataFim) {
        boolean temStatus = status != null && !status.isBlank();
        boolean temDatas  = dataInicio != null && dataFim != null;

        if (temStatus && temDatas) {
            return receitaRepository.findByStatusAndDataRegistroBetween(status, dataInicio, dataFim);
        }
        if (temStatus) {
            return receitaRepository.findByStatus(status);
        }
        if (temDatas) {
            return receitaRepository.findByDataRegistroBetween(dataInicio, dataFim);
        }
        return receitaRepository.findAll();
    }

    // -------------------------------------------------------------------------
    // Persistência
    // -------------------------------------------------------------------------

    public Receita save(Receita receita) {
        boolean isNew = (receita.getId() == null);
        Receita salva = receitaRepository.save(receita);
        if (isNew) {
            emailService.enviarEmailCriacao(salva);
        } else {
            emailService.enviarEmailAtualizacao(salva);
        }
        return salva;
    }

    public void deleteById(Long id) {
        receitaRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Exportação PDF
    // -------------------------------------------------------------------------

    public void exportarPdf(List<Receita> receitas, HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=receitas.pdf");

        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        // Título
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        doc.add(new Paragraph("Relatório de Receitas", fontTitulo));
        doc.add(new Paragraph("Gerado em: " + LocalDate.now()));
        doc.add(Chunk.NEWLINE);

        // Tabela
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 5f, 2.5f, 2f, 2f, 2f});

        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String col : new String[]{"Nome", "Descrição", "Tipo", "Custo", "Status", "Data"}) {
            PdfPCell cell = new PdfPCell(new Phrase(col, fontHeader));
            cell.setBackgroundColor(new java.awt.Color(52, 73, 94));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            // text color via font
            cell.setPhrase(new Phrase(col, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10,
                    java.awt.Color.WHITE)));
            table.addCell(cell);
        }

        Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Receita r : receitas) {
            table.addCell(new Phrase(nvl(r.getNome()), fontData));
            table.addCell(new Phrase(nvl(r.getDescricao()), fontData));
            table.addCell(new Phrase(nvl(r.getTipoReceita()), fontData));
            table.addCell(new Phrase(r.getCusto() != null ? r.getCusto().toString() : "", fontData));
            table.addCell(new Phrase(nvl(r.getStatus()), fontData));
            table.addCell(new Phrase(r.getDataRegistro() != null ? r.getDataRegistro().toString() : "", fontData));
        }

        doc.add(table);
        doc.close();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}