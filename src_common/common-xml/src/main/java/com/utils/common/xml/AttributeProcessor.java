package com.utils.common.xml;

import org.w3c.dom.Node;

/**
 * Callback for looping attributes
 */
public interface AttributeProcessor {

	/**
	 * Called for each attribute
	 * @param attribute the attribute
	 * @throws XmlParserException may be thrown is something went wrong
	 */
	void onAttribute(Node attribute) throws XmlParserException;
}