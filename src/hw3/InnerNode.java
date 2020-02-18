package hw3;

import java.util.ArrayList;

import org.hamcrest.SelfDescribing;

import hw1.Field;

public class InnerNode implements Node {
	
	private int degree;
	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	private InnerNode parent;
	public InnerNode(int degree) {
		this.degree = degree;
		keys = new ArrayList<>();
		children = new ArrayList<>();
		parent = null;
	}
	
	public ArrayList<Field> getKeys() {
		return keys;
	}
	
	public void addKey(Field key,int index){
		keys.add(index, key);
	}
	
	public void addChild(Node child,int index,boolean shouldUpdateKeys){
		children.add(index, child);
		child.setParent(this);
		if(shouldUpdateKeys)
			updateKeys(index,child);
	}
	
	public void updateKeys(){
		keys.clear();
		for(int i=0;i<children.size()-1;i++){
			ArrayList<Entry> entries = ((LeafNode) children.get(i)).getEntries();
			keys.add(entries.get(entries.size()-1).getField());
		}
	}
	
	public boolean pushThrough(){
		ArrayList<Node> parentChildren = parent.getChildren();
		ArrayList<Field> parentKeys = parent.getKeys();
		int index = parentChildren.indexOf(this);
		if(index>0){
			InnerNode leftSibling = (InnerNode) parentChildren.get(index - 1);
			ArrayList<Node> leftChildren = leftSibling.getChildren();
			if(leftChildren.size()>Math.ceil((double)degree/2)){
				ArrayList<Field> leftKeys = leftSibling.getKeys();
				parentKeys.add(index - 1,leftKeys.get(leftKeys.size()-1));
				leftKeys.remove(leftKeys.size()-1);
				keys.add(0, parentKeys.get(index));
				parentKeys.remove(index);
				children.add(0,leftChildren.get(leftChildren.size()-1));
				leftChildren.remove(leftChildren.size()-1);
				return true;
			}
		}
		if(index<parentChildren.size()-1){
			InnerNode rightSibling = (InnerNode) parentChildren.get(index + 1);
			ArrayList<Node> rightChildren = rightSibling.getChildren();
			if(rightChildren.size()>Math.ceil((double)degree/2)){
				ArrayList<Field> rightKeys = rightSibling.getKeys();
				parentKeys.add(index + 1,rightKeys.get(0));
				rightKeys.remove(0);
				keys.add(parentKeys.get(index));
				parentKeys.remove(index);
				children.add(children.size(), rightChildren.get(0));
				rightChildren.remove(0);
				return true;
			}
		}
		return false;
	}
	
	public Node merge(){
		ArrayList<Node> parentChildren = parent.getChildren();
		ArrayList<Field> parentKeys = parent.getKeys();
		int index = parentChildren.indexOf(this);
		if(index>0){
			InnerNode leftSibling = (InnerNode) parentChildren.get(index - 1);
			ArrayList<Node> leftChildren = leftSibling.getChildren();
			ArrayList<Field> leftKeys = leftSibling.getKeys();
			keys.add(0,parentKeys.get(index-1));
			parentKeys.remove(index-1);
			for(int i=0;i<children.size();i++){
				leftChildren.add(children.get(i));
				if(i<keys.size())
					leftKeys.add(keys.get(i));
			}
			parentChildren.remove(index);
			if(parentChildren.size()<Math.ceil((double)degree/2)){
				if(parent.getParent() == null){
					if(parentChildren.size()==1){
						leftSibling.setParent(null);
						return leftSibling;
					}
				}else if(!parent.pushThrough()){
					return parent.merge();
				}
			}
		}
		if(index<parentChildren.size()-1){
			InnerNode rightSibling = (InnerNode) parentChildren.get(index + 1);
			ArrayList<Node> rightChildren = rightSibling.getChildren();
			ArrayList<Field> rightKeys = rightSibling.getKeys();
			keys.add(parentKeys.get(index));
			parentKeys.remove(index);
			for(int i=0;i<rightChildren.size();i++){
				children.add(rightChildren.get(i));
				if(i<rightKeys.size())
					keys.add(rightKeys.get(i));
			}
			parentChildren.remove(index+1);
			if(parentChildren.size()<Math.ceil((double)degree/2)){
				if(parent.getParent() == null){
					if(parentChildren.size()==1){
						this.setParent(null);
						return this;
					}
				}else if(!parent.pushThrough()){
					return parent.merge();
				}
			}
		}
		return null;
	}
	
	public void updateKeys(int index,Node child){
		if(child.isLeafNode()){
			LeafNode leaf = (LeafNode)child;
			ArrayList<Entry> entries = leaf.getEntries();
			Field key = entries.get(entries.size()-1).getField();
			if(!keys.contains(key))
				keys.add(index, key);
		}
		else{
			InnerNode inner = (InnerNode)child;
			ArrayList<Field> childKeys = inner.getKeys();
			Field key = childKeys.get(childKeys.size()-1);
			if(!keys.contains(key)){
				childKeys.remove(childKeys.size()-1);
				keys.add(index, key);
			}
		}
	}
	
	public int removeChild(Node child){
		int i = children.indexOf(child);
		children.remove(i);
		return i;
	}
	
	public ArrayList<Node> getChildren() {
		return children;
	}

	public int getDegree() {
		return degree;
	}
	
	public boolean isLeafNode() {
		return false;
	}

	public InnerNode getParent() {
		return parent;
	}

	public void setParent(InnerNode parent) {
		this.parent = parent;
	}

}