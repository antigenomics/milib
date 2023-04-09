package com.milaboratory.core.alignment.blast;

import com.milaboratory.core.io.sequence.fasta.FastaReader;
import com.milaboratory.core.io.sequence.fasta.FastaRecord;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Sequence;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlastDB<S extends Sequence<S>> {
    private final String name;
    private final String title;
    private final long recordsCount, lettersCount;
    private final Alphabet<S> alphabet;
    private final List<String> volumes;
    private final boolean temp;

    public BlastDB(String name, String title, long recordsCount, long lettersCount, Alphabet<S> alphabet,
                   List<String> volumes, boolean temp) {
        this.name = name;
        this.title = title;
        this.recordsCount = recordsCount;
        this.lettersCount = lettersCount;
        this.alphabet = alphabet;
        this.volumes = volumes;
        this.temp = temp;
    }

    public S retriveSequenceById(String id) {
        try {
            Process proc = Blast.getProcessBuilder(Blast.CMD_BLASTDBCMD, "-db", name, "-entry", id).start();
            FastaReader<S> reader = new FastaReader<>(proc.getInputStream(), getAlphabet());
            FastaRecord<S> record = reader.take();
            while (reader.take() != null) ;
            proc.waitFor();
            if (record == null)
                return null;
            else
                return record.getSequence();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Alphabet<S> getAlphabet() {
        return alphabet;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getVolumes() {
        return volumes;
    }

    public long getRecordsCount() {
        return recordsCount;
    }

    public long getLettersCount() {
        return lettersCount;
    }

    public static Pattern[] getLinePatterns() {
        return linePatterns;
    }

    private static final Map<Alphabet<?>, String[]> extensionsToDelete = new HashMap<>();

    static {
        extensionsToDelete.put(NucleotideSequence.ALPHABET, new String[]{".nhr", ".nin", ".nnd", ".nni", ".nog", ".nsd", ".nsi", ".nsq"});
        extensionsToDelete.put(AminoAcidSequence.ALPHABET, new String[]{".phr", ".pin", ".pnd", ".pni", ".pog", ".psd", ".psi", ".psq"});
    }

    @Override
    protected void finalize() throws Throwable {
        if (temp) {
            String path = volumes.get(0);
            Path p;
            for (String ext : extensionsToDelete.get(alphabet))
                if (Files.exists(p = Paths.get(path + ext)))
                    Files.delete(p);
        }
        super.finalize();
    }

    private static final int
            DATABASE_INDEX = 0,
            STAT_INDEX = 1,
            VOLUMES_INDEX = 2;

    private static final Pattern[] linePatterns = {
            Pattern.compile("^Database: (.*)$"),
            Pattern.compile("^\\s*([0-9,]+)\\s*sequences;\\s*([0-9,]+)\\s*total .*$"),
            Pattern.compile("^\\s*Volumes:\\s*$")
    };

    public static <S extends Sequence<S>> BlastDB<S> get(String name) {
        return get(name, false);
    }

    static <S extends Sequence<S>> BlastDB<S> get(String name, boolean temp) {
        try {
            ProcessBuilder processBuilder = Blast.getProcessBuilder(Blast.CMD_BLASTDBCMD, "-db", name, "-info");
            Process proc = processBuilder.start();
            List<String> lines = IOUtils.readLines(proc.getInputStream(), Charset.forName("US-ASCII"));
            String error = IOUtils.toString(proc.getErrorStream(), Charset.forName("US-ASCII"));
            if (proc.waitFor() != 0) {
                throw new RuntimeException("Error: " + error);
            }

            // Previous
            /*
             * Database: rnd
             * 	6,298 sequences; 2,974,038 total residues
             *
             * Date: Aug 8, 2015  11:46 AM	Longest sequence: 4,910 residues
             *
             * Volumes:
             * 	/Volumes/Data/tools/ncbi-blast-2.2.31+/db/yeast
             */


            // Upgrade
            /*
             * Database: 1f0a83a9195ab6b7aecded3fb535bfc21d2fd988
             * 	2 sequences; 67 total bases
             *
             * Date: Jan 30, 2019  6:49 PM	Longest sequence: 51 bases
             *
             * BLASTDB Version: 4
             *
             * Volumes:
             * 	/private/var/folders/6q/jtkhqqw55rg102dplq_396y40000gn/T/milib_50f9e9bcf14f2b676fcd97ec2e7ca5aabd52415c8026027369250916275/1f0a83a9195ab6b7aecded3fb535bfc21d2fd988
             */

            LineMatch[] matchers = new LineMatch[linePatterns.length];
            int patternIndex = 0;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher m = linePatterns[patternIndex].matcher(line);
                if (!m.matches())
                    continue;

                matchers[patternIndex] = new LineMatch(i, m);
                ++patternIndex;

                if (patternIndex == linePatterns.length)
                    // All required patterns are found
                    break;
            }
            if (patternIndex != linePatterns.length)
                throw new RuntimeException("Pattern " + patternIndex + " not matched.");

            String dbTitle = matchers[DATABASE_INDEX].matcher.group(1);
            long records = Long.parseLong(matchers[STAT_INDEX].matcher.group(1).replace(",", ""));
            long letters = Long.parseLong(matchers[STAT_INDEX].matcher.group(2).replace(",", ""));

            List<String> volumes = new ArrayList<>(lines.subList(matchers[VOLUMES_INDEX].lineNumber + 1, lines.size()));
            for (int i = 0; i < volumes.size(); i++)
                volumes.set(i, volumes.get(i).trim());

            String path = volumes.get(0);

            Alphabet<?> alphabet;

            boolean n = Files.exists(Paths.get(path + ".nsq"));
            boolean p = Files.exists(Paths.get(path + ".psq"));

            if (!n ^ p)
                throw new RuntimeException("Can't determine db type.");

            if (n)
                alphabet = NucleotideSequence.ALPHABET;
            else
                alphabet = AminoAcidSequence.ALPHABET;

            return new BlastDB(name, dbTitle, records, letters, alphabet, volumes, temp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final class LineMatch {
        final int lineNumber;
        final Matcher matcher;

        public LineMatch(int lineNumber, Matcher matcher) {
            this.lineNumber = lineNumber;
            this.matcher = matcher;
        }
    }
}
