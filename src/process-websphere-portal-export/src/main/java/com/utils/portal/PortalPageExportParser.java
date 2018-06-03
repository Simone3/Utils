package com.utils.portal;

import com.utils.common.core.Properties;
import com.utils.common.core.StringUtils;
import com.utils.common.xml.NodeProcessor;
import com.utils.common.xml.XmlParser;
import com.utils.common.xml.XmlParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class PortalPageExportParser {

    private final static String SEP = "#";

    private final static String ACCESS_CONTROL_NAME = (String) Properties.get("element.access.control.name");
    private final static String PORTLET_NAME = (String) Properties.get("element.portlet.name");
    private final static String NEW_ACCESS_CONTROL_VALUE = (String) Properties.get("value.access.control.new");
    private final static String OBJECT_ID_ATTRIBUTE_NAME = (String) Properties.get("attribute.object.id");
    private final static String UNIQUE_NAME_ATTRIBUTE_NAME = (String) Properties.get("attribute.unique.name");
    private final static String UNDEFINED_VALUE = (String) Properties.get("value.undefined");
    private final static String NULL_VALUE = (String) Properties.get("value.null");
    private final static String ACTION_ATTRIBUTE = (String) Properties.get("attribute.action.name");
    private final static String NEW_PORTLET_ACTION = (String) Properties.get("value.action.new");
	private final Map<String, Boolean> REF_ATTRIBUTES;
	
	private Map<String, String> objectIdsMap;
	private XmlParser parser;

	public PortalPageExportParser() {

		REF_ATTRIBUTES = new HashMap<>();
        for(String ref: ((String) Properties.get("attributes.ref")).split(SEP)) {

            REF_ATTRIBUTES.put(ref, true);
        }
	}

	public void fixPageExportXml(String inputFile, String outputFile) throws XmlParserException {
		
		this.objectIdsMap = new HashMap<>();
		this.parser = new XmlParser(inputFile);

		// Process XML
		parser.loopNodes(new FirstPhaseProcessor());
		parser.loopNodes(new SecondPhaseProcessor());
		
		// Save new file
		parser.writeTo(outputFile);
	}
	
	private class FirstPhaseProcessor implements NodeProcessor {
		
		@Override
		public void onNode(Node node) throws XmlParserException {
			
			// Fix access control nodes
			boolean isAccessControl = ACCESS_CONTROL_NAME.equals(node.getNodeName());
			boolean isPortlet = PORTLET_NAME.equals(node.getNodeName());
			if(isAccessControl || isPortlet) {
				
			    Node newChild = getNewAccessControlNode(node);
			    if(isAccessControl) {
			    	
			    	node.getParentNode().replaceChild(newChild, node);
			    }
			    else {
			    	
			    	node.appendChild(newChild);
			    }
			}

			// Fix portlet action
            if(isPortlet) {

                parser.setOrAddAttribute(node, ACTION_ATTRIBUTE, NEW_PORTLET_ACTION);
            }
			
			// Save objectId -> uniqueName associations and remove objectId attributes
			final String[] objectIdAndUniqueName = new String[2];
			parser.loopAttributes(node, attribute -> {

				if(OBJECT_ID_ATTRIBUTE_NAME.equals(attribute.getNodeName())) {

					objectIdAndUniqueName[0] = attribute.getNodeValue();
				}
				else {

					if(UNIQUE_NAME_ATTRIBUTE_NAME.equals(attribute.getNodeName())) {

						objectIdAndUniqueName[1] = attribute.getNodeValue();
					}
				}
			});
			if(!StringUtils.isEmpty(objectIdAndUniqueName[0])) {
								
				parser.removeAttribute(node, OBJECT_ID_ATTRIBUTE_NAME);
				objectIdsMap.put(objectIdAndUniqueName[0], objectIdAndUniqueName[1]);
			}
		}
		
		private Node getNewAccessControlNode(Node current) throws XmlParserException {
			
			try {
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    InputSource is = new InputSource(new StringReader(NEW_ACCESS_CONTROL_VALUE));
			    Document doc = builder.parse(is);

			    return current.getOwnerDocument().importNode(doc.getDocumentElement(), true);
			}
			catch(TransformerFactoryConfigurationError | IOException | ParserConfigurationException | SAXException e) {
				
				throw new XmlParserException("This should never happen...");
			}
		}
	}
	
	private class SecondPhaseProcessor implements NodeProcessor {
		
		@Override
		public void onNode(final Node node) throws XmlParserException {

			// Replace objectIds in all "ref" attributes with the uniqueNames saved during phase 1
			parser.loopAttributes(node, attribute -> {

				if(REF_ATTRIBUTES.containsKey(attribute.getNodeName())) {

					String idRef = attribute.getNodeValue();
					if(!StringUtils.isEmpty(idRef) && !UNDEFINED_VALUE.equals(idRef) && !NULL_VALUE.equals(idRef)) {

						String uniqueName = objectIdsMap.get(idRef);
						if(StringUtils.isEmpty(uniqueName)) {

							throw new XmlParserException(node.getNodeName() + " -> " + attribute.getNodeName() + " = " + idRef + " doesn't have a unique name!");
						}
						else {

							attribute.setNodeValue(uniqueName);
						}
					}
				}
			});
		}
	}
}
