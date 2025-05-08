package com.hepl.budgie.dto.preonboarding;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
@Data
@RequiredArgsConstructor
public class EmpIdVerifiedDTO {
    private List<String> empIds;

}
