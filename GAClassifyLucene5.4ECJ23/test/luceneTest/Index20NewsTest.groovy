package luceneTest
import lucene.*

import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.TotalHitCountCollector

//@groovy.transform.TypeChecked
class Index20NewsTest extends GroovyTestCase {
	
	def i = new Index20News()
	void setUp2(){
			i.buildIndex()		
	}
	
	//tester for 20NG index
	 void testIndex(){
		String querystr =  "europa";	
		IndexReader reader =  i.writer.getReader();

		println " reader max doc " + reader.maxDoc() 
		assert reader.maxDoc() ==18846
		//assert
		IndexSearcher searcher = new IndexSearcher(reader);
		TotalHitCountCollector collector = new TotalHitCountCollector();				
		Query q = new TermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS,  querystr))
		
		searcher.search(q, collector);
		def totalHits = collector.getTotalHits();
		println "searching for $querystr Found: $totalHits hits"
		assert totalHits==12
		reader.close();		
	}
}
