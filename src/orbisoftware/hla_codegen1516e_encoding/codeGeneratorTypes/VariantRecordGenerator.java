/*
 *  HLA Codegen 1516E Encoding
 *
 *  Copyright (C) 2024 Harlan Murphy
 *  Orbis Software - orbisoftware@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package orbisoftware.hla_codegen1516e_encoding.codeGeneratorTypes;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orbisoftware.hla_codegen1516e_encoding.codeGenerator.CodeGeneratorJava;
import orbisoftware.hla_codegen1516e_encoding.codeGenerator.LedgerEntry;
import orbisoftware.hla_codegen1516e_encoding.codeGenerator.NonBasicTypeLedger;
import orbisoftware.hla_codegen1516e_encoding.codeGenerator.SharedResources.ElementType;
import orbisoftware.hla_pathbuilder.DatabaseAPI;
import orbisoftware.hla_pathbuilder.Utils;
import orbisoftware.hla_pathbuilder.db_classes.DbEnumeratedDatatype;
import orbisoftware.hla_shared.Utilities;

public class VariantRecordGenerator {

	public static int indentSpace;

	private Utils utils = new Utils();

	private LedgerEntry ledgerEntry;

	private String indentFormat = "";
	
	private List<String> nonBasicFields = new ArrayList<String>();
	
	private String variantClassName = "";
	
	private String variantDiscriminant = "";
	
	private boolean useVariantOrdering;
	
	private int[] variantOrdering;
	
	private int caseIncCounter;
	
	private int largestStructureMember;
	
	public VariantRecordGenerator(LedgerEntry ledgerEntry) {

		this.ledgerEntry = ledgerEntry;
		nonBasicFields.clear();
		largestStructureMember = 1;
	}
	
	public void setDefaults() {

		indentSpace = 0;
		caseIncCounter = 0;
	}
	
	private void depthIncSpace() {
		indentSpace = indentSpace + 3;
		
		if (indentSpace > 0)
			indentFormat = String.format("%" + (indentSpace) + "s", "");
	}
	
	private void depthCurSpace() {
		
		if (indentSpace > 0)
			indentFormat = String.format("%" + (indentSpace) + "s", "");
	}

	private void depthDecSpace() {
		indentSpace = indentSpace - 3;

		if (indentSpace < 1)
			indentSpace = 0;
		
		if (indentSpace > 0)
			indentFormat = String.format("%" + (indentSpace) + "s", "");
	}

	public Node findElementID(Node node, String elementID) {

		// Check if the node is an element node
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			// Get the attributes of the node
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null) {
				// Check if the node has the "id" attribute and if it matches the search id
				Node idAttr = attributes.getNamedItem("ID");
				if (idAttr != null && idAttr.getNodeValue().equals(elementID)) {
					return node; // Matching node found
				}
			}
		}

		// Recursively search the child nodes
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node found = findElementID(children.item(i), elementID); // Recursive call
			if (found != null) {
				return found; // Return the found node if a match is found
			}
		}

		// Return null if no matching node is found in this subtree
		return null;
	}
	
	public void setVariantOrderingArray(String variantClassName, String variantDiscriminant, int[] variantOrdering) {
		
		this.variantClassName = variantClassName;
		this.variantDiscriminant = variantDiscriminant;
		this.variantOrdering = variantOrdering;
		
		useVariantOrdering = true;
	}

	public void printHeader(String elementClassname, ElementType elementType) {
		
		System.out.println("package " + Utilities.packageRoot + "Common.VariantRecords;");

		System.out.println();

		CodeGeneratorJava.printCommonImports();
		
		System.out.println("@SuppressWarnings(\"unused\")");
		System.out.println("public class " + ledgerEntry.entryType + "_Encode {");
		System.out.println();
		
		depthIncSpace();

		System.out.println(indentFormat + "private Utilities utilities = new Utilities();");
	}

	public void generateAccessorsMutators(Node node, int depth) {

		final int activeDepth = 1; // This is the depth we are working with

		if (depth == activeDepth)
			processAccessorsMutatorsNode(node, depth);

		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {

			Node childNode = children.item(i);

			generateAccessorsMutators(childNode, depth + 1);

		}
	}

	private void processAccessorsMutatorsNode(Node node, int depth) {

		switch (node.getNodeType()) {

		case Node.ELEMENT_NODE:
			LedgerEntry ledgerEntry = new LedgerEntry();

			// Check and print attributes if any
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);

					switch (attribute.getNodeName()) {

					case "ID":
						ledgerEntry.entryID = attribute.getNodeValue();
						break;

					case "TEXT":
						String text = attribute.getNodeValue();

						String[] parts = text.split(" ");
						if (parts.length > 1) {
							ledgerEntry.entryType = parts[0];
							ledgerEntry.entryDataField = utils.convertToCamelCase(parts[1]);
						} else {
							Utils utils = new Utils();
							ledgerEntry.entryType = text;
							ledgerEntry.entryDataField = utils.convertToCamelCase(text);
						}
						break;

					case "TID":
						ledgerEntry.entryTID = attribute.getNodeValue();
						break;

					case "cardinality":
						ledgerEntry.entryCardinality = attribute.getNodeValue();
						break;

					case "classtype":
						ledgerEntry.entryClassType = attribute.getNodeValue();
						break;

					case "encoding":
						ledgerEntry.entryEncoding = attribute.getNodeValue();
						break;
						
					case "isDiscriminant":
						ledgerEntry.entryIsDiscriminant = Boolean.parseBoolean(attribute.getNodeValue());
						break;

					default:
						break;
					// System.out.println(indent + " Attribute: " + attribute.getNodeName() + " = "
					// + attribute.getNodeValue());
					}
				}
			}
			
			boolean nonBasicType = false;
			
			if (ledgerEntry.entryTID.equals("Basic")) {
				ledgerEntry.entryType = utils.getPrimitiveFromEncodingType(ledgerEntry.entryType);
				nonBasicType = false;
			} else if (ledgerEntry.entryTID.equals("Enumerated")) {
				ledgerEntry.entryType = ledgerEntry.entryType;
				nonBasicType = true;
			} else {
				NonBasicTypeLedger.getInstance().nonBasicTypeLedger.put(ledgerEntry.entryID, ledgerEntry);
				nonBasicType = true;
			}
			
			System.out.println();
			System.out.println(indentFormat + "// Class Variable");
			if (nonBasicType) {
				System.out.println(indentFormat + "private " + ledgerEntry.entryType + "_Encode " + ledgerEntry.entryDataField + 
						" = new " + ledgerEntry.entryType + "_Encode();");
			} else {
				System.out.println(indentFormat + "private " + ledgerEntry.entryType + " " + ledgerEntry.entryDataField + ";");
			}
			System.out.println();

			// Setter implementation
			System.out.println(indentFormat + "// Setter");
			if (nonBasicType) {
				System.out.println(indentFormat + "public void set" + utils.capitalizeFirstLetter(ledgerEntry.entryDataField) + "("
						+ ledgerEntry.entryType + "_Encode " + ledgerEntry.entryDataField + ") {");
			} else {
				System.out.println(indentFormat + "public void set" + utils.capitalizeFirstLetter(ledgerEntry.entryDataField) + "("
						+ ledgerEntry.entryType + " " + ledgerEntry.entryDataField + ") {");
			}
			depthIncSpace();
			System.out.println(
					indentFormat + "this." + ledgerEntry.entryDataField + " = " + ledgerEntry.entryDataField + ";");
			depthDecSpace();
			System.out.println(indentFormat + "}");
			System.out.println();

			// Getter implementation
			System.out.println(indentFormat + "// Getter");
			if (nonBasicType) {
				System.out.println(
					indentFormat + "public " + ledgerEntry.entryType + "_Encode get" + utils.capitalizeFirstLetter(ledgerEntry.entryDataField)  + "() {");
			} else {
				System.out.println(
					indentFormat + "public " + ledgerEntry.entryType + " get" + utils.capitalizeFirstLetter(ledgerEntry.entryDataField)  + "() {");
			}
			depthIncSpace();
			System.out.println(indentFormat + "return " + ledgerEntry.entryDataField + ";");
			depthDecSpace();
			System.out.println(indentFormat + "}");
			
			break;

		default:
			// System.out.println(indent + "Other Node Type: " + node.getNodeName());
			break;
		}
	}
	
	public void generateEncode(Node node, int depth) {

		final int activeDepth = 1; // This is the depth we are working with

		if (depth == activeDepth)
			processEncodeNode(node, depth);

		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {

			Node childNode = children.item(i);

			generateEncode(childNode, depth + 1);

		}
	}
	
	private void processEncodeNode(Node node, int depth) {
		
		switch (node.getNodeType()) {

		case Node.ELEMENT_NODE:
			LedgerEntry ledgerEntry = new LedgerEntry();

			// Check and print attributes if any
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);

					switch (attribute.getNodeName()) {

					case "ID":
						ledgerEntry.entryID = attribute.getNodeValue();
						break;

					case "TEXT":
						String text = attribute.getNodeValue();

						String[] parts = text.split(" ");
						if (parts.length > 1) {
							ledgerEntry.entryType = parts[0];
							ledgerEntry.entryDataField = utils.convertToCamelCase(parts[1]);
						} else {
							Utils utils = new Utils();
							ledgerEntry.entryType = text;
							ledgerEntry.entryDataField = utils.convertToCamelCase(text);
						}
						break;

					case "TID":
						ledgerEntry.entryTID = attribute.getNodeValue();
						break;

					case "cardinality":
						ledgerEntry.entryCardinality = attribute.getNodeValue();
						break;

					case "classtype":
						ledgerEntry.entryClassType = attribute.getNodeValue();
						break;

					case "encoding":
						ledgerEntry.entryEncoding = attribute.getNodeValue();
						break;

					case "isDiscriminant":
						ledgerEntry.entryIsDiscriminant = Boolean.parseBoolean(attribute.getNodeValue());
						break;
						
					default:
						break;
					// System.out.println(indent + " Attribute: " + attribute.getNodeName() + " = "
					// + attribute.getNodeValue());
					}
				}
			}
			
			String classPrimitive = null;
			String internalValue = null;
			
			if (ledgerEntry.entryTID.equals("Basic"))
				classPrimitive = utils.getClassFromEncodingType(ledgerEntry.entryType);
			else if (ledgerEntry.entryTID.equals("Enumerated")) {
				
				// Select all enumerated data types matching entryType
		    	DatabaseAPI databaseAPI = new DatabaseAPI();
		    	String enumeratedSelect = 
		    			"SELECT * FROM EnumeratedDatatype WHERE name = '" + ledgerEntry.entryType + "'";
		    	
		    	List<DbEnumeratedDatatype> list1 = databaseAPI.selectFromEnumeratedDatatypeTable(enumeratedSelect);
		    	
				for (DbEnumeratedDatatype var1 : list1) {

					internalValue = utils.getClassFromEncodingType(utils.convertFromRPRType(var1.type));
				}
		    	
				classPrimitive = ledgerEntry.entryType;	
			}

			this.depthIncSpace();
			this.depthIncSpace();
			
			System.out.println();
			
			// This is a possible discriminant field for the variant record
			if (ledgerEntry.entryTID.equals("Basic") && ledgerEntry.entryIsDiscriminant) {

				System.out.println(indentFormat + "// Align and write the " + ledgerEntry.entryDataField + " field");
				System.out.println(indentFormat + "utilities.insertPadding(buffer, bufferOffset, alignment);");
				System.out.println(indentFormat + "buffer.put(utilities.getBytesFrom" + classPrimitive + "(" + ledgerEntry.entryDataField + "));");
				System.out.println(indentFormat + "bufferOffset = buffer.position();");
				System.out.println();
				System.out.println(indentFormat + "switch (" + ledgerEntry.entryDataField + ") {");
				
				this.depthIncSpace();
				
				int fieldSize = utils.getNumberBytesFromEncodingType(ledgerEntry.entryType);
				if (fieldSize > largestStructureMember)
					largestStructureMember = fieldSize;
				
				caseIncCounter = 0;
				if (variantDiscriminant.equals(ledgerEntry.entryDataField.toLowerCase()))
					this.useVariantOrdering = true;
				else
					this.useVariantOrdering = false;
				
			// This is a possible discriminant field for the variant record
			} else if (ledgerEntry.entryTID.equals("Enumerated") && ledgerEntry.entryIsDiscriminant) {

				System.out.println(indentFormat + "// Align and write the " + classPrimitive + " field");
				System.out.println(indentFormat + "utilities.insertPadding(buffer, bufferOffset, alignment);");
				System.out.println(indentFormat + "buffer.put(utilities.getBytesFrom" + internalValue + "(" + ledgerEntry.entryDataField + ".value));");
				System.out.println(indentFormat + "bufferOffset = buffer.position();");
				System.out.println();
				System.out.println(indentFormat + "switch (" + ledgerEntry.entryDataField + ".value) {");
				
				this.depthIncSpace();
				
				int fieldSize = utils.getNumberBytesFromEncodingType(ledgerEntry.entryType);
				if (fieldSize > largestStructureMember)
					largestStructureMember = fieldSize;
				
				caseIncCounter = 0;
				if (variantDiscriminant.equals(ledgerEntry.entryDataField.toLowerCase()))
					this.useVariantOrdering = true;
				else
					this.useVariantOrdering = false;
			} else if (ledgerEntry.entryTID.equals("Basic")) {
				
				try {
					if (useVariantOrdering)
						System.out.println(indentFormat + "case " + variantOrdering[caseIncCounter] + ":");
					else
						System.out.println(indentFormat + "case " + caseIncCounter + ":");
				} catch (Exception e) {
					System.out.println("Exception in variant record Encode, when using variant ordering for class" + variantClassName);
				}
				
				System.out.println(indentFormat + "// Align and write the " + classPrimitive + " field");
				System.out.println(indentFormat + "utilities.insertPadding(buffer, bufferOffset, alignment);");
				System.out.println(indentFormat + "buffer.put(utilities.getBytesFrom" + classPrimitive + "(" + ledgerEntry.entryDataField + "));");
				System.out.println(indentFormat + "bufferOffset = buffer.position();");
				System.out.println(indentFormat + "break;");
				
				caseIncCounter++;
			
			} else {
				
				try {
					if (useVariantOrdering)
						System.out.println(indentFormat + "case " + variantOrdering[caseIncCounter] + ":");
					else
						System.out.println(indentFormat + "case " + caseIncCounter + ":");
				} catch (Exception e) {
					System.out.println("Exception in variant record Encode, when using variant ordering for class" + variantClassName);
				}
				
				System.out.println(indentFormat + "// Write the nested structure, aligned with largest field size");
				System.out.println(indentFormat + ledgerEntry.entryDataField + ".encode(buffer, alignment);");
				System.out.println(indentFormat + "bufferOffset = buffer.position();");
				System.out.println(indentFormat + "break;");
				
				nonBasicFields.add(ledgerEntry.entryDataField);
				
				caseIncCounter++;
			}
			this.depthDecSpace();
			this.depthDecSpace();
			
			break;

		default:
			// System.out.println(indent + "Other Node Type: " + node.getNodeName());
			break;
		}
	}
	
	public void generateDecode(Node node, int depth) {

		final int activeDepth = 1; // This is the depth we are working with

		if (depth == activeDepth)
			processDecodeNode(node, depth);

		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {

			Node childNode = children.item(i);

			generateDecode(childNode, depth + 1);

		}
	}
	
	private void processDecodeNode(Node node, int depth) {
		
		switch (node.getNodeType()) {

		case Node.ELEMENT_NODE:
			LedgerEntry ledgerEntry = new LedgerEntry();

			// Check and print attributes if any
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);

					switch (attribute.getNodeName()) {

					case "ID":
						ledgerEntry.entryID = attribute.getNodeValue();
						break;

					case "TEXT":
						String text = attribute.getNodeValue();

						String[] parts = text.split(" ");
						if (parts.length > 1) {
							ledgerEntry.entryType = parts[0];
							ledgerEntry.entryDataField = utils.convertToCamelCase(parts[1]);
						} else {
							Utils utils = new Utils();
							ledgerEntry.entryType = text;
							ledgerEntry.entryDataField = utils.convertToCamelCase(text);
						}
						break;

					case "TID":
						ledgerEntry.entryTID = attribute.getNodeValue();
						break;

					case "cardinality":
						ledgerEntry.entryCardinality = attribute.getNodeValue();
						break;

					case "classtype":
						ledgerEntry.entryClassType = attribute.getNodeValue();
						break;

					case "encoding":
						ledgerEntry.entryEncoding = attribute.getNodeValue();
						break;

					case "isDiscriminant":
						ledgerEntry.entryIsDiscriminant = Boolean.parseBoolean(attribute.getNodeValue());
						break;
						
					default:
						break;
					// System.out.println(indent + " Attribute: " + attribute.getNodeName() + " = "
					// + attribute.getNodeValue());
					}
				}
			}
			
			String classPrimitive = null;
			String internalValue = null;
			
			if (ledgerEntry.entryTID.equals("Basic"))
				classPrimitive = utils.getClassFromEncodingType(ledgerEntry.entryType);
			else if (ledgerEntry.entryTID.equals("Enumerated")) {
				
				// Select all enumerated data types
		    	DatabaseAPI databaseAPI = new DatabaseAPI();
		    	String enumeratedSelect = 
		    			"SELECT * FROM EnumeratedDatatype WHERE name = '" + ledgerEntry.entryType + "'";
		    	
		    	List<DbEnumeratedDatatype> list1 = databaseAPI.selectFromEnumeratedDatatypeTable(enumeratedSelect);
		    	
				for (DbEnumeratedDatatype var1 : list1) {

					internalValue = utils.getClassFromEncodingType(utils.convertFromRPRType(var1.type));
				}
		    	
				classPrimitive = ledgerEntry.entryType;	
			}
			
			System.out.println();

			this.depthIncSpace();
			
			// This is a possible discriminant field for the variant record
			if (ledgerEntry.entryTID.equals("Basic") && ledgerEntry.entryIsDiscriminant) {

				System.out.println(indentFormat + "// Align and read the " + classPrimitive + " field");
				System.out.println(indentFormat + "bufferOffset = utilities.align(bufferOffset, alignment);");
				System.out.println(indentFormat + "buffer.position(bufferOffset);");
				if (classPrimitive.equals("Boolean"))
					System.out.println(indentFormat + "bytes = new byte[1];");
				else
					System.out.println(indentFormat + "bytes = new byte[" + classPrimitive + ".BYTES];");
				System.out.println(indentFormat + "buffer.get(bytes);");
				System.out.println(indentFormat + ledgerEntry.entryDataField + " = utilities.get" + classPrimitive + "FromBytes(bytes);");
				if (classPrimitive.equals("Boolean"))
					System.out.println(indentFormat + "bufferOffset += 1;");
				else
					System.out.println(indentFormat + "bufferOffset += " + classPrimitive + ".BYTES;");
				System.out.println();
				System.out.println(indentFormat + "switch (" + ledgerEntry.entryDataField + ") {");
				
				this.depthIncSpace();
				
				caseIncCounter = 0;
				if (variantDiscriminant.equals(ledgerEntry.entryDataField.toLowerCase()))
					this.useVariantOrdering = true;
				else
					this.useVariantOrdering = false;
			} 
			// This is a possible discriminant field for the variant record
			else if (ledgerEntry.entryTID.equals("Enumerated") && ledgerEntry.entryIsDiscriminant) {

				System.out.println(indentFormat + "// Align and read the " + classPrimitive + " field");
				System.out.println(indentFormat + "bufferOffset = utilities.align(bufferOffset, alignment);");
				System.out.println(indentFormat + "buffer.position(bufferOffset);");
				if (internalValue.equals("Boolean"))
					System.out.println(indentFormat + "bytes = new byte[1];");
				else
					System.out.println(indentFormat + "bytes = new byte[" + internalValue + ".BYTES];");
				System.out.println(indentFormat + "buffer.get(bytes);");
				System.out.println(indentFormat + ledgerEntry.entryDataField + ".value = utilities.get" + internalValue + "FromBytes(bytes);");
				if (internalValue.equals("Boolean"))
					System.out.println(indentFormat + "bufferOffset += 1;");
				else
					System.out.println(indentFormat + "bufferOffset += " + internalValue + ".BYTES;");
				System.out.println();
				System.out.println(indentFormat + "switch (" + ledgerEntry.entryDataField + ".value) {");
				
				this.depthIncSpace();
				
				caseIncCounter = 0;
				if (variantDiscriminant.equals(ledgerEntry.entryDataField.toLowerCase()))
					this.useVariantOrdering = true;
				else
					this.useVariantOrdering = false;
			} else if (ledgerEntry.entryTID.equals("Basic")) {
				
				try {
					if (useVariantOrdering)
						System.out.println(indentFormat + "case " + variantOrdering[caseIncCounter] + ":");
					else
						System.out.println(indentFormat + "case " + caseIncCounter + ":");
				} catch (Exception e) {
					System.out.println("Exception in variant record Decode, when using variant ordering for class" + variantClassName);
				}
				
				System.out.println(indentFormat + "// Align and read the " + classPrimitive + " field");
				System.out.println(indentFormat + "bufferOffset = utilities.align(bufferOffset, alignment);");
				System.out.println(indentFormat + "buffer.position(bufferOffset);");
				if (classPrimitive.equals("Boolean"))
					System.out.println(indentFormat + "bytes = new byte[1];");
				else
					System.out.println(indentFormat + "bytes = new byte[" + classPrimitive + ".BYTES];");
				System.out.println(indentFormat + "buffer.get(bytes);");
				System.out.println(indentFormat + ledgerEntry.entryDataField + " = utilities.get" + classPrimitive + "FromBytes(bytes);");
				if (classPrimitive.equals("Boolean"))
					System.out.println(indentFormat + "bufferOffset += 1;");
				else
					System.out.println(indentFormat + "bufferOffset += " + classPrimitive + ".BYTES;");
				System.out.println(indentFormat + "break;");
				
				caseIncCounter++;
				
			} else {
				
				try {
					if (useVariantOrdering)
						System.out.println(indentFormat + "case " + variantOrdering[caseIncCounter] + ":");
					else
						System.out.println(indentFormat + "case " + caseIncCounter + ":");
				} catch (Exception e) {
					System.out.println("Exception in variant record Decode, when using variant ordering for class" + variantClassName);
				}
				
				System.out.println(indentFormat + "// Write the nested structure, aligned with largest field size");
				System.out.println(indentFormat + ledgerEntry.entryDataField + ".decode(buffer, alignment);");
				System.out.println(indentFormat + "bufferOffset = buffer.position();");
				System.out.println(indentFormat + "break;");
				
				caseIncCounter++;
			}
			
			this.depthDecSpace();
			
			break;

		default:
			// System.out.println(indent + "Other Node Type: " + node.getNodeName());
			break;
		}
	}
	
	public void generateAlignmentMethod() {
		
		depthDecSpace();
		System.out.println(indentFormat + "// Get the structure alignment");
		System.out.println(indentFormat + "public int getAlignment() {");
		System.out.println();
		
		depthIncSpace();
		
		System.out.println(indentFormat + "int largestStructureMember = " + this.largestStructureMember + ";");
		System.out.println();
		
		for (String fieldName : nonBasicFields) {
			
			System.out.println(indentFormat + "if (" + fieldName + ".getAlignment() > largestStructureMember)");
			
			depthIncSpace();
			System.out.println(indentFormat + "largestStructureMember = " + fieldName + ".getAlignment();");
			depthDecSpace();
			System.out.println();
		}
		
		System.out.println(indentFormat + "return largestStructureMember;");
		depthDecSpace();
		System.out.println(indentFormat + "}");
	}
	
	private void printNodeInfo(Node node, int depth) {
		// Create indentation based on depth
		String indent = " ".repeat(depth * 2);

		switch (node.getNodeType()) {

		case Node.ELEMENT_NODE:
			// Element node (tag)
			System.out.println(indent + "Element: " + node.getNodeName());

			// Check and print attributes if any
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);
					System.out.println(
							indent + "  Attribute: " + attribute.getNodeName() + " = " + attribute.getNodeValue());
				}
			}
			break;

		default:
			// System.out.println(indent + "Other Node Type: " + node.getNodeName());
			break;
		}
	}
}
