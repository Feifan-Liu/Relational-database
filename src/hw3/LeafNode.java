package hw3;

import java.util.ArrayList;

import hw1.RelationalOperator;

public class LeafNode implements Node {
	private int degree;
	private ArrayList<Entry> entries;
	private InnerNode parent;
	public LeafNode(int degree) {
		this.degree = degree;
		entries = new ArrayList<>();
		parent = null;
	}
	
	public ArrayList<Entry> getEntries() {
		return entries;
	}

	public int getDegree() {
		return degree;
	}
	
	public boolean borrowFromSibling(){
		ArrayList<Node> children = parent.getChildren();
		int index = children.indexOf(this);
		if(index>0){
			LeafNode leftSibling = (LeafNode) children.get(index - 1);
			ArrayList<Entry> leftEntries = leftSibling.getEntries();
			if(leftEntries.size()>Math.ceil((double)degree/2)){
				entries.add(0, leftEntries.get(leftEntries.size()-1));
				leftEntries.remove(leftEntries.size()-1);
				parent.updateKeys();
				return true;
			}
		}
		if(index<children.size()-1){
			LeafNode rightSibling = (LeafNode) children.get(index + 1);
			ArrayList<Entry> rightEntries = rightSibling.getEntries();
			if(rightEntries.size()>Math.ceil((double)degree/2)){
				entries.add(rightEntries.get(0));
				rightEntries.remove(0);
				parent.updateKeys();
				return true;
			}
		}
		return false;
	}
	
	public Node merge(){
		ArrayList<Node> children = parent.getChildren();
		int index = children.indexOf(this);
		if(index>0){
			LeafNode leftSibling = (LeafNode) children.get(index - 1);
			ArrayList<Entry> leftEntries = leftSibling.getEntries();
			for(Entry e:entries){
				leftEntries.add(e);
			}
			children.remove(this);
			parent.updateKeys();
			if(children.size()<Math.ceil((double)parent.getDegree()/2)){
				if(parent.getParent() == null){
					if(children.size() == 1){
						leftSibling.setParent(null);
						return leftSibling;
					}
				}
				else if(!parent.pushThrough()){
					return parent.merge();
				}
			}
			return null;
		}
		if(index<children.size()-1){
			LeafNode rightSibling = (LeafNode) children.get(index + 1);
			ArrayList<Entry> rightEntries = rightSibling.getEntries();
			for(Entry e:entries){
				rightEntries.add(0,e);
			}
			children.remove(this);
			parent.updateKeys();
			if(children.size()<Math.ceil((double)parent.getDegree()/2)){
				if(parent.getParent() == null){
					rightSibling.setParent(null);
					return rightSibling;
				}
				if(!parent.pushThrough()){
					return parent.merge();
				}
			}
			return null;
		}
		return null;
	}
	
	public void removeEntry(Entry e){
		entries.remove(e);
	}
	
	public void addEntry(Entry e){
		int i=0;
		for(;i<entries.size();i++){
			if(e.getField().compare(RelationalOperator.LT, entries.get(i).getField()))
				break;
		}
		entries.add(i, e);
	}
	
	public boolean isLeafNode() {
		return true;
	}

	public void setParent(InnerNode parent){
		this.parent = parent;
	}
	
	public InnerNode getParent() {
		return parent;
	}
	

}