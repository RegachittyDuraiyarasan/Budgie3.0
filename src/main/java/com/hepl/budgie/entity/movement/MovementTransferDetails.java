package com.hepl.budgie.entity.movement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MovementTransferDetails {
   private String old;
   private String now;
   public MovementTransferDetails(String old, String now) {
      this.old = old;
      this.now = now;
   }
}
