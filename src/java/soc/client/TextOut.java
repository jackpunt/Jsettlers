
class TextOut extends JScrollPane {
    int towidth = 280;
    int maxlen = 0;
    JTextArea texta = new JTextArea();;
    StringBuffer texto = new StringBuffer();
    // JScrollPane scrollPane;
    JScrollBar vscrollBar;

    TextOut(int towidth) {
	// super( VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
	super( VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	setViewportView(texta);
	this.towidth = towidth;
	setMinimumSize(new Dimension(towidth, 40));
	vscrollBar = getVerticalScrollBar();
    }

    public void setWidth(int width) {
	this.towidth = width;
    }
    public void setLength(int len) {
	this.maxlen = len;
    }
    public Dimension getPreferredSize() {
	Dimension d = super.getPreferredSize();
	if (d.width < towidth) d.width = towidth;
	return d;
    }

    public void outNew(String str) {
	texto.setLength(0);
	outMore(str);
    }
    public void outMore(String str) {
	int strlen = str.length();
	int txtlen = texto.length();
	int end = (txtlen + strlen) - maxlen;
	if ((maxlen > 0) && (end > 0)) {
	    // if (end > txtlen) end = txtlen;
	    texto.delete(0, end);
	}
	texto.append(str+"\n");
	// System.out.println("VscrollMax0 = "+vscrollBar.getMaximum());
	texta.setText(texto.toString());
	repaint();
	// System.out.println("VscrollMax1 = "+vscrollBar.getMaximum());
	vscrollBar.setValue(vscrollBar.getMaximum());
    }
    public void repaint() {
	// constructor insists on calling repaint,
	// but will not allow us 
	// if (texta != null) texta.repaint();
	super.repaint();
    }
}
