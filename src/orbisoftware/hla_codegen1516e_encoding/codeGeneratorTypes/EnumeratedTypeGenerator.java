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

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import orbisoftware.hla_codegen1516e_encoding.codeGenerator.LedgerEntry;
import orbisoftware.hla_pathbuilder.DatabaseAPI;
import orbisoftware.hla_pathbuilder.Utils;
import orbisoftware.hla_pathbuilder.db_classes.DbEnumeratedDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbEnumeratorDatatype;
import orbisoftware.hla_shared.Utilities;

public class EnumeratedTypeGenerator {

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

	public EnumeratedTypeGenerator() {
		
	}
	
	public void generateEnums() {

		try {
			
			final String enumsString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + "Enums";
			File enumsDir = new File(System.getProperty("user.dir") + File.separator + enumsString);
			
			// Select all enumerated data types
	    	DatabaseAPI databaseAPI = new DatabaseAPI();
	    	String enumeratedSelect = "SELECT * FROM EnumeratedDatatype";
	    	
	    	List<DbEnumeratedDatatype> list1 = databaseAPI.selectFromEnumeratedDatatypeTable(enumeratedSelect);
	    	ArrayList<String> enumeratorList = new ArrayList<String>();

			for (DbEnumeratedDatatype var1 : list1) {

				PrintStream outputStream =
						new PrintStream(new File(enumsDir + File.separator + var1.name + ".java"));
				PrintStream console = System.out;
				System.setOut(outputStream);

				System.out.println("package " + Utilities.packageRoot + "Enums;");
	    		System.out.println();
	    		
				System.out.println("import " + Utilities.sharedRoot);
				System.out.println();
				
				System.out.println("public class " + var1.name + " {");
				System.out.println();

				System.out.println("   // Constructor");
				System.out.println("   public " + var1.name + "() {");
				System.out.println();
				System.out.println("   }");
				System.out.println();
				
				System.out.println("   private Utilities utilities = new Utilities();");
				System.out.println();
				
				String internalValue = utils.getPrimitiveFromEncodingType(utils.convertFromRPRType(var1.type));
				System.out.println("   public " + internalValue + " value = 0;");
				System.out.println();
				
				String enumeratorSelect = "Select * FROM EnumeratorDatatype WHERE parentObject = '" + var1.id + "'";

				// Have to keep a list of enumerators to avoid adding duplicate names
				enumeratorList.clear();

				System.out.println("   // Enumerator Values");
				List<DbEnumeratorDatatype> list2 = databaseAPI.selectFromEnumeratorDatatypeTable(enumeratorSelect);

				for (DbEnumeratorDatatype var2 : list2) {

					String validName = var2.name.replaceAll("-", "_");

					// Only add the enumerator if it hasn't already been added
					if (!enumeratorList.contains(validName)) {
						System.out.println(
								"   " + "public static final int " + validName + " = " + var2.ordinalValue + ";");
						enumeratorList.add(validName);
					}
				}

				System.out.println();
				System.out.println("   // Get Enumerator Name from the integer Enumerator value");
				System.out.println("   public static String getName(int enumValue) {");
				System.out.println();
				System.out.println("      String returnValue = \"\";");
				System.out.println();
				System.out.println("      switch(enumValue) {");
				System.out.println();

				// Have to keep a list of enumerators to avoid adding duplicate names
				enumeratorList.clear();

				for (DbEnumeratorDatatype var3 : list2) {

					String validName = var3.name.replaceAll("-", "_");

					// Only add the enumerator if it hasn't already been added
					if (!enumeratorList.contains(validName)) {
						System.out.println("      case " + var3.ordinalValue + ":");
						System.out.println("         " + "returnValue = \"" + validName + "\";");
						System.out.println("         " + "break;");
						System.out.println();
						enumeratorList.add(validName);
					}
				}

				System.out.println("      }");
				System.out.println();
				System.out.println("      return returnValue;");
				System.out.println("   }");
				System.out.println();
				
				depthCurSpace();
				
				processEncodeNode(internalValue);
				
				processDecodeNode(internalValue);
				
				generateAlignmentMethod(internalValue);
				
				System.out.println("}");

				System.setOut(console);
				
				setDefaults();
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	
	public void processEncodeNode(String internalValue) {
		
		depthIncSpace();
		System.out.println(indentFormat + 
				"// Encode outgoing data obtained from internal type into DynamicBuffer");
		System.out.println(indentFormat + "public void encode(DynamicBuffer buffer, int alignment) {");
		System.out.println();
		
		depthIncSpace();
		System.out.println(indentFormat + "buffer.put(utilities.getBytesFrom" + 
				utils.getClassFromPrimitive(internalValue) + "(value));");
		depthDecSpace();
		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
	}
	
	public void processDecodeNode(String internalValue) {
		
		depthIncSpace();
		System.out.println(indentFormat + 
				"// Decode incoming data obtained from DynamicBuffer into internal type");
		System.out.println(indentFormat + "public void decode(DynamicBuffer buffer, int alignment) {");
		System.out.println();
		
		depthIncSpace();
		System.out.println(indentFormat + "byte[] elementBytes = new byte[" + 
				utils.getNumberBytesFromPrimitiveType(internalValue) + "];");
		System.out.println(indentFormat + "buffer.get(elementBytes);");
		System.out.println(indentFormat + "value = utilities.get" + 
				utils.getClassFromPrimitive(internalValue) + "FromBytes(elementBytes);");
		
		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
	}
	
	public void generateAlignmentMethod(String internalValue) {
		
		System.out.println(indentFormat + "// Get the structure alignment");
		System.out.println(indentFormat + "public int getAlignment() {");
		System.out.println();
		
		depthIncSpace();
		
		System.out.println(indentFormat + "int largestStructureMember = " +
				utils.getNumberBytesFromPrimitiveType(internalValue) + ";");
		System.out.println();
		
		System.out.println(indentFormat + "return largestStructureMember;");
		depthDecSpace();
		System.out.println(indentFormat + "}");
		//depthDecSpace();
		//System.out.println("}");
	}
}
