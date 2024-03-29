package com.jmatio.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.jmatio.common.MatDataTypes;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;

/**
 * MAT-file writer.
 * 
 * Usage:
 * 
 * <pre>
 * <code>
 * //1. First create example arrays
 * double[] src = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
 * MLDouble mlDouble = new MLDouble( "double_arr", src, 3 );
 * MLChar mlChar = new MLChar( "char_arr", "I am dummy" );
 * 
 * //2. write arrays to file
 * ArrayList<MLArray> list = new ArrayList<MLArray>();
 * list.add( mlDouble );
 * list.add( mlChar );
 * 
 * new MatFileWriter( "mat_file.mat", list );
 * </code>
 * </pre>
 * 
 * this is "equal" to Matlab commands:
 * 
 * <pre>
 * <code>
 * >> double_arr = [ 1 2; 3 4; 5 6];
 * >> char_arr = 'I am dummy';
 * >>
 * >> save('mat_file.mat', 'double_arr', 'char_arr');
 * </pre></code>
 * 
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
public class MatFileWriter {

    private MatFileWriter() {
    }

    private static void initialize(FileOutputStream outputStream, Collection<MLArray> data) throws IOException {
	write(outputStream.getChannel(), data);
    }

    /**
     * Writes <code>MLArrays</code> into file created from <code>filepath</code>.
     * 
     * @param filepath
     *            the absolute file path of a MAT-file to which data is written
     * @param data
     *            the collection of <code>{@link MLArray}</code> objects
     * @throws IOException
     *             if error occurred during MAT-file writing
     */
    public static void write(String filepath, Collection<MLArray> data)
	    throws IOException {
	write(new File(filepath), data);
    }

    /**
     * Writes <code>MLArrays</code> into <code>File</code>
     * 
     * @param file
     *            the MAT-file to which data is written
     * @param data
     *            the collection of <code>{@link MLArray}</code> objects
     * @throws IOException
     *             if error occurred during MAT-file writing
     */
    public static void write(File file, Collection<MLArray> data)
	    throws IOException {

	try (FileOutputStream fos = new FileOutputStream(file)) {
	    initialize(fos, data);
	}
    }

    /**
     * Writes <code>MLArrays</code> into <code>WritableByteChannel</code>.
     * 
     * @param channel
     *            the channel to write to
     * @param data
     *            the collection of <code>{@link MLArray}</code> objects
     * @throws IOException
     *             if writing fails
     */
    private static void write(WritableByteChannel channel,
	    Collection<MLArray> data) throws IOException {
	try {
	    // write header
	    writeHeader(channel);

	    // write data
	    for (MLArray matrix : data) {
		// prepare buffer for MATRIX data
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		// write MATRIX bytes into buffer
		writeMatrix(dos, matrix);

		// compress data to save storage
		Deflater compresser = new Deflater();

		byte[] input = baos.toByteArray();

		try (ByteArrayOutputStream compressed = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(
				new DeflaterOutputStream(compressed, compresser))) {

		    dout.write(input);

		    dout.close();
		    compressed.close();

		    // write COMPRESSED tag and compressed data into output channel
		    byte[] compressedBytes = compressed.toByteArray();
		    ByteBuffer buf = ByteBuffer.allocateDirect(2 * 4 /* Int size */ + compressedBytes.length);
		    buf.putInt(MatDataTypes.miCOMPRESSED);
		    buf.putInt(compressedBytes.length);
		    buf.put(compressedBytes);

		    buf.flip();
		    channel.write(buf);
		}
	    }
	} finally {
	    channel.close();
	}
    }

    /**
     * Writes MAT-file header into <code>OutputStream</code>
     * 
     * @param os
     *            <code>OutputStream</code>
     * @throws IOException
     */
    private static void writeHeader(WritableByteChannel channel) throws IOException {
	// write descriptive text
	MatFileHeader header = MatFileHeader.createHeader();
	char[] dest = new char[116];
	char[] src = header.getDescription().toCharArray();
	System.arraycopy(src, 0, dest, 0, src.length);

	byte[] endianIndicator = header.getEndianIndicator();

	ByteBuffer buf = ByteBuffer.allocateDirect(dest.length * 2 /* Char size */ + 2 + endianIndicator.length);

	for (int i = 0; i < dest.length; i++) {
	    buf.put((byte) dest[i]);
	}
	// write subsyst data offset
	buf.position(buf.position() + 8);

	// write version
	int version = header.getVersion();
	buf.put((byte) (version >> 8));
	buf.put((byte) version);

	buf.put(endianIndicator);

	buf.flip();
	channel.write(buf);
    }

    /**
     * Writes MATRIX into <code>OutputStream</code>.
     * 
     * @param os
     *            - <code>OutputStream</code>
     * @param array
     *            - a <code>MLArray</code>
     * @throws IOException
     */
    private static void writeMatrix(DataOutputStream output, MLArray array) throws IOException {
	OSArrayTag tag;
	ByteArrayOutputStream buffer;
	DataOutputStream bufferDOS;
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DataOutputStream dos = new DataOutputStream(baos);

	// flags
	writeFlags(dos, array);

	// dimensions
	writeDimensions(dos, array);

	// array name
	writeName(dos, array);

	switch (array.getType()) {
	case MLArray.mxCHAR_CLASS:
	    // write char data
	    buffer = new ByteArrayOutputStream();
	    bufferDOS = new DataOutputStream(buffer);
	    Character[] ac = ((MLChar) array).exportChar();
	    for (int i = 0; i < ac.length; i++) {
		bufferDOS.writeByte((byte) ac[i].charValue());
	    }
	    tag = new OSArrayTag(MatDataTypes.miUTF8, buffer.toByteArray());
	    tag.writeTo(dos);

	    break;
	case MLArray.mxDOUBLE_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miDOUBLE,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miDOUBLE,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxSINGLE_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miSINGLE,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miSINGLE,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxUINT8_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miUINT8,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miUINT8,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxINT8_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miINT8,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miINT8,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxUINT16_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miUINT16,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miUINT16,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxINT16_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miINT16,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miINT16,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxUINT32_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miUINT32,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miUINT32,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxINT32_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miINT32,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miINT32,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxINT64_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miINT64,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miINT64,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxUINT64_CLASS:

	    tag = new OSArrayTag(MatDataTypes.miUINT64,
		    ((MLNumericArray<?>) array).getRealByteBuffer());
	    tag.writeTo(dos);

	    // write real imaginary
	    if (array.isComplex()) {
		tag = new OSArrayTag(MatDataTypes.miUINT64,
			((MLNumericArray<?>) array).getImaginaryByteBuffer());
		tag.writeTo(dos);
	    }
	    break;
	case MLArray.mxSTRUCT_CLASS:
	    // field name length
	    int itag = 4 << 16 | MatDataTypes.miINT32 & 0xffff;
	    dos.writeInt(itag);
	    dos.writeInt(((MLStructure) array).getMaxFieldLenth());

	    // get field names
	    tag = new OSArrayTag(MatDataTypes.miINT8, ((MLStructure) array).getKeySetToByteArray());
	    tag.writeTo(dos);

	    for (MLArray a : ((MLStructure) array).getAllFields()) {
		writeMatrix(dos, a);
	    }
	    break;
	case MLArray.mxCELL_CLASS:
	    for (MLArray a : ((MLCell) array).cells()) {
		writeMatrix(dos, a);
	    }
	    break;
	case MLArray.mxSPARSE_CLASS:
	    int[] ai;
	    // write ir
	    buffer = new ByteArrayOutputStream();
	    bufferDOS = new DataOutputStream(buffer);
	    ai = ((MLSparse) array).getIR();
	    for (int i : ai) {
		bufferDOS.writeInt(i);
	    }
	    tag = new OSArrayTag(MatDataTypes.miINT32, buffer.toByteArray());
	    tag.writeTo(dos);
	    // write jc
	    buffer = new ByteArrayOutputStream();
	    bufferDOS = new DataOutputStream(buffer);
	    ai = ((MLSparse) array).getJC();
	    for (int i : ai) {
		bufferDOS.writeInt(i);
	    }
	    tag = new OSArrayTag(MatDataTypes.miINT32, buffer.toByteArray());
	    tag.writeTo(dos);
	    // write real
	    buffer = new ByteArrayOutputStream();
	    bufferDOS = new DataOutputStream(buffer);

	    Double[] ad = ((MLSparse) array).exportReal();

	    for (int i = 0; i < ad.length; i++) {
		bufferDOS.writeDouble(ad[i].doubleValue());
	    }

	    tag = new OSArrayTag(MatDataTypes.miDOUBLE, buffer.toByteArray());
	    tag.writeTo(dos);
	    // write real imaginary
	    if (array.isComplex()) {
		buffer = new ByteArrayOutputStream();
		bufferDOS = new DataOutputStream(buffer);
		ad = ((MLSparse) array).exportImaginary();
		for (int i = 0; i < ad.length; i++) {
		    bufferDOS.writeDouble(ad[i].doubleValue());
		}
		tag = new OSArrayTag(MatDataTypes.miDOUBLE, buffer.toByteArray());
		tag.writeTo(dos);
	    }
	    break;
	default:
	    throw new MatlabIOException("Cannot write matrix of type: " + MLArray.typeToString(array.getType()));

	}

	// write matrix
	output.writeInt(MatDataTypes.miMATRIX); // matrix tag
	output.writeInt(baos.size()); // size of matrix
	output.write(baos.toByteArray()); // matrix data
    }

    /**
     * Writes MATRIX flags into <code>OutputStream</code>.
     * 
     * @param os
     *            - <code>OutputStream</code>
     * @param array
     *            - a <code>MLArray</code>
     * @throws IOException
     */
    private static void writeFlags(DataOutputStream os, MLArray array) throws IOException {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	DataOutputStream bufferDOS = new DataOutputStream(buffer);

	bufferDOS.writeInt(array.getFlags());

	if (array.isSparse()) {
	    bufferDOS.writeInt(((MLSparse) array).getMaxNZ());
	} else {
	    bufferDOS.writeInt(0);
	}
	OSArrayTag tag = new OSArrayTag(MatDataTypes.miUINT32, buffer.toByteArray());
	tag.writeTo(os);

    }

    /**
     * Writes MATRIX dimensions into <code>OutputStream</code>.
     * 
     * @param os
     *            - <code>OutputStream</code>
     * @param array
     *            - a <code>MLArray</code>
     * @throws IOException
     */
    private static void writeDimensions(DataOutputStream os, MLArray array) throws IOException {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	DataOutputStream bufferDOS = new DataOutputStream(buffer);

	int[] dims = array.getDimensions();
	for (int i = 0; i < dims.length; i++) {
	    bufferDOS.writeInt(dims[i]);
	}
	OSArrayTag tag = new OSArrayTag(MatDataTypes.miUINT32, buffer.toByteArray());
	tag.writeTo(os);

    }

    /**
     * Writes MATRIX name into <code>OutputStream</code>.
     * 
     * @param os
     *            - <code>OutputStream</code>
     * @param array
     *            - a <code>MLArray</code>
     * @throws IOException
     */
    private static void writeName(DataOutputStream os, MLArray array) throws IOException {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	DataOutputStream bufferDOS = new DataOutputStream(buffer);

	byte[] nameByteArray = array.getNameToByteArray();
	buffer = new ByteArrayOutputStream();
	bufferDOS = new DataOutputStream(buffer);
	bufferDOS.write(nameByteArray);
	OSArrayTag tag = new OSArrayTag(16, buffer.toByteArray());
	tag.writeTo(os);
    }

}
