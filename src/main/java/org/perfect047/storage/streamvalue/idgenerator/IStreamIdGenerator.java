package org.perfect047.storage.streamvalue.idgenerator;

import org.perfect047.storage.streamvalue.cursor.IdCursor;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public interface IStreamIdGenerator {
    IdCursor generate(
            String streamName,
            List<String> args,
            ConcurrentMap<String, Long> lastMsMap,
            ConcurrentMap<String, ConcurrentSkipListMap<Long, Deque<Map<String, String>>>> store
    );
}
