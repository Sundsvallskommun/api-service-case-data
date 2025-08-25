package se.sundsvall.casedata.configuration;

import java.io.IOException;
import org.zalando.logbook.ForwardingHttpRequest;
import org.zalando.logbook.ForwardingHttpResponse;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Strategy;

public class MultipartStrategy implements Strategy {

	private static final String MULTIPART_FORM_DATA = "multipart/form-data";
	private static final String REPLACEMENT = "[file content removed]";

	@Override
	public HttpRequest process(final HttpRequest request) throws IOException {
		if (request.getContentType() != null && request.getContentType().startsWith(MULTIPART_FORM_DATA)) {
			return new MultipartForwardingHttpRequest(request);
		}
		return request.withBody();
	}

	@Override
	public HttpResponse process(final HttpRequest request, final HttpResponse response) throws IOException {
		if (response.getContentType() != null && response.getContentType().startsWith(MULTIPART_FORM_DATA)) {
			return new MultipartForwardingHttpResponse(response);
		}
		return response.withBody();
	}

	private record MultipartForwardingHttpRequest(HttpRequest delegate)
		implements
		ForwardingHttpRequest {

		@Override
		public String getBodyAsString() {
			return REPLACEMENT;
		}
	}

	private record MultipartForwardingHttpResponse(HttpResponse delegate)
		implements
		ForwardingHttpResponse {

		@Override
		public String getBodyAsString() {
			return REPLACEMENT;
		}
	}
}
