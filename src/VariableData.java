import java.util.Vector;

import org.w3c.dom.Node;

public class VariableData {

	Vector<Node> nodes;

	public VariableData() {
		nodes = new Vector<Node>();
	}

	public int nodeStartIndex;
	public int nodeEndIndex;
	public int textStartIndex;
	public int textEndIndex;

	public void reset() {
		nodes.removeAllElements();
		nodeStartIndex = -1;
		nodeEndIndex = -1;
		textStartIndex = -1;
		textEndIndex = -1;
	}

}
