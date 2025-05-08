package com.hepl.budgie.config.db;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class ZonedDateTimeCodec implements Codec<ZonedDateTime> {

    @Override
    public void encode(
            final BsonWriter writer, final ZonedDateTime value, final EncoderContext encoderContext) {
        if (value != null) {
            writer.writeDateTime(Date.from(value.toInstant()).getTime());
        } else {
            writer.writeNull();
        }
    }

    @Override
    public ZonedDateTime decode(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType type = reader.getCurrentBsonType();

        if (type == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (type == BsonType.DATE_TIME) {
            long milliseconds = reader.readDateTime();
            Instant instant = Instant.ofEpochMilli(milliseconds);
            return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        } else {
            throw new UnsupportedOperationException("Unsupported BSON type for ZonedDateTime: " + type);
        }
    }

    @Override
    public Class<ZonedDateTime> getEncoderClass() {
        return ZonedDateTime.class;
    }

}
