/* Copyright 2007, Alexander Patterson IV
 * 
 * This file is part of JptView.
 * 
 * JptView is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package jSkelExtractor.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 
 * @author Sandy Patterson <br>
 *         Adapted by pmarco(20080622)
 * 
 * @see <a href="http://www.seas.upenn.edu/~aiv/jptview/index.html">
 *      http://www.seas.upenn.edu/~aiv/jptview/index.html</a>
 * 
 */
public class PLY {
    /* enums */
    /*
     * an enumeration type to keep track of the various possible types for ply
     * files
     */
    private enum PlyDataType {
	CHAR("char"), UCHAR("uchar"), SHORT("short"), USHORT("ushort"), INT(
		"int"), UINT("uint"), FLOAT("float"), DOUBLE("double");
	private final String datatype;

	PlyDataType(String s) {
	    datatype = s;
	}

	public String getDataTypeString() {
	    return datatype;
	}

	public static PlyDataType getDataType(String typename) {
	    return table.get(typename);
	}

	@SuppressWarnings("unchecked")
	public static PlyDataType getListDataType(List<List> v)
		throws Exception {
	    long maxlen = 0;
	    for (List l : v)
		if (l.size() > maxlen)
		    maxlen = l.size();
	    return getDataTypeFromUnsigned(maxlen);
	}

	@SuppressWarnings("unchecked")
	public static PlyDataType getDataType(List v) throws Exception {
	    long maxint = 0;
	    boolean neg = false;
	    // boolean floatoverflow = false;
	    boolean floatingpoint = false;
	    boolean isdouble = false;

	    if (v.size() == 0)
		throw new Exception("can't get datatype of empty vector");

	    // find max and min values
	    if (v.get(0) instanceof List) {
		if (((List) v.get(0)).get(0) instanceof Float) {
		    floatingpoint = true;
		    isdouble = false;
		}
		if (((List) v.get(0)).get(0) instanceof Double) {
		    floatingpoint = true;
		    isdouble = true;
		}
		for (List vs : (List<List>) v) {
		    for (Number n : (List<Number>) vs)
			if (!floatingpoint) {
			    // if(n.doubleValue() != (double)n.floatValue())
			    // floatoverflow = true;
			    // } else {
			    if (n.longValue() > maxint)
				maxint = Math.abs(n.longValue());
			    if (n.longValue() < 0)
				neg = true;
			}
		}
	    } else {
		if (v.get(0) instanceof Float) {
		    floatingpoint = true;
		    isdouble = false;
		}
		if (v.get(0) instanceof Double) {
		    floatingpoint = true;
		    isdouble = true;
		}
		for (Number n : (List<Number>) v)
		    if (floatingpoint) {
			// double a = n.doubleValue();
			// float b = n.floatValue();
			// if(Double.compare(a, (double)b ) != 0)
			// floatoverflow = true;
			// } else {
			if (n.longValue() > maxint)
			    maxint = Math.abs(n.longValue());
			if (n.longValue() < 0)
			    neg = true;
		    }
	    }
	    if (floatingpoint)
		if (isdouble)
		    return DOUBLE;
		else
		    return FLOAT;
	    else if (neg)
		return getDataTypeFromSigned((int) maxint);
	    else
		return getDataTypeFromUnsigned(maxint);
	}

	// public static PlyDataType getDataType(Object o) {
	//          
	// return table.get(typename);
	// }

	// Changed by pmarco 20080902 for compatibility with MeshLab
	// Force int-compatible types into int
	public static PlyDataType getDataTypeFromUnsigned(long maxint)
		throws Exception {

	    if (maxint < 0)
		throw new Exception(
			"Error, can not get unsigned type for a negative number");

	    if ((maxint & 0x00ff) == maxint)
		return PlyDataType.UCHAR;
	    if ((maxint & 0x00ffff) == maxint)
		// return PlyDataType.USHORT;
		return PlyDataType.INT;
	    if ((maxint & 0x00ffffffff) == maxint)
		// return PlyDataType.UINT;
		return PlyDataType.INT;

	    return null;
	}

	public static PlyDataType getDataTypeFromSigned(int maxint) {
	    int maxintpos = Math.abs(maxint);

	    if ((maxint & 0x007f) == maxintpos)
		return PlyDataType.CHAR;
	    if ((maxint & 0x007fff) == maxintpos)
		return PlyDataType.SHORT;
	    if ((maxint & 0x007fffffff) == maxintpos)
		return PlyDataType.INT;

	    return null;
	}

	@SuppressWarnings("unchecked")
	public Class getJavaDataType() {
	    return typeTable.get(this);
	}

