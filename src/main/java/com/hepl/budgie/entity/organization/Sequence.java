package com.hepl.budgie.entity.organization;

import lombok.Data;

@Data
public class Sequence {
    private String roleType;
    private String roleSequence;
    private String autoGenerationStatus;
    private String itDeclaration;
}
