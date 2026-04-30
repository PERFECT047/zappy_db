package org.perfect047.storage.streamvalue.idgenerator;

public class StreamIdGeneratorFactory {
    private static final IStreamIdGenerator streamIdGenerator;

    static{
        streamIdGenerator = new DefaultStreamIdGenerator();
    }

    public static IStreamIdGenerator getStreamIdGenerator(){
        return streamIdGenerator;
    }
}
