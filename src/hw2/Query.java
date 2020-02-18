package hw2;

import java.util.ArrayList;
import java.util.List;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.TupleDesc;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;

public class Query {

	private String q;
	
	public Query(String q) {
		this.q = q;
	}
	
	public Relation execute()  {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
		Catalog c = Database.getCatalog();
		ColumnVisitor cv = new ColumnVisitor();
		WhereExpressionVisitor whv = new WhereExpressionVisitor();

		int tableid = c.getTableId(sb.getFromItem().toString());
		HeapFile hf = c.getDbFile(tableid);
		TupleDesc td = c.getTupleDesc(tableid);
		Relation r = new Relation(hf.getAllTuples(), td);
		if(sb.getJoins()!=null){
			for(Join join:sb.getJoins()){
				int joinTableid = c.getTableId(join.getRightItem().toString());
				HeapFile jhf = c.getDbFile(joinTableid);
				TupleDesc jtd = c.getTupleDesc(joinTableid);
				Relation jr = new Relation(jhf.getAllTuples(), jtd);
				String[] splits = join.getOnExpression().toString().split("=");
				String[] lsplits = splits[0].trim().split("\\.");
				String[] rsplits = splits[1].trim().split("\\.");
				if(c.getTableId(lsplits[0])==joinTableid){
					String temp = lsplits[1];
					lsplits[1] = rsplits[1];
					rsplits[1] = temp;
				}
				
				for(int i=0;i<td.numFields();i++){
					if(td.getFieldName(i).equals(lsplits[1])){
						for(int j=0;j<jtd.numFields();j++){
							if(jtd.getFieldName(j).equals(rsplits[1])){
								r = r.join(jr, i, j);
								td = r.getDesc();
								break;
							}
						}
						break;
					}
				}
			}
		}
		
		if(sb.getWhere()!=null){
			sb.getWhere().accept(whv);
			for(int i=0;i<td.numFields();i++){
				if(td.getFieldName(i).equals(whv.getLeft())){
					r=r.select(i, whv.getOp(), whv.getRight());
				}
			}
		}
		
		ArrayList<Integer> l = new ArrayList<Integer>();
		ArrayList<Integer> renameList = new ArrayList<>();
		ArrayList<String> nameList = new ArrayList<>();
		for(SelectItem selectItem:sb.getSelectItems()){
			selectItem.accept(cv);
			if(cv.getColumn().equals("*")){
				for(int i=0;i<td.numFields();i++)
						l.add(i);
				break;
			}
			for(int i=0;i<td.numFields();i++){
				if(td.getFieldName(i).equals(cv.getColumn())){
					l.add(i);
					SelectExpressionItem se = (SelectExpressionItem) selectItem;
					if(se.getAlias()!=null){
						renameList.add(l.size()-1);
						nameList.add(se.getAlias().getName());
					}
				}
			}
		}
		r = r.project(l);
		if(!renameList.isEmpty()){
			try {
				r = r.rename(renameList, nameList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		td = r.getDesc();
		
		if(cv.isAggregate()){
			if(sb.getGroupByColumnReferences()==null)
				r = r.aggregate(cv.getOp(), false);
			else r = r.aggregate(cv.getOp(), true);
		}
		//your code here
		return r;
		
	}
}
