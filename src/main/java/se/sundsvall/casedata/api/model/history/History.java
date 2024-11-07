package se.sundsvall.casedata.api.model.history;

import java.util.List;

import lombok.Data;

@Data
public class History {

	private String changeType;
	private CommitMetadata commitMetadata;
	private GlobalId globalId;
	private String property;
	private String propertyChangeType;
	private List<EntryChangesItem> entryChanges;
	private Object left;
	private Object right;
	private List<ElementChangesItem> elementChanges;
}
