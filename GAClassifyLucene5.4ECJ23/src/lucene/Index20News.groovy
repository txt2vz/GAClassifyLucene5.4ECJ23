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
	def docsPath = "C:\\Users\\Laurie\\Dataset\\20bydate"
	
	Path path = Paths.get(indexPath)
	Directory directory = FSDirectory.open(path)
	Analyzer analyzer = new StandardAnalyzer();
	IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	IndexWriter writer = new IndexWriter(directory, iwc);

	static main(args) {
		def i = new Index20News()
		i.buildIndex()
		i.close()
	}

	def buildIndex() {
		Date start = new Date();
		println("Indexing to directory '" + indexPath + "'...");
	
		// Create a new index in the directory, removing any
		// previously indexed documents:
		iwc.setOpenMode(OpenMode.CREATE);

		new File(docsPath).eachDir {
			def catNumber=0;
			it .eachDir {

				it.eachFileRecurse {
					if (!it.hidden && it.exists() && it.canRead() && !it.directory){   
						indexDocs(writer,it, catNumber)
					}
				}
				catNumber++;
			}
		}

		Date end = new Date();
		println(end.getTime() - start.getTime() + " total milliseconds");
		println "***************************************************************"	
		//writer.close();		
	}
	
	def close (){
		writer.close()
	}

	//index the doc adding fields for path, category, test/train and contents
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
