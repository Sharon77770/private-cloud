package com.sharon77770.private_cloud.data_class;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileData {
    private String fileName;
    private String fileType;
    private long fileSize;
    private boolean isFolder;

    public String getFormattedSize() {
        if (isFolder) return "-";
        return String.format("%.2f MB", (double) fileSize / (1024 * 1024));
    }
}