package ru.cdfe.gdr.exfor;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
public class ExforDataKey extends ExforDataHeaderKey {
	@Column(name = "row")
	private int row;
}
