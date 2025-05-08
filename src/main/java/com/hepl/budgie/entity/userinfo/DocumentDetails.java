package com.hepl.budgie.entity.userinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDetails {
    private String folderName;
    private String fileName;
}