	public Object readIntegerVal(InputStream is, MachineFormat mf)
		throws IOException {
	    switch (this) {
	    case FLOAT:
	    case DOUBLE:
		// case UINT:
		throw new IOException(
			"Error, tried to read float, double or as a list index.");
	    }
	    return readVal(is, mf);
	}

	public Object readVal(InputStream is, MachineFormat mf)
		throws IOException {
	    switch (this) {
	    case CHAR:
		return (Object) (Byte) (byte) readEndian(is, mf, 1);
	    case UCHAR:
		return (Object) (Short) (short) (readEndian(is, mf, 1) & 0x00000000ffffffff);
	    case SHORT:
		return (Object) (Short) (short) readEndian(is, mf, 2);
	    case USHORT:
		return (Object) (Integer) (int) (readEndian(is, mf, 2) & 0x00000000ffffffff);
	    case INT:
		return (Object) (Integer) (int) readEndian(is, mf, 4);
	    case UINT:
		long re = readEndian(is, mf, 4);
		if (re > Integer.MAX_VALUE)
		    throw new IOException(
			    "Error, java doesn't let indexes into containers get larger than max signed integer.");
		return (Object) (Long) (re & 0x00000000ffffffff);
	    case FLOAT:
		return (Object) (Float.intBitsToFloat((int) readEndian(is, mf,
			4)));
	    case DOUBLE:
		return (Object) (Double.longBitsToDouble((long) readEndian(is,
			mf, 8)));
	    }
	    return null;
	}

	public long readEndian(InputStream is, MachineFormat mf, int bytes)
		throws IOException {
	    long result = 0;
	    int by;
	    byte[] arr = new byte[bytes];
	    for (int i = 0; i < bytes; i++) {
		by = is.read();
		if (by < 0)
		    throw new IOException(
			    "Error reading binary data, read() returned " + by);
		if (mf == MachineFormat.LITTLE)
		    arr[bytes - i - 1] = (byte) by;
		else
		    arr[i] = (byte) by;
	    }

	    for (int i = 0; i < bytes; i++)
		result =
			((result << 8) & 0xffffffffffffff00l)
				| (0x00000000000000FFl & arr[i]);

	    return result;
	}

	public void writeEndian(long val, DataOutputStream dos,
		MachineFormat mf, int bytes) throws IOException {
	    byte[] arr = new byte[bytes];

	    for (int i = 0; i < bytes; i++) {
		arr[i] = (byte) (val & 0x00ffl);
		val = (val >> 8) & 0x00ffffffffffffffl;
	    }

	    if (mf == MachineFormat.BIG) {
		byte[] arr2 = new byte[bytes];
		for (int i = 0; i < bytes; i++)
		    arr2[i] = arr[i];
		for (int i = 0; i < bytes; i++)
		    arr[i] = arr2[bytes - i - 1];
	    }

	    dos.write(arr);
	}

	public void writeVal(Object val, DataOutputStream dos, MachineFormat mf)
		throws IOException {
	    switch (this) {
	    case CHAR:
		writeEndian(((Number) val).longValue(), dos, mf, 1);
		break;
	    case UCHAR:
		writeEndian(((Number) val).longValue() & 0x00000000ffffffff,
			dos, mf, 1);
		break;
	    case SHORT:
		writeEndian(((Number) val).longValue(), dos, mf, 2);
		break;
	    case USHORT:
		writeEndian(((Number) val).longValue() & 0x00000000ffffffff,
			dos, mf, 2);
		break;
	    case INT:
		writeEndian(((Number) val).longValue(), dos, mf, 4);
		break;
	    case UINT:
		writeEndian(((Number) val).longValue() & 0x00000000ffffffff,
			dos, mf, 4);
		break;
	    case FLOAT:
		writeEndian(Float.floatToRawIntBits((Float) val), dos, mf, 4);
		break;
	    case DOUBLE:
		writeEndian(Double.doubleToRawLongBits((Double) val), dos, mf,
			8);
		break;
	    }
	}

	@SuppressWarnings("unchecked")
	private static Map<PlyDataType, Class> typeTable =
		new HashMap<PlyDataType, Class>();
	private static Map<String, PlyDataType> table =
		new HashMap<String, PlyDataType>();
	static {
	    for (PlyDataType d : PlyDataType.values()) {
		table.put(d.datatype, d);
	    }
	    typeTable.put(CHAR, Byte.class);
	    typeTable.put(UCHAR, Short.class);
	    typeTable.put(SHORT, Short.class);
	    typeTable.put(USHORT, Integer.class);
	    typeTable.put(INT, Integer.class);
	    typeTable.put(UINT, Long.class);
	    typeTable.put(FLOAT, Float.class);
	    typeTable.put(DOUBLE, Double.class);
	}

