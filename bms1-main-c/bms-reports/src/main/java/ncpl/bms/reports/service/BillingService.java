package ncpl.bms.reports.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.Map;
import java.io.ByteArrayOutputStream;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.time.Month;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Table;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.core.io.ClassPathResource;
import com.itextpdf.layout.element.Image;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;


@Service
public class BillingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TenantAndEnergyMeterService tenantAndEnergyMeterService;

    private String convertNumberToWords(int num) {
        final String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen" };

        final String[] tens = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

        if (num == 0) {
            return "Zero";
        }

        if (num < 20) {
            return units[num];
        }

        if (num < 100) {
            return tens[num / 10] + (num % 10 != 0 ? " " + units[num % 10] : "");
        }

        if (num < 1000) {
            return units[num / 100] + " Hundred" + (num % 100 != 0 ? " and " + convertNumberToWords(num % 100) : "");
        }

        if (num < 100000) {
            return convertNumberToWords(num / 1000) + " Thousand" + (num % 1000 != 0 ? " " + convertNumberToWords(num % 1000) : "");
        }

        if (num < 10000000) {
            return convertNumberToWords(num / 100000) + " Lakh" + (num % 100000 != 0 ? " " + convertNumberToWords(num % 100000) : "");
        }

        return convertNumberToWords(num / 10000000) + " Crore" + (num % 10000000 != 0 ? " " + convertNumberToWords(num % 10000000) : "");
    }


    public byte[] generateBillPdf(Long tenantId, String month, String year) {
        // Convert numeric month to full name
        String monthName;
        try {
            int monthNumber = Integer.parseInt(month);
            monthName = Month.of(monthNumber).name();
            monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid month format: " + month);
        }



        float cellFontSize = 9;

        // Fetch energy usage
        Map<String, Double> energyUsage = tenantAndEnergyMeterService.getEnergyMetersForTenant(tenantId.intValue(), month, year);
        double ebUsage = BigDecimal.valueOf(energyUsage.get("totalEbKwh")).setScale(2, RoundingMode.HALF_UP).doubleValue();
        double dgUsage = BigDecimal.valueOf(energyUsage.get("totalDgKwh")).setScale(2, RoundingMode.HALF_UP).doubleValue();

        Map<String, Double> energyUsageCommonArea = tenantAndEnergyMeterService.getEnergyMetersForTenant(1, month, year);
        double ebUsageCommonArea = BigDecimal.valueOf(energyUsageCommonArea.get("totalEbKwh")).setScale(2, RoundingMode.HALF_UP).doubleValue();
        double dgUsageCommonArea = BigDecimal.valueOf(energyUsageCommonArea.get("totalDgKwh")).setScale(2, RoundingMode.HALF_UP).doubleValue();

        // Fetch static information from the database
        String staticInfoSql = "SELECT TOP 1 bill_title, client_name, email, phone_number, eb_tarrif, dg_tarrif, total_building_area  FROM static_information";
        Map<String, Object> staticInfo = jdbcTemplate.queryForMap(staticInfoSql);

        String billTitle = (String) staticInfo.get("bill_title");
        String clientName = (String) staticInfo.get("client_name");
        String email = (String) staticInfo.get("email");
        String phoneNumber = (String) staticInfo.get("phone_number");
        double totalBuildingArea = getDoubleValue(staticInfo.get("total_building_area"));
        double ebTariff = ((BigDecimal) staticInfo.get("eb_tarrif")).doubleValue();
        double dgTariff = ((BigDecimal) staticInfo.get("dg_tarrif")).doubleValue();

        // Calculate total amounts
        double ebAmount = ebUsage * ebTariff;
        double dgAmount = dgUsage * dgTariff;
        double totalAmount = ebAmount + dgAmount;
        double ebAmountCommonArea = ebUsageCommonArea * ebTariff;
        double dgAmountCommonArea = dgUsageCommonArea * dgTariff;
        double totalAmountCommonArea = ebAmountCommonArea + dgAmountCommonArea;

        // Fetch tenant details based on tenantId
        String tenantInfoSql = "SELECT name, person_of_contact, email, mobile_number, unit_address,  area_occupied FROM tenant WHERE id = ? ";
        Map<String, Object> tenantInfo = jdbcTemplate.queryForMap(tenantInfoSql, tenantId);
        double tenantArea = getDoubleValue(tenantInfo.get("area_occupied"));
        double tenantShareCommonArea = (totalAmountCommonArea / totalBuildingArea) * tenantArea;
        double netPayableAmount = totalAmount + tenantShareCommonArea;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);


            // Add "Company Name" in bold on the top-left corner
            Paragraph companyName = new Paragraph("Vajram CMR One")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10)
                    .setFixedPosition(30, pdf.getDefaultPageSize().getHeight() - 40, 200); // Adjust X and Y position
            document.add(companyName);

            PdfFont font = PdfFontFactory.createFont("C:/Windows/Fonts/arial.ttf", PdfEncodings.IDENTITY_H);
            document.setFont(font);

            // Add the logo in the top-right corner
            try {
                ImageData logoData = ImageDataFactory.create(new ClassPathResource("static/images/logo1.png").getURL());
                Image logo = new Image(logoData);

                // Scale image properly to fit within the header
                logo.scaleToFit(100, 100);

                // Get page size
                float pageWidth = pdf.getDefaultPageSize().getWidth();
                float pageHeight = pdf.getDefaultPageSize().getHeight();

                // Positioning logo correctly at top-right
                float marginX = 20; // Adjust margin from right
                float marginY = 10; // Adjust margin from top
                float logoX = pageWidth - logo.getImageScaledWidth() - marginX;
                float logoY = pageHeight - logo.getImageScaledHeight() - marginY;

                // Set fixed position
                logo.setFixedPosition(logoX, logoY);

                document.add(logo);
            } catch (Exception e) {
                throw new RuntimeException("Error loading logo image", e);
            }


            // Add centered title
            Paragraph title = new Paragraph(billTitle + " - " + monthName + " " + year)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setMarginBottom(20);
            document.add(title);

            // Add Client Details section
