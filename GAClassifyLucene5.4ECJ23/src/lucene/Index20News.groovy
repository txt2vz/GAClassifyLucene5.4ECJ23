package lucene

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

class Index20News {
	// Create Lucene index in this directory
	def indexPath =  "C:\\Users\\laurie\\Java\\indexes2\\20bydate"	 

	// Index files in this directory
	def docsPath = "C:\\Users\\Laurie\\Dataset\\20NGb10"

	static main(args) {
		def i = new Index20News()
		i.setup()
	}

	def setup() {

		Date start = new Date();
		println("Indexing to directory '" + indexPath + "'...");
		Path path = Paths.get(indexPath)
		Directory directory = FSDirectory.open(path)
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		// Create a new index in the directory, removing any
		// previously indexed documents:
		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(directory, iwc);
		//def z=0

		new File(docsPath).eachDir {
			def catNumber=0;
			it .eachDir {

				it.eachFileRecurse {
					if (!it.hidden && it.exists() && it.canRead() && !it.directory){   //&& !docsCatMap.containsKey(it.name)) {

						indexDocs(writer,it, catNumber)
					}
				}
				catNumber++;
			}
		}

		Date end = new Date();
		println(end.getTime() - start.getTime() + " total milliseconds");
		println "***************************************************************"

		String querystr =  "gun";

		Query q = new QueryParser(IndexInfoStaticG.FIELD_CONTENTS, analyzer).parse(querystr);
	
		//  search
		int hitsPerPage = 5;
		IndexReader reader2 =  writer.getReader();

		println " reader max doc " + reader2.maxDoc()
		IndexSearcher searcher = new IndexSearcher(reader2);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 4. display results
		println "Searching for: $querystr Found ${hits.length} hits:"
		hits.each{
			int docId = it.doc;
			Document d = searcher.doc(docId);
			println(d.get(IndexInfoStaticG.FIELD_TEST_TRAIN) + "\t" + d.get("path") + "\t" +
					d.get(IndexInfoStaticG.FIELD_CATEGORY) );
		}
		
		reader2.close();
		writer.close();
	}
	/**
	 * Indexes the given file using the given writer, or if a directory is given,
	 * recurses over files and directories found under the given directory.
	 */
	def indexDocs(IndexWriter writer, File f, categoryNumber)
	throws IOException {

		def doc = new Document()

		FileInputStream fis=new FileInputStream(f);

		Field pathField = new StringField(IndexInfoStaticG.FIELD_PATH, f.getPath(), Field.Store.YES);
		doc.add(pathField);

		doc.add(new TextField(IndexInfoStaticG.FIELD_CONTENTS, new BufferedReader(new InputStreamReader(fis, "UTF-8"))) );

		Field categoryField = new StringField(IndexInfoStaticG.FIELD_CATEGORY, categoryNumber.toString(), Field.Store.YES);
		doc.add(categoryField)

		//set test train field
		String test_train
		if ( f.canonicalPath.contains("test")) test_train="test" else test_train="train";
		
		Field ttField = new StringField(IndexInfoStaticG.FIELD_TEST_TRAIN, test_train, Field.Store.YES)
		doc.add(ttField)

		//println "Indexing ${f.canonicalPath} categorynumber: $categoryNumber  testtrain $test_train"
		writer.addDocument(doc);
	}
}