	public static PlyDataType string2DataType(String s) {
	    return table.get(s);
	}

	public Object parse(String s) {
	    switch (this) {
	    case CHAR:
		return (Object) Byte.parseByte(s);
	    case UCHAR:
		return (Object) Short.parseShort(s);
	    case SHORT:
		return (Object) Short.parseShort(s);
	    case USHORT:
		return (Object) Integer.parseInt(s);
	    case INT:
		return (Object) Integer.parseInt(s);
	    case UINT:
		return (Object) Long.parseLong(s);
	    case FLOAT:
		return (Object) Float.parseFloat(s);
	    case DOUBLE:
		return (Object) Double.parseDouble(s);
	    }
	    return null;
	}

	// for some reason we have to give the instance of ply to generate these
	// objects because PlyProperty is private
	@SuppressWarnings("unchecked")
	private PlyProperty getPlyPropertyObj(String name, int len, PLY p) {
	    switch (this) {
	    case CHAR:
		return p.new PlyProperty<Byte>(this, name, len);
	    case UCHAR:
		return p.new PlyProperty<Short>(this, name, len);
	    case SHORT:
		return p.new PlyProperty<Short>(this, name, len);
	    case USHORT:
		return p.new PlyProperty<Integer>(this, name, len);
	    case INT:
		return p.new PlyProperty<Integer>(this, name, len);
	    case UINT:
		return p.new PlyProperty<Long>(this, name, len);
	    case FLOAT:
		return p.new PlyProperty<Float>(this, name, len);
	    case DOUBLE:
		return p.new PlyProperty<Double>(this, name, len);
	    }
	    return null;
	}

	@SuppressWarnings("unchecked")
	private PlyProperty getPlyPropertyObj(String name, int len, PLY p,
		PlyDataType dt_lst) throws Exception {
	    // PlyDataType dt_lst = getDataTypeFromUnsigned(maxint);
	    switch (this) {
	    case CHAR:
		return p.new PlyProperty<List<Byte>>(dt_lst, this, name, len);
	    case UCHAR:
		return p.new PlyProperty<List<Short>>(dt_lst, this, name, len);
	    case SHORT:
		return p.new PlyProperty<List<Short>>(dt_lst, this, name, len);
	    case USHORT:
		return p.new PlyProperty<List<Integer>>(dt_lst, this, name, len);
	    case INT:
		return p.new PlyProperty<List<Integer>>(dt_lst, this, name, len);
	    case UINT:
		return p.new PlyProperty<List<Long>>(dt_lst, this, name, len);
	    case FLOAT:
	    case DOUBLE:
		throw new Exception("can't have floating point index");
	    }
	    return null;
	}

	@SuppressWarnings("unchecked")
	static PlyProperty getPlyPropertyObj(String name, List v, PLY ply)
		throws Exception {
	    // if we have a list string, then conver tot a list list char
	    if (v.size() == 0)
		throw new Exception("can't add empty list to property");
	    if (v.get(0) instanceof String)
		v = convertStringListToCharList(v);

	    PlyDataType data_t = PlyDataType.getDataType(v);
	    PlyProperty p;
	    if (v.size() == 0)
		throw new Exception("vector must be non-zero size");
	    if (v.get(0) instanceof List) {
		PlyDataType list_t = PlyDataType.getListDataType(v);
		p = data_t.getPlyPropertyObj(name, v.size(), ply, list_t);
	    } else
		p = data_t.getPlyPropertyObj(name, v.size(), ply);
	    p.setData(v);
	    return p;
	}

	private static List<List<Byte>> convertStringListToCharList(
		List<String> v) throws UnsupportedEncodingException {
	    List<List<Byte>> lout = new ArrayList<List<Byte>>(v.size());
	    for (int i = 0; i < v.size(); i++) {
		byte[] chararray = v.get(i).getBytes("US-ASCII");
		List<Byte> l = new ArrayList<Byte>(chararray.length);
		for (int j = 0; j < chararray.length; j++)
		    l.add(new Byte(chararray[j]));
		lout.add(l);
	    }
	    return lout;
	}

	public Number cast(Number n) {
	    switch (this) {
	    case CHAR:
		return n.byteValue();
	    case UCHAR:
		return n.shortValue();
	    case SHORT:
		return n.shortValue();
	    case USHORT:
		return n.intValue();
	    case INT:
		return n.intValue();
	    case UINT:
		return n.longValue();
	    case FLOAT:
		return n.floatValue();
	    case DOUBLE:
		return n.doubleValue();
	    }
	    return null;
	}

    }

    private enum MachineFormat {
	ASCII("ascii"), LITTLE("binary_little_endian"), BIG("binary_big_endian");
	private final String text;

