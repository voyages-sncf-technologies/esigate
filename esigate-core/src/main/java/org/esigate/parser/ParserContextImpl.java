/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.esigate.parser;

import java.io.IOException;
import java.util.Stack;

import org.esigate.HttpErrorPage;
import org.esigate.api.HttpRequest;
import org.esigate.api.HttpResponse;

/**
 * 
 * The stack of tags corresponding to the current position in the document
 * 
 * @author Francois-Xavier Bonnet
 * 
 */
class ParserContextImpl implements ParserContext {
	private final RootAdapter root;
	private final HttpRequest httpRequest;
	private final HttpResponse httpResponse;

	private final Stack<Pair> stack = new Stack<Pair>();

	ParserContextImpl(Appendable root, HttpRequest httpRequest, HttpResponse httpResponse) {
		this.root = new RootAdapter(root);
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
	}

	public <T> T findAncestor(Class<T> type) {
		T result = null;
		for (int i = stack.size() - 1; i > -1; i--) {
			Element currentElement = stack.elementAt(i).element;
			if (type.isInstance(currentElement)) {
				result = type.cast(currentElement);
				break;
			}
		}
		// try with root
		if (result == null && type.isInstance(root.root)) {
			result = type.cast(root.root);
		}

		return result;
	}

	/** {@inheritDoc} */
	public boolean reportError(Exception e) {
		boolean result = false;
		for (int i = stack.size() - 1; i > -1; i--) {
			Element element = stack.elementAt(i).element;
			if (element.onError(e, this)) {
				result = true;
				break;
			}
		}
		return result;
	}

	void startElement(ElementType type, Element element, String tag) throws IOException, HttpErrorPage {
		element.onTagStart(tag, this);
		stack.push(new Pair(type, element));
	}

	void endElement(String tag) throws IOException, HttpErrorPage {
		Element element = stack.pop().element;
		element.onTagEnd(tag, this);
	}

	boolean isCurrentTagEnd(String tag) {
		return !stack.isEmpty() && stack.peek().type.isEndTag(tag);
	}

	/** Writes characters into current writer. */
	void characters(CharSequence cs) throws IOException {
		characters(cs, 0, cs.length());
	}

	/** Writes characters into current writer. */
	void characters(CharSequence csq, int start, int end) throws IOException {
		getCurrent().characters(csq, start, end);
	}

	public Element getCurrent() {
		return (!stack.isEmpty()) ? stack.peek().element : root;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	private static class Pair {
		private final ElementType type;
		private final Element element;

		public Pair(ElementType type, Element element) {
			this.type = type;
			this.element = element;
		}
	}

	private static class RootAdapter implements Element {
		private final Appendable root;

		public RootAdapter(Appendable root) {
			this.root = root;
		}

		public void onTagStart(String tag, ParserContext ctx) {
		}

		public void onTagEnd(String tag, ParserContext ctx) {
		}

		public boolean onError(Exception e, ParserContext ctx) {
			return false;
		}

		public void characters(CharSequence csq, int start, int end) throws IOException {
			root.append(csq, start, end);
		}

		public boolean isClosed() {
			return false;
		}
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}
}