package se.sundsvall.casedata.service.util;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.util.Base64;
import java.util.Base64.Decoder;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class BlobBuilder {

	private static final Decoder DECODER = Base64.getDecoder();

	public Blob createBlob(final String base64content) {
		final var decodedBytes = DECODER.decode(base64content.getBytes(UTF_8));
		final var stream = new ByteArrayInputStream(decodedBytes);
		return Hibernate.getLobHelper().createBlob(stream, decodedBytes.length);
	}

	public Blob createBlob(final byte[] content) {
		return Hibernate.getLobHelper().createBlob(content);
	}
}