	MachineFormat(String s) {
	    text = s;
	}

	String getText() {
	    return text;
	}
    }

    private enum MachineTxtFormat {
	MAC("\r"), PC("\r\n"), UNIX("\n");
	private final String delimiter;

	MachineTxtFormat(String s) {
	    delimiter = s;
	}

	public String getDelim() {
	    return delimiter;
	}

	public static MachineTxtFormat getTxtFormat(String delim) {
	    return table.get(delim);
	}

	private static Map<String, MachineTxtFormat> table =
		new HashMap<String, MachineTxtFormat>();
	static {
	    for (MachineTxtFormat d : MachineTxtFormat.values())
		table.put(d.delimiter, d);
	}

    }

    /*
     * properties are lists of numbers, we can chain these too for the list type
     * in the ply file
     */
    @SuppressWarnings("unchecked")
    private class PlyProperty<E> {
	private List<E> data;
	private List<List<E>> dataList;
	private String name;
	private PlyDataType dataType;
	private PlyDataType listType;
	private int length;
	private boolean list;

	PlyProperty(PlyDataType lst_index_t, PlyDataType lst_data_t, String n,
		int len) {
	    listType = lst_index_t;
	    dataType = lst_data_t;
	    name = n;
	    list = true;
	    length = len;
	    // dataList = new ArrayList<List<E>>(length);
	    dataList = new Vector<List<E>>(length);
	    ((Vector) dataList).setSize(length);
	    for (int i = 0; i < length; i++)
		// dataList.add(new ArrayList<E>());
		dataList.set(i, new Vector<E>());
	}

	PlyProperty(PlyDataType lst_data_t, String n, int len) {
	    dataType = lst_data_t;
	    name = n;
	    list = false;
	    length = len;
	    // data = new ArrayList<E>(length);
	    data = new Vector<E>(length);
	    ((Vector) data).setSize(length);
	}

	public void setData(List v) throws Exception {
	    if (v.size() == 0)
		throw new Exception("trying to add an empty vector");

	    if (v.get(0) instanceof List) {
		List<E> tempvar;
		Vector<List<E>> dl = new Vector<List<E>>();
		dl.setSize(length);
		for (int i = 0; i < v.size(); i++) {
		    tempvar = new Vector<E>();
		    for (Number v2 : (List<Number>) (v.get(i)))
			tempvar.add((E) dataType.cast(v2));
		    dl.set(i, tempvar);
		}
		dataList = dl;
	    } else {
		Vector<E> d = new Vector<E>();
		d.setSize(length);
		for (int i = 0; i < v.size(); i++)
		    d.set(i, (E) (dataType.cast((Number) v.get(i))));
		data = d;
	    }
	}

	public String getName() {
	    return name;
	}

	public PlyDataType getDataType() {
	    return dataType;
	}

	public PlyDataType getListType() {
	    return listType;
	}

	public boolean isList() {
	    return list;
	};

	public String toString() {
	    if (list)
		return "property list " + listType.getDataTypeString() + " "
			+ dataType.getDataTypeString() + " " + name;
	    else
		return "property " + dataType.getDataTypeString() + " " + name;
	}
    };

    /*
     * elements are lists of properties. They are stored in the file as prop1
     * prop2 prop3 prop1 prop2...
     */
    @SuppressWarnings("unchecked")
    private class PlyElement {
	private int length;
	private List<PlyProperty> props;
	private String name;

	public PlyElement(int len, String element_name) {
	    props = new ArrayList<PlyProperty>();
	    name = element_name;
	    length = len;
	}

	public void addProperty(PlyProperty prop) {
	    props.add(prop);
	    length = prop.length;
	}

	public long getLength() {
	    return length;
	}

	public String getName() {
	    return name;
	}

	public String toString() {
	    return "element " + name + " " + length;
	}
    };

    /* instance variables */
    // store the format of what type of file we're dealing with, this can be set
    // before writing.
    private MachineFormat endian;
    private MachineTxtFormat txtFormat;
    private String ply_version;

    // here's where i'm putting the data for the ply file
    private List<String> comment_list;
    private List<PlyElement> element_list;

    private File plyFileName;
    private long dataStartLoc;

    private static final int readBuffSize = 1024;

    public PLY() {
	element_list = new ArrayList<PlyElement>();
	comment_list = new ArrayList<String>();
	ply_version = "1.0";
	endian = MachineFormat.BIG;
	txtFormat = MachineTxtFormat.UNIX;
	plyFileName = null;
    }

    /* external interface to do with loading and saving */
    public void setFile(String fn) throws IOException {
	setFile(new File(fn));
    }

