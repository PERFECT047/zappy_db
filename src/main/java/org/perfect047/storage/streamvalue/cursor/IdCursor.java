package org.perfect047.storage.streamvalue.cursor;

public final class IdCursor {

    public final long ms;
    public final long seq;

    private IdCursor(long ms, long seq) {
        this.ms = ms;
        this.seq = seq;
    }

    public static IdCursor of(long ms, long seq) {
        return new IdCursor(ms, seq);
    }

    public static IdCursor parse(String id) {
        int dash = id.indexOf('-');
        if (dash == -1) {
            return new IdCursor(Long.parseLong(id), 0);
        }
        return new IdCursor(
                Long.parseLong(id.substring(0, dash)),
                Long.parseLong(id.substring(dash + 1))
        );
    }

    public String toId() {
        return ms + "-" + seq;
    }

    public static IdCursor parseStart(String start) {
        if ("-".equals(start)) {
            return new IdCursor(Long.MIN_VALUE, 0);
        }
        return parse(start);
    }

    public static IdCursor parseEnd(String end) {
        if ("+".equals(end)) {
            return new IdCursor(Long.MAX_VALUE, Long.MAX_VALUE);
        }

        if (!end.contains("-")) {
            return new IdCursor(Long.parseLong(end), Long.MAX_VALUE);
        }

        return parse(end);
    }
}
