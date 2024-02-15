package se.sundsvall.casedata.integration.db.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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
public class Law {

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
		if (this == o) return true;
		if (!(o instanceof final Law law)) return false;
		return Objects.equals(heading, law.heading) && Objects.equals(sfs, law.sfs) && Objects.equals(chapter, law.chapter) && Objects.equals(article, law.article);
	}

	@Override
	public int hashCode() {
		return Objects.hash(heading, sfs, chapter, article);
	}

}