    public void setFile(File f) throws IOException {
	if (f.getName().toLowerCase().endsWith(".ply"))
	    plyFileName = f;
	else
	    throw new IOException("Error, can only load ply files.");
    }

    public void loadfile(String fn) throws IOException {
	loadfile(new File(fn));
    }

    public void loadfile(File f) throws IOException {
	setFile(f);
	loadfile();
    }

    public void loadfile() throws IOException {
	if (plyFileName == null)
	    throw new IOException("No filename specified.");
	determineHeaderType();
	readHeader();
	readData();
    }

    public void savefile(String fn) throws IOException {
	savefile(new File(fn));
    }

    public void savefile(File f) throws IOException {
	setFile(f);
	savefile();
    }

    public void savefile() throws IOException {
	checkConsistency();
	writeHeader();
	writeData();
    }

    @SuppressWarnings("unchecked")
    private void checkConsistency() throws IOException {
	for (PlyElement e : element_list)
	    for (PlyProperty p : e.props) {
		if (e.length != p.length)
		    throw new IOException(
			    "Error, inconsistent element and property length");
		if (p.list) {
		    if (e.length != p.dataList.size())
			throw new IOException(
				"Error, inconsistent element and property length (list)");
		} else {
		    if (e.length != p.data.size())
			throw new IOException(
				"Error, inconsistent element and property length (list)");
		}
	    }

    }

    /* external methods to create the elements and properties */
    private void addElement(String name) {
	element_list.add(new PlyElement(0, name));
    }

    private PlyElement getElement(String name) {
	for (PlyElement e : element_list)
	    if (name.equals(e.name))
		return e;
	return null;
    }

    @SuppressWarnings("unchecked")
    private void addProperty(String name, List v, PlyElement e)
	    throws Exception {
	if (getProperty(name, e) != null)
	    throw new Exception(
		    "trying to add PlyProperty when that property already exists");

	PlyProperty p = PlyDataType.getPlyPropertyObj(name, v, this);
	e.addProperty(p);
    }

    @SuppressWarnings("unchecked")
    private PlyProperty getProperty(String name, PlyElement e) {
	for (PlyProperty p : e.props) {
	    if (name.equals(p.name))
		return p;
	}
	return null;
    }

    @SuppressWarnings("unchecked")
    public void addProperty(String name, String ename, List v) throws Exception {
	PlyElement e = getElement(ename);
	if (e == null) {
	    addElement(ename);
	    e = getElement(ename);
	}
	addProperty(name, v, e);
    }

    @SuppressWarnings("unchecked")
    public List getProperty(String pname, String ename) throws Exception {
	PlyElement e = getElement(ename);
	PlyProperty p = getProperty(pname, e);
	if (p.list)
	    return p.dataList;
	else
	    return p.data;
    }

    @SuppressWarnings("unchecked")
    public String[] getPropertyStrings(String pname, String ename)
	    throws Exception {
	PlyElement e = getElement(ename);
	PlyProperty p = getProperty(pname, e);
	if (!p.list)
	    throw new Exception(
		    "Tried to get propertyString from non-list property");
	if (p.dataType != PlyDataType.CHAR && p.dataType != PlyDataType.UCHAR)
	    throw new Exception(
		    "Tried to get propertyString from a list that's not uchar or char type");

	String[] propstrings = new String[p.length];
	for (int i = 0; i < p.length; i++) {
	    List sublist = (List) p.dataList.get(i);
	    int len = sublist.size();
	    byte[] bytes = new byte[len];

	    for (int j = 0; j < len; j++) {
		bytes[j] = ((Number) sublist.get(j)).byteValue();
		;
	    }

	    propstrings[i] = new String(bytes);
	}
	return propstrings;
    }

    public List<String> getComments() {
	return comment_list;
    }

    public void addComment(String comm) {
	comment_list.add(comm);
    }

    /* external methods to set type of file for output */
    public void setFileType(String s) {
	if ("ascii".equals(s))
	    endian = MachineFormat.ASCII;
	if ("binary_big_endian".equals(s))
	    endian = MachineFormat.BIG;
	if ("binary_little_endian".equals(s))
	    endian = MachineFormat.LITTLE;
    }

    public void setTextType(String s) {
	if ("mac".equals(s))
	    txtFormat = MachineTxtFormat.MAC;
	if ("pc".equals(s))
	    txtFormat = MachineTxtFormat.PC;
	if ("unix".equals(s))
	    txtFormat = MachineTxtFormat.UNIX;
    }

