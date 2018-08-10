package com.example.marik.maporganizer.db;

import android.arch.persistence.room.TypeConverter;
import android.location.Address;

import com.google.android.gms.location.places.AddPlaceRequest;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Converters {

    @TypeConverter
    public static UUID toUUID(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    @TypeConverter
    public static String toLong(UUID value) {
        return value == null ? null : value.toString();
    }


    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long toLong(Date value) {
        return value == null ? null : value.getTime();

    }
    @TypeConverter
    public static Address toString(String address){
        return address==null? null: (new Address(Locale.getDefault()));
    }

    @TypeConverter
    public static String toAddress(Address address){
        return  address==null ? null:(new Address(Locale.getDefault())).toString();
    }
}
