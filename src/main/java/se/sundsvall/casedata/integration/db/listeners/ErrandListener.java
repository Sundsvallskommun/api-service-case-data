package se.sundsvall.casedata.integration.db.listeners;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Optional;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;

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
	private void beforePersist(final Errand errand) {
		errand.setErrandNumber(generateErrandNumber(errand.getCaseType()));
	}

	@PostPersist
	private void postPersist(final Errand errand) {
		errand.setCreatedByClient(incomingRequestFilter.getSubscriber());
		errand.setCreatedBy(incomingRequestFilter.getAdUser());
		LOG.info("Created errand with errandNumber: {}. Subscriber: {}. AD-user: {}", errand.getErrandNumber(), incomingRequestFilter.getSubscriber(), incomingRequestFilter.getAdUser());
	}

	@PostUpdate
	private void beforeUpdate(final Errand errand) {
		updateErrandFields(errand);
	}

	void updateErrandFields(final Errand errand) {
		if (errand != null) {
			// Behöver sätta datum för att errand-objektet ska uppdateras med ny version o.s.v.
			errand.setUpdated(OffsetDateTime.now());
			errand.setUpdatedByClient(incomingRequestFilter.getSubscriber());
			errand.setUpdatedBy(incomingRequestFilter.getAdUser());
			LOG.info("Updated errand with updated: {}. errandNumber: {}. Subscriber: {}. AD-user: {}", errand.getUpdated(), errand.getErrandNumber(), incomingRequestFilter.getSubscriber(), incomingRequestFilter.getAdUser());
		}
	}

	private String generateErrandNumber(final CaseType caseType) {
		// Get the latest errand with an errandNumber and only the ones within the same year. If this year i different, a new sequenceNumber begins.
		final Optional<Errand> latestErrand = errandRepository.findAllByErrandNumberStartingWith(caseType.getAbbreviation())
			.stream()
			.filter(errand -> errand.getErrandNumber() != null
				&& !errand.getErrandNumber().isBlank()
				&& LocalDate.now().getYear() == Integer.parseInt(errand.getErrandNumber().substring(errand.getErrandNumber().lastIndexOf(DELIMITER) - 4, errand.getErrandNumber().lastIndexOf(DELIMITER))))
			.max(Comparator.comparing(Errand::getCreated));

		// Default start value = 1
		long nextSequenceNumber = 1;

		// If there is some errand before this one, use the sequenceNumber + 1
		if (latestErrand.isPresent()) {
			nextSequenceNumber = Long.parseLong(latestErrand.get().getErrandNumber().substring(latestErrand.get().getErrandNumber().lastIndexOf(DELIMITER) + 1)) + 1;
		}

		final StringBuilder stringBuilder = new StringBuilder();
		final String prefix = caseType.getAbbreviation();
		stringBuilder.append(prefix).append(DELIMITER);
		stringBuilder.append(LocalDate.now().getYear()).append(DELIMITER);
		stringBuilder.append(String.format("%06d", nextSequenceNumber));
		return stringBuilder.toString();
	}

}
