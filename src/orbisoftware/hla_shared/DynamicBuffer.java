/**
 * Copyright (C) 2025, Harlan Murphy
 * Use of this source code is governed by a BSD Zero Clause License
 * 
 * See project license.txt for details
*/

package orbisoftware.hla_shared;

import java.nio.ByteBuffer;

public class DynamicBuffer {
	
    private ByteBuffer buffer;
    private int currentCapacity = 0;
    private final int DEFAULT_CAPACITY = 1024;

    // Constructor to initialize with an initial capacity
    public DynamicBuffer() {
        buffer = ByteBuffer.allocate(DEFAULT_CAPACITY);
        buffer.limit(DEFAULT_CAPACITY);
        currentCapacity = DEFAULT_CAPACITY;
    }

    // Ensures the buffer has enough capacity to accommodate new data
    private void ensureCapacity(int requiredCapacity) {
    	
        if (buffer.remaining() < requiredCapacity) {
            // New capacity is either twice the current size or large enough to accommodate requiredCapacity
            int newCapacity = Math.max(buffer.capacity() * 2, buffer.capacity() + requiredCapacity);

            // Create a new buffer with the new capacity and copy existing data
            ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
            newBuffer.limit(newCapacity);
            currentCapacity = newCapacity;
            buffer.flip(); // Switch from writing mode to reading mode
            newBuffer.put(buffer); // Copy old data into the new buffer
            
            // Assign new buffer
            buffer = newBuffer;
        }
    }

    // Put a single byte into the buffer, dynamically resizing if necessary
    public void put(byte b) {
        ensureCapacity(1); // Ensure there is enough space for a single byte
        buffer.put(b); // Write the byte into the buffer
    }
    
    // Put data into the buffer, dynamically resizing if necessary
    public void put(byte[] bytes) {
        ensureCapacity(bytes.length); // Ensure there is enough space
        buffer.put(bytes); // Write the data into the buffer
    }
    
    // Get a single byte from the buffer
    public byte get() {
        return buffer.get();
    }

    // Get data from the buffer and fill the provided byte array
    public void get(byte[] bytes) {
        buffer.get(bytes);
    }

    // Returns the bytes that have been written to the buffer
    public byte[] getWrittenBytes() {
       
       int currentPosition = buffer.position();
       
       byte[] writtenBytes = new byte[currentPosition];
       buffer.rewind();
       buffer.get(writtenBytes); // Get the bytes that were written
       
       return writtenBytes;
    }
    
    // Returns the current position of the buffer which is the number of bytes have been written to the buffer
    public int position() {
        return buffer.position();
    }

    // Rewinds this buffer. The position is set to zero and the mark is discarded. 
    public void rewind() {
    	buffer.rewind();
    }
    
    // Set the position of the buffer
    public void position(int newPosition) {
        buffer.position(newPosition);
    }

    // Returns the current limit of the buffer
    public int limit() {
        return buffer.limit();
    }

    // Set the limit of the buffer
    public void limit(int newLimit) {
        buffer.limit(newLimit);
    }

    // Flip the buffer from writing mode to reading mode
    public void flip() {
        buffer.flip();
        buffer.limit(currentCapacity);
    }

    // Clear the buffer for new writing
    public void clear() {
        buffer.clear();
    }
}