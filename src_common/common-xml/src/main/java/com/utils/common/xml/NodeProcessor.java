package com.utils.common.xml;

import org.w3c.dom.Node;

/**
 * Callback for looping nodes
 */
public interface NodeProcessor {

	/**
	 * Called for each attribute
	 * @param node the node
	 * @throws XmlParserException may be thrown is something went wrong
	 */
	void onNode(Node node) throws XmlParserException;
}