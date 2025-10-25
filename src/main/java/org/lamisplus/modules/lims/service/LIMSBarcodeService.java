package org.lamisplus.modules.lims.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.lims.domain.dto.*;
import org.lamisplus.modules.lims.domain.entity.LIMSConfig;
import org.lamisplus.modules.lims.domain.mapper.LimsMapper;
import org.lamisplus.modules.lims.repository.LimsConfigRepository;
import org.lamisplus.modules.lims.repository.LimsManifestRepository;
import org.lamisplus.modules.lims.util.BarcodeGenerator;
import org.lamisplus.modules.lims.util.BarcodeQRUtil;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LIMSBarcodeService {
    String loginUrl = "/login.php";
    String testLoginUrl = "https://lims.ng/apidemo/login.php";
    String barcodeUrl = "https://lims.ng/apidemo/remote-sample-tagging/barcode.php";

    private final LimsManifestRepository limsManifestRepository;
    private final LimsConfigRepository limsConfigRepository;
    private final LimsMapper limsMapper;
    private final ObjectMapper mapper = new ObjectMapper();


    public RestTemplate GetRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jacksonConverter =
                        (MappingJackson2HttpMessageConverter) converter;

                List<MediaType> mediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                mediaTypes.add(MediaType.TEXT_HTML);
                mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                mediaTypes.add(MediaType.ALL);
                jacksonConverter.setSupportedMediaTypes(mediaTypes);
            }
        }

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        return restTemplate;
    }

    private HttpHeaders GetHTTPHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Application");
        return headers;
    }

    private LIMSLoginResponseDTO LoginToLIMS(RestTemplate restTemplate, HttpHeaders headers, LIMSConfig config) {
        LIMSLoginRequestDTO loginRequestDTO = new LIMSLoginRequestDTO();
        loginRequestDTO.setEmail(config.getConfigEmail());
        loginRequestDTO.setPassword(config.getConfigPassword());

        HttpEntity<LIMSLoginRequestDTO> loginEntity = new HttpEntity<>(loginRequestDTO, headers);
        ResponseEntity<LIMSLoginResponseDTO> loginResponse = restTemplate.exchange(testLoginUrl, HttpMethod.POST, loginEntity, LIMSLoginResponseDTO.class);
        //LOG.info("LOGIN_RESPONSE " + loginResponse.getBody());

        return loginResponse.getBody();
    }

    public ManifestDTO findById(Integer id) {
        return limsMapper.toManifestDto(limsManifestRepository.findById(id).orElse(null));
    }

    private JsonNode postBarcode(RestTemplate restTemplate, HttpHeaders headers, int ManifestId,
                                 LIMSLoginResponseDTO loginResponseDTO, LIMSConfig config, int count){
        try{
            LIMSManifestDTO manifest = limsMapper.toLimsManifestDto(findById(ManifestId));

            LIMSSsampleBarcodeRequestDTO barcodeRequest = new LIMSSsampleBarcodeRequestDTO();
            barcodeRequest.setToken(loginResponseDTO.getJwt());
            barcodeRequest.setReceivingLabID(manifest.getReceivingLabID());
            barcodeRequest.setReceivingLabName(manifest.getReceivingLabName());
            barcodeRequest.setSendingFacilityID(manifest.getSendingFacilityID());
            barcodeRequest.setSendingFacilityName(manifest.getSendingFacilityName());
            barcodeRequest.setTestType("VL");
            barcodeRequest.setSessionId(UUID.randomUUID().toString());
            barcodeRequest.setCount(count);

            HttpEntity<LIMSSsampleBarcodeRequestDTO> barcodeRequestEntity = new HttpEntity<>(barcodeRequest, headers);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    barcodeUrl,
                    HttpMethod.POST,
                    barcodeRequestEntity,
                    String.class
            );

            if (rawResponse.getStatusCode() != HttpStatus.OK) {
                    throw new RuntimeException("LIMS API Error: " + rawResponse.getStatusCodeValue());
            }

            JsonNode root = mapper.readTree(rawResponse.getBody());

            if (!"success".equalsIgnoreCase(root.path("status").asText())) {
                throw new RuntimeException(root.path("message").asText("Unknown LIMS API error"));
            }
            //TODO: save in the db. manifest Id, sessionId, serial numbers, sample id

            return root.path("serial_numbers");
        }catch (HttpStatusCodeException ex) {
            String errorBody = ex.getResponseBodyAsString();
            throw new RuntimeException(String.format(
                    "HTTP error from LIMS: Status %s, Body: %s",
                    ex.getStatusCode(),
                    errorBody
            ), ex);

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while requesting barcode serial numbers: " + ex.getMessage(), ex);
        }
    }

    public List<String> getSerialNumbers(int configId, int manifestId, int count) {
        RestTemplate restTemplate = GetRestTemplate();
        HttpHeaders headers = GetHTTPHeaders();
//        LIMSConfig config = limsConfigRepository.findById(configId).orElse(null);
//
//        assert config != null;
        LIMSConfig CONFIG = new LIMSConfig();
        CONFIG.setConfigEmail("");
        CONFIG.setConfigPassword("");

        LIMSLoginResponseDTO loginResponseDTO = LoginToLIMS(restTemplate, headers, CONFIG);
        LOG.info("loginResponse response " + loginResponseDTO.toString());
        JsonNode serialResponseDTO = postBarcode(restTemplate, headers, manifestId, loginResponseDTO,CONFIG,count);
        LOG.info("serialResponse response " + serialResponseDTO.toString());

        List<String> serialNumbers = new ArrayList<>();

        for (JsonNode sn : serialResponseDTO) {
            serialNumbers.add(sn.asText());
        }

        return serialNumbers;
    }

    public List<LIMSBarcodeResponseDTO> generateBarcodes(List<String> serialNumbers){
        return serialNumbers.stream().map(serialNumber -> {
            try{
                //String img = BarcodeQRUtil.generateBarcode(serialNumber, 400, 100);
                String img = BarcodeGenerator.generateBarcode(serialNumber, 400, 100);
                return new LIMSBarcodeResponseDTO(serialNumber, img);
            }catch(Exception ex) {
                return new LIMSBarcodeResponseDTO(serialNumber, "");
            }
        }).collect(Collectors.toList());
    }

}
