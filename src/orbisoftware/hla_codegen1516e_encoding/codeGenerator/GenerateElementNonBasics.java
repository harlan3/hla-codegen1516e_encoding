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

package orbisoftware.hla_codegen1516e_encoding.codeGenerator;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import orbisoftware.hla_codegen1516e_encoding.codeGenerator.SharedResources.ElementType;
import orbisoftware.hla_codegen1516e_encoding.codeGeneratorTypes.*;
import orbisoftware.hla_pathbuilder.DatabaseAPI;
import orbisoftware.hla_pathbuilder.Utils;
import orbisoftware.hla_pathbuilder.db_classes.DbEnumeratedDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbVariantOrderingDatatype;
import orbisoftware.hla_shared.Utilities;

public class GenerateElementNonBasics {

	private int indentSpace = 3;
	private String indentFormat = "";
	private Utils utils = new Utils();

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

	public void generateClasses(Node baseNode, ElementType elementType, String elementName) {

		Iterator<LedgerEntry> valueIterator = NonBasicTypeLedger.getInstance().nonBasicTypeLedger.values().iterator();

		// This should continue until every entry in the ledger has been processed
		while (valueIterator.hasNext()) {

			LedgerEntry value = valueIterator.next();

			switch (value.entryTID) {
			
			case "Array":
				generateArrayClasses(baseNode, elementType, elementName, value);
				break;
				
			case "FixedRecord":
				implementFixedRecord(baseNode, elementType, elementName, value);
				break;
				
			case "VariantRecord":
				implementVariantRecord(baseNode, elementType, elementName, value);
			}
		}
	}

	private void generateArrayClasses(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		switch (value.entryEncoding) {

		case "HLAfixedArray":
			implementFixedArray(baseNode, elementType, elementName, value);
			break;

		case "HLAvariableArray":
			if (value.entryType.equals("HLAASCIIstringImp"))
				implementPrefixedStringLength(baseNode, elementType, elementName, value);
			else
				implementVariableArray(baseNode, elementType, elementName, value);
			break;
			
		case "RPRlengthlessArray":
			implementLengthlessArray(baseNode, elementType, elementName, value);
			break;

		case "RPRnullTerminatedArray":
			implementNullTerminatedArray(baseNode, elementType, elementName, value);
			break;
			
		case "RPRpaddingTo32Array":
			implementPaddingArray(baseNode, elementType, elementName, value);
			break;
			
		case "RPRpaddingTo64Array":
			implementPaddingArray(baseNode, elementType, elementName, value);
			break;	
		}
	}
			
