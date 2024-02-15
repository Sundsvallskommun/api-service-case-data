package se.sundsvall.casedata.apptest;

import java.util.List;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import se.sundsvall.dept44.test.AbstractAppTest;

public class CustomAbstractAppTest extends AbstractAppTest {

	@Override
	public boolean verifyAllStubs() {
		final List<LoggedRequest> unmatchedRequests = this.wiremock.findAllUnmatchedRequests();
		if (!unmatchedRequests.isEmpty()) {
			final List<String> unmatchedUrls = unmatchedRequests.stream().map(LoggedRequest::getUrl).toList();
			throw new AssertionError(String.format("The following requests was not matched: %s", unmatchedUrls));
		}
		return true;
	}

}
