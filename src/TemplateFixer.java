import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class TemplateFixer 
{
	Element records;
	NodeList nodeList;
	int nodeIndex = 0;
	int textIndex = 0;
	
	/**
	 * Parses all tags in the document to find variables, and merges their nodes together so that they can be found via computation
	 * @param records
	 */
	public void fix(Element records)
	{
		this.records = records;
		nodeList = records.getElementsByTagName("w:t");
		
		VariableData currentData = null;
		Vector<VariableData> variables = new Vector<VariableData>();
		
		do
		{
			currentData = getNextVariable();
			if(currentData != null)
			{
				variables.add(currentData);
				System.out.println("Found variable.");
			}
		}while (currentData!= null);
		System.out.println("Merging Nodes");
		mergeNodes(variables);
	}
	/**
	 * Returns the next variable in the document
	 * @return
	 */
	private VariableData getNextVariable()
	{
		System.out.println("Searching for next variable");
		VariableData variable = new VariableData();
		
		while(isInRange())
		{
			//look for $
			getNextOccurrence('$');
			if(isInRange())
			{
				variable.nodeStartIndex = nodeIndex;
				variable.textStartIndex = textIndex;
				//look for (
				getNextOccurrence('(');
				if(isInRange() && comesDirectlyAfter(nodeIndex, textIndex, variable.nodeStartIndex, variable.textStartIndex))
				{
					//look for )
					getNextOccurrence(')');
					
					if(isInRange())
					{
						variable.nodeEndIndex = nodeIndex;
						variable.textEndIndex = textIndex;
						return variable;
					}
				}
			}
		}
		//could not find matching 
		return null;
		
	}
	/**
	 * Finds the next occurence of a specified character
	 * @param c
	 */
	private void getNextOccurrence(char c)
	{
		textIndex++;
		//get the next element
		while(isInRange())
			//&& nodeList.item(nodeIndex).getTextContent().charAt(textIndex)!=c)
		{
			while(textIndex < nodeList.item(nodeIndex).getTextContent().length()) 
				//&&nodeList.item(nodeIndex).getTextContent().charAt(textIndex)!=c)
			{
				if(nodeList.item(nodeIndex).getTextContent().charAt(textIndex)==c)
				{
					System.out.println("Found "+ c + " at: "+ nodeIndex + ", " + textIndex);
					return;
				}
				textIndex++;
			}
			nodeIndex ++;
			textIndex = 0;
		}
	}
	private boolean isInRange()
	{
		return nodeIndex < nodeList.getLength() ;
	}
	/**
	 * Returns whether the index of a character comes directly after the specified index.
	 * @param followNodeIndex
	 * @param followTextIndex
	 * @param leadNodeIndex
	 * @param leadTextIndex
	 * @return
	 */
	private boolean comesDirectlyAfter(int followNodeIndex, int followTextIndex, int leadNodeIndex, int leadTextIndex)
	{
		int currentNodeIndex = leadNodeIndex;
		int currentTextIndex = leadTextIndex + 1;
		
		//get the element directly after our lead node
		while(currentTextIndex >= nodeList.item(currentNodeIndex).getTextContent().length() )
		{
			currentNodeIndex ++;
			currentTextIndex = 0;
		}
		
		//if the element after our lead node is the same as our follow node, this is the following character
		return (currentNodeIndex == followNodeIndex && currentTextIndex == followTextIndex );
		
	}
	/**
	 * Merges all of the nodes for each variable found
	 * @param variables
	 */
	private void mergeNodes(Vector<VariableData> variables)
	{
		//do this in reverse order so that we aren't deleting nodes that don't exist anymore
		for(int i=variables.size()-1;i>=0;i--)
		{
			VariableData variable = variables.get(i);
			
			if(variable.nodeStartIndex != variable.nodeEndIndex)
			{
				System.out.println("Need to merge node");
				
				Vector<Node> nodes = new Vector<Node>();
				for(int j = variable.nodeStartIndex; j<=variable.nodeEndIndex; j++)
				{
					nodes.add(nodeList.item(j));
				}
				
				//need to merge these nodes
				Node parent = lowestCommonAncestor(nodes);
				//System.out.println(parent.toString());
				Node firstNode = nodes.get(0);
				for(int j = 1;j<nodes.size();j++)
				{
					firstNode.setTextContent(firstNode.getTextContent() + nodes.get(j).getTextContent());
				}
				for(int j = nodes.size()-1;j>0;j--)
				{
					Node firstChildOfParent = getAncestorBefore(nodes.get(j), parent);
					parent.removeChild(firstChildOfParent);
				}
			}
		}
	}
	/**
	 * Finds the lowest common ancestor of a set of nodes.
	 * @param nodes
	 * @return
	 */
	private Node lowestCommonAncestor(Vector<Node> nodes)
	{
		Vector<Node> pathReverse = new Vector<Node>();
		Vector<Node> path = new Vector<Node>();
		Node curr = nodes.get(0).getParentNode();
		
		//set the original path to be nodes from our first node to the root
		while(curr!= null)
		{
			pathReverse.add(curr);
			curr = curr.getParentNode();
		}
		//our path is reversed, goes from child to parent -> need to go from parent to child
		for(int i=pathReverse.size()-1; i>=0; i--)
		{
			path.add(pathReverse.get(i));
		}
		//trim down our path for each node in the list, if the common ancestor is higher
		for(int i=1;i<nodes.size();i++)
		{
			curr = nodes.get(i).getParentNode();
			while(curr != null && !path.contains(curr))
			{
				curr = curr.getParentNode();
			}
			//trim the array to the size of the parent to keep track of lca
			int indexOfParent = path.indexOf(curr);
			path.setSize(indexOfParent + 1);
		}
		
		return path.get(path.size()-1);
	}
	/**
	 * Returns the child of a nodes ancestor, which is also an ancestor of the node
	 * This is used so that we can remove all nodes that have to do with the child from the root.
	 * @param node
	 * @param parent
	 * @return
	 */
	private Node getAncestorBefore(Node node, Node parent)
	{
		Node curr = node;
		while(curr.getParentNode()!= parent)
		{
			curr = curr.getParentNode();
		}
		return curr;
	}
}
