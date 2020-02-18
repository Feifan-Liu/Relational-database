package hw2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import hw1.Field;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

	private ArrayList<Tuple> tuples;
	private TupleDesc td;
	
	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		//your code here
		tuples = l;
		this.td = td;
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		//your code here
		ArrayList<Tuple> resTuples = new ArrayList<>();
		for(Tuple t:tuples){
			if(t.getField(field).compare(op, operand)){
				resTuples.add(t);
				
			}
		}
		return new Relation(resTuples, td);
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 * @throws Exception 
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) throws Exception {
		//your code here
		Type[] types = new Type[td.numFields()];
		String[] fieldNames = new String[td.numFields()];
		int i=0;
		Set<String> set = new HashSet<>();
		for(;i<td.numFields();i++){
			types[i] = td.getType(i);
			fieldNames[i] = td.getFieldName(i);
			set.add(fieldNames[i]);
		}
		for(i=0;i<fields.size();i++){
			if(set.contains(names.get(i)))
				throw new Exception();
			if(names.get(i).length()>0)
				fieldNames[fields.get(i)]=names.get(i);
		}
		TupleDesc newTd = new TupleDesc(types, fieldNames);
		return new Relation(tuples, newTd);
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 */
	public Relation project(ArrayList<Integer> fields) {
		//your code here
		
		Type[] types = new Type[fields.size()];
		String[] fieldNames = new String[fields.size()];
		if(fields.isEmpty()) return new Relation(new ArrayList<>(),new TupleDesc(types, fieldNames));
		for(int i=0;i<fields.size();i++){
			try {
				types[i] = td.getType(fields.get(i));
				fieldNames[i] = td.getFieldName(fields.get(i));
			} catch (NoSuchElementException e) {
				// TODO: handle exception
				throw new IllegalArgumentException();
			}
		}
		TupleDesc projectTd = new TupleDesc(types, fieldNames);
		ArrayList<Tuple> projectTuples = new ArrayList<>();
		for(Tuple t:tuples){
			Tuple tuple = new Tuple(projectTd);
			for(int i=0;i<fields.size();i++){
				tuple.setField(i, t.getField(fields.get(i)));
			}
			projectTuples.add(tuple);
		}
		return new Relation(projectTuples, projectTd);
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		//your code here
		int n1 = td.numFields(), n2 = other.getDesc().numFields();
		Type[] types = new Type[n1+n2];
		String[] fieldNames = new String[n1+n2];
		int i = 0;
		for(;i<n1;i++){
			types[i] = td.getType(i);
			fieldNames[i] = td.getFieldName(i);
		}
		for(;i<n1+n2;i++){
			types[i] = other.getDesc().getType(i-n1);
			fieldNames[i] = other.getDesc().getFieldName(i-n1);
		}
		TupleDesc joinTd = new TupleDesc(types, fieldNames);
		ArrayList<Tuple> joinTuples = new ArrayList<>();
		for(Tuple t1:tuples){
			for(Tuple t2:other.getTuples()){
				if(t1.getField(field1).compare(RelationalOperator.EQ, t2.getField(field2))){
					Tuple joinTuple = new Tuple(joinTd);
					for(i=0;i<n1;i++){
						joinTuple.setField(i, t1.getField(i));
					}
					for(;i<n1+n2;i++){
						joinTuple.setField(i, t2.getField(i-n1));
					}
					joinTuples.add(joinTuple);
				}
			}
		}
		return new Relation(joinTuples, joinTd);
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		//your code here
		Aggregator ag = new Aggregator(op, groupBy, td);
		for(Tuple t:tuples){
			ag.merge(t);
		}
		ArrayList<Tuple> res = ag.getResults();
		return new Relation(res, res.get(0).getDesc());
	}
	
	public TupleDesc getDesc() {
		//your code here
		return this.td;
	}
	
	public ArrayList<Tuple> getTuples() {
		//your code here
		return this.tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		//your code here
		String res = td.toString();
		for(Tuple tuple:tuples){
			res += " " + tuple.toString();
		}
		return res;
	}
}
