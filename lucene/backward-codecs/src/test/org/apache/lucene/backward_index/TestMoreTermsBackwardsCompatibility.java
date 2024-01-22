/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.backward_index;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.NoMergePolicy;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.FieldExistsQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.tests.analysis.MockAnalyzer;
import org.apache.lucene.tests.util.LineFileDocs;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TestMoreTermsBackwardsCompatibility extends BackwardsCompatibilityTestBase {

  public TestMoreTermsBackwardsCompatibility(Version version, String pattern) {
    super(version, pattern);
  }

  @ParametersFactory(argumentFormatting = "Lucene-Version:%1$s; Pattern: %2$s")
  public static Iterable<Object[]> testVersionsFactory() {
    List<Object[]> params = new ArrayList<>();
    // NOCOMMIT: why are we only testing one version here?
    params.add(new Object[] {Version.LUCENE_9_0_0, "moreterms.%1$s.zip"});
    return params;
  }

  @Override
  protected void createIndex(Directory directory) throws IOException {
    LogByteSizeMergePolicy mp = new LogByteSizeMergePolicy();
    mp.setNoCFSRatio(1.0);
    mp.setMaxCFSSegmentSizeMB(Double.POSITIVE_INFINITY);
    MockAnalyzer analyzer = new MockAnalyzer(random());
    analyzer.setMaxTokenLength(TestUtil.nextInt(random(), 1, IndexWriter.MAX_TERM_LENGTH));

    IndexWriterConfig conf =
            new IndexWriterConfig(analyzer)
                    .setMergePolicy(mp)
                    .setCodec(TestUtil.getDefaultCodec())
                    .setUseCompoundFile(false);
    IndexWriter writer = new IndexWriter(directory, conf);
    LineFileDocs docs = new LineFileDocs(new Random(0));
    for (int i = 0; i < 50; i++) {
      Document doc = TestUtil.cloneDocument(docs.nextDoc());
      doc.add(
              new NumericDocValuesField(
                      "docid_intDV", doc.getField("docid_int").numericValue().longValue()));
      doc.add(
              new SortedDocValuesField("titleDV", new BytesRef(doc.getField("title").stringValue())));
      writer.addDocument(doc);
      if (i % 10 == 0) { // commit every 10 documents
        writer.commit();
      }
    }
    docs.close();
    writer.close();
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      TestIndexSortBackwardsCompatibility.searchExampleIndex(reader); // make sure we can search it
    }
  }
  public void testMoreTerms() throws Exception {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {

      TestUtil.checkIndex(directory);
      TestIndexSortBackwardsCompatibility.searchExampleIndex(reader);
    }
  }
}