    /* writing methods */
    @SuppressWarnings("unchecked")
    private void writeHeader() throws IOException {
	BufferedWriter out =
		new BufferedWriter(new FileWriter(plyFileName, false));

	// write magic number
	out.write("ply");
	out.write(txtFormat.delimiter); // newline

	// write out format
	out.write("format " + endian.getText() + " " + ply_version);
	out.write(txtFormat.delimiter); // newline

	// write comments
	for (String c : comment_list) {
	    out.write("comment " + c);
	    out.write(txtFormat.delimiter); // newline
	}

	// write elements and properties
	for (PlyElement e : element_list) {
	    out.write(e.toString());
	    out.write(txtFormat.delimiter); // newline
	    for (PlyProperty p : e.props) {
		out.write(p.toString());
		out.write(txtFormat.delimiter); // newline
	    }
	}

	// write end_header
	out.write("end_header");
	out.write(txtFormat.delimiter); // newline

	out.close();

    }

    private void writeData() throws IOException {
	if (endian == MachineFormat.ASCII)
	    writeAsciiData();
	else
	    writeBinaryData();
    }

    @SuppressWarnings("unchecked")
    private void writeAsciiData() throws IOException {
	BufferedWriter out =
		new BufferedWriter(new FileWriter(plyFileName, true));

	// for each element we write all of its data
	for (int k = 0; k < element_list.size(); k++) {
	    PlyElement e = element_list.get(k);
	    for (int i = 0; i < e.length; i++) {
		for (int j = 0; j < e.props.size(); j++) {
		    PlyProperty p = e.props.get(j);
		    if (p.list) {
			Vector vec = (Vector) p.dataList.get(i);
			out.write(new Integer(vec.size()).toString());
			out.write(" ");
			for (int l = 0; l < vec.size(); l++) {
			    out.write(vec.get(l).toString());
			    if (l < vec.size() - 1)
				out.write(" ");
			}
		    } else
			out.write(p.data.get(i).toString());
		    if (j < e.props.size() - 1) // write a space if we're not at
			// the end of the line yet
			out.write(" ");
		}
		out.write(txtFormat.delimiter); // always write a return.
	    }
	}
	out.close();
    }

    @SuppressWarnings("unchecked")
    private void writeBinaryData() throws IOException {
	DataOutputStream dos =
		new DataOutputStream(new FileOutputStream(plyFileName, true));

	// for each element we write all of its data
	for (int k = 0; k < element_list.size(); k++) {
	    PlyElement e = element_list.get(k);
	    for (int i = 0; i < e.length; i++) {
		for (int j = 0; j < e.props.size(); j++) {
		    PlyProperty p = e.props.get(j);
		    if (p.list) {
			Vector vec = (Vector) p.dataList.get(i);
			p.listType.writeVal(vec.size(), dos, endian);
			for (int l = 0; l < vec.size(); l++)
			    p.dataType.writeVal(vec.get(l), dos, endian);
		    } else
			p.dataType.writeVal(p.data.get(i), dos, endian);
		}
	    }
	}
	dos.close();
    }

    /* reading methods */
    private void determineHeaderType() throws IOException {
	InputStream is = new FileInputStream(plyFileName);
	byte[] bytes = new byte[5];

	if (is.read(bytes, 0, 5) != 5)
	    throw new IOException("couldn't read first 5 bytes of ply file.");

	String firstline = new String(bytes, 0, 5, "US-ASCII");
	if ("ply".compareTo(firstline.substring(0, 3)) != 0)
	    throw new IOException(
		    "File is not a ply file, first 3 characters are "
			    + firstline.substring(0, 2));

	if (bytes[3] == '\n')
	    this.txtFormat = MachineTxtFormat.UNIX;
	else if (bytes[3] == '\r')
	    if (bytes[4] == '\n')
		this.txtFormat = MachineTxtFormat.PC;
	    else
		this.txtFormat = MachineTxtFormat.MAC;
	else
	    throw new IOException(
		    "File begins with ply, but it is not followed by a newline.");

    }

    private String getHeaderString() throws IOException {
	InputStream is = new FileInputStream(plyFileName);
	String headerString = "";
	byte[] byteBuff = new byte[readBuffSize];

	do {
	    if (is.read(byteBuff) <= 0)
		throw new IOException(
			"End of file reached without finding \"end_header\".");
	    headerString += (new String(byteBuff));
	} while (!headerString.contains("end_header"));

	// write the data_start_loc down and trim string
	headerString =
		headerString.substring(0, headerString
			.lastIndexOf("end_header"));
	return headerString;
    }

