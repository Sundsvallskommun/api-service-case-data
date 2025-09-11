package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.MetadataMapper.toCaseType;
import static se.sundsvall.casedata.service.util.mappers.MetadataMapper.toCaseTypeEntity;
import static se.sundsvall.casedata.service.util.mappers.MetadataMapper.toCaseTypes;

import java.util.List;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.integration.db.CaseTypeRepository;

@Service
public class MetadataService {

	private final CaseTypeRepository caseTypeRepository;

	public MetadataService(final CaseTypeRepository caseTypeRepository) {
		this.caseTypeRepository = caseTypeRepository;
	}

	public List<CaseType> getCaseTypes(final String municipalityId, final String namespace) {
		return toCaseTypes(caseTypeRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace));
	}

	public CaseType getCaseType(final String municipalityId, final String namespace, final String type) {
		return toCaseType(caseTypeRepository.findByMunicipalityIdAndNamespaceAndType(municipalityId, namespace, type)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "CaseType not found in database")));
	}

	public String createCaseType(final String municipalityId, final String namespace, final CaseType caseType) {
		return caseTypeRepository.save(toCaseTypeEntity(municipalityId, namespace, caseType)).getType();
	}

	public void deleteCaseType(final String municipalityId, final String namespace, final String type) {
		caseTypeRepository.delete(caseTypeRepository.findByMunicipalityIdAndNamespaceAndType(municipalityId, namespace, type)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "CaseType not found in database")));
	}
}
