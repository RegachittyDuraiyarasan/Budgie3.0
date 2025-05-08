package com.hepl.budgie.entity.pms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PmsWorkflow {
    private String type;
    private List<String> status;
}
