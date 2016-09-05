package ru.cdfe.gdr.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Parameters {
	public static final String ID = "exfor_subent_number";
	
	public static final String ENERGY_COLUMN = "energy_column";
	public static final String CROSS_SECTION_COLUMN = "cross_section_column";
	public static final String CROSS_SECTION_ERROR_COLUMN = "cross_section_error_column";
}
