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

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Utilities {

	public static String packageRoot = "orbisoftware.hla_1516e_encoding.";
	public static String packageRootDir = "orbisoftware" + File.separator + "hla_1516e_encoding";
	
	public byte[] getBytesFromBoolean(boolean value) {

		if (value)
			return new byte[] { (byte) 1 };
		else
			return new byte[] { (byte) 0 };
	}

	public byte[] getBytesFromByte(byte value) {

		byte bytes[] = new byte[1];
		bytes[0] = value;

		return bytes;
	}

	public byte[] getBytesFromShort(short value) {

		return ByteBuffer.allocate(2).putShort(value).array();
	}

	public byte[] getBytesFromInteger(int value) {

		return ByteBuffer.allocate(4).putInt(value).array();
	}

	public byte[] getBytesFromLong(long value) {

		return ByteBuffer.allocate(8).putLong(value).array();
	}

	public byte[] getBytesFromFloat(float value) {

		return ByteBuffer.allocate(4).putFloat(value).array();
	}

	public byte[] getBytesFromDouble(double value) {

		return ByteBuffer.allocate(8).putDouble(value).array();
	}

///////////////////////////////////////////////////////////////////

	public boolean getBooleanFromBytes(byte[] bytes) {

		if (bytes[0] == 1)
			return true;
		else
			return false;
	}
	
	public byte getByteFromBytes(byte[] bytes) {

		return ByteBuffer.wrap(bytes).get(0);
	}

	public short getShortFromBytes(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getShort();
	}

	public int getIntegerFromBytes(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getInt();
	}

	public long getLongFromBytes(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getLong();
	}

	public float getFloatFromBytes(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getFloat();
	}

	public double getDoubleFromBytes(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getDouble();
	}

	public String removeLastLetter(String text) {

		String substring = text.substring(0, text.length() - 1);
		return substring;
	}

	public String bytesToString(byte[] bytes) {

		String javaString = new String(bytes, StandardCharsets.UTF_8);

		int lastChar = (int) javaString.charAt(javaString.length() - 1);

		if (lastChar == 0)
			return removeLastLetter(javaString);
		else
			return javaString;
	}
		
    // Method to align the offset to the nearest multiple of alignment
    public int align(int offset, int alignment) {
        return (offset + alignment - 1) & ~(alignment - 1);
    }

    // Method to insert padding into the buffer
    public void insertPadding(DynamicBuffer buffer, int offset, int alignment) {
        int alignedOffset = align(offset, alignment);
        int paddingSize = alignedOffset - offset;

        for (int i = 0; i < paddingSize; i++) {
            buffer.put((byte) 0x00); // Insert padding bytes (0x00)
        }
    }
}
