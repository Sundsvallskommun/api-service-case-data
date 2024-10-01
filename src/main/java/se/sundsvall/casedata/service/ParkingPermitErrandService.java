package se.sundsvall.casedata.service;

import static java.util.Objects.nonNull;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_NUMBER_EXTRA_PARAMETER_KEY;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_STATUS_EXTRA_PARAMETER_KEY;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.GetParkingPermitDTO;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
public class ParkingPermitErrandService {

	private final ErrandRepository errandRepository;

	public ParkingPermitErrandService(final ErrandRepository errandRepository) {
		this.errandRepository = errandRepository;
	}

	public List<GetParkingPermitDTO> findAllByPersonIdAndMunicipalityId(final String personId, final String municipalityId, final String namespace) {
		final List<GetParkingPermitDTO> parkingPermitsDTOList = new ArrayList<>();

		final List<Errand> allErrands = personId == null ? errandRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace) : findAllErrandsWithApplicant(personId, municipalityId, namespace);

		final List<ErrandDTO> allErrandsWithPrh = allErrands.stream()
			.filter(errand -> errand.getExtraParameters().containsKey(PERMIT_NUMBER_EXTRA_PARAMETER_KEY))
			.map(EntityMapper::toErrandDto)
			.toList();

		allErrandsWithPrh.forEach(errand -> parkingPermitsDTOList.add(GetParkingPermitDTO.builder()
			.withArtefactPermitNumber(errand.getExtraParameters().get(PERMIT_NUMBER_EXTRA_PARAMETER_KEY))
			.withArtefactPermitStatus(errand.getExtraParameters().get(PERMIT_STATUS_EXTRA_PARAMETER_KEY))
			.withErrandId(errand.getId())
			.withErrandDecision(errand.getDecisions().stream().filter(decisionDTO -> DecisionType.FINAL.equals(decisionDTO.getDecisionType())).findFirst().orElse(null))
			.build()));

		return parkingPermitsDTOList;
	}

	/**
	 * @param personId of the applicant
	 * @return all errands with stakeholder who has the role APPLICANT and matching personId
	 */
	private List<Errand> findAllErrandsWithApplicant(final String personId, final String municipalityId, final String namespace) {
		return errandRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace).stream()
			.filter(errand -> errand.getStakeholders().stream()
				.filter(stakeholder -> nonNull(stakeholder.getPersonId()))
				.anyMatch(stakeholder -> stakeholder.getPersonId().equals(personId) && stakeholder.getRoles().contains(StakeholderRole.APPLICANT.name())))
			.toList();
	}

}
