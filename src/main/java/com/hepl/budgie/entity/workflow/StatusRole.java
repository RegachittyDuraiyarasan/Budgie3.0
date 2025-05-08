package com.hepl.budgie.entity.workflow;

public enum StatusRole {
  TRUE("True"),FALSE("False");


  public final String label;

  private StatusRole(String label) {
      this.label = label;
  }
}
