package test;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw3.BPlusTree;
import hw3.Entry;
import hw3.InnerNode;
import hw3.LeafNode;
import hw3.Node;

public class YourHW3Tests {

	@Test
	public void testInsertDuplicate() {
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		LeafNode l = (LeafNode)bt.getRoot();
		assertTrue(l.getEntries().get(0).getField().equals(new IntField(9)));
		bt.insert(new Entry(new IntField(9), 0));
		//inserting duplication should not add another leaf node
		assertTrue(l.getEntries().get(1) == null);
	}

	@Test
	public void testDeleteNotInTree() {
		//Create a tree, then delete not exist value
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));

		try{
			bt.delete(new Entry(new IntField(7), 0));
			fail("Not in tree");
		}catch(NoSuchElementException e){
			
		}
	}

}
