package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


import jakarta.persistence.PreUpdate;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.api.model.validation.enums.Shortcode;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;



@Component
public class ErrandListener {

	private static final Logger LOG = LoggerFactory.getLogger(ErrandListener.class);

	private static final String DELIMITER = "-";

	private final IncomingRequestFilter incomingRequestFilter;

	private final ErrandRepository errandRepository;

	public ErrandListener(final IncomingRequestFilter incomingRequestFilter, @Lazy final ErrandRepository errandRepository) {
		this.incomingRequestFilter = incomingRequestFilter;
		this.errandRepository = errandRepository;
	}

	@PrePersist
	private void prePersist(final ErrandEntity errand) {
		errand.setErrandNumber(generateErrandNumber(errand.getMunicipalityId(), errand.getNamespace()));
	}

	@PostPersist
	private void postPersist(final ErrandEntity errand) {
		errand.setCreatedByClient(incomingRequestFilter.getSubscriber());
		errand.setCreatedBy(incomingRequestFilter.getAdUser());
		LOG.info("Created errand with errandNumber: {}. Subscriber: {}. AD-user: {}", errand.getErrandNumber(), incomingRequestFilter.getSubscriber(),
			incomingRequestFilter.getAdUser());
	}

	@PreUpdate
	private void preUpdate(final ErrandEntity errand) {
		updateErrandFields(errand);
		LOG.info("Updated errand with updated: {}. errandNumber: {}. Subscriber: {}. AD-user: {}", errand.getUpdated(), errand.getErrandNumber(),
			incomingRequestFilter.getSubscriber(), incomingRequestFilter.getAdUser());
	}

	void updateErrandFields(final ErrandEntity errand) {
		if (errand != null) {
			// Behöver sätta datum för att errand-objektet ska uppdateras med ny version o.s.v.
			errand.setUpdated(now(systemDefault()));
			errand.setUpdatedByClient(incomingRequestFilter.getSubscriber());
			errand.setUpdatedBy(incomingRequestFilter.getAdUser());
		}
	}

	private String generateErrandNumber(final String municipalityId, final String namespace) {
		// Get the latest errand with an errandNumber and only the ones within the same year. If this year i different, a new
		// sequenceNumber begins.
		final Optional<ErrandEntity> latestErrand = errandRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace)
			.stream()
			.filter(errand -> isNotBlank(errand.getErrandNumber()))
			.filter(errand -> LocalDate.now().getYear() == extractYearFromErrandNumber(errand))
			.max(Comparator.comparing(ErrandEntity::getCreated));

		// Default start value = 1
		long nextSequenceNumber = 1;

		// If there is some errand before this one, use the sequenceNumber + 1
		if (latestErrand.isPresent()) {
			nextSequenceNumber = Long.parseLong(latestErrand.get().getErrandNumber().substring(latestErrand.get().getErrandNumber().lastIndexOf(DELIMITER) + 1)) + 1;
		}

		// prefix with the abbreviation
		return Shortcode.getByNamespace(namespace) + DELIMITER +
			LocalDate.now().getYear() + DELIMITER +
			String.format("%06d", nextSequenceNumber);
	}

	private int extractYearFromErrandNumber(final ErrandEntity errand) {
		return Integer.parseInt(errand.getErrandNumber().substring(errand.getErrandNumber().lastIndexOf(DELIMITER) - 4, errand.getErrandNumber().lastIndexOf(DELIMITER)));
	}

}