    // pmarco 20090209
    // Added a rather rude treatment of obj_info
    private LinkedList<String> readComments(LinkedList<String> headerList) {
	String[] line_split;
	do {
	    line_split = headerList.get(0).split("\\s", 2);
	    if ("comment".compareTo(line_split[0]) == 0
		    || "obj_info".compareTo(line_split[0]) == 0) {
		comment_list.add(line_split[1]);
		headerList.subList(0, 1).clear();
	    }
	} while ("comment".compareTo(line_split[0]) == 0
		|| "obj_info".compareTo(line_split[0]) == 0);
	return headerList;
    }

    private void readFormatVersion(String line) throws IOException {
	String[] line_split = line.split("\\s", 3);
	if (!"format".equals(line_split[0]))
	    throw new IOException("\"format\" not found in ply file");

	// format type
	if (line_split[1].equals("ascii"))
	    endian = MachineFormat.ASCII;
	else if (line_split[1].equals("binary_big_endian"))
	    endian = MachineFormat.BIG;
	else if (line_split[1].equals("binary_little_endian"))
	    endian = MachineFormat.LITTLE;
	else
	    throw new IOException("invalid format type for ply file");

	// version number
	this.ply_version = line_split[2];
	if (!"1.0".equals(ply_version))
	    throw new IOException("invalid ply version");
    }

    private void readHeader() throws IOException {

	// first get the whole header
	String headerString = getHeaderString();
	dataStartLoc =
		headerString.length() + "end_header".length()
			+ txtFormat.getDelim().length();

	// split up the header into lines
	String[] header_split = headerString.split(txtFormat.getDelim());
	LinkedList<String> headerList =
		new LinkedList<String>(Arrays.asList(header_split));

	// first check the magic numbers
	if ("ply".compareTo(headerList.get(0)) != 0)
	    throw new IOException(
		    "first 3 characters of a ply file must be \"ply\"");
	headerList.subList(0, 1).clear(); // clear magic number

	// next read the format and version number
	readFormatVersion(headerList.get(0));
	headerList.subList(0, 1).clear(); // clear format line

	// read comments
	headerList = readComments(headerList);

	// read data structure
	readHeaderStructure(headerList);

    }

    @SuppressWarnings("unchecked")
    private void readHeaderStructure(List<String> headerList)
	    throws IOException {
	String[] line_split;
	PlyElement plye;
	while (!headerList.isEmpty()) {
	    // read element name
	    line_split = headerList.get(0).split("\\s", 3);
	    assert "element".equals(line_split[0]) : "element not present on line it is expected on.";
	    plye =
		    new PlyElement(Integer.parseInt(line_split[2]),
			    line_split[1]);
	    headerList.subList(0, 1).clear();
	    assert headerList.size() > 0 : "element has no properties";

	    do { // read one property from the element list
		line_split = headerList.get(0).split("\\s", 3);
		if ("property".equals(line_split[0])) {
		    if ("list".equals(line_split[1])) {
			PlyProperty thisprop;
			line_split = headerList.get(0).split("\\s", 5);

			// now check which datatype we have for the data itself
			// Couldn't find a better way to do this. It seems like
			// there should be :(
			PlyDataType lst_index_t =
				PlyDataType.getDataType(line_split[2]);
			PlyDataType lst_data_t =
				PlyDataType.getDataType(line_split[3]);

			// pass index/data/name to the constructor:
			if (lst_data_t.getJavaDataType() == Byte.class)
			    thisprop =
				    new PlyProperty<Vector<Byte>>(lst_index_t,
					    lst_data_t, line_split[4],
					    plye.length);
			else if (lst_data_t.getJavaDataType() == Short.class)
			    thisprop =
				    new PlyProperty<Vector<Short>>(lst_index_t,
					    lst_data_t, line_split[4],
					    plye.length);
			else if (lst_data_t.getJavaDataType() == Integer.class)
			    thisprop =
				    new PlyProperty<Vector<Integer>>(
					    lst_index_t, lst_data_t,
					    line_split[4], plye.length);
			else if (lst_data_t.getJavaDataType() == Long.class)
			    thisprop =
				    new PlyProperty<Vector<Long>>(lst_index_t,
					    lst_data_t, line_split[4],
					    plye.length);
			else if (lst_data_t.getJavaDataType() == Float.class)
			    thisprop =
				    new PlyProperty<Vector<Float>>(lst_index_t,
					    lst_data_t, line_split[4],
					    plye.length);
			else if (lst_data_t.getJavaDataType() == Double.class)
			    thisprop =
				    new PlyProperty<Vector<Double>>(
					    lst_index_t, lst_data_t,
					    line_split[4], plye.length);
			else
			    throw new IOException(
				    "Something funky with reflection");

			plye.addProperty(thisprop);
			headerList.subList(0, 1).clear();
		    } else { // not a list
			PlyProperty thisprop;

			// now check which datatype we have for the data itself
			// Couldn't find a better way to do this. It seems like
			// there should be :(
			PlyDataType lst_data_t =
				PlyDataType.getDataType(line_split[1]);

			// pass index/data/name to the constructor:
			if (lst_data_t.getJavaDataType() == Byte.class)
			    thisprop =
				    new PlyProperty<Byte>(lst_data_t,
					    line_split[2], plye.length);
			else if (lst_data_t.getJavaDataType() == Short.class)
			    thisprop =
				    new PlyProperty<Short>(lst_data_t,
					    line_split[2], plye.length);
			else if (lst_data_t.getJavaDataType() == Integer.class)
			    thisprop =
				    new PlyProperty<Integer>(lst_data_t,
					    line_split[2], plye.length);
			else if (lst_data_t.getJavaDataType() == Long.class)
			    thisprop =
				    new PlyProperty<Long>(lst_data_t,
					    line_split[2], plye.length);
			else if (lst_data_t.getJavaDataType() == Float.class)
			    thisprop =
				    new PlyProperty<Float>(lst_data_t,
					    line_split[2], plye.length);
			else if (lst_data_t.getJavaDataType() == Double.class)
			    thisprop =
				    new PlyProperty<Double>(lst_data_t,
					    line_split[2], plye.length);
			else
			    throw new IOException(
				    "Something funky with reflection");

			plye.addProperty(thisprop);
			headerList.subList(0, 1).clear();
		    }
		}
	    } while ("property".equals(line_split[0]) && !headerList.isEmpty());

	    element_list.add(plye);
	}

    }

