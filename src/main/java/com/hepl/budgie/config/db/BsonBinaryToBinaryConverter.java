package com.hepl.budgie.config.db;

import org.bson.BsonBinary;
import org.springframework.core.convert.converter.Converter;

public class BsonBinaryToBinaryConverter implements Converter<BsonBinary, BsonBinary> {

    @Override
    public BsonBinary convert(BsonBinary source) {
        return source.asBinary();
    }

}
