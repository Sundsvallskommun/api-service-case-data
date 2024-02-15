package se.sundsvall.casedata.service.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
public class BlobBuilder {

	private static final Decoder DECODER = Base64.getDecoder();

	private EntityManager entityManager;

	public BlobBuilder(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Blob createBlob(String base64content) {
		final var session = entityManager.unwrap(Session.class);
		final var decodedBytes = DECODER.decode(base64content.getBytes(UTF_8));
		final var stream = new ByteArrayInputStream(decodedBytes);

		return session.getLobHelper().createBlob(stream, decodedBytes.length);
	}
}
