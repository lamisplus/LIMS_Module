package org.lamisplus.modules.lims.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private String message;
    private List<T> data;

    public static <T> ApiResponse<T> success(List<T> data) {
        return new ApiResponse<>("success", "Request processed successfully", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
}
