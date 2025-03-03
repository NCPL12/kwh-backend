package ncpl.bms.reports.service;

import com.itextpdf.kernel.events.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

@Service
public class PageNumberEventHandler implements IEventHandler {

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();

        int currentPage = pdfDoc.getPageNumber(page);
        int totalPages = pdfDoc.getNumberOfPages();

        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
        Canvas canvas = new Canvas(pdfCanvas, pageSize);

        // Create footer text for page number
        Paragraph pageNumber = new Paragraph("Page " + currentPage )
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);

        // Place page number at the bottom right
        canvas.showTextAligned(pageNumber, pageSize.getWidth() - 40, 20, TextAlignment.RIGHT);

        pdfCanvas.release();
    }
}