package ru.cdfe.gdr;

import com.mongodb.MongoClientOptions;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.cdfe.gdr.domain.*;
import ru.cdfe.gdr.services.FittingService;
import ru.cdfe.gdr.repositories.RecordsRepository;

import javax.validation.Validator;
import java.util.*;
import java.util.stream.IntStream;

@SpringBootApplication
public class GDRApplication {
    public static void main(String[] args) {
        SpringApplication.run(GDRApplication.class, args);
    }
    
    @Bean
    public CurieProvider curieProvider(GDRApplicationProperties conf) {
        return new DefaultCurieProvider(conf.getCurieName(), new UriTemplate(conf.getCurieUrlTemplate()));
    }
    
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
    
    @Bean
    public MongoClientOptions mongoClientOptions() {
        return MongoClientOptions.builder()
            .writeConcern(WriteConcern.MAJORITY)
            .readConcern(ReadConcern.MAJORITY)
            .build();
    }
    
    @Bean
    @Profile("init")
    public ApplicationRunner createTestData(RecordsRepository repo) {
        return args -> {
            repo.deleteAll();
            IntStream.range(0, 1000).parallel().forEach(value -> {
                final Random rnd = new Random();
                final List<DataPoint> source = new ArrayList<>();
                
                IntStream.range(0, 10).forEach(i -> source.add(
                    new DataPoint(
                        new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"),
                        new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))));
                
                final List<Approximation> approximations = new ArrayList<>();
                
                IntStream.range(0, 2).forEach(i -> {
                    final List<Curve> curves = new ArrayList<>();
                    
                    IntStream.range(0, 2).forEach(j -> {
                        Curve curve = Curve.builder()
                            .type(rnd.nextBoolean() ? FittingService.Curves.GAUSSIAN : FittingService.Curves.LORENTZIAN)
                            .energyAtMaxCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
                            .fullWidthAtHalfMaximum(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
                            .maxCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))
                            .build();
                        
                        curves.add(curve);
                    });
                    
                    final double chiSquared = rnd.nextDouble() * 1000;
                    
                    final Approximation approximation = Approximation.builder()
                        .chiSquared(chiSquared)
                        .chiSquaredReduced(chiSquared / (source.size() - (curves.size() * 3)))
                        .description("Sample data " + rnd.nextInt())
                        .sourceData(source)
                        .curves(curves)
                        .build();
                    
                    approximations.add(approximation);
                });
                
                final Reaction reaction1 = Reaction.builder()
                    .incident("A")
                    .outgoing("B")
                    .target(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1))
                    .product(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1))
                    .build();
                
                final Reaction reaction2 = Reaction.builder()
                    .incident("C")
                    .outgoing("D")
                    .target(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1))
                    .product(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1))
                    .build();
                
                final Record record = Record.builder()
                    .id(UUID.randomUUID().toString())
                    .energyCenter(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
                    .firstMoment(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))
                    .integratedCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV*mb"))
                    .reactions(Arrays.asList(reaction1, reaction2))
                    .sourceData(source)
                    .approximations(approximations)
                    .build();
                
                repo.save(record);
            });
        };
    }
}
