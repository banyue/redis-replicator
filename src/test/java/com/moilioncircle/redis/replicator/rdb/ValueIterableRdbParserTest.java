/*
 * Copyright 2016 leon chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moilioncircle.redis.replicator.rdb;

import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.FileType;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueHash;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueList;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueSet;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueZSet;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.ZSetEntry;
import com.moilioncircle.redis.replicator.rdb.iterable.ValueIterableRdbVisitor;
import com.moilioncircle.redis.replicator.rdb.iterable.datatype.KeyStringValueByteArrayIterator;
import com.moilioncircle.redis.replicator.rdb.iterable.datatype.KeyStringValueMapEntryIterator;
import com.moilioncircle.redis.replicator.rdb.iterable.datatype.KeyStringValueZSetEntryIterator;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Leon Chen
 * @since 2.3.0
 */
public class ValueIterableRdbParserTest {

    @Test
    public void test() {
        String[] resources = new String[]{"dictionary.rdb",
                "easily_compressible_string_key.rdb", "empty_database.rdb",
                "hash_as_ziplist.rdb", "integer_keys.rdb", "intset_16.rdb",
                "intset_32.rdb", "intset_64.rdb", "keys_with_expiry.rdb",
                "linkedlist.rdb", "multiple_databases.rdb",
                "parser_filters.rdb", "rdb_version_5_with_checksum.rdb", "regular_set.rdb",
                "regular_sorted_set.rdb", "sorted_set_as_ziplist.rdb", "uncompressible_string_keys.rdb",
                "ziplist_that_compresses_easily.rdb", "ziplist_that_doesnt_compress.rdb",
                "ziplist_with_integers.rdb", "zipmap_that_compresses_easily.rdb",
                "zipmap_that_doesnt_compress.rdb", "zipmap_with_big_values.rdb", "rdb_version_8_with_64b_length_and_scores.rdb", "non_ascii_values.rdb", "binarydump.rdb", "module.rdb"};
        for (String f : resources) {
            assertEquals(testFile(f), testFile1(f));
        }
    }

    private int testFile(String fileName) {
        final AtomicInteger acc = new AtomicInteger(0);
        Replicator r = new RedisReplicator(ValueIterableRdbParserTest.class.getClassLoader().getResourceAsStream(fileName), FileType.RDB, Configuration.defaultSetting());
        r.setRdbVisitor(new ValueIterableRdbVisitor(r));
        r.addModuleParser("hellotype", 0, new ModuleTest.HelloTypeModuleParser());
        r.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                if (kv instanceof KeyStringValueByteArrayIterator) {
                    KeyStringValueByteArrayIterator kv1 = (KeyStringValueByteArrayIterator) kv;
                    Iterator<byte[]> it = kv1.getValue();
                    while (it.hasNext()) {
                        it.next();
                        acc.incrementAndGet();
                    }
                } else if (kv instanceof KeyStringValueMapEntryIterator) {
                    KeyStringValueMapEntryIterator kv1 = (KeyStringValueMapEntryIterator) kv;
                    Iterator<Map.Entry<byte[], byte[]>> it = kv1.getValue();
                    while (it.hasNext()) {
                        it.next();
                        acc.incrementAndGet();
                    }
                } else if (kv instanceof KeyStringValueZSetEntryIterator) {
                    KeyStringValueZSetEntryIterator kv1 = (KeyStringValueZSetEntryIterator) kv;
                    Iterator<ZSetEntry> it = kv1.getValue();
                    while (it.hasNext()) {
                        it.next();
                        acc.incrementAndGet();
                    }
                } else {
                    acc.incrementAndGet();
                }
            }
        });
        try {
            r.open();
        } catch (Exception e) {
            fail();
        }
        return acc.get();
    }

    @SuppressWarnings("unused")
    private int testFile1(String fileName) {
        final AtomicInteger acc = new AtomicInteger(0);
        @SuppressWarnings("resource")
        Replicator r = new RedisReplicator(ValueIterableRdbParserTest.class.getClassLoader().getResourceAsStream(fileName), FileType.RDB, Configuration.defaultSetting());
        r.addModuleParser("hellotype", 0, new ModuleTest.HelloTypeModuleParser());
        r.addRdbListener(new RdbListener.Adaptor() {
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                if (kv instanceof KeyStringValueList) {
                    KeyStringValueList kv1 = (KeyStringValueList) kv;
                    for (String s : kv1.getValue()) {
                        acc.incrementAndGet();
                    }
                } else if (kv instanceof KeyStringValueSet) {
                    KeyStringValueSet kv1 = (KeyStringValueSet) kv;
                    for (String s : kv1.getValue()) {
                        acc.incrementAndGet();
                    }
                } else if (kv instanceof KeyStringValueHash) {
                    KeyStringValueHash kv1 = (KeyStringValueHash) kv;
                    for (Map.Entry<String, String> entry : kv1.getValue().entrySet()) {
                        acc.incrementAndGet();
                    }
                } else if (kv instanceof KeyStringValueZSet) {
                    KeyStringValueZSet kv1 = (KeyStringValueZSet) kv;
                    for (ZSetEntry entry : kv1.getValue()) {
                        acc.incrementAndGet();
                    }
                } else {
                    acc.incrementAndGet();
                }
            }
        });
        try {
            r.open();
        } catch (Exception e) {
            fail();
        }
        return acc.get();
    }
}
