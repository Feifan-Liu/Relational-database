package hw3;

public interface Node {
	
	
	public int getDegree();
	public boolean isLeafNode();
	public void setParent(InnerNode parent);
	public InnerNode getParent();
}
