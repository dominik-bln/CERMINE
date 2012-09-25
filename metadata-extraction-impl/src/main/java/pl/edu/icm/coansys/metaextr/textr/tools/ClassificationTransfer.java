package pl.edu.icm.coansys.metaextr.textr.tools;

import pl.edu.icm.coansys.metaextr.textr.model.BxZone;
import pl.edu.icm.coansys.metaextr.textr.model.BxLine;
import pl.edu.icm.coansys.metaextr.textr.model.BxChunk;
import pl.edu.icm.coansys.metaextr.textr.model.BxPage;
import pl.edu.icm.coansys.metaextr.textr.model.BxWord;
import pl.edu.icm.coansys.metaextr.textr.model.BxZoneLabel;
import pl.edu.icm.coansys.metaextr.textr.model.BxDocument;
import java.util.*;

/**
 *
 * @author krusek
 */
public class ClassificationTransfer {

    private static final double TOLERANCE = 1.0e-3;

    public void transferClassification(BxDocument source, BxDocument target) {
        if (source.getPages().size() != target.getPages().size()) {
            throw new IllegalArgumentException("Page counts of the documents must be equal");
        }
        for (int i = 0; i < source.getPages().size(); i++) {
            transferClassification(source.getPages().get(i), target.getPages().get(i));
        }
    }

    public void transferClassification(BxPage source, BxPage target) {
        Map<String, Map<BxChunk, Entry>> map = new HashMap<String, Map<BxChunk, Entry>>();
        List<Entry> entries = new ArrayList<Entry>();
        for (BxZone zone : target.getZones()) {
            Entry entry = new Entry();
            entry.zone = zone;
            for (BxLine line : zone.getLines()) {
                for (BxWord word : line.getWords()) {
                    for (BxChunk chunk : word.getChunks()) {
                        Map<BxChunk, Entry> map2 = map.get(chunk.toText());
                        if (map2 == null) {
                            map2 = new HashMap<BxChunk, Entry>();
                            map.put(chunk.toText(), map2);
                        }
                        map2.put(chunk, entry);
                    }
                }
            }
            entries.add(entry);
        }

        for (BxZone zone : source.getZones()) {
            if (zone.getLabel() == null || zone.getLabel() == BxZoneLabel.OTH_UNKNOWN) {
                continue;
            }
            for (BxLine line : zone.getLines()) {
                for (BxWord word : line.getWords()) {
                    chunkLoop: for (BxChunk chunk : word.getChunks()) {
                        Map<BxChunk, Entry> map2 = map.get(chunk.toText());
                        if (map2 != null) {
                            for(Map.Entry<BxChunk, Entry> mapEntry : map2.entrySet()) {
                                if (mapEntry.getKey().getBounds().isSimilarTo(chunk.getBounds(), TOLERANCE)) {
                                    mapEntry.getValue().hit(zone.getLabel());
                                    continue chunkLoop;
                                }
                            }
                        }
                    }
                }
            }
        }

        for(Entry entry : entries) {
            entry.zone.setLabel(entry.findBest());
        }
    }

    private static class Entry {
        BxZone zone;
        Map<BxZoneLabel, Integer> hits = new EnumMap<BxZoneLabel, Integer>(BxZoneLabel.class);

        Entry() {
            for(BxZoneLabel label : BxZoneLabel.values()) {
                hits.put(label, 0);
            }
        }

        void hit(BxZoneLabel label) {
            hits.put(label, hits.get(label) + 1);
        }

        BxZoneLabel findBest() {
            BxZoneLabel best = BxZoneLabel.OTH_UNKNOWN;
            int bestHits = 0;
            for (Map.Entry<BxZoneLabel, Integer> entry : hits.entrySet()) {
                if (entry.getValue() > bestHits) {
                    bestHits = entry.getValue();
                    best = entry.getKey();
                }
            }
            return best;
        }
    }
}