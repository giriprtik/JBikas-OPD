package com.hospital.lab.service;

import com.hospital.lab.model.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Renders a lab report as PDF (OpenPDF). Abnormal values are printed bold red with H/L, critical
 * values additionally on a red background with HH/LL. Unreleased reports get a PRELIMINARY watermark.
 */
@Service
public class ReportService {

    private static final Color INK = new Color(19, 32, 46);
    private static final Color MUTED = new Color(91, 107, 124);
    private static final Color RULE = new Color(214, 221, 228);
    private static final Color BRAND = new Color(14, 118, 102);
    private static final Color ALERT = new Color(176, 24, 24);
    private static final Color CRITICAL_BG = new Color(253, 226, 226);
    private static final Color HEADER_BG = new Color(242, 245, 247);

    private static final Font TITLE = font(15, Font.BOLD, INK);
    private static final Font SUBTITLE = font(9, Font.NORMAL, MUTED);
    private static final Font SECTION = font(10.5f, Font.BOLD, BRAND);
    private static final Font LABEL = font(8, Font.NORMAL, MUTED);
    private static final Font VALUE = font(9, Font.NORMAL, INK);
    private static final Font VALUE_BOLD = font(9, Font.BOLD, INK);
    private static final Font ABNORMAL = font(9, Font.BOLD, ALERT);
    private static final Font TH = font(8, Font.BOLD, MUTED);
    private static final Font SMALL = font(7.5f, Font.NORMAL, MUTED);

    private final LabOrderService orderService;
    private final Clock clock;
    private final String hospitalName;
    private final String hospitalAddress;
    private final DateTimeFormatter timeFormat;

    public ReportService(LabOrderService orderService, Clock clock,
                         @Value("${lab.report.hospital-name:JBikas Hospital}") String hospitalName,
                         @Value("${lab.report.hospital-address:Department of Laboratory Medicine}") String hospitalAddress) {
        this.orderService = orderService;
        this.clock = clock;
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.timeFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(clock.getZone());
    }

    public record Report(String filename, byte[] pdf) {
    }

    /** Released reports for anyone allowed to read them; drafts (watermarked) only for lab staff. */
    @Transactional(readOnly = true)
    public Report render(Long orderId, boolean allowPreliminary) {
        LabOrder order = orderService.find(orderId);
        boolean released = order.getStatus() == LabOrderStatus.RELEASED;
        if (!released && !allowPreliminary) {
            throw new com.hospital.lab.exception.BusinessRuleException("The report is not released yet.");
        }
        if (order.getStatus() == LabOrderStatus.CANCELLED) {
            throw new com.hospital.lab.exception.BusinessRuleException("This order was cancelled; there is no report.");
        }
        return new Report(order.getOrderNumber() + (released ? "" : "-PRELIMINARY") + ".pdf",
                render(order, orderService.amendmentsFor(order), !released));
    }

