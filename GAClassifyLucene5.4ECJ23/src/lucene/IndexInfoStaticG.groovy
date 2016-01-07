package lucene

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.CachingWrapperFilter
import org.apache.lucene.search.Filter
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.QueryWrapperFilter
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanTermQuery
import org.apache.lucene.store.Directory
import org.apache.lucene.index.DirectoryReader

import org.apache.lucene.store.FSDirectory

import query.*

/**
 * Static class to store index information.
 * Set the path to the lucene index here
 */

public class IndexInfoStaticG {

	private final static String pathToIndex =
	  "C:\\Users\\laurie\\Java\\indexes2\\20bydate"
	
	static IndexSearcher indexSearcher;

	private static String categoryNumber="0";

	// Lucene field names
	public static final String FIELD_CATEGORY = "category";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_TEST_TRAIN = "test_train";

	public static Filter catTrainF, othersTrainF, catTestF, othersTestF, trainF;

	public static BooleanQuery catTrainBQ, othersTrainBQ, catTestBQ, othersTestBQ;

	public static int totalTrainDocsInCat, totalTestDocsInCat, totalOthersTrainDocs, totalTestDocs;

	private static final TermQuery trainQ = new TermQuery(new Term(
	IndexInfoStaticG.FIELD_TEST_TRAIN, "train"));

	private static final TermQuery testQ = new TermQuery(new Term(
	IndexInfoStaticG.FIELD_TEST_TRAIN, "test"));

	static {
		Path path = Paths.get(pathToIndex)
		Directory directory = FSDirectory.open(path)			
		IndexReader reader = DirectoryReader.open(directory)	
		indexSearcher = new IndexSearcher(reader);
		setFilters()
	}

	public static void setCatNumber(final int cn) {
		categoryNumber = cn;
		setFilters()
	}

	public static String getCatnumberAsString() {
		//return categoryNumber;
		return String.valueOf(categoryNumber);
	}

	private static void setFilters() throws IOException {

		final TermQuery catQ = new TermQuery(new Term(IndexInfoStaticG.FIELD_CATEGORY,
					categoryNumber));

		catTrainBQ = new BooleanQuery(true);
		othersTrainBQ = new BooleanQuery(true);
		catTestBQ = new BooleanQuery(true);
		othersTestBQ = new BooleanQuery(true);

		catTrainBQ.add(catQ, BooleanClause.Occur.MUST);
		catTrainBQ.add(trainQ, BooleanClause.Occur.MUST);

		catTestBQ.add(catQ, BooleanClause.Occur.MUST);
		catTestBQ.add(testQ, BooleanClause.Occur.MUST);

		othersTrainBQ.add(catQ, BooleanClause.Occur.MUST_NOT);
		othersTrainBQ.add(trainQ, BooleanClause.Occur.MUST);

		othersTestBQ.add(catQ, BooleanClause.Occur.MUST_NOT);
		othersTestBQ.add(testQ, BooleanClause.Occur.MUST);

		TotalHitCountCollector collector  = new TotalHitCountCollector();
		indexSearcher.search(catTrainBQ, collector);
		totalTrainDocsInCat = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(catTestBQ, collector);
		totalTestDocsInCat = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(othersTrainBQ, collector);
		totalOthersTrainDocs = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(trainQ, collector);
		int totalTrain = collector.getTotalHits();
		
		collector  = new TotalHitCountCollector();
		indexSearcher.search(testQ, collector);
		totalTestDocs = collector.getTotalHits();

		
		catTrainF = new CachingWrapperFilter(new QueryWrapperFilter(catTrainBQ));
		othersTrainF = new CachingWrapperFilter(new QueryWrapperFilter(
				othersTrainBQ));

		catTestF = new CachingWrapperFilter(new QueryWrapperFilter(catTestBQ));
		othersTestF = new CachingWrapperFilter(new QueryWrapperFilter(
				othersTestBQ));

		trainF = new CachingWrapperFilter(new QueryWrapperFilter(trainQ));

		println "Total train docs: $totalTrain"
		println "CategoryNumber $categoryNumber Total train in cat: $totalTrainDocsInCat  Total others tain: $totalOthersTrainDocs   Total test in cat : $totalTestDocsInCat  "
	}
}