//            document.add(new Paragraph("Client Details:").setFontSize(10).setMarginBottom(10));
//            Table clientTable = new Table(UnitValue.createPercentArray(new float[]{2, 4}))
//                    .setWidth(UnitValue.createPercentValue(100));
//            clientTable.addCell(new Cell().add(new Paragraph("Name").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//            clientTable.addCell(new Cell().add(new Paragraph(clientName)).setFontSize(cellFontSize));
//            clientTable.addCell(new Cell().add(new Paragraph("Email").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//            clientTable.addCell(new Cell().add(new Paragraph(email)).setFontSize(cellFontSize));
//            clientTable.addCell(new Cell().add(new Paragraph("Phone Number").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//            clientTable.addCell(new Cell().add(new Paragraph(phoneNumber)).setFontSize(cellFontSize));
//            document.add(clientTable);


            // Add Tenant Details section
            document.add(new Paragraph("Tenant Details:")
                    .setFontSize(10)
                    .setMarginTop(10)
                    .setBold()
                    .setMarginBottom(2));
            Table tenantTable = new Table(UnitValue.createPercentArray(new float[]{2, 4}))
                    .setWidth(UnitValue.createPercentValue(100));
            tenantTable.addCell(new Cell().add(new Paragraph("Name").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            tenantTable.addCell(new Cell().add(new Paragraph((String) tenantInfo.get("name")).setFontSize(cellFontSize)));
            tenantTable.addCell(new Cell().add(new Paragraph("Person of Contact").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            tenantTable.addCell(new Cell().add(new Paragraph((String) tenantInfo.get("person_of_contact")).setFontSize(cellFontSize)));
            tenantTable.addCell(new Cell().add(new Paragraph("Email").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            tenantTable.addCell(new Cell().add(new Paragraph((String) tenantInfo.get("email")).setFontSize(cellFontSize)));
            tenantTable.addCell(new Cell().add(new Paragraph("Phone Number").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            tenantTable.addCell(new Cell().add(new Paragraph((String) tenantInfo.get("mobile_number")).setFontSize(cellFontSize)));
            tenantTable.addCell(new Cell().add(new Paragraph("Unit Address").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            tenantTable.addCell(new Cell().add(new Paragraph((String) tenantInfo.get("unit_address")).setFontSize(cellFontSize)));
            document.add(tenantTable);

            // Add Bill Details section
            document.add(new Paragraph("Bill Details:")
                    .setFontSize(10)
                    .setMarginTop(20)
                    .setBold()
                    .setMarginBottom(2));

            // Define the table structure
            Table billTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2, 3}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Add headers for the main columns
            billTable.addCell(new Cell(2, 1).add(new Paragraph("Description").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            billTable.addCell(new Cell(1, 2).add(new Paragraph("EB").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            billTable.addCell(new Cell(1, 2).add(new Paragraph("DG").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            billTable.addCell(new Cell(2, 1).add(new Paragraph("Amount(₹)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            // Add sub-headers for EB and DG
            billTable.addCell(new Cell().add(new Paragraph("Usage (kWh)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            billTable.addCell(new Cell().add(new Paragraph("Tariff (₹)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            billTable.addCell(new Cell().add(new Paragraph("Usage (kWh)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            billTable.addCell(new Cell().add(new Paragraph("Tariff (₹)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            // Add data rows for EB and DG
            billTable.addCell(new Cell().add(new Paragraph("Electricity Consumption ").setFontSize(cellFontSize)));
            billTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", ebUsage)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            billTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", ebTariff)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            billTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", dgUsage)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            billTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", dgTariff)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            billTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", totalAmount)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            document.add(billTable);
            // common area details-----------------------------

            document.add(new Paragraph("Common Area Bill Details:")
                    .setFontSize(10)
                    .setBold()
                    .setMarginTop(20)
                    .setMarginBottom(2));

            // Define the table structure
            Table commonAreabillTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2, 3}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Add headers for the main columns
            commonAreabillTable.addCell(new Cell(2, 1).add(new Paragraph("Description").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            commonAreabillTable.addCell(new Cell(1, 2).add(new Paragraph("EB").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            commonAreabillTable.addCell(new Cell(1, 2).add(new Paragraph("DG").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            commonAreabillTable.addCell(new Cell(2, 1).add(new Paragraph("Amount(₹)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            // Add sub-headers for EB and DG
            commonAreabillTable.addCell(new Cell().add(new Paragraph("Usage (kWh)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            commonAreabillTable.addCell(new Cell().add(new Paragraph("Tariff (₹)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            commonAreabillTable.addCell(new Cell().add(new Paragraph("Usage (kWh)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            commonAreabillTable.addCell(new Cell().add(new Paragraph("Tariff (₹)").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            // Add data rows for EB and DG
            commonAreabillTable.addCell(new Cell().add(new Paragraph("Electricity Consumption of common Area").setFontSize(cellFontSize)));
            commonAreabillTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", ebUsageCommonArea)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            commonAreabillTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", ebTariff)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            commonAreabillTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", dgUsageCommonArea)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            commonAreabillTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", dgTariff)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            commonAreabillTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f ", totalAmountCommonArea)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            commonAreabillTable.addCell(new Cell(1, 5).add(new Paragraph("Net Payble Ammount Of CommonArea").setFontSize(cellFontSize))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT));
            commonAreabillTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", tenantShareCommonArea)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));

            document.add(commonAreabillTable);

            // Add Final Net Payable Amount

            document.add(new Paragraph("Net Payable Amount:").setFontSize(10).setMarginTop(20).setBold().setMarginBottom(2));
            Table netPayableTable = new Table(UnitValue.createPercentArray(new float[]{5, 1}))
                    .setWidth(UnitValue.createPercentValue(100));
            netPayableTable.addCell(new Cell().add(new Paragraph("Tenant Unit  Consumption (₹)").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            netPayableTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", totalAmount)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            netPayableTable.addCell(new Cell().add(new Paragraph("Tenant Share of Common Area (₹)").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            netPayableTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", tenantShareCommonArea)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            netPayableTable.addCell(new Cell().add(new Paragraph("Total Amount (₹)").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            netPayableTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", netPayableAmount)).setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT)));
            netPayableTable.addCell(new Cell().add(new Paragraph("GST (18%) (₹)").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            double taxRate = 18.0; // 18% GST
            double taxAmount = netPayableAmount * (taxRate / 100);
            double finalAmountWithTax = netPayableAmount + taxAmount;

// Extract the decimal part after two decimal places
            double roundedValue = Math.round(finalAmountWithTax * 100.0) / 100.0;
            int roundedIntPart = (int) roundedValue;
            double decimalPart = roundedValue - roundedIntPart;

// If the decimal part is greater than 0.50, round up
            if (decimalPart > 0.50) {
                finalAmountWithTax = Math.ceil(finalAmountWithTax);
            } else {
                finalAmountWithTax = Math.floor(finalAmountWithTax);
            }

// Format without showing .00 if it's a whole number
            String finalFormattedAmount = (finalAmountWithTax % 1 == 0)
                    ? String.format("%,.0f", finalAmountWithTax)  // No decimal places for whole numbers
                    : String.format("%,.2f", finalAmountWithTax); // Show two decimal places otherwise

            netPayableTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", taxAmount)))
                    .setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT));

            netPayableTable.addCell(new Cell().add(new Paragraph("Net Payable Amount (₹)").setFontSize(cellFontSize)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            netPayableTable.addCell(new Cell().add(new Paragraph(finalFormattedAmount))
                    .setFontSize(cellFontSize).setTextAlignment(TextAlignment.RIGHT));

            document.add(netPayableTable);

// **Amount in Words**
            document.add(new Paragraph("\nAmount in Words: " + convertNumberToWords((int) finalAmountWithTax) + " Only")
                    .setBold().setFontSize(10));

            //-------------------------------------------------


            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{4, 2}))
                    .setWidth(UnitValue.createPercentValue(100));


            document.add(summaryTable);
            document.add(new Paragraph("\n"));

            // **Terms & Conditions (Inside a Box)**
            Table termsTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100));

// **Title Row**
            termsTable.addCell(new Cell()
                    .add(new Paragraph("Terms & Conditions").setBold().setFontSize(8))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(Border.NO_BORDER));

// **Condition 1**
            termsTable.addCell(new Cell()
                    .add(new Paragraph("1. If any discrepancy is noted in the above, kindly inform within seven days to "
                            + clientName + " at " + email)
                            .setFontSize(8))
                    .setBorder(Border.NO_BORDER));


// **Condition 2**
            termsTable.addCell(new Cell()
                    .add(new Paragraph("2. Payment to be made in favour of ______________________ by Account Payee Cheque / Demand Draft / Par Order Payable at ______________.")
                            .setFontSize(8))
                    .setBorder(Border.NO_BORDER));


            // **Condition 3**
            // Add Bank Details
            termsTable.addCell(new Cell()
                    .add(new Paragraph("3. Bank Details \nAccount No: ______________________   Account Name: ______________________   IFSC Code: ______________________  ")
                            .setFontSize(8))
                    .setBorder(Border.NO_BORDER));




            document.add(termsTable);

// Get page width and bottom margin
            float pageWidth = pdf.getDefaultPageSize().getWidth();
            float bottomMargin = 15;  // Space from the bottom of the page
            float rightMargin = 30;  // Space from the right edge

// Create italic font
            PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

// Create footer paragraph
            Paragraph footer = new Paragraph("Bill generated by Neptune Control Pvt Ltd")
                    .setFont(italicFont)  // Set italic font
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT) // Align text to the right
                    .setFixedPosition(pageWidth - rightMargin - 180, bottomMargin, 180);  // Adjusted for perfect alignment

// Add the footer to the document
            document.add(footer);


            // Close document and return as byte array
            document.close();




            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }


    private double getDoubleValue(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else {
            throw new RuntimeException("Unexpected type: " + value.getClass().getName());
        }
    }
}
