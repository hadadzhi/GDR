package ru.cdfe.gdr.domain;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Reaction {
	@NotNull
	@Valid
	private Nucleus target;
	
	@NotNull
	@Valid
	private Nucleus product;
	
	@NotBlank
	private String incident;
	
	@NotBlank
	private String outgoing;
}
