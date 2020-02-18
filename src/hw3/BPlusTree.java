package hw3;


import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class BPlusTree {
    private int innerDegree;
    private int leafDegree;
    private Node root;
    private LeafNode searchedPosition;
    public BPlusTree(int pInner, int pLeaf) {
    	innerDegree = pInner;
    	leafDegree = pLeaf;
    	root = null;
    	searchedPosition = null;
    }
    
    public LeafNode search(Field f) {
    	Node node = getRoot();
    	if(node == null) return null;
    	while(!node.isLeafNode()){
    		InnerNode innerNode = (InnerNode) node;
    		ArrayList<Field> keys = innerNode.getKeys();
    		ArrayList<Node> children = innerNode.getChildren();
    		int i=0;
    		for(;i<keys.size();i++){
    			if(f.compare(RelationalOperator.LTE, keys.get(i))){
    				break;
    			}
    		}
			node = children.get(i);
    	}
    	searchedPosition = (LeafNode) node;
    	ArrayList<Entry> entries = searchedPosition.getEntries();
    	for(Entry entry:entries){
    		if(entry.getField().equals(f)) return searchedPosition;
    	}
    	return null;
    }
    
    public void insert(Entry e) {
    	if(getRoot() == null){
    		LeafNode leaf = new LeafNode(leafDegree);
    		leaf.addEntry(e);
    		root = leaf;
    		return;
    	}
    	if(search(e.getField())!=null) return;
    	searchedPosition.addEntry(e);
    	ArrayList<Entry> entries = searchedPosition.getEntries();
    	if(entries.size()>leafDegree){
    		split(searchedPosition);
    	}
    }
    
    public void delete(Entry e) {
    	if(search(e.getField())==null) return;
    	ArrayList<Entry> entries = searchedPosition.getEntries();
    	for(int i=0;i<entries.size();i++){
    		if(entries.get(i).getField().equals(e.getField())) 
    			entries.remove(i);
    	}
		if(searchedPosition.getParent()==null){
			if(searchedPosition.getEntries().size()==0) root = null;
			return;
		}
    	if(entries.size()<Math.ceil((double)leafDegree/2)){
    		if(!searchedPosition.borrowFromSibling()){
    			Node newRoot = searchedPosition.merge();
    			if(newRoot != null)
    				root = newRoot;
    		}
    	}
    }
    
    public Node getRoot() {
    	return root;
    }
    
    public void split(Node node){
    	if(node.isLeafNode()){
    		LeafNode leaf = (LeafNode) node;
    		LeafNode left = new LeafNode(leafDegree);
    		LeafNode right = new LeafNode(leafDegree);
    		ArrayList<Entry> entries = leaf.getEntries();
    		int i=0;
    		for(;i<=(entries.size()-1)/2;i++){
    			left.addEntry(entries.get(i));
    		}
    		for(;i<entries.size();i++){
    			right.addEntry(entries.get(i));
    		}
    		InnerNode parent = node.getParent();
    		int addIndex = 0;
    		if(parent == null){
    			parent = new InnerNode(innerDegree);
    			root = parent;
    		}else{
    			addIndex = parent.removeChild(node);
    		}
    		parent.addChild(right, addIndex,false);
    		parent.addChild(left, addIndex,true);
    		ArrayList<Node> children = parent.getChildren();
    		if(children.size()>innerDegree){
    			split(parent);
    		}
    	}else{
    		InnerNode inner = (InnerNode) node;
    		InnerNode left = new InnerNode(innerDegree);
    		InnerNode right = new InnerNode(innerDegree);
    		ArrayList<Node> children = inner.getChildren();
    		ArrayList<Field> keys = inner.getKeys();
    		int i=0;
    		int middle = (children.size()-1)/2;
    		for(;i<=middle;i++){
    			left.addChild(children.get(i), i,false);
    			left.addKey(keys.get(i), i);
    		}
    		for(;i<children.size();i++){
    			right.addChild(children.get(i), i-middle-1,false);
    			if(i<keys.size())
    				right.addKey(keys.get(i), i-middle-1);
    		}
    		InnerNode parent = node.getParent();
    		int addIndex = 0;
    		if(parent == null){
    			parent = new InnerNode(innerDegree);
    			root = parent;
    		}else{
    			addIndex = parent.removeChild(node);
    		}
    		parent.addChild(right, addIndex,false);
    		parent.addChild(left, addIndex,true);
    		ArrayList<Node> parentChildren = parent.getChildren();
    		if(parentChildren.size()>innerDegree){
    			split(parent);
    		}
    	}
    }

	
}
