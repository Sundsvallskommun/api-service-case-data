package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "conversation",
	indexes = {
		@Index(name = "idx_municipality_id_namespace_errand_id", columnList = "municipality_id, namespace, errand_id"),
		@Index(name = "idx_message_exchange_id", columnList = "message_exchange_id")
	})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder(setterPrefix = "with")
@EqualsAndHashCode
@ToString
public class ConversationEntity {

	@Id
	@UuidGenerator
	@Column(name = "id", length = 36)
	private String id;

	@Column(name = "message_exchange_id", length = 36, nullable = false)
	private String messageExchangeId;

	@Column(name = "errand_id", length = 36, nullable = false)
	private String errandId;

	@Column(name = "namespace", length = 32, nullable = false)
	private String namespace;

	@Column(name = "municipality_id", length = 4, nullable = false)
	private String municipalityId;
	@With
	@Column(name = "topic")
	private String topic;

	@With
	@Column(name = "type", length = 32, nullable = false)
	private String type;

	@With
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "conversation_relation_id", joinColumns = @JoinColumn(name = "conversation_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_conversation_relation_conversation_id")))
	@Column(name = "relation_id", length = 36)
	private List<String> relationIds;

	@With
	@Column(name = "target_relation_id", length = 36)
	private String targetRelationId;

	@With
	@Column(name = "latest_synced_sequence_number")
	private Long latestSyncedSequenceNumber;

}
