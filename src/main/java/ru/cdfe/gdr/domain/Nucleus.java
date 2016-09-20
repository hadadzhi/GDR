package ru.cdfe.gdr.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Nucleus {
    private int charge;
    private int mass;
    
    public Nucleus(int charge, int mass) {
        this.charge = charge;
        this.mass = mass;
    }
}
