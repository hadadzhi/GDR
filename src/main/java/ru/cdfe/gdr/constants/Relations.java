package ru.cdfe.gdr.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Relations {
	public static final String RECORD = "record";
	public static final String RECORD_COLLECTION = "records";
	
	public static final String CREATE_RECORD_FROM_EXFOR = "createRecordFromExfor";
	public static final String CREATE_APPROXIMATION = "createApproximation";
}
