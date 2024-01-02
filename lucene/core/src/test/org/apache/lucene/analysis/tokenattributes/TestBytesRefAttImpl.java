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
package org.apache.lucene.analysis.tokenattributes;

import java.util.Arrays;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;

public class TestBytesRefAttImpl extends LuceneTestCase {

  public void testCopyTo() throws Exception {
    BytesTermAttributeImpl t = new BytesTermAttributeImpl();
    BytesTermAttributeImpl copy = assertCopyIsEqual(t);

    // first do empty
    assertEquals(t.getBytesRef(), copy.getBytesRef());
    assertNull(copy.getBytesRef());
    // now after setting it
    t.setBytesRef(new BytesRef("hello"));
    copy = assertCopyIsEqual(t);
    assertEquals(t.getBytesRef(), copy.getBytesRef());
    assertNotSame(t.getBytesRef(), copy.getBytesRef());
  }

  public static <T extends AttributeImpl> T assertCopyIsEqual(T att) throws Exception {
    @SuppressWarnings("unchecked")
    T copy = (T) att.getClass().getConstructor().newInstance();
    att.copyTo(copy);
    assertEquals("Copied instance must be equal", att, copy);
    assertEquals("Copied instance's hashcode must be equal", att.hashCode(), copy.hashCode());
    return copy;
  }

  public void testLucene9856() {
    assertTrue(
        "BytesTermAttributeImpl must explicitly declare to implement TermToBytesRefAttribute",
        Arrays.asList(BytesTermAttributeImpl.class.getInterfaces())
            .contains(TermToBytesRefAttribute.class));
  }
}
