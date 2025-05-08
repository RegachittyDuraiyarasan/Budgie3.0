package com.hepl.budgie.entity.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.entity.master.MasterFormOptions;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "m_settings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MasterFormSettings {
    @Id
    private String id;
    private String referenceName;
    @Builder.Default
    private List<MasterFormOptions> options = new ArrayList<>();
}
