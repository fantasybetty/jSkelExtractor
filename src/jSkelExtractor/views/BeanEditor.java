/**  Copyright 2008 Universita' degli Studi di Pavia
 *  Laboratorio di Visione Artificiale
 *  http://vision.unipv.it
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package jSkelExtractor.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

public class BeanEditor extends JFrame implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    protected static String defaultPath = System.getProperty("user.dir");

    protected Object bean;
    protected JTable propertyJTable;
    protected PropertyTableData tableData;
    protected JMenuItem refreshMenuItem;
    protected JMenuItem exportMenuItem;
    protected JMenuItem importMenuItem;

    public BeanEditor(Object bean) {

	this.bean = bean;

	getContentPane().setLayout(new BorderLayout());

	// Horizontal menu
	JMenuBar menuBar = new JMenuBar();
	JMenu menu = new JMenu("Options");
	refreshMenuItem = new JMenuItem("Refresh");
	exportMenuItem = new JMenuItem("Export values as Properties");
	importMenuItem = new JMenuItem("Import values from Properties");

	ActionListener menuListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {

		if (e.getSource() == refreshMenuItem) {
		    refresh();

		} else if (e.getSource() == exportMenuItem) {
		    while (true) {
			JFileChooser fc = new JFileChooser(defaultPath);
			fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Properties Files", "properties"));
			fc.setDialogTitle(this.getClass().getName()
				+ " : Export to Properties");

			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    defaultPath = file.getAbsolutePath();

			    if (!exportAsProperties(file)) {

				JOptionPane.showMessageDialog(null,
					"Could not export to \""
						+ file.getPath() + "\"",
					"Error", JOptionPane.ERROR_MESSAGE);
				continue;
			    }
			    break;
			} else {
			    break;
			}
		    }

		} else if (e.getSource() == importMenuItem) {
		    while (true) {
			JFileChooser fc = new JFileChooser(defaultPath);
			fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Properties Files", "properties"));
			fc.setDialogTitle(this.getClass().getName()
				+ " : Import from Properties");

			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    defaultPath = file.getAbsolutePath();

			    if (!importFromProperties(file)) {

				JOptionPane.showMessageDialog(null,
					"Could not import from \""
						+ file.getPath() + "\"",
					"Error", JOptionPane.ERROR_MESSAGE);
				continue;
			    }

			    break;
			} else {
			    break;
			}
		    }
		}
	    }
	};

	refreshMenuItem.addActionListener(menuListener);
	exportMenuItem.addActionListener(menuListener);
	importMenuItem.addActionListener(menuListener);

	menu.add(refreshMenuItem);
	menu.add(exportMenuItem);
	menu.addSeparator();
	menu.add(importMenuItem);
	menuBar.add(menu);
	add(menuBar, BorderLayout.PAGE_START);

	tableData = new PropertyTableData(bean);
	propertyJTable = new JTable(tableData);

	JScrollPane ps = new JScrollPane();
	ps.getViewport().add(propertyJTable);
	getContentPane().add(ps, BorderLayout.CENTER);

	setDefaultCloseOperation(HIDE_ON_CLOSE);

	pack();
    }

    public void refresh() {

	tableData.refresh();
	tableData.fireTableAllRowsUpdated();
    }

    public boolean exportAsProperties(File file) {
	refresh();
	Properties tmp = tableData.asProperties();

	try {
	    FileWriter writer = new FileWriter(file);
	    tmp.store(writer, bean.toString());
	    writer.flush();
	    writer.close();

	} catch (IOException e) {
	    return false;
	}

	return true;
    }

    public boolean importFromProperties(File file) {
	Properties tmp = tableData.asProperties();

	try {
	    FileReader reader = new FileReader(file);
	    tmp.load(reader);
	    reader.close();

	} catch (IOException e) {
	    return false;
	}

	for (String name : tmp.stringPropertyNames()) {
	    tableData.setPropertyValue(name, tmp.getProperty(name));
	}
	
	refresh();
	return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {

	tableData.setProperty(evt.getPropertyName(), evt.getNewValue());

    }

    @SuppressWarnings("unchecked")
    class PropertyTableData extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	protected String[][] properties;
	protected int numProps = 0;
	protected Vector descriptors;

	public PropertyTableData(Object bean) {

	    try {

		BeanInfo info;
		if (bean instanceof Component) {
		    // Get declared properties only
		    info =
			    Introspector.getBeanInfo(bean.getClass(), bean
				    .getClass().getSuperclass());
		} else {
		    info =
			    Introspector.getBeanInfo(bean.getClass(),
				    Object.class);
		}
		BeanDescriptor descr = info.getBeanDescriptor();

		setTitle(descr.getName() + " Parameters");

		PropertyDescriptor[] props = info.getPropertyDescriptors();

		numProps = props.length;
		descriptors = new Vector(numProps);

		for (int k = 0; k < numProps; k++) {

		    // Skip read-only properties
		    if (props[k].getReadMethod() == null
			    || props[k].getWriteMethod() == null) {
			continue;
		    }

		    String name = props[k].getDisplayName();
		    boolean added = false;

		    for (int i = 0; i < descriptors.size(); i++) {
			String str =
				((PropertyDescriptor) descriptors.elementAt(i))
					.getDisplayName();
			if (name.compareToIgnoreCase(str) < 0) {
			    descriptors.insertElementAt(props[k], i);
			    added = true;
			    break;
			}
		    }

		    if (!added)
			descriptors.addElement(props[k]);
		}

		numProps = descriptors.size();

		properties = new String[numProps][2];

		for (int k = 0; k < numProps; k++) {
		    PropertyDescriptor prop =
			    (PropertyDescriptor) descriptors.elementAt(k);

		    properties[k][0] = prop.getDisplayName();
		    Method readMethod = prop.getReadMethod();

		    if (readMethod != null
			    && readMethod.getParameterTypes().length == 0) {
			Object value = readMethod.invoke(bean, (Object[]) null);
			properties[k][1] = objToString(value);
		    } else {
			properties[k][1] = "error";
		    }
		}

	    }

	    catch (Exception ex) {
		ex.printStackTrace();
		JOptionPane
			.showMessageDialog(BeanEditor.this, "Error: "
				+ ex.toString(), "Warning",
				JOptionPane.WARNING_MESSAGE);
	    }
	}

	public void refresh() {
	    try {
		for (int k = 0; k < numProps; k++) {
		    PropertyDescriptor prop =
			    (PropertyDescriptor) descriptors.elementAt(k);

		    properties[k][0] = prop.getDisplayName();
		    Method readMethod = prop.getReadMethod();

		    if (readMethod != null
			    && readMethod.getParameterTypes().length == 0) {
			Object value = readMethod.invoke(bean, (Object[]) null);
			properties[k][1] = objToString(value);
		    } else {
			properties[k][1] = "error";
		    }
		}

	    } catch (Exception ex) {
		ex.printStackTrace();
	    }

	}

	public Properties asProperties() {
	    Properties tmp = new Properties();
	    for (int k = 0; k < numProps; k++) {
		tmp.put(properties[k][0], properties[k][1]);
	    }

	    return tmp;
	}

	public void fireTableAllRowsUpdated() {
	    fireTableRowsUpdated(0, numProps - 1);
	}

	public void setProperty(String name, Object value) {

	    for (int k = 0; k < numProps; k++)
		if (name.equals(properties[k][0])) {
		    properties[k][1] = objToString(value);
		    propertyJTable.tableChanged(new TableModelEvent(this, k));
		    propertyJTable.repaint();

		    break;
		}
	}

	public void setPropertyValue(String name, Object value) {

	    for (int k = 0; k < numProps; k++)
		if (name.equals(properties[k][0])) {
		    setValueAt(value, k, 1);
		    
		    break;
		}
	}

	public int getRowCount() {
	    return numProps;
	}

	public int getColumnCount() {
	    return 2;
	}

	@Override
	public String getColumnName(int nCol) {
	    return nCol == 0 ? "Property" : "Value";
	}

	@Override
	public boolean isCellEditable(int nRow, int nCol) {
	    return (nCol == 1);
	}

	public Object getValueAt(int nRow, int nCol) {
	    if (nRow < 0 || nRow >= getRowCount())
		return "";

	    switch (nCol) {

	    case 0:
		return properties[nRow][0];

	    case 1:
		return properties[nRow][1];

	    }

	    return "";
	}

	@Override
	public void setValueAt(Object value, int nRow, int nCol) {

	    if (nRow < 0 || nRow >= getRowCount())
		return;

	    String str = value.toString();
	    PropertyDescriptor prop =
		    (PropertyDescriptor) descriptors.elementAt(nRow);
	    Class<?> cls = prop.getPropertyType();
	    Object obj = stringToObj(str, cls);

	    if (obj == null)
		return; // can't process

	    Method methodWrite = prop.getWriteMethod();
	    if (methodWrite == null
		    || methodWrite.getParameterTypes().length != 1)
		return;
	    try {
		methodWrite.invoke(bean, new Object[] { obj });

		if (bean instanceof Component) {
		    ((Component) bean).repaint();
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
		JOptionPane
			.showMessageDialog(BeanEditor.this, "Error: "
				+ ex.toString(), "Warning",
				JOptionPane.WARNING_MESSAGE);
	    }
	    properties[nRow][1] = str;
	}

	public String objToString(Object value) {
	    if (value == null)
		return "null";

	    if (value instanceof Dimension) {
		Dimension dim = (Dimension) value;
		return "" + dim.width + "," + dim.height;
	    } else if (value instanceof Insets) {
		Insets ins = (Insets) value;
		return "" + ins.left + "," + ins.top + "," + ins.right + ","
			+ ins.bottom;
	    } else if (value instanceof Rectangle) {
		Rectangle rc = (Rectangle) value;
		return "" + rc.x + "," + rc.y + "," + rc.width + ","
			+ rc.height;
	    } else if (value instanceof Color) {
		Color col = (Color) value;
		return "" + col.getRed() + "," + col.getGreen() + ","
			+ col.getBlue();
	    }
	    return value.toString();
	}

	public Object stringToObj(String str, Class<?> cls) {
	    try {
		if (str == null)
		    return null;

		String name = cls.getName();
		if (name.equals("java.lang.String"))
		    return str;
		else if (name.equals("int"))
		    return new Integer(str);
		else if (name.equals("long"))
		    return new Long(str);
		else if (name.equals("float"))
		    return new Float(str);
		else if (name.equals("double"))
		    return new Double(str);
		else if (name.equals("boolean"))
		    return new Boolean(str);
		else if (name.equals("java.awt.Dimension")) {
		    int[] i = strToInts(str);
		    return new Dimension(i[0], i[1]);
		} else if (name.equals("java.awt.Point")) {
		    int[] i = strToInts(str);
		    return new Point(i[0], i[1]);
		} else if (name.equals("java.awt.Insets")) {
		    int[] i = strToInts(str);
		    return new Insets(i[0], i[1], i[2], i[3]);
		} else if (name.equals("java.awt.Rectangle")) {
		    int[] i = strToInts(str);
		    return new Rectangle(i[0], i[1], i[2], i[3]);
		} else if (name.equals("java.awt.Color")) {
		    int[] i = strToInts(str);
		    return new Color(i[0], i[1], i[2]);
		}

		return null; // not supported

	    } catch (Exception ex) {
		return null;
	    }
	}

	public int[] strToInts(String str) throws Exception {

	    int[] i = new int[4];
	    StringTokenizer tokenizer = new StringTokenizer(str, ",");

	    for (int k = 0; k < i.length && tokenizer.hasMoreTokens(); k++)
		i[k] = Integer.parseInt(tokenizer.nextToken());

	    return i;
	}
    }

}