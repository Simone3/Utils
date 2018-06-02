package com.utils.common.xml;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper to parse an XML file
 */
public class XmlParser {

	private static final String XML_DECLARATION = "(<\\?xml[^>]+\\?>)";
	private Document doc;

	/**
	 * Constructor
	 * @param inputFile the full path of the XML file to parse
	 * @throws XmlParserException if a read error occurs
	 */
	public XmlParser(String inputFile) throws XmlParserException {
				
		try {
			
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(inputFile));
		}
		catch(SAXException | IOException | ParserConfigurationException | TransformerFactoryConfigurationError e) {
			
			throw new XmlParserException(e);
		}
	}

	/**
	 * Allows to loop all nodes in the XML
	 * @param processor callback invoked for each node
	 * @throws XmlParserException may be thrown by the processor if something is wrong
	 */
	public void loopNodes(NodeProcessor processor) throws XmlParserException {
		
		loopRecursive(doc.getDocumentElement(), processor);
	}

	/**
	 * Writes the XML file to the given file
	 * @param outputFile the full path of the output file
	 * @throws XmlParserException if a write error occurs
	 */
	public void writeTo(String outputFile) throws XmlParserException {
		
		try {
			
			// Write XML
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(doc), new StreamResult(new File(outputFile)));
			
			// Get new file contents
			Path path = Paths.get(outputFile);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path), charset);
			
			// Fix newline after XML declaration (Java bug?)
			content = content.replaceAll(XML_DECLARATION, "$1\r\n");
			
			// Fix newline after comments before root element (don't get parsed...)
			content = content.replaceAll("--><", "-->\r\n<");
			
			// Write fixed file
			Files.write(path, content.getBytes(charset));
		}
		catch(TransformerFactoryConfigurationError | TransformerException | IOException e) {
			
			throw new XmlParserException(e);
		}
	}

	/**
	 * Allows to loop all attributes of the given node
	 * @param node the node
	 * @param processor callback invoked for each attribute
	 * @throws XmlParserException may be thrown by the processor if something is wrong
	 */
	public void loopAttributes(Node node, AttributeProcessor processor) throws XmlParserException {
		
		NamedNodeMap attributes = node.getAttributes();
		if(attributes == null) {

			throw new XmlParserException("This should never happen! Is the node an Element?");
		}

		for(int i = 0; i < attributes.getLength(); i++) {

			Node attribute = attributes.item(i);
			processor.onAttribute(attribute);
		}
	}

	/**
	 * Allows to set (or add if not present) an attribute
	 * @param node the node
	 * @param attributeName the attribute name
	 * @param attributeValue the new attribute value
	 */
	public void setOrAddAttribute(Node node, String attributeName, String attributeValue) throws XmlParserException {

		if(!(node instanceof Element)) {

			throw new XmlParserException("This should never happen! Is the node an Element?");
		}

		((Element) node).setAttribute(attributeName, attributeValue);
	}

	/**
	 * Removes an attribute, if present
	 * @param node the node
	 * @param attributeName the attribute to remove
	 */
	public void removeAttribute(Node node, String attributeName) {
		
		NamedNodeMap attributes = node.getAttributes();
		if(attributes != null) {
			
			try {
				attributes.removeNamedItem(attributeName);
			}
			catch(DOMException e) {

				// Node was not present, do nothing
			}
		}
	}

	/**
	 * Helper
	 */
	private void loopRecursive(Node current, NodeProcessor processor) throws XmlParserException {
		
		// Current node
		processor.onNode(current);
		
		// Recurse
	    NodeList children = current.getChildNodes();
	    for(int i = 0; i < children.getLength(); i++) {
	    	
	        Node child = children.item(i);
	        if(child.getNodeType() == Node.ELEMENT_NODE) {
	            
	        	loopRecursive(child, processor);
	        }
	    }
	}
}
