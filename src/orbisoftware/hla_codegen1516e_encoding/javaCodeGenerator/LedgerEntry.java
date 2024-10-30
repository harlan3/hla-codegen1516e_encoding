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

package orbisoftware.hla_codegen1516e_encoding.javaCodeGenerator;

public class LedgerEntry {

	public String entryID;
	public String entryType;
	public String entryDataField;
	public String entryTID;
	public String entryClassType;
	public String entryCardinality;
	public String entryEncoding;
	public boolean entryIsDiscriminant;
	public boolean entryHasBeenGenerated;
	
	public LedgerEntry() {
		
		entryID = "";
		entryType = "";
		entryDataField = "";
		entryTID = "";
		entryClassType = "";
		entryCardinality = "";
		entryEncoding = "";
		entryIsDiscriminant = false;
		entryHasBeenGenerated = false;	
	}
}