    byte[] render(LabOrder order, Map<Long, List<ResultAmendment>> amendments, boolean preliminary) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 56);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageDecorations(order, preliminary));
            doc.addTitle("Lab report " + order.getOrderNumber());
            doc.addAuthor(hospitalName);
            doc.open();

            header(doc);
            patientBlock(doc, order);
            for (LabOrderItem item : order.getItems()) {
                testTable(doc, item, amendments);
            }
            footnotes(doc, order, amendments);
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not render report " + order.getOrderNumber(), e);
        }
        return out.toByteArray();
    }

    // ---------------------------------------------------------------- sections

    private void header(Document doc) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{3, 2});
        t.setWidthPercentage(100);
        PdfPCell left = cell(null, Rectangle.NO_BORDER);
        left.addElement(new Paragraph(pdf(hospitalName), TITLE));
        left.addElement(new Paragraph(pdf(hospitalAddress), SUBTITLE));
        t.addCell(left);
        PdfPCell right = cell(null, Rectangle.NO_BORDER);
        Paragraph title = new Paragraph("LABORATORY REPORT", font(11, Font.BOLD, BRAND));
        title.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(title);
        t.addCell(right);
        doc.add(t);
        doc.add(rule());
    }

    private void patientBlock(Document doc, LabOrder o) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1.1f, 2.2f, 1.1f, 2.2f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setSpacingAfter(8);
        pair(t, "Patient", o.getPatientName(), true);
        pair(t, "Order no.", o.getOrderNumber(), true);
        pair(t, "MRN", o.getMrn(), false);
        pair(t, "Ordered", format(o.getOrderedAt()), false);
        pair(t, "Age / Sex", o.getPatientAgeYears() + " y / " + capitalise(o.getPatientGender()), false);
        pair(t, "Collected", format(o.getSampleCollectedAt()), false);
        pair(t, "Referred by", o.getOrderingDoctorName(), false);
        pair(t, "Reported", format(o.getReleasedAt()), false);
        if (o.getClinicalNotes() != null) {
            PdfPCell label = cell(new Phrase("Clinical notes", LABEL), Rectangle.NO_BORDER);
            t.addCell(label);
            PdfPCell notes = cell(new Phrase(pdf(o.getClinicalNotes()), VALUE), Rectangle.NO_BORDER);
            notes.setColspan(3);
            t.addCell(notes);
        }
        doc.add(t);
    }

    private void testTable(Document doc, LabOrderItem item, Map<Long, List<ResultAmendment>> amendments) throws DocumentException {
        Paragraph heading = new Paragraph();
        heading.add(new Chunk(pdf(item.getTestName()), SECTION));
        if (item.getSpecimen() != null) {
            heading.add(new Chunk("   Specimen: " + pdf(item.getSpecimen()), SMALL));
        }
        heading.setSpacingBefore(8);
        heading.setSpacingAfter(3);
        heading.setKeepTogether(true);
        doc.add(heading);

        PdfPTable t = new PdfPTable(new float[]{3.4f, 1.6f, 0.8f, 1.5f, 2.2f});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String h : new String[]{"Investigation", "Result", "Flag", "Unit", "Reference range"}) {
            PdfPCell c = cell(new Phrase(h.toUpperCase(), TH), Rectangle.BOTTOM);
            c.setBackgroundColor(HEADER_BG);
            t.addCell(c);
        }
        for (LabResult r : item.getResults()) {
            boolean amended = amendments.containsKey(r.getId());
            if (r.getValueType() == ValueType.NARRATIVE) {
                if (!r.hasValue()) {
                    continue;
                }
                t.addCell(cell(new Phrase(pdf(r.getParameterName()) + (amended ? " *" : ""), VALUE_BOLD), Rectangle.BOTTOM));
                PdfPCell text = cell(new Phrase(pdf(r.getTextValue()), VALUE), Rectangle.BOTTOM);
                text.setColspan(4);
                t.addCell(text);
                continue;
            }
            ResultFlag flag = r.getFlag();
            boolean abnormal = flag != null && flag.isAbnormal();
            boolean critical = flag != null && flag.isCritical();
            Font valueFont = abnormal ? ABNORMAL : VALUE;
            Color bg = critical ? CRITICAL_BG : null;

            t.addCell(shade(cell(new Phrase(pdf(r.getParameterName()) + (amended ? " *" : ""), VALUE), Rectangle.BOTTOM), bg));
            t.addCell(shade(cell(new Phrase(r.hasValue() ? pdf(r.displayValue()) : "—", valueFont), Rectangle.BOTTOM), bg));
            t.addCell(shade(cell(new Phrase(flagCode(flag), valueFont), Rectangle.BOTTOM), bg));
            t.addCell(shade(cell(new Phrase(pdf(r.getUnit()), VALUE), Rectangle.BOTTOM), bg));
            t.addCell(shade(cell(new Phrase(pdf(LabMapper.referenceText(r)), VALUE), Rectangle.BOTTOM), bg));
        }
        doc.add(t);
    }

    private void footnotes(Document doc, LabOrder o, Map<Long, List<ResultAmendment>> amendments) throws DocumentException {
        doc.add(new Paragraph(" ", SMALL));
        if (o.getCriticalNotifiedTo() != null) {
            Paragraph p = new Paragraph("Critical values were communicated to " + pdf(o.getCriticalNotifiedTo())
                    + " before release.", font(8.5f, Font.BOLD, ALERT));
            p.setSpacingAfter(4);
            doc.add(p);
        }
        if (!amendments.isEmpty()) {
            Paragraph title = new Paragraph("* Amended results", font(8.5f, Font.BOLD, INK));
            doc.add(title);
            for (LabResult r : o.allResults()) {
                for (ResultAmendment a : amendments.getOrDefault(r.getId(), List.of())) {
                    doc.add(new Paragraph(pdf(r.getParameterName() + ": " + nullDash(a.getPreviousValue()) + " -> "
                            + nullDash(a.getNewValue()) + " (" + a.getReason() + ") by " + a.getAmendedBy() + ", "
                            + format(a.getAmendedAt())), SMALL));
                }
            }
        }
        Paragraph legend = new Paragraph("Flags: H high, L low, HH / LL critical (phoned to clinician), A abnormal. "
                + "Reference ranges apply to the patient's age and sex at the time of the order.", SMALL);
        legend.setSpacingBefore(8);
        doc.add(legend);
        if (o.getReleasedByName() != null) {
            Paragraph signed = new Paragraph("Released by " + pdf(o.getReleasedByName()) + " on " + format(o.getReleasedAt())
                    + ". This report was generated electronically.", SMALL);
            doc.add(signed);
        }
    }

    // ---------------------------------------------------------------- page decorations

    private final class PageDecorations extends PdfPageEventHelper {
        private final LabOrder order;
        private final boolean preliminary;

        PageDecorations(LabOrder order, boolean preliminary) {
            this.order = order;
            this.preliminary = preliminary;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            float y = document.bottom() - 22;
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase(pdf(order.getPatientName()) + "  |  " + order.getMrn() + "  |  " + order.getOrderNumber(), SMALL),
                    document.left(), y, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("Printed " + format(Instant.now(clock)) + "   Page " + writer.getPageNumber(), SMALL),
                    document.right(), y, 0);
            if (preliminary) {
                ColumnText.showTextAligned(writer.getDirectContentUnder(), Element.ALIGN_CENTER,
                        new Phrase("PRELIMINARY - NOT VALIDATED", font(46, Font.BOLD, new Color(236, 239, 242))),
                        (document.left() + document.right()) / 2, (document.top() + document.bottom()) / 2, 45);
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    private void pair(PdfPTable t, String label, String value, boolean bold) {
        t.addCell(cell(new Phrase(label, LABEL), Rectangle.NO_BORDER));
        t.addCell(cell(new Phrase(pdf(nullDash(value)), bold ? VALUE_BOLD : VALUE), Rectangle.NO_BORDER));
    }

    private static PdfPCell cell(Phrase phrase, int border) {
        PdfPCell c = phrase == null ? new PdfPCell() : new PdfPCell(phrase);
        c.setBorder(border);
        c.setBorderColor(RULE);
        c.setPaddingTop(3.5f);
        c.setPaddingBottom(4.5f);
        c.setPaddingLeft(3);
        c.setPaddingRight(3);
        return c;
    }

    private static PdfPCell shade(PdfPCell c, Color bg) {
        if (bg != null) {
            c.setBackgroundColor(bg);
        }
        return c;
    }

    /** A full-width brand-coloured line under the header. */
    private static PdfPTable rule() {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(BRAND);
        c.setBorderWidthBottom(1.2f);
        c.setFixedHeight(4);
        line.addCell(c);
        return line;
    }

    private static String flagCode(ResultFlag flag) {
        if (flag == null) {
            return "";
        }
        return switch (flag) {
            case HIGH -> "H";
            case LOW -> "L";
            case CRITICAL_HIGH -> "HH";
            case CRITICAL_LOW -> "LL";
            case ABNORMAL -> "A";
            default -> "";
        };
    }

    private String format(Instant instant) {
        return instant == null ? "—" : timeFormat.format(instant);
    }

    private static String nullDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    private static String capitalise(String s) {
        return s == null || s.isEmpty() ? "" : s.charAt(0) + s.substring(1).toLowerCase();
    }

    private static Font font(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    /**
     * The built-in Helvetica font only covers Latin-1 (WinAnsi). Replace the few symbols used in units
     * and ranges that it cannot draw. To print Devanagari names, embed a Unicode TTF instead.
     */
    static String pdf(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("≤", "<=").replace("≥", ">=").replace("⁶", "^6").replace("→", "->");
    }
}
