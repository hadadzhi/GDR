package ru.cdfe.gdr.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.cdfe.gdr.domain.Approximation;
import ru.cdfe.gdr.domain.Curve;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Quantity;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
class TestFitting implements CommandLineRunner {
	private final ExforService exforService;
	private final FittingService fittingService;
	
	@Autowired
	public TestFitting(ExforService exforService, FittingService fittingService) {
		this.exforService = exforService;
		this.fittingService = fittingService;
	}
	
	@Override
	public void run(String... args) throws Exception {
		final List<DataPoint> sourceData = exforService.getData("L0028002", 0, 1, 2);
		
		final Curve curve1 = Curve.builder()
			.type(FittingService.Curves.GAUSSIAN)
			.maxCrossSection(new Quantity(50.))
			.energyAtMaxCrossSection(new Quantity(25.))
			.fullWidthAtHalfMaximum(new Quantity(10.))
			.build();
		
		final Curve curve2 = Curve.builder()
			.type(FittingService.Curves.GAUSSIAN)
			.maxCrossSection(new Quantity(50.))
			.energyAtMaxCrossSection(new Quantity(25.))
			.fullWidthAtHalfMaximum(new Quantity(10.))
			.build();
		
		final Approximation approximation = Approximation.builder()
			.curves(Arrays.asList(curve1, curve2))
			.sourceData(sourceData)
			.description("Test")
			.build();
		
		final long start = System.currentTimeMillis();
		
		fittingService.fit(approximation);
		
		final long end = System.currentTimeMillis();
		
		log.info(approximation.toString());
		log.info("Time to fit: " + (end - start) + " ms");
	}
}
