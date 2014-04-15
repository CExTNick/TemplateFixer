import java.util.Vector;

import org.w3c.dom.Node;

public class VariableData {

	public int nodeStartIndex;
	public int nodeEndIndex;
	public int textStartIndex;
	public int textEndIndex;

	public void reset() {
		nodeStartIndex = -1;
		nodeEndIndex = -1;
		textStartIndex = -1;
		textEndIndex = -1;
	}

}
