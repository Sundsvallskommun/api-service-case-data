package se.sundsvall.casedata.api.model.history;

import java.util.List;
import lombok.Data;

@Data
public class CommitMetadata {

	private String author;
	private String commitDateInstant;
	private double id;
	private List<Object> properties;
	private String commitDate;
}
