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

package orbisoftware.hla_codegen1516e_encoding;

import java.util.ArrayList;
import java.util.List;

import jargs.gnu.CmdLineParser;
import orbisoftware.hla_codegen1516e_encoding.codeGeneratorTypes.EnumeratedTypeGenerator;
import orbisoftware.hla_codegen1516e_encoding.javaCodeGenerator.CodeGeneratorJava;
import orbisoftware.hla_pathbuilder.HlaPathBuilder;


public class MainApplication {
	
	private static String fomFilename = "";
	private static String elementModel = "";
	
	private static String encoderLanguage = "";
	
	public static List<String> elementObjectList = new ArrayList<String>();
	public static List<String> elementInteractionList = new ArrayList<String>();
	
	private static void printUsage() {

		System.out.println("Usage: HlaCodeGen1516e [OPTION]...");
		System.out.println("Generate proto specs and mindmap files.");
		System.out.println();
		System.out.println("   -f, --fom          FOM file used by HLA federation");
		System.out.println("   -e, --element      Element model file which controls which of the FOM models are generated");
		System.out.println("   -l, --language     Language used for encoders. Either \"java\" or \"c++\" is valid.");
		System.out.println("   -h, --help         Show this help message");

	}
	
	public static void main(String[] args) {

		MainApplication mainApplication = new MainApplication();
		HlaPathBuilder hlaPathBuilder = new HlaPathBuilder();
		CodeGeneratorJava codeGeneratorJava = new CodeGeneratorJava();
		CmdLineParser parser = new CmdLineParser();

		CmdLineParser.Option fomOption = parser.addStringOption('f', "fom");
		CmdLineParser.Option elementOption = parser.addStringOption('e', "element");
		CmdLineParser.Option languageOption = parser.addStringOption('l', "language");
		CmdLineParser.Option helpOption = parser.addBooleanOption('h', "help");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.out.println(e.getMessage());
			printUsage();
			System.exit(0);
		}

		String fomValue = (String) parser.getOptionValue(fomOption);
		String elementValue = (String) parser.getOptionValue(elementOption);
		String languageValue = (String) parser.getOptionValue(languageOption);
		Boolean helpValue = (Boolean) parser.getOptionValue(helpOption);

		if ((helpValue != null) || (fomValue == null || elementValue == null || languageValue == null)) {
			printUsage();
			System.exit(0);
		} else if (!languageValue.equals("java") && !languageValue.equals("c++")) {
			
			if ((helpValue != null) || (fomValue == null || elementValue == null)) {
				printUsage();
				System.exit(0);
			}
		}

		fomFilename = fomValue;
		elementModel = elementValue;
		encoderLanguage = languageValue;
		
		codeGeneratorJava.createRootDirectories();
		
		codeGeneratorJava.generateEnumPlaceHolderFile();
		codeGeneratorJava.generateMiscPlaceHolderFile();
		
		hlaPathBuilder.generateDatabase(fomFilename, elementModel, "Encode");
		
		codeGeneratorJava.generateCode();
		
		EnumeratedTypeGenerator enumeratedTypeGenerator = new EnumeratedTypeGenerator();
		enumeratedTypeGenerator.generateEnums();
	}
}