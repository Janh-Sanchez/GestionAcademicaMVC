package com.modelo;

import com.modelo.dominio.Estudiante;
import com.modelo.dominio.Grupo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generador de PDFs para listados de clase
 * Utiliza Apache PDFBox para crear documentos PDF
 */
public class GeneradorPDFListado {
    
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 16;
    private static final float FONT_SIZE_SUBTITLE = 12;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float LINE_SPACING = 15;

    /**
     * Genera un PDF con el listado de estudiantes del grupo
     */
    public static File generarListadoClase(
            Grupo grupo, 
            List<Estudiante> estudiantesOrdenados,
            String nombreProfesor) throws IOException {
        
        PDDocument document = new PDDocument();
        
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            
            // Título principal
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE);
            String titulo = "LISTADO DE CLASE";
            float tituloWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(titulo) / 1000 * FONT_SIZE_TITLE;
            float tituloX = (page.getMediaBox().getWidth() - tituloWidth) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(tituloX, yPosition);
            contentStream.showText(titulo);
            contentStream.endText();
            
            yPosition -= LINE_SPACING * 2;
            
            // Información del grupo
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Grado: " + grupo.getGrado().getNombreGrado());
            contentStream.endText();
            
            yPosition -= LINE_SPACING;
            
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Grupo: " + grupo.getNombreGrupo());
            contentStream.endText();
            
            yPosition -= LINE_SPACING;
            
            // Información del período
            LocalDate fechaActual = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String periodo = obtenerPeriodoActual(fechaActual);
            
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Período: " + periodo);
            contentStream.endText();
            
            yPosition -= LINE_SPACING;
            
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Fecha: " + fechaActual.format(formatter));
            contentStream.endText();
            
            yPosition -= LINE_SPACING;
            
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Profesor: " + nombreProfesor);
            contentStream.endText();
            
            yPosition -= LINE_SPACING * 2;
            
            // Encabezado de la tabla
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_NORMAL);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("No.    NOMBRE COMPLETO");
            contentStream.endText();
            
            yPosition -= LINE_SPACING;
            
            // Línea separadora
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
            contentStream.stroke();
            
            yPosition -= LINE_SPACING;
            
            // Lista de estudiantes
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
            
            int numero = 1;
            for (Estudiante estudiante : estudiantesOrdenados) {
                // Verificar si necesitamos una nueva página
                if (yPosition < MARGIN + LINE_SPACING) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - MARGIN;
                    contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
                }
                
                String numeroStr = String.format("%2d.", numero);
                String nombreCompleto = estudiante.obtenerNombreCompleto();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(numeroStr + "    " + nombreCompleto);
                contentStream.endText();
                
                yPosition -= LINE_SPACING;
                numero++;
            }
            
            // Total de estudiantes
            yPosition -= LINE_SPACING;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_NORMAL);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Total de estudiantes: " + estudiantesOrdenados.size());
            contentStream.endText();
            
            contentStream.close();
            
            // Guardar archivo
            String nombreArchivo = generarNombreArchivo(grupo);
            File archivo = new File(System.getProperty("user.home") + "/Downloads/" + nombreArchivo);
            document.save(archivo);
            
            return archivo;
            
        } finally {
            document.close();
        }
    }
    
    /**
     * Obtiene el período actual basado en la fecha
     */
    private static String obtenerPeriodoActual(LocalDate fecha) {
        int mes = fecha.getMonthValue();
        int año = fecha.getYear();
        
        if (mes >= 1 && mes <= 3) {
            return "Primer Período " + año;
        } else if (mes >= 4 && mes <= 6) {
            return "Segundo Período " + año;
        } else if (mes >= 7 && mes <= 9) {
            return "Tercer Período " + año;
        } else {
            return "Cuarto Período " + año;
        }
    }
    
    /**
     * Genera el nombre del archivo PDF
     */
    private static String generarNombreArchivo(Grupo grupo) {
        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fechaStr = fecha.format(formatter);
        
        String nombreGrupo = grupo.getNombreGrupo().replaceAll("[^a-zA-Z0-9]", "");
        String nombreGrado = grupo.getGrado().getNombreGrado().replaceAll("[^a-zA-Z0-9]", "");
        
        return "Listado_" + nombreGrado + "_" + nombreGrupo + "_" + fechaStr + ".pdf";
    }
}