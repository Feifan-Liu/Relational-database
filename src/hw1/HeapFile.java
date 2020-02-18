package hw1;

import java.awt.print.Printable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	public static final int PAGE_SIZE = 4096;
	
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param types type of tuples contained in the file
	 */
	private File file;
	private TupleDesc tupleDesc;
	public HeapFile(File f, TupleDesc type) {
		//your code here
		this.file = f;
		tupleDesc = type;
	}
	
	public File getFile() {
		//your code here
		return file;
	}
	
	public TupleDesc getTupleDesc() {
		//your code here
		return tupleDesc;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 */
	public HeapPage readPage(int id) {
		//your code here
		try {
			RandomAccessFile ra = new RandomAccessFile(file,"r");
			ra.skipBytes(HeapFile.PAGE_SIZE*id);
			byte[] data = new byte[HeapFile.PAGE_SIZE];
			ra.read(data);
			ra.close();
			return new HeapPage(id, data, this.getId());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		//your code here
		return file.hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) {
		//your code here
		try {
			byte[] pageData = p.getPageData();
			RandomAccessFile ra = new RandomAccessFile(file,"rw");
			ra.skipBytes(HeapFile.PAGE_SIZE*p.getId());
			ra.write(pageData);
			ra.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 */
	public HeapPage addTuple(Tuple t) {
		//your code here
		int n = getNumPages();
		int i = 0;
		HeapPage page;
		for(;i<n;i++){
			page = readPage(i);
			if(page.hasEmptySlot()){
				try {
					page.addTuple(t);
					writePage(page);
					return page;
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		if(i==n){
			try {
				page = new HeapPage(n, new byte[HeapFile.PAGE_SIZE], getId());
				page.addTuple(t);
				writePage(page);
				return page;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public HeapPage findAvailablePage(){
		HeapPage page;
		for(int i = 0;i<getNumPages();i++){
			page = readPage(i);
			if(page.hasEmptySlot()){
				return page;
			}
		}
		return null;
	}
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 */
	public void deleteTuple(Tuple t){
		//your code here
		HeapPage page = readPage(t.getPid());
		page.deleteTuple(t);
//		writePage(page);
	}
	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		//your code here
		ArrayList<Tuple> tupleList = new ArrayList<>();
		for(int i=0;i<getNumPages();i++){
			Iterator<Tuple> iter = readPage(i).iterator();
			while(iter.hasNext()){
				tupleList.add(iter.next());
			}
		}
		return tupleList;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		//your code here
		return (int) (file.length()/4096);
	}
}
