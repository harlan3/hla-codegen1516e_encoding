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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orbisoftware.hla_codegen1516e_encoding.codeGenerator.CodeGeneratorJava;
import orbisoftware.hla_codegen1516e_encoding.codeGenerator.LedgerEntry;
import orbisoftware.hla_codegen1516e_encoding.codeGenerator.SharedResources.ElementType;
import orbisoftware.hla_pathbuilder.Utils;
import orbisoftware.hla_shared.Utilities;

// This array is of primitive type
public class PrefixedStringLengthGenerator {

	public static int indentSpace;

	private Utils utils = new Utils();

	private LedgerEntry ledgerEntry;

	private String indentFormat = "";
	
	public void setDefaults() {

		indentSpace = 0;
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

	public PrefixedStringLengthGenerator(LedgerEntry ledgerEntry) {

		this.ledgerEntry = ledgerEntry;
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

	public void printHeader(String elementClassname, ElementType elementType, LedgerEntry value) {
		
		System.out.println("package " + Utilities.packageRoot + "Common.PrefixedStringLength;");

		System.out.println();

		CodeGeneratorJava.printCommonImports();

		System.out.println(indentFormat + "import java.nio.charset.StandardCharsets;");
		System.out.println(indentFormat + "import orbisoftware.hla_shared.*;");
		System.out.println();
		System.out.println(indentFormat + "// HLAASCIIstringImp has a size prefix containing the length of the string");
		
		System.out.println(indentFormat + "@SuppressWarnings(\"unused\")");
		System.out.println(indentFormat + "public class " + ledgerEntry.entryType + "_Encode" + " {");
		System.out.println();
		
		depthIncSpace();
		System.out.println(indentFormat + "private Utilities utilities = new Utilities();");
		System.out.println();
		
		System.out.println(indentFormat + "// Constructor");
		System.out.println(indentFormat + "public " + ledgerEntry.entryType + "_Encode()" + " {");
		System.out.println();
		
		System.out.println(indentFormat + "}");
	}

	public void processAccessorsMutatorsNode(LedgerEntry ledgerEntry) {

		String primitiveClass;
		
		depthCurSpace();
		
		System.out.println();
		
		System.out.println(indentFormat + "// Class String");
		System.out.println(indentFormat + "private String internalClassRepresentation = new String();");
		System.out.println();
		
		System.out.println(indentFormat + "// Setter");
		System.out.println(indentFormat + "public void setString(String newString) {");
		System.out.println();
		depthIncSpace();
		
		System.out.println(indentFormat + "internalClassRepresentation = newString;");
		
		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
		
		System.out.println(indentFormat + "// Getter");
		System.out.println(indentFormat + "public String getString() {");
		System.out.println();
		depthIncSpace();
		
		System.out.println(indentFormat + "return internalClassRepresentation;");
		
		depthDecSpace();
		System.out.println(indentFormat + "}");
	}
	
	public void processEncodeNode(LedgerEntry ledgerEntry) {

		depthCurSpace();
		System.out.println();
		System.out.println(indentFormat + "// Encode outgoing data obtained from internal class representation into DynamicBuffer");
		System.out.println(indentFormat + "public void encode(DynamicBuffer buffer, int alignment) {");
		System.out.println();
		
		depthIncSpace();
		System.out.println(indentFormat + "buffer.put(utilities.getBytesFromInteger(internalClassRepresentation.length()));");
		System.out.println(indentFormat + "buffer.put(internalClassRepresentation.getBytes());");
		
		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
	}
	
	public void processDecodeNode(LedgerEntry ledgerEntry) {
				
		depthCurSpace();
		System.out.println(indentFormat + "// Decode incoming data obtained from DynamicBuffer into internal class representation");
		System.out.println(indentFormat + "public void decode(DynamicBuffer buffer, int alignment) {");
		System.out.println();
		
		depthIncSpace();

		System.out.println(indentFormat + "byte[] sizeBytes = new byte[Integer.BYTES];");
		System.out.println();
		
		System.out.println();
		System.out.println(indentFormat + "buffer.rewind();");
		System.out.println(indentFormat + "buffer.get(sizeBytes);");
		System.out.println(indentFormat + "int numElements = utilities.getIntegerFromBytes(sizeBytes);");
		System.out.println();
		System.out.println(indentFormat + "internalClassRepresentation.clear();");
		System.out.println();
		System.out.println(indentFormat + "byte[] stringBytes = new byte[numElements];");
		System.out.println(indentFormat + "buffer.get(stringBytes);");
		System.out.println();
		System.out.println(indentFormat + "internalClassRepresentation = new String(stringBytes, StandardCharsets.UTF_8);");

		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
	}
	
	public void generateAlignmentMethod(LedgerEntry ledgerEntry) {
		
		System.out.println(indentFormat + "// Get the structure alignment");
		System.out.println(indentFormat + "public int getAlignment() {");
		System.out.println();
		
		depthIncSpace();
		
		System.out.println(indentFormat + "int largestStructureMember = 1;");
		System.out.println();
		
		System.out.println(indentFormat + "return largestStructureMember;");
		depthDecSpace();
		System.out.println(indentFormat + "}");
		depthDecSpace();
		System.out.println("}");
	}
}