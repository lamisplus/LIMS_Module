package org.lamisplus.modules.lims.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.audit4j.core.util.Log;
import org.lamisplus.modules.lims.domain.dto.*;
import org.lamisplus.modules.lims.domain.entity.LIMSManifest;
import org.lamisplus.modules.lims.domain.entity.LIMSResult;
import org.lamisplus.modules.lims.domain.entity.LIMSSample;
import org.lamisplus.modules.lims.domain.entity.LIMSTest;
import org.lamisplus.modules.lims.service.LimsManifestService;
import org.lamisplus.modules.lims.service.LimsResultService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/lims")
public class LimsResultController {
    private final LimsResultService limsResultService;
    private final LimsManifestService limsManifestService;

//    @PostMapping("/results")
//    public List<LIMSResult> SaveResults(@RequestBody List<LIMSResult> results){
//
//        return limsResultService.SaveAll(results);
//    }

    @PutMapping("/results/{id}")
    public LIMSResult UpdateResult(@PathVariable int id, @RequestBody LIMSResult result) {
        return limsResultService.Update(result, id);
    }

    @DeleteMapping("/results/{id}")
    public String DeleteResult(@PathVariable int id) {
        return limsResultService.Delete(id);
    }

    @GetMapping("/results/{id}")
    public LIMSResult GetResultById(@PathVariable int id){
        return limsResultService.FindById(id);
    }

    @GetMapping("/results/manifests/{id}")
    public ManifestDTO GetResultByManifestId(@PathVariable int id){
        return limsResultService.FindResultsByManifestId(id);
    }

    @GetMapping("/sample/result/{sampleId}")
    public LIMSResult GetResultBySampleId(@PathVariable String sampleId){
        if(sampleId.contains("-")) {
            sampleId = sampleId.replace("-", "/");
        }
        return limsResultService.getSampleResultBySampleId(sampleId);
    }

    @GetMapping("/results/sample/{sampleId}")
    public LIMSTest GetPatientIDBySampleId(@PathVariable String sampleId){
        if(sampleId.contains("-")) {
            sampleId = sampleId.replace("-", "/");
        }
        return limsResultService.getPatientIDBySampleID(sampleId);
    }

