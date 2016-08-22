package ru.cdfe.gdr;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.cdfe.gdr.domain.*;
import ru.cdfe.gdr.repositories.RecordsRepository;

import javax.validation.Validator;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@ServletComponentScan
public class MongoGDRApplication {
	public static void main(String[] args) {
		SpringApplication.run(MongoGDRApplication.class, args);
	}
	
	@Bean
	public CurieProvider curieProvider(MongoGDRProperties conf) {
		return new DefaultCurieProvider(conf.getCurieName(), new UriTemplate(conf.getCurieUrlTemplate()));
	}
	
	@Bean
	public Validator validator() {
		return new LocalValidatorFactoryBean();
	}
	
	@Bean
	public EmbeddedServletContainerCustomizer errorPageCustomizer() {
		return container -> container.addErrorPages(new ErrorPage("/error"));
	}
	
	@Bean
	@Profile(Constants.PROFILE_OPERATOR)
	public EmbeddedServletContainerCustomizer operatorPortAndAddressCustomizer() {
		return container -> {
			container.setPort(8888);
			container.setAddress(InetAddress.getLoopbackAddress());
		};
	}
	
	@Bean
	@Profile(Constants.PROFILE_OPERATOR)
	public ApplicationRunner createTestData(RecordsRepository repo) {
		return args -> {
			repo.deleteAll();
			IntStream.range(0, 1000).parallel().forEach(value -> {
				Random rnd = new Random();
				
				List<DataPoint> source = new ArrayList<>();
				IntStream.range(0, 10).forEach(i -> source.add(
					new DataPoint(
						new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"),
						new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))));
				
				List<Approximation> approximations = new ArrayList<>();
				IntStream.range(0, 2).forEach(i -> {
					List<Curve> curves = new ArrayList<>();
					IntStream.range(0, 2).forEach(j -> {
						Curve curve = Curve.builder()
							.type(j % 2 == 0 ? "Gaussian" : "Lorentzian")
							.energyAtMaxCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
							.fullWidthAtHalfMaximum(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
							.maxCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))
							.build();
						
						curves.add(curve);
					});
					
					Approximation approximation = Approximation.builder()
						.chiSquaredUnweighted(rnd.nextDouble() * 100)
						.chiSquaredWeighted(rnd.nextDouble() * 100)
						.description("Sample data " + rnd.nextInt())
						.sourceData(source.stream().limit(source.size() / 2).collect(toList()))
						.curves(curves)
						.build();
					
					approximations.add(approximation);
				});
				
				Record record = Record.builder()
					.exforSubEntNumber(UUID.randomUUID().toString().replace("\\-", "").substring(0, 8))
					.energyCenter(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
					.firstMoment(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))
					.integratedCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV*mb"))
					.target(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1, "ZZ"))
					.product(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1, "ZZ"))
					.reaction(new Reaction("A", "B"))
					.sourceData(source)
					.approximations(approximations)
					.build();
				
				repo.save(record);
			});
		};
	}
}
