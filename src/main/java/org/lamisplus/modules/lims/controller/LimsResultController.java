package org.lamisplus.modules.lims.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.audit4j.core.util.Log;
import org.lamisplus.modules.lims.domain.dto.LIMSLoginResponseDTO;
import org.lamisplus.modules.lims.domain.dto.LIMSResultDTO;
import org.lamisplus.modules.lims.domain.dto.LIMSResultsResponseDTO;
import org.lamisplus.modules.lims.domain.dto.ManifestDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSManifest;
import org.lamisplus.modules.lims.domain.entity.LIMSResult;
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

    PdfPCell makeCell(String text, boolean isHeader) {
        Font font = isHeader
                ? new Font(
                Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE
        ) : new Font(
                Font.FontFamily.HELVETICA, 12, Font.NORMAL
        );

        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        if (isHeader) {
            cell.setBackgroundColor(new BaseColor(63, 81, 181));
        }
        cell.setPadding(8f);
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
                    ByteArrayOutputStream pdfBoas = new ByteArrayOutputStream();
                    Document document = new Document();
                    PdfWriter.getInstance(document, pdfBoas);
                    document.open();
                    Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                    Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                    Paragraph title = new Paragraph("NISRN Viral Load Result", titleFont);
                    Paragraph subTitle = new Paragraph(response.getReceivingFacilityName(), subTitleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.setSpacingAfter(15f);
                    document.add(title);

                    subTitle.setAlignment(Element.ALIGN_CENTER);
                    subTitle.setSpacingAfter(5f);
                    document.add(subTitle);

                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10f);
                    table.setSpacingAfter(10f);
                    table.setWidths(new float[]{2f, 4f});

                    table.addCell(makeCell("Receiving PCR Lab: ", true));
                    table.addCell(makeCell(String.valueOf(manifest.get().getReceivingLabName()), false));

                    table.addCell(makeCell("First Name: ", true));
                    table.addCell(makeCell(String.valueOf(result.getFirstName()), false));

                    table.addCell(makeCell("Surname: ", true));
                    table.addCell(makeCell(String.valueOf(result.getSurName()), false));

                    table.addCell(makeCell("Sex: ", true));
                    table.addCell(makeCell(String.valueOf(result.getSex()), false));

                    table.addCell(makeCell("Patient ID: ", true));
                    table.addCell(makeCell(String.valueOf(result.getPatientID().get(1).get("idNumber")), false));

                    table.addCell(makeCell("Date Of Birth: ", true));
                    table.addCell(makeCell(String.valueOf(result.getDateOfBirth()), false));

                    table.addCell(makeCell("Sample Id: ", true));
                    table.addCell(makeCell(String.valueOf(result.getSampleID()), false));

                    table.addCell(makeCell("Sample Package By: ", true));
                    table.addCell(makeCell(String.valueOf(manifest.get().getSamplePackagedBy()), false));

                    table.addCell(makeCell("Manifest Record ID: ", true));
                    table.addCell(makeCell(String.valueOf(response.getManifestID()), false));

                    table.addCell(makeCell("Pcr Lab Sample Number: ", true));
                    table.addCell(makeCell(String.valueOf(result.getPcrLabSampleNumber()), false));

                    table.addCell(makeCell("Date Sample Received At PCR Lab: ", true));
                    table.addCell(makeCell(String.valueOf(result.getDateSampleReceivedAtPCRLab()), false));

                    table.addCell(makeCell("Sample Status: ", true));
                    table.addCell(makeCell(String.valueOf(result.getSampleStatus()), false));

                    table.addCell(makeCell("Sample Testable: ", true));
                    table.addCell(makeCell(String.valueOf(result.getSampleTestable()), false));

                    table.addCell(makeCell("Assay By: ", true));
                    table.addCell(makeCell(String.valueOf(result.getTestedBy()), false));

                    table.addCell(makeCell("Assay Date: ", true));
                    table.addCell(makeCell(String.valueOf(result.getAssayDate()), false));

                    table.addCell(makeCell("Result Date: ", true));
                    table.addCell(makeCell(String.valueOf(result.getResultDate()), false));

                    table.addCell(makeCell("Test Result: ", true));
                    table.addCell(makeCell(String.valueOf(result.getTestResult()), false));

                    table.addCell(makeCell("Approval By: ", true));
                    table.addCell(makeCell(String.valueOf(result.getApprovedBy()), false));

                    table.addCell(makeCell("Approval Date: ", true));
                    table.addCell(makeCell(String.valueOf(result.getApprovalDate()), false));

                    table.addCell(makeCell("Date Result Dispatched: ", true));
                    table.addCell(makeCell(String.valueOf(result.getDateResultDispatched()), false));

                    table.addCell(makeCell("Date Transferred Out: ", true));
                    table.addCell(makeCell(String.valueOf(result.getDate_Transferred_Out()), false));

                    table.addCell(makeCell("Transfer Status: ", true));
                    table.addCell(makeCell(String.valueOf(result.getTransferStatus()), false));

                    table.addCell(makeCell("Secondary PCR Lab ID: ", true));
                    table.addCell(makeCell(String.valueOf(result.getSecondary_PCR_Lab_ID()), false));

                    table.addCell(makeCell("Secondary PCR Lab Name: ", true));
                    table.addCell(makeCell(String.valueOf(result.getSecondary_PCR_Lab_Name()), false));

                    table.addCell(makeCell("Other Rejection Reason: ", true));
                    table.addCell(makeCell(String.valueOf(result.getOtherRejectionReason()), false));

                    document.add(table);

                    Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.DARK_GRAY);
                    Paragraph footer = new Paragraph("Generated by LAMISPlus EMR - Confidential " + dateStamp, footerFont);
                    footer.setAlignment(Element.ALIGN_CENTER);
                    footer.setSpacingBefore(20f);
                    document.add(footer);

                    document.close();

                    String filename = "LIMS_Result_" + result.getSampleID().replace("/", "_") + "_" +
                            result.getTestID() + ".pdf";
                    zos.putNextEntry(new ZipEntry(filename));
                    zos.write(pdfBoas.toByteArray());
                    zos.closeEntry();
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
