package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class LABSampleMetaDataDTO {
    private  long totalRecords;
    private Integer totalPages;
    private Integer pageSize;
    private Integer currentPage;
    private List<LABSampleDTO> records;
}
