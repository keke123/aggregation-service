/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.aggregate.protocol.avro;

import com.google.common.io.ByteSource;
import java.nio.ByteBuffer;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericRecord;

/** Implementation of AvroRecordReaders that deserializes to {@code Reports.} */
public final class AvroReportsReader extends AvroRecordReader<AvroReportRecord> {

  AvroReportsReader(DataFileStream<GenericRecord> streamReader) {
    super(streamReader);
  }

  AvroReportRecord deserializeRecordFromGeneric(GenericRecord record) {
    return AvroReportRecord.create(
        ByteSource.wrap(((ByteBuffer) record.get("payload")).array()),
        record.get("key_id").toString(),
        record.get("shared_info").toString());
  }
}
