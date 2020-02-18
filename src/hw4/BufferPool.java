package hw4;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.xml.crypto.Data;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {
    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;

    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    private Map<HeapPage, Lock> locks;
    private Map<HeapPage, ArrayList<Lock>> lockQueue;
    private Map<HeapPage, Boolean> bp;
    private int numPages;
    private class Lock{
    	Permissions perm;
    	int tid;
    	ArrayList<Integer> tids;
    	public Lock(Permissions p,int tid){
    		perm = p;
    		tids = new ArrayList<>();
    		if(perm.toString().equals("READ_ONLY")){
    			tids.add(tid);
    			this.tid = tid;
    		}else{
    			this.tid = tid;
    		}
    	}
    	public boolean addReadLock(int tid){
    		if(perm.toString().equals("READ_ONLY")){
    			tids.add(tid);
    			return true;
    		}else if(this.tid == tid){
    			perm = Permissions.READ_ONLY;
    			this.tids.add(tid);
    			return true;
    		}
    		return false;
    	}
    }
    
    public BufferPool(int numPages) {
        // your code here
    	locks = new HashMap<>();
    	bp = new HashMap<>();
    	this.numPages = numPages;
//    	lockQueue = new HashMap<>();
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {
        // your code here
    	Catalog c = Database.getCatalog();
    	HeapPage hp = null;
    	boolean exist = false;
    	for(HeapPage page : bp.keySet()){
    		if(page.getTableId() == tableId && page.getId() == pid){
    			hp = page;
    			exist = true;
    			break;
    		}
    	}
    	if(hp == null){
    		HeapFile hf = c.getDbFile(tableId);
        	hp = hf.readPage(pid);
    	}
		if(perm.toString().equals("READ_ONLY")){
			if(locks.containsKey(hp)){
				Lock l = locks.get(hp);
				if(!l.addReadLock(tid)){
					transactionComplete(tid, false);
				}
			}else{
				locks.put(hp, new Lock(perm, tid));
			}
		}else{
			if(!locks.containsKey(hp)){
				locks.put(hp, new Lock(perm, tid));
			}else{
				Lock l = locks.get(hp);
				if(l.perm.toString().equals("READ_ONLY") && l.tids.contains(tid)){
					l.tids.remove((Integer)tid);
					if(l.tids.size() == 0){
						l.perm = perm;
						l.tid = tid;
					}
				}else if(l.perm.toString().equals("READ_WRITE") && l.tid != tid){
					transactionComplete(tid, false);
				}
			}
		}
    	if(!exist){
        	if(bp.size() < numPages)
        		bp.put(hp, false);
        	else{
        		evictPage();
        		bp.put(hp, false);
        	}
    	}
        return hp;
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableID the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(int tid, int tableId, int pid) {
        // your code here
    	HeapPage hp = null;
    	for(HeapPage page : bp.keySet()){
    		if(page.getTableId() == tableId && page.getId() == pid){
    			hp = page;
    			break;
    		}
    	}
    	if(hp == null) return;
    	Lock l = locks.get(hp);
    	if(l != null && l.perm.toString().equals("READ_ONLY")){
    		l.tids.remove((Integer)tid);
    		if(l.tids.isEmpty()){
    			locks.remove(hp);
    		}
    	}
    	else if(l != null) {
    		locks.remove(hp);
    		l.tid = -1;
    	}
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public   boolean holdsLock(int tid, int tableId, int pid) {
        // your code here
    	HeapPage hp = null;
    	for(HeapPage page : bp.keySet()){
    		if(page.getTableId() == tableId && page.getId() == pid){
    			hp = page;
    			break;
    		}
    	}
    	Lock l = locks.get(hp);
    	if(l != null){
    		if(l.perm.toString().equals("READ_ONLY"))
    			return l.tids.contains(tid);
    		else return l.tid == tid;
    	}
        return false;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public   void transactionComplete(int tid, boolean commit)
        throws IOException {
        // your code here
    	for(Map.Entry<HeapPage, Boolean> e : bp.entrySet()){
			HeapPage hp = e.getKey();
			Lock l = locks.get(hp);
			if(l != null && l.perm.toString().equals("READ_WRITE") && l.tid == tid){
				int tableId = hp.getTableId();
				int pid = hp.getId();
				if(commit && e.getValue()){
					flushPage(tableId, pid);
					releasePage(tid, tableId, pid);
		    	}else{
		    		releasePage(tid, tableId, pid);
		    		bp.remove(hp);
		    	}
			}else if(l.perm.toString().equals("READ_ONLY")){
				int tableId = hp.getTableId();
				int pid = hp.getId();
				releasePage(tid, tableId, pid);
			}
		}
    	
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public  void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    	Catalog c = Database.getCatalog();
    	HeapFile hf = c.getDbFile(tableId);
    	HeapPage hp = hf.findAvailablePage();
    	boolean exist = false;
    	for(HeapPage page : bp.keySet()){
    		if(page.getTableId() == tableId && page.getId() == hp.getId()){
    			hp = page;
    			exist = true;
    			break;
    		}
    	}
    	if(exist && holdsLock(tid, tableId, hp.getId()) && locks.get(hp).perm.toString().equals("READ_WRITE")){
    		hp.addTuple(t);
    		bp.put(hp, true);
    	}else{
    		throw new Exception();
    	}
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public  void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    	HeapPage hp = null;
    	int pid = t.getPid();
    	for(HeapPage page : bp.keySet()){
    		if(page.getTableId() == tableId && page.getId() == pid){
    			hp = page;
    			break;
    		}
    	}
    	if(hp != null && holdsLock(tid, tableId, hp.getId()) && locks.get(hp).perm.toString().equals("READ_WRITE")){
    		hp.deleteTuple(t);
    		bp.put(hp, true);
    	}
    }

    private synchronized  void flushPage(int tableId, int pid) throws IOException {
        // your code here
    	HeapFile hf = Database.getCatalog().getDbFile(tableId);
    	for(HeapPage page : bp.keySet()){
    		if(page.getTableId() == tableId && page.getId() == pid){
    			hf.writePage(page);
    			bp.put(page, false);
    			return;
    		}
    	}
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void evictPage() throws Exception {
        // your code here
    	for(HeapPage page : bp.keySet()){
			if(bp.get(page) == false){
				bp.remove(page);
				break;
			}
		}
		if(bp.size() >= numPages)
			throw new Exception();
    }

}
