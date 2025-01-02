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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NonBasicTypeLedger {

	private static NonBasicTypeLedger instance = null;
	
	public Map<String, LedgerEntry> nonBasicTypeLedger = new ConcurrentHashMap<>();
	
    public static NonBasicTypeLedger getInstance()
    {
        if (instance == null)
        	instance = new NonBasicTypeLedger();
 
        return instance;
    }
    
    public void clearLedger() {
    	
    	nonBasicTypeLedger.clear();
    }
    
    public void displayLedger() {
    	
    	for (Map.Entry<String, LedgerEntry> entry : nonBasicTypeLedger.entrySet()) {
    		String key = entry.getKey();
    		LedgerEntry value = entry.getValue();
    		
    		System.out.println("entryID = " + value.entryID);
    		System.out.println("entryType = " + value.entryType);
    		System.out.println("entryDataField = " + value.entryDataField);
    		System.out.println("entryTID = " + value.entryTID);
    		System.out.println("entryClassType = " + value.entryClassType);
    		System.out.println("entryCardinality = " + value.entryCardinality);
    		System.out.println("entryEncoding = " + value.entryEncoding);
    		System.out.println("entryHasBeenGenerated = " + value.entryHasBeenGenerated);
    		System.out.println();
    	}
    }
}
