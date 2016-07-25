package com.paypal.butterfly.basic.utilities.xml;

import com.paypal.butterfly.extensions.api.TransformationContext;
import com.paypal.butterfly.extensions.api.TransformationUtility;
import com.paypal.butterfly.extensions.api.exception.TransformationUtilityException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Utility that results in the value of an element,
 * or one of its attributes, in a XML file
 *
 * @author facarvalho
 */
public class XmlElement extends TransformationUtility<XmlElement, String> {

    private static final String DESCRIPTION = "Retrieve the value of element %s in XML file %s";

    private String xmlElement;
    private String attribute;

    public XmlElement() {
    }

    /**
     * Result in the value of an element in a XML file
     *
     * @see {@link #setXmlElement(String)}
     * @param xmlElement
     */
    public XmlElement(String xmlElement) {
        setXmlElement(xmlElement);
    }

    /**
     * The XML element whose value, or an attribute, should be
     * the result of this transformation utility. The element specified
     * here should be set based on a path containing all its
     * parent elements separated by '.'. See the example bellow.
     * </br>
     * To retrieve the value of the child' name set {@code xmlElement}
     * to {@code person.child.name}.
     * </br>
     * {@code
     *  <?xml version="1.0" encoding="UTF-8"?>
     *  <person>
     *      <name>Bruna</name>
     *      <child>
     *          <name>Gabriela</name>
     *      </child>
     *  </peson>
     * }
     *
     * @param xmlElement the XML element
     * @return this instance
     */
    public XmlElement setXmlElement(String xmlElement) {
        this.xmlElement = xmlElement;
        return this;
    }

    /**
     * Set the name of the XML element attribute
     * to be retrieved. If null, the element
     * value will be retrieved instead
     *
     * @param attribute the name of the XML element attribute
     * to be retrieved
     * @return this instance
     */
    public XmlElement setAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    @Override
    public String getDescription() {
        return String.format(DESCRIPTION, xmlElement, getRelativePath());
    }

    private static final Pattern XML_ELEMENT_SPLIT_REGEX_PATTERN = Pattern.compile("\\.");

    @Override
    protected String execution(File transformedAppFolder, TransformationContext transformationContext) throws Exception {
        File xmlFile = getAbsoluteFile(transformedAppFolder, transformationContext);

        Node node;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        node = findNode(doc.getChildNodes(), XML_ELEMENT_SPLIT_REGEX_PATTERN.split(xmlElement), 0);

        String result;

        if(attribute == null) {
            result = node.getTextContent();
        } else {
            result = node.getAttributes().getNamedItem(attribute).getTextContent();
        }

        return result;
    }

    private Node findNode(NodeList nodeList, String[] xmlElementPath, int i) throws TransformationUtilityException {
        String nextElement = xmlElementPath[i];
        Node node = null;

        for(int j = 0; j < nodeList.getLength(); j++) {
            node = nodeList.item(j);
            if(node.getNodeName().equals(nextElement)) {
                break;
            }
        }
        if(node == null) {
            throw new TransformationUtilityException("Element " + xmlElement + " could not be found in XML file");
        }

        i++;
        if(i == xmlElementPath.length) {
            return node;
        }

        return findNode(node.getChildNodes(), xmlElementPath, i);
    }

}