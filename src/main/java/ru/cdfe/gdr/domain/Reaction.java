package ru.cdfe.gdr.domain;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Reaction {
    private Nucleus target;
    private Nucleus product;
    private String incident;
    private String outgoing;
}
