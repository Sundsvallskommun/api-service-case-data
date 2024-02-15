package se.sundsvall.casedata.api.model.history;

import java.util.List;

import lombok.Data;

@Data
public class HistoryDTO {

	private String changeType;

	private CommitMetadata commitMetadata;

	private GlobalId globalId;

	private String property;

	private String propertyChangeType;

	private List<EntryChangesItem> entryChanges;

	private String left;

	private String right;

	private List<ElementChangesItem> elementChanges;

}
