package uk.gov.justice.digital.hmpps.keyworker.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.keyworker.dto.OffenderKeyworkerDto;
import uk.gov.justice.digital.hmpps.keyworker.model.AllocationReason;
import uk.gov.justice.digital.hmpps.keyworker.model.AllocationType;
import uk.gov.justice.digital.hmpps.keyworker.model.OffenderKeyworker;
import uk.gov.justice.digital.hmpps.keyworker.model.PrisonSupported;
import uk.gov.justice.digital.hmpps.keyworker.repository.OffenderKeyworkerRepository;
import uk.gov.justice.digital.hmpps.keyworker.repository.PrisonSupportedRepository;
import uk.gov.justice.digital.hmpps.keyworker.utils.ConversionHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class KeyworkerMigrationService {

    private final NomisService nomisService;
    private final PrisonSupportedRepository repository;
    private final PrisonSupportedService prisonSupportedService;
    private final OffenderKeyworkerRepository offenderKeyworkerRepository;

    public KeyworkerMigrationService(NomisService nomisService,
                                     PrisonSupportedRepository repository,
                                     PrisonSupportedService prisonSupportedService,
                                     OffenderKeyworkerRepository offenderKeyworkerRepository) {
        this.repository = repository;
        this.nomisService = nomisService;
        this.prisonSupportedService = prisonSupportedService;
        this.offenderKeyworkerRepository = offenderKeyworkerRepository;
    }

    @PreAuthorize("hasRole('ROLE_KW_MIGRATION')")
    public void migrateKeyworkerByPrison(String prisonId) {
        if (prisonSupportedService.isMigrated(prisonId)) return;

        // If we get here, agency is eligible for migration and has not yet been migrated.
        final List<OffenderKeyworkerDto> allocations = nomisService.getOffenderKeyWorkerPage(prisonId, 0, Integer.MAX_VALUE);
        log.debug("[{}] allocations retrieved for agency [{}]", allocations.size(), prisonId);
        // persist all allocations
        offenderKeyworkerRepository.save(translate(allocations));

        // Mark prison as migrated
        PrisonSupported prison = repository.findOne(prisonId);
        prison.setMigrated(true);
        prison.setMigratedDateTime(LocalDateTime.now());
    }

    private Set<OffenderKeyworker> translate(List<OffenderKeyworkerDto> dtos) {
        Validate.notNull(dtos);

        Set<OffenderKeyworker> okwList = ConversionHelper.convertOffenderKeyworkerDto2Model(dtos);

        okwList.forEach(item -> {
            item.setAllocationType(AllocationType.MANUAL);
            item.setAllocationReason(AllocationReason.MANUAL);
        });

        return okwList;
    }

}
