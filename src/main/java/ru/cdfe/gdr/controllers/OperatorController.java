package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.GDRParameters;
import ru.cdfe.gdr.constants.Parameters;
import ru.cdfe.gdr.constants.Profiles;
import ru.cdfe.gdr.constants.Relations;
import ru.cdfe.gdr.domain.Approximation;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.NoSuchRecordException;
import ru.cdfe.gdr.exceptions.ValidationException;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.services.ExforService;
import ru.cdfe.gdr.services.FittingService;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@Profile(Profiles.OPERATOR)
public class OperatorController {
    private final ExforService exforService;
    private final FittingService fittingService;
    private final RecordsRepository records;
    private final Validator validator;
    
    @Autowired
    public OperatorController(ExforService exforService, FittingService fittingService, RecordsRepository records, Validator validator) {
        this.exforService = exforService;
        this.fittingService = fittingService;
        this.records = records;
        this.validator = validator;
    }
    
    private <T> void validate(T object) {
        final Set<ConstraintViolation<T>> violations = validator.validate(object);
        
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }
    
    @RequestMapping(path = "/records", method = RequestMethod.GET)
    public PagedResources<Resource<Record>> listRecords(Pageable pageable, PagedResourcesAssembler<Record> assembler) {
        return assembler.toResource(
            records.findAll(pageable),
            record -> new Resource<>(
                record,
                linkTo(methodOn(OperatorController.class).fitApproximation(null)).withRel(Relations.FITTING),
                linkTo(methodOn(OperatorController.class).findRecord(record.getId())).withSelfRel()
            )
        );
    }
    
    @RequestMapping(path = "/record", method = RequestMethod.GET)
    public Resource<Record> findRecord(@RequestParam(Parameters.ID) String id) {
        final Record record = Optional.ofNullable(records.findOne(id)).orElseThrow(NoSuchElementException::new);
        
        return new Resource<>(
            record,
            linkTo(methodOn(OperatorController.class).fitApproximation(null)).withRel(Relations.FITTING),
            linkTo(methodOn(OperatorController.class).findRecord(record.getId())).withSelfRel()
        );
    }
    
    @RequestMapping(path = "/records", method = RequestMethod.POST)
    public ResponseEntity<?> postRecord(@RequestBody Resource<Record> requestEntity) {
        Record newRecord = requestEntity.getContent();
        
        validate(newRecord);
        
        newRecord = records.save(newRecord);
        
        return ResponseEntity.created(linkTo(methodOn(ConsumerController.class).findRecord(newRecord.getId())).toUri()).build();
    }
    
    @RequestMapping(path = "/record", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putRecord(@RequestParam(Parameters.ID) String id, @RequestBody Resource<Record> request) {
        final Record newRecord = request.getContent();
        
        validate(newRecord);
        
        newRecord.setId(id);
        
        final Record oldRecord = records.findOne(id);
        
        if (oldRecord != null) {
            newRecord.setVersion(oldRecord.getVersion());
        }
        
        records.save(newRecord);
    }
    
    @RequestMapping(path = "/record", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(@RequestParam(Parameters.ID) String id) {
        if (!records.exists(id)) {
            throw new NoSuchRecordException();
        }
        
        records.delete(id);
    }
    
    @RequestMapping(path = "/record", method = RequestMethod.POST)
    public Resource<Record> createRecord(@RequestParam(Parameters.ID) String subEntNumber,
                                         @RequestParam(Parameters.ENERGY_COLUMN) int energyColumn,
                                         @RequestParam(Parameters.CROSS_SECTION_COLUMN) int crossSectionColumn,
                                         @RequestParam(Parameters.CROSS_SECTION_ERROR_COLUMN) int crossSectionErrorColumn) {
        final List<DataPoint> sourceData = exforService.getData(subEntNumber, energyColumn, crossSectionColumn, crossSectionErrorColumn);
        final GDRParameters parameters = new GDRParameters(sourceData);
        
        return new Resource<>(
            Record.builder()
                .reactions(exforService.getReactions(subEntNumber))
                .sourceData(sourceData)
                .integratedCrossSection(parameters.getIntegratedCrossSection())
                .firstMoment(parameters.getFirstMoment())
                .energyCenter(parameters.getEnergyCenter())
                .build(),
            linkTo(methodOn(OperatorController.class).fitApproximation(null)).withRel(Relations.FITTING),
            linkTo(methodOn(OperatorController.class).createRecord(subEntNumber, energyColumn, crossSectionColumn, crossSectionErrorColumn)).withSelfRel()
        );
    }
    
    @RequestMapping(path = "/fitting", method = RequestMethod.POST)
    public Resource<Approximation> fitApproximation(@RequestBody Resource<Approximation> request) {
        final Approximation initialGuess = request.getContent();
        
        validate(initialGuess);
        
        fittingService.fit(initialGuess);
        
        return new Resource<>(initialGuess);
    }
}
