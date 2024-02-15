package se.sundsvall.casedata.integration.db.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.sql.Blob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder(setterPrefix = "with")
@EqualsAndHashCode
@ToString
public class MessageAttachmentData {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private int id;

	@Column(columnDefinition = "longblob")
	@Lob
	private Blob file;

}
