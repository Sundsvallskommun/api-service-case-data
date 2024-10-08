package se.sundsvall.casedata.service;

import static java.util.Objects.nonNull;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_NUMBER_EXTRA_PARAMETER_KEY;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_STATUS_EXTRA_PARAMETER_KEY;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.GetParkingPermit;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
public class ParkingPermitErrandService {

	private final ErrandRepository errandRepository;

	public ParkingPermitErrandService(final ErrandRepository errandRepository) {
		this.errandRepository = errandRepository;
	}

	public List<GetParkingPermit> findAllByPersonIdAndMunicipalityId(final String personId, final String municipalityId, final String namespace) {
		final List<GetParkingPermit> parkingPermitsList = new ArrayList<>();

		final List<ErrandEntity> allErrands = personId == null ? errandRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace) : findAllErrandsWithApplicant(personId, municipalityId, namespace);

		final List<Errand> allErrandsWithPrh = allErrands.stream()
			.filter(errand -> errand.getExtraParameters().containsKey(PERMIT_NUMBER_EXTRA_PARAMETER_KEY))
			.map(EntityMapper::toErrand)
			.toList();

		allErrandsWithPrh.forEach(errand -> parkingPermitsList.add(GetParkingPermit.builder()
			.withArtefactPermitNumber(errand.getExtraParameters().get(PERMIT_NUMBER_EXTRA_PARAMETER_KEY))
			.withArtefactPermitStatus(errand.getExtraParameters().get(PERMIT_STATUS_EXTRA_PARAMETER_KEY))
			.withErrandId(errand.getId())
			.withErrandDecision(errand.getDecisions().stream().filter(decision -> DecisionType.FINAL.equals(decision.getDecisionType())).findFirst().orElse(null))
			.build()));

		return parkingPermitsList;
	}

	/**
	 * @param personId of the applicant
	 * @return all errands with stakeholder who has the role APPLICANT and matching personId
	 */
	private List<ErrandEntity> findAllErrandsWithApplicant(final String personId, final String municipalityId, final String namespace) {
		return errandRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace).stream()
			.filter(errand -> errand.getStakeholders().stream()
				.filter(stakeholder -> nonNull(stakeholder.getPersonId()))
				.anyMatch(stakeholder -> stakeholder.getPersonId().equals(personId) && stakeholder.getRoles().contains(StakeholderRole.APPLICANT.name())))
			.toList();
	}

}