    PdfPCell makeSection(String titleText, Font sectionFont) {
        PdfPCell cell = new PdfPCell(new Phrase( titleText, sectionFont));
        cell.setBackgroundColor(new BaseColor(0, 102, 204));
        cell.setColspan(4);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    PdfPCell makeCell(String text, Font font, boolean shaded) {
       PdfPCell cell = new PdfPCell(new Phrase(text, font));
       cell.setPadding(5f);
       cell.setBorderColor(BaseColor.LIGHT_GRAY);
       if(shaded){
           cell.setBackgroundColor(new BaseColor(245, 245, 245));
       }
       return cell;
    }

    @GetMapping("/bulk-download")
    public ResponseEntity<Resource> bulkDownload(
            @RequestParam int manifestId,
            @RequestParam int configId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) throws IOException {

        Optional<LIMSManifest> manifest = limsManifestService.getManifestById(manifestId);

        if ( manifest.isPresent()) {

            LIMSResultsResponseDTO response = limsManifestService.DownloadResultsFromLIMS(manifestId, configId);
            List<LIMSResultDTO> allResults = response.getViralLoadTestReport();

            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, allResults.size());

            if (fromIndex >= allResults.size()) {
                return ResponseEntity.noContent().build();
            }

            List<LIMSResultDTO> paginated = allResults.subList(fromIndex, toIndex);

            String BASE_DIR = "runtime/lims/results";
            Path basePath = Paths.get(BASE_DIR);

            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }

            String dateStamp = LocalDate.now().toString();
            String zipFileName = "Lims_results_" + response.getManifestID() + "_" + dateStamp + ".zip";
            Path zipPath = basePath.resolve(zipFileName);

            try(ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))){
                for (LIMSResultDTO result : paginated) {
                    Optional<LIMSSampleProjection> sample = limsManifestService.getSampleById(result.getSampleID(), result.getTestID());

                    if (sample.isPresent()) {
                        ByteArrayOutputStream pdfBoas = new ByteArrayOutputStream();
                        Document document = new Document(PageSize.A4, 36, 36, 60, 36);
                        PdfWriter.getInstance(document, pdfBoas);
                        document.open();

                        Image logo = Image.getInstance(Objects.requireNonNull(getClass().getResource("/logo.png")));
                        logo.scaleToFit(80, 80);
                        logo.setAlignment(Element.ALIGN_LEFT);

                        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
                        Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GRAY);

                        Paragraph title = new Paragraph("NISRN VIRAL LOAD LABORATORY REPORT", titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
//                        title.setSpacingAfter(5f);
//                        document.add(title);

                        Paragraph subTitle = new Paragraph(response.getReceivingFacilityName(), subTitleFont);
                        subTitle.setAlignment(Element.ALIGN_CENTER);
//                        subTitle.setSpacingAfter(5f);
//                        document.add(subTitle);

                        PdfPTable header = new PdfPTable(2);
                        header.setWidthPercentage(100);
                        header.setWidths(new float[]{1.5f, 5f});
                        header.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                        PdfPCell logoCell = new PdfPCell(logo);
                        logoCell.setBorder(Rectangle.NO_BORDER);
                        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        header.addCell(logoCell);

                        PdfPCell titleCell = new PdfPCell();
                        titleCell.setBorder(Rectangle.NO_BORDER);
                        titleCell.addElement(title);
                        titleCell.addElement(subTitle);
                        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        header.addCell(titleCell);

                        document.add(header);

                        LineSeparator line = new LineSeparator();
                        line.setOffset(-2);
                        document.add(line);
                        document.add(Chunk.NEWLINE);

                        PdfPTable table = new PdfPTable(4);
                        table.setWidthPercentage(100);
                        table.setSpacingBefore(5f);
                        table.setSpacingAfter(5f);
                        table.setWidths(new float[]{2f, 3f, 2f, 3f});

                        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
                        Font labelFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.DARK_GRAY);
                        Font valueFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);

                        table.addCell(makeSection("PATIENT INFORMATION", sectionFont));
                        table.addCell(makeCell("First Name:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getFirstName()), valueFont, true));
                        table.addCell(makeCell("Surname:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getSurName()), valueFont,true));

                        table.addCell(makeCell("Sex:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getSex()), valueFont,false));
                        table.addCell(makeCell("Age:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(sample.get().getAge()), valueFont,false));

                        table.addCell(makeCell("Date Of Birth:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getDateOfBirth()), valueFont,true));
                        table.addCell(makeCell("Unique client ID:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(sample.get().getUniqueId()), valueFont,true));

                        table.addCell(makeCell("Hospital number:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getPatientID().get(0).get("idNumber")), valueFont,false));
                        table.addCell(makeCell("Patient ID:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getPatientID().get(1).get("idNumber")), valueFont,false));

                        table.addCell(makeSection("PCR DETAILS", sectionFont));
                        table.addCell(makeCell("Receiving PCR Lab:", labelFont, true));
                        table.addCell(makeCell(String.valueOf(manifest.get().getReceivingLabName()), valueFont,true));
                        table.addCell(makeCell("Receiving PCR Lab number:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(manifest.get().getReceivingLabID()), valueFont,true));

                        table.addCell(makeCell("Manifest Record ID:", labelFont, false));
                        table.addCell(makeCell(String.valueOf(response.getManifestID()), valueFont,false));
                        table.addCell(makeCell("PCR Lab Sample Number:", labelFont, false));
                        table.addCell(makeCell(String.valueOf(result.getPcrLabSampleNumber()), valueFont,false));

                        table.addCell(makeSection("SAMPLE DETAILS", sectionFont));
                        table.addCell(makeCell("Sample Id:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getSampleID()), valueFont,true));
                        table.addCell(makeCell("Sample type:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(sample.get().getSampleType()), valueFont,true));

                        table.addCell(makeCell("Sample collected by:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(sample.get().getSampleCollectedBy()), valueFont,false));
                        table.addCell(makeCell("Sample Collection date/time:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(sample.get().getSampleCollectionDate()), valueFont,false));

                        table.addCell(makeCell("Sample Package By:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(manifest.get().getSamplePackagedBy()), valueFont,true));
                        table.addCell(makeCell("Date Sample Received At PCR Lab:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getDateSampleReceivedAtPCRLab()), valueFont,true));

                        table.addCell(makeCell("Sample Status:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getSampleStatus()), valueFont,false));
                        table.addCell(makeCell("Sample Testable:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getSampleTestable()), valueFont,false));

                        table.addCell(makeCell("Ordered by:", labelFont, true));
                        table.addCell(makeCell(String.valueOf(sample.get().getSampleOrderedBy()), valueFont,true));
                        table.addCell(makeCell("Ordered date:", labelFont, true));
                        table.addCell(makeCell(String.valueOf(sample.get().getSampleOrderDate()), valueFont,true));

                        table.addCell(makeSection("TEST DETAILS", sectionFont));
                        table.addCell(makeCell("Test type:", labelFont,true));
                        table.addCell(makeCell("Viral Load", valueFont,true));
                        table.addCell(makeCell("VL indication:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(sample.get().getIndicationVLTest()), valueFont,true));

                        table.addCell(makeCell("Tested by:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getTestedBy()), valueFont,false));
                        table.addCell(makeCell("Tested date:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getAssayDate()), valueFont,false));

                        table.addCell(makeCell("Assay By:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getTestedBy()), valueFont,false));
                        table.addCell(makeCell("Assay Date:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getAssayDate()), valueFont,false));

                        table.addCell(makeCell("Result Date:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getResultDate()), valueFont,true));
                        table.addCell(makeCell("Test Result:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getTestResult()), valueFont,true));

                        table.addCell(makeCell("Date Transferred Out:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getDate_Transferred_Out()), valueFont,false));
                        table.addCell(makeCell("Transfer Status:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getTransferStatus()), valueFont,false));

                        table.addCell(makeCell("Secondary PCR Lab ID:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getSecondary_PCR_Lab_ID()), valueFont,true));
                        table.addCell(makeCell("Secondary PCR Lab Name:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getSecondary_PCR_Lab_Name()), valueFont,true));

                        table.addCell(makeCell("Test number:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(sample.get().getTestID()), valueFont,false));
                        table.addCell(makeCell("", labelFont,false));
                        table.addCell(makeCell("", valueFont,false));


                        table.addCell(makeSection("APPROVAL & DISPATCH DETAILS", sectionFont));
                        table.addCell(makeCell("Date Result Dispatched:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getDateResultDispatched()), valueFont,true));
                        table.addCell(makeCell("Other Rejection Reason:", labelFont,true));
                        table.addCell(makeCell(String.valueOf(result.getOtherRejectionReason()), valueFont,true));

                        table.addCell(makeCell("Approval By:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getApprovedBy()), valueFont,false));
                        table.addCell(makeCell("Approval Date:", labelFont,false));
                        table.addCell(makeCell(String.valueOf(result.getApprovalDate()), valueFont,false));

                        table.addCell(makeCell("Reviewed by:", labelFont,true));
                        table.addCell(makeCell("----------------------------", valueFont,true));
                        table.addCell(makeCell("Signature:", labelFont,true));
                        table.addCell(makeCell("----------------------------", valueFont,true));

                        document.add(table);

                        document.add(Chunk.NEWLINE);


                        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.DARK_GRAY);
                        Paragraph footer = new Paragraph("Generated by LAMISPlus EMR - Confidential " + dateStamp, footerFont);
                        footer.setAlignment(Element.ALIGN_CENTER);
//                        footer.setSpacingBefore(20f);
                        document.add(footer);

                        document.close();

                        String filename = "LIMS_Result_" + result.getSampleID().replace("/", "_") + "_" +
                                result.getTestID() + ".pdf";
                        zos.putNextEntry(new ZipEntry(filename));
                        zos.write(pdfBoas.toByteArray());
                        zos.closeEntry();
                    }
                }
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }

//            byte[] zipBytes = Files.readAllBytes(zipPath);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDispositionFormData("attachment", zipFileName);
//            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
            if (!Files.exists(zipPath)) {
                throw new FileNotFoundException("Zip file not: " + zipPath);
            }

            FileSystemResource fileResource = new FileSystemResource(zipPath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .contentLength(fileResource.contentLength())
                    .body(fileResource);
        }

        return null;
    }
}
