package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class LawEntity {

	// Rubrik
	@Column(name = "heading")
	private String heading;

	// Svensk författningssamling, (SFS)
	@Column(name = "sfs")
	private String sfs;

	// kapitel
	@Column(name = "chapter")
	private String chapter;

	// paragraf
	@Column(name = "article")
	private String article;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof final LawEntity lawEntity))
			return false;
		return Objects.equals(heading, lawEntity.heading) && Objects.equals(sfs, lawEntity.sfs) && Objects.equals(chapter, lawEntity.chapter) && Objects.equals(article, lawEntity.article);
	}

	@Override
	public int hashCode() {
		return Objects.hash(heading, sfs, chapter, article);
	}

}
