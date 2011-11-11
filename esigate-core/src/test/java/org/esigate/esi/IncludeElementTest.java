package org.esigate.esi;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import junit.framework.TestCase;

import org.esigate.HttpErrorPage;
import org.esigate.MockDriver;
import org.esigate.test.MockHttpServletRequest;
import org.esigate.test.MockHttpServletResponse;

public class IncludeElementTest extends TestCase {

	private MockDriver provider;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@Override
	protected void setUp() throws Exception {
		provider = new MockDriver("mock");
		provider.addResource("/test", "test");
		provider.addResource("http://www.foo.com/test", "test");
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	public void testIncludeProvider() throws IOException, HttpErrorPage {
		String page = "before <esi:include src=\"$PROVIDER({mock})/test\" /> after";
		EsiRenderer tested = new EsiRenderer(request, response, provider);
		StringWriter out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before test after", out.toString());
	}

	public void testIncludeAbsolute() throws IOException, HttpErrorPage {
		String page = "before <esi:include src=\"http://www.foo.com/test\" /> after";
		EsiRenderer tested = new EsiRenderer(request, response, provider);
		StringWriter out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before test after", out.toString());
	}

	public void testIncludeFragment() throws IOException, HttpErrorPage {
		String page = "before <esi:include src=\"$PROVIDER({mock})/testFragment\" fragment =\"myFragment\" /> after";
		provider.addResource("/testFragment", "before fragment <esi:fragment name=\"myFragment\">---fragment content---</esi:fragment> after fragment");
		EsiRenderer tested = new EsiRenderer(request, response, provider);
		StringWriter out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before ---fragment content--- after", out.toString());
	}

	public void testIncludeQueryString() throws IOException, HttpErrorPage {
		String page = "before <esi:include src=\"$PROVIDER({mock})/test?$(QUERY_STRING)\" /> after";
		provider.addResource("/test?queryparameter1=test&queryparameter2=test2", "query OK");
		request = new MockHttpServletRequest("http://localhost/test?queryparameter1=test&queryparameter2=test2");
		EsiRenderer tested = new EsiRenderer(request, response, provider);
		StringWriter out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before query OK after", out.toString());
	}

	public void testIncludeQueryStringParameter() throws IOException, HttpErrorPage {
		String page = "before <esi:include src=\"$PROVIDER({mock})/$(QUERY_STRING{queryparameter2})\" /> after";
		provider.addResource("/test2", "queryparameter2 OK");
		request = new MockHttpServletRequest("http://localhost/test?queryparameter1=test&queryparameter2=test2");
		EsiRenderer tested = new EsiRenderer(request, response, provider);
		StringWriter out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before queryparameter2 OK after", out.toString());
	}

	public void testIncludeInlineCache() throws IOException, HttpErrorPage {
		String page = "before <esi:include src='$PROVIDER({mock})/inline-cache' /> after";
		InlineCache.storeFragment("$PROVIDER({mock})/inline-cache", null, false, null, "---inline cache item---");
		EsiRenderer tested = new EsiRenderer(request, response, provider);
		StringWriter out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before ---inline cache item--- after", out.toString());

		InlineCache.storeFragment("$PROVIDER({mock})/inline-cache", new Date(System.currentTimeMillis() + 10L * 1000L), false, null,
				"---updated inline cache item---");
		out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before ---updated inline cache item--- after", out.toString());

		InlineCache.storeFragment("$PROVIDER({mock})/inline-cache", new Date(System.currentTimeMillis() - 10L * 1000L), false, null,
				"---expired inline cache item---");
		out = new StringWriter();
		provider.addResource("/inline-cache", "---fetched inline cache item---");
		tested.render(null, page, out);
		assertEquals("before ---fetched inline cache item--- after", out.toString());
	}

	public void testIncludeInlineElement() throws IOException, HttpErrorPage {
		String page = "before <esi:include src='$PROVIDER({mock})/inline-cache' /> middle "
				+ "<esi:inline name='$PROVIDER({mock})/inline-cache' fetchable='false'>---inline cache item---</esi:inline>"
				+ "<esi:include src='$PROVIDER({mock})/inline-cache' /> after";
		EsiRenderer tested = new EsiRenderer(request, response, provider);
		provider.addResource("/inline-cache", "---fetched inline cache item---");
		StringWriter out = new StringWriter();
		tested.render(null, page, out);
		assertEquals("before ---fetched inline cache item--- middle ---inline cache item--- after", out.toString());
	}
}