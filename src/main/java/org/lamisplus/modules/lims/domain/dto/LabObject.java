package org.lamisplus.modules.lims.domain.dto;
import lombok.Data;

import java.io.Serializable;
@Data
public class LabObject implements Serializable
{
    private String labNo;
    private String name;
}