    private void readData() throws IOException {
	switch (endian) {
	case ASCII:
	    readDataAscii();
	    break;
	case BIG:
	case LITTLE:
	    readDataBinary();
	    break;
	}

    }

    @SuppressWarnings("unchecked")
    private void readDataAscii() throws IOException {
	String line;
	String[] words;
	PlyProperty thisprop;
	BufferedReader in = new BufferedReader(new FileReader(plyFileName));
	in.skip(dataStartLoc);

	// for each element we read all of its data
	for (PlyElement e : element_list) {
	    for (int i = 0; i < e.length; i++) {
		line = in.readLine();
		words = line.split("\\s");
		for (int j = 0; j < e.props.size(); j++) {
		    thisprop = e.props.get(j);
		    if (thisprop.list) {
			// set the size of the vector we are about to fill
			Vector thisvec = (Vector) thisprop.dataList.get(i);
			thisvec.setSize(((Number) thisprop.listType
				.parse(words[j])).intValue());
			for (int k = 0; k < thisvec.size(); k++)
			    thisvec.set(k, thisprop.dataType.parse(words[j + k
				    + 1]));
		    } else
			thisprop.data.set(i, thisprop.dataType.parse(words[j]));
		}
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private void readDataBinary() throws IOException {

	InputStream is = new FileInputStream(plyFileName);
	is.skip(dataStartLoc);

	// for each element we read all of its data
	for (PlyElement e : element_list) {
	    for (int i = 0; i < e.length; i++) {
		for (int j = 0; j < e.props.size(); j++) {
		    PlyProperty thisprop = e.props.get(j);
		    if (thisprop.list) {
			// set the size of the vector we are about to fill
			Vector thisvec = (Vector) thisprop.dataList.get(i);
			// read length of list
			int len =
				((Number) thisprop.listType.readIntegerVal(is,
					endian)).intValue();
			thisvec.setSize(len);
			for (int k = 0; k < len; k++)
			    // thisvec.set(k,thisprop.dataType.parse(words[j+k+1]
			    // ));
			    thisvec.set(k, thisprop.dataType
				    .readVal(is, endian));
		    } else {
			// thisprop.data.add(i,thisprop.dataType.parse(words[j]))
			// ;
			thisprop.data.set(i, thisprop.dataType.readVal(is,
				endian));
		    }
		}
	    }
	}

    }

    /**
     * 
     * For testing purposes only
     */
    @SuppressWarnings( { "unchecked", "unused" })
    public static void main(String[] argv) {

	for (int i = 0; i < argv.length; i++) {
	    try {
		PLY plyObj = new PLY();
		plyObj.loadfile(argv[i]);

		Vector x = null;
		Vector y = null;
		Vector z = null;

		x = (Vector) plyObj.getProperty("x", "vertex");
		y = (Vector) plyObj.getProperty("y", "vertex");
		z = (Vector) plyObj.getProperty("z", "vertex");

		System.out.println("Succesfully loaded file: " + argv[i]);
	    } catch (Exception e) {
		System.out.println();
		System.out.println("Error in file: " + argv[i]);
		e.printStackTrace();
		return;
	    }
	}
    }

}
