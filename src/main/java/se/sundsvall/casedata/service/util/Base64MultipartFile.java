package se.sundsvall.casedata.service.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public class Base64MultipartFile implements MultipartFile {

	private final String name;
	private final String originalFilename;
	private final String contentType;
	private final byte[] content;

	public Base64MultipartFile(final String name, final String originalFilename, final String contentType, final byte[] content) {
		this.name = name;
		this.originalFilename = originalFilename;
		this.contentType = contentType;
		this.content = content != null ? content : new byte[0];
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalFilename() {
		return originalFilename;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return content.length == 0;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public byte[] getBytes() {
		return content;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(final File dest) throws IOException, IllegalStateException {
		throw new UnsupportedOperationException("transferTo is not supported");
	}
}
