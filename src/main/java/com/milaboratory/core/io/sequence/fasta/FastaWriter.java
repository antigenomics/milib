/*
 * Copyright 2015 MiLaboratory.com
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
package com.milaboratory.core.io.sequence.fasta;

import com.milaboratory.core.sequence.Sequence;

import java.io.*;

/**
 * Writer of FASTA files.
 *
 * @param <S> sequence type
 */
public final class FastaWriter<S extends Sequence<S>> implements AutoCloseable {
    public static final int DEFAULT_MAX_LENGTH = 75;
    final int maxLength;
    final OutputStream outputStream;

    /**
     * Creates FASTA writer
     *
     * @param fileName file to be created
     */
    public FastaWriter(String fileName) throws FileNotFoundException {
        this(new File(fileName), DEFAULT_MAX_LENGTH);
    }

    /**
     * Creates FASTA writer
     *
     * @param file output file
     */
    public FastaWriter(File file) throws FileNotFoundException {
        this(file, DEFAULT_MAX_LENGTH);
    }

    /**
     * Creates FASTA writer
     *
     * @param file      output file
     * @param maxLength line length limit after which sequence will be split into several lines
     */
    public FastaWriter(File file, int maxLength) throws FileNotFoundException {
        this.outputStream = new BufferedOutputStream(new FileOutputStream(file));
        this.maxLength = maxLength;
    }

    /**
     * Creates FASTA writer
     *
     * @param outputStream output stream
     */
    public FastaWriter(OutputStream outputStream) {
        this(outputStream, DEFAULT_MAX_LENGTH);
    }

    /**
     * Creates FASTA writer
     *
     * @param outputStream output stream
     * @param maxLength    line length limit after which sequence will be split into several lines
     */
    public FastaWriter(OutputStream outputStream, int maxLength) {
        this.outputStream = outputStream;
        this.maxLength = maxLength;
    }

    public void write(FastaRecord<S> record) {
        write(record.getDescription(), record.getSequence());
    }

    public void write(String description, S sequence) {
        writeRaw(description, sequence.toString());
    }

    public void writeRaw(FastaReader.RawFastaRecord record) {
        writeRaw(record.description, record.sequence);
    }

    public synchronized void writeRaw(String description, String sequence) {
        try {
            outputStream.write('>');
            if (description != null)
                outputStream.write(description.getBytes());
            outputStream.write('\n');

            byte[] seq = sequence.getBytes();
            int pointer = 0;
            do {
                outputStream.write(seq, pointer, Math.min(seq.length - pointer, maxLength));
                pointer += maxLength;
                outputStream.write('\n');
            } while (seq.length > pointer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Flush underlying stream.
     */
    public synchronized void flush() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close writer.
     */
    @Override
    public synchronized void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
