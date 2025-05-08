package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RegimeEnum {
    OLD_REGIME("Old Regime"),
    NEW_REGIME("New Regime");

    private final String label;
}