	private void implementFixedRecord(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		try {
			final String fixedRecordsString = "codegen_java" + File.separator + Utilities.packageRootDir +
					File.separator + elementType.toString() + "s" +
					File.separator + elementName + File.separator + "FixedRecords";
			File fixedRecordsDir = new File(System.getProperty("user.dir") + File.separator + fixedRecordsString);

			FixedRecordGenerator fixedRecordGenerator = new FixedRecordGenerator(value);
			
			PrintStream outputStream = new PrintStream(
					new File(fixedRecordsDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			fixedRecordGenerator.setDefaults();
			Node foundNode = fixedRecordGenerator.findElementID(baseNode, value.entryID);
			fixedRecordGenerator.printHeader(elementName, elementType);
			fixedRecordGenerator.generateAccessorsMutators(foundNode, 0);
			System.out.println();

			fixedRecordGenerator.setDefaults();
			this.setDefaults();

			this.depthIncSpace();
			System.out.println(
					indentFormat + "// Encode outgoing data obtained from member variables into DynamicBuffer");
			System.out.println(indentFormat + "public void encode(DynamicBuffer buffer, int alignment) {");

			this.depthIncSpace();

			System.out.println();
			System.out.println(indentFormat + "int bufferOffset = buffer.position();");

			fixedRecordGenerator.setDefaults();
			FixedRecordGenerator.indentSpace = 3;
			fixedRecordGenerator.generateEncode(foundNode, 0);

			this.depthDecSpace();
			System.out.println(indentFormat + "}");
			System.out.println();

			System.out.println(
					indentFormat + "// Decode incoming data obtained from DynamicBuffer into member variables");
			System.out.println(indentFormat + "public void decode(DynamicBuffer buffer, int alignment) {");

			this.depthIncSpace();

			System.out.println();
			System.out.println(indentFormat + "int bufferOffset = buffer.position();");
			System.out.println(indentFormat + "byte[] bytes;");

			fixedRecordGenerator.generateDecode(foundNode, 0);

			this.depthDecSpace();
			System.out.println(indentFormat + "}");
			System.out.println();

			fixedRecordGenerator.generateAlignmentMethod();

			this.depthIncSpace();
			System.out.println("}"); // End of class

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}

	private void implementVariantRecord(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		try {
			final String variantRecordsString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" +
					File.separator + elementName + File.separator + "VariantRecords";
			File variantRecordsDir = new File(System.getProperty("user.dir") + File.separator + variantRecordsString);
		
			VariantRecordGenerator variantRecordGenerator = new VariantRecordGenerator(value);
			
			// Select variant ordering for entry type
	    	DatabaseAPI databaseAPI = new DatabaseAPI();
	    	String variantOrderingSelect = 
	    			"SELECT * FROM VariantOrdering WHERE variant = '" + value.entryType + "'";
	    	
	    	List<DbVariantOrderingDatatype> list1 = databaseAPI.selectFromVariantOrderingDatatypeTable(variantOrderingSelect);
	    	
			for (DbVariantOrderingDatatype var1 : list1) {

				String variant = var1.variant;
				String discrimant = var1.discriminant.toLowerCase();
				
		        // Split the string by commas
		        String[] strArray = var1.ordering.split(",");

		        // Create an integer array to store the converted values
		        int[] intArray = new int[strArray.length];

		        // Convert each element to an integer and store it in the int array
		        for (int i = 0; i < strArray.length; i++) {
		            intArray[i] = Integer.parseInt(strArray[i].trim());
		        }
		           
		        variantRecordGenerator.setVariantOrderingArray(variant, discrimant, intArray);
			}

			PrintStream outputStream = new PrintStream(
					new File(variantRecordsDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			variantRecordGenerator.setDefaults();
			Node foundNode = variantRecordGenerator.findElementID(baseNode, value.entryID);
			variantRecordGenerator.printHeader(elementName, elementType);
			variantRecordGenerator.generateAccessorsMutators(foundNode, 0);
			System.out.println();

			variantRecordGenerator.setDefaults();
			this.setDefaults();

			this.depthIncSpace();
			System.out.println(
					indentFormat + "// Encode outgoing data obtained from member variables into DynamicBuffer");
			System.out.println(indentFormat + "public void encode(DynamicBuffer buffer, int alignment) {");

			this.depthIncSpace();

			System.out.println();
			System.out.println(indentFormat + "int bufferOffset = buffer.position();");

			variantRecordGenerator.setDefaults();
			FixedRecordGenerator.indentSpace = 3;
			variantRecordGenerator.generateEncode(foundNode, 0);

			System.out.println(indentFormat + "}");
			this.depthDecSpace();
			System.out.println(indentFormat + "}");
			System.out.println();

			System.out.println(
					indentFormat + "// Decode incoming data obtained from DynamicBuffer into member variables");
			System.out.println(indentFormat + "public void decode(DynamicBuffer buffer, int alignment) {");

			this.depthIncSpace();

			System.out.println();
			System.out.println(indentFormat + "int bufferOffset = buffer.position();");
			System.out.println(indentFormat + "byte[] bytes;");

			variantRecordGenerator.generateDecode(foundNode, 0);

			System.out.println(indentFormat + "}");
			this.depthDecSpace();
			System.out.println(indentFormat + "}");
			System.out.println();
			
			variantRecordGenerator.generateAlignmentMethod();

			this.depthIncSpace();
			System.out.println("}"); // End of class

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void implementPrefixedStringLength(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {
		
		try {
			final String prefixedStringLengthString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" + 
					File.separator + elementName + File.separator + "PrefixedStringLength";
			File prefixedStringLengthDir = new File(System.getProperty("user.dir") + File.separator + prefixedStringLengthString);
			
			PrintStream outputStream = new PrintStream(
					new File(prefixedStringLengthDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);
		
			PrefixedStringLengthGenerator prefixedStringLengthGenerator = new PrefixedStringLengthGenerator(value);
			prefixedStringLengthGenerator.setDefaults();

			prefixedStringLengthGenerator.printHeader(elementName, elementType, value);
			prefixedStringLengthGenerator.processAccessorsMutatorsNode(value);
			prefixedStringLengthGenerator.processEncodeNode(value);
			prefixedStringLengthGenerator.processDecodeNode(value);
			prefixedStringLengthGenerator.generateAlignmentMethod(value);

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementVariableArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String variableArraysString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" + 
					File.separator + elementName + File.separator + "VariableArrays";
			File variableArraysDir = new File(System.getProperty("user.dir") + File.separator + variableArraysString);

			PrintStream outputStream = new PrintStream(
					new File(variableArraysDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType;
			
			if (utils.isPrimitiveHLAClass(value.entryClassType)) {
				encodingType = utils.getClassFromEncodingType(value.entryClassType);
				value.entryClassType = encodingType;
			} else {
				encodingType = utils.getEncodingType(value.entryClassType);
			}

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				
				VariableArrayType1Generator variableArrayType1Generator = new VariableArrayType1Generator(value);
				variableArrayType1Generator.setDefaults();

				variableArrayType1Generator.printHeader(elementName, elementType, value);
				variableArrayType1Generator.processAccessorsMutatorsNode(value);
				variableArrayType1Generator.processEncodeNode(value);
				variableArrayType1Generator.processDecodeNode(value);
				variableArrayType1Generator.generateAlignmentMethod(value);
				
			} else { // This array is of non primitive type
				
				VariableArrayType2Generator variableArrayType2Generator = new VariableArrayType2Generator(value);
				variableArrayType2Generator.setDefaults();

				variableArrayType2Generator.printHeader(elementName, elementType, value);
				variableArrayType2Generator.processAccessorsMutatorsNode(value);
				variableArrayType2Generator.processGetElementSizeMethod(value);
				variableArrayType2Generator.processEncodeNode(value);
				variableArrayType2Generator.processDecodeNode(value);
				variableArrayType2Generator.generateAlignmentMethod(value);
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementLengthlessArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String lengthlessString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" + 
					File.separator + elementName + File.separator + "LengthlessArrays";
			File lengthlessArraysDir = new File(System.getProperty("user.dir") + File.separator + lengthlessString);

			PrintStream outputStream = new PrintStream(
					new File(lengthlessArraysDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType;
			
			if (utils.isPrimitiveHLAClass(value.entryClassType)) {
				encodingType = utils.getClassFromEncodingType(value.entryClassType);
				value.entryClassType = encodingType;
			} else {
				encodingType = utils.getEncodingType(value.entryClassType);
			}

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				
				LengthlessType1Generator lengthlessType1Generator = new LengthlessType1Generator(value);
				lengthlessType1Generator.setDefaults();

				lengthlessType1Generator.printHeader(elementName, elementType, value);
				lengthlessType1Generator.processAccessorsMutatorsNode(value);
				lengthlessType1Generator.processEncodeNode(value);
				lengthlessType1Generator.processDecodeNode(value);
				lengthlessType1Generator.generateAlignmentMethod(value);
				
			} else { // This array is of non primitive type
			
				LengthlessType2Generator lengthlessType2Generator = new LengthlessType2Generator(value);
				lengthlessType2Generator.setDefaults();

				lengthlessType2Generator.printHeader(elementName, elementType, value);
				lengthlessType2Generator.processAccessorsMutatorsNode(value);
				lengthlessType2Generator.processGetElementSizeMethod(value);
				lengthlessType2Generator.processEncodeNode(value);
				lengthlessType2Generator.processDecodeNode(value);
				lengthlessType2Generator.generateAlignmentMethod(value);
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementPaddingArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String miscString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + "Misc";
			File miscDir = new File(System.getProperty("user.dir") + File.separator + miscString);

			PrintStream outputStream = new PrintStream(
					new File(miscDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType;
			
			if (utils.isPrimitiveHLAClass(value.entryClassType)) {
				encodingType = utils.getClassFromEncodingType(value.entryClassType);
				value.entryClassType = encodingType;
			} else {
				encodingType = utils.getEncodingType(value.entryClassType);
			}

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				
				PaddingArrayGenerator paddingArrayGenerator = new PaddingArrayGenerator(value);
				paddingArrayGenerator.setDefaults();

				paddingArrayGenerator.printHeader(elementName, elementType, value);
				paddingArrayGenerator.processAccessorsMutatorsNode(value);
				paddingArrayGenerator.processEncodeNode(value);
				paddingArrayGenerator.processDecodeNode(value);
				paddingArrayGenerator.generateAlignmentMethod(value);
				
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementFixedArray(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		try {
			final String fixedArraysString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + 
					elementType.toString() + "s" +
					File.separator + elementName + File.separator + "FixedArrays";
			File fixedArraysDir = new File(System.getProperty("user.dir") + File.separator + fixedArraysString);

			PrintStream outputStream = new PrintStream(
					new File(fixedArraysDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType = utils.getClassFromEncodingType(value.entryClassType);

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				FixedArrayType1Generator fixedArrayType1Generator = new FixedArrayType1Generator(value);
				fixedArrayType1Generator.setDefaults();

				fixedArrayType1Generator.printHeader(elementName, elementType, value);
				fixedArrayType1Generator.processAccessorsMutatorsNode(value);
				fixedArrayType1Generator.processGetElementSizeMethod(value);
				fixedArrayType1Generator.processEncodeNode(value);
				fixedArrayType1Generator.processDecodeNode(value);
				fixedArrayType1Generator.generateAlignmentMethod(value);
			} else { // This array is of non primitive type
				FixedArrayType2Generator fixedArrayType2Generator = new FixedArrayType2Generator(value);
				fixedArrayType2Generator.setDefaults();

				fixedArrayType2Generator.printHeader(elementName, elementType, value);
				fixedArrayType2Generator.processAccessorsMutatorsNode(value);
				fixedArrayType2Generator.processGetElementSizeMethod(value);
				fixedArrayType2Generator.processEncodeNode(value);
				fixedArrayType2Generator.processDecodeNode(value);
				fixedArrayType2Generator.generateAlignmentMethod(value);
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}

	private void implementNullTerminatedArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String nullTerminatedString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + 
					elementType.toString() + "s" +
					File.separator + elementName + File.separator + "NullTerminatedArrays";
			File nullTerminatedDir = new File(System.getProperty("user.dir") + File.separator + nullTerminatedString);

			PrintStream outputStream = new PrintStream(
					new File(nullTerminatedDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			NullTerminatedGenerator nullTerminatedGenerator = new NullTerminatedGenerator(value);
			nullTerminatedGenerator.setDefaults();

			nullTerminatedGenerator.printHeader(elementName, elementType, value);
			nullTerminatedGenerator.processAccessorsMutatorsNode(value);
			nullTerminatedGenerator.processEncodeNode(value);
			nullTerminatedGenerator.processDecodeNode(value);
			nullTerminatedGenerator.generateAlignmentMethod(value);

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
}
