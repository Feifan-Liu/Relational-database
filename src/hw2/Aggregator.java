package hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * @author Doug Shook
 *
 */
public class Aggregator {

	private AggregateOperator o;
	private boolean groupBy;
	private TupleDesc td;
	private ArrayList<Tuple> tupleList;
	private ArrayList<Integer> countList;
	private int intSum;
	private int count;
	private Field field;
	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
		//your code here
		this.o = o;
		this.groupBy = groupBy;
		this.td = td;
		tupleList = new ArrayList<>();
		countList = new ArrayList<>();
	}

	/**
	 * Merges the given tuple into the current aggregation
	 * @param t the tuple to be aggregated
	 */
	public void merge(Tuple t) {
		//your code here	
		if(groupBy){
			Field f1 = t.getField(0);
			Field f2 = t.getField(1);
			int i = 0;
			for(;i<tupleList.size();i++){
				if(f1.equals(tupleList.get(i).getField(0))) break;
			}
			if(i==tupleList.size()){
				tupleList.add(t);
				countList.add(1);
				return;
			}
			Field field = tupleList.get(i).getField(1);
			switch (o) {
			case COUNT:
			case SUM:
			case AVG:
				tupleList.get(i).setField(1, new IntField(((IntField)field).getValue()+((IntField)f2).getValue()));
				countList.set(i, countList.get(i)+1);
				break;
			case MAX:
				if(f2.compare(RelationalOperator.GT, field))
					tupleList.get(i).setField(1, f2);
				break;
			case MIN:
				if(f2.compare(RelationalOperator.LT, field))
					tupleList.get(i).setField(1, f2);
				break;
			}
			return;
		}
		int value = 0;
		Field f = t.getField(0);
		Type dataType = td.getType(0);
		if(dataType.equals(Type.INT))
			value = ((IntField) f).getValue();
		if(count == 0){
			count++;
			field = f;
			intSum += value;
			return;
		}
		switch (o) {
		case AVG:
			count++;
			intSum += value;
			break;
		case COUNT:
			count++;
			break;
		case MAX:
			if(f.compare(RelationalOperator.GT, field)){
				field = f;
			}
			break;
		case MIN:
			if(f.compare(RelationalOperator.LT, field)){
				field = f;
			}
			break;
		case SUM:
			intSum += value;
			break;
		}
	}
	
	/**
	 * Returns the result of the aggregation
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		//your code here
		if(groupBy){
			switch(o){
			case AVG:
				for(int i=0;i<tupleList.size();i++){
					Tuple t = tupleList.get(i);
					t.setField(1, new IntField(((IntField)t.getField(1)).getValue()/countList.get(i)));
				}
				return tupleList;
			case COUNT:
				for(int i=0;i<tupleList.size();i++){
					Tuple t = tupleList.get(i);
					t.setDesc(new TupleDesc(new Type[]{td.getType(0),Type.INT}, new String[]{td.getFieldName(0),td.getFieldName(1)}));
					t.setField(1, new IntField(countList.get(i)));
				}
				return tupleList;
			case MAX:
			case MIN:
			case SUM:
				return tupleList;
			}
		}
		Tuple tuple = new Tuple(td);
		switch (o) {
		case AVG:
			tuple.setField(0, new IntField(intSum/count));
			break;
		case COUNT:
			TupleDesc tDesc = new TupleDesc(new Type[]{Type.INT}, new String[]{td.getFieldName(0)});
			tuple.setDesc(tDesc);
			tuple.setField(0, new IntField(count));
			break;
		case MAX:
		case MIN:
			if(td.getType(0).equals(Type.INT))
				tuple.setField(0, (IntField)field);
			else tuple.setField(0, (StringField)field);
			break;
		case SUM:
			tuple.setField(0, new IntField(intSum));
			break;
		}
		tupleList.add(tuple);
		return tupleList;
	}

}
