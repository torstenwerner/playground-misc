package com.westernacher.csvconvert;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
public class CsvConverter {
    private static String[] resultColumns = new String[] { "SchuldnerID", "SchuldnerTyp", "GläubigerID",
            "Buchungskreis", "KunKtoInhaber", "KunKtoNummer", "KunBLZ", "KunIBAN", "KunBIC", "Bankname",
            "Mandatsreferenz", "KunKONTO_VON", "KunKONTO_BIS", "Zahlart", "Mandat_aktiv", "KUNDE_PLZ",
            "KUNDE_ORT", "KUNDE_ADRESSE", "KUNDE_LAND"
    };

    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            throw new RuntimeException("need 2 filenames as argument");
        final String inputname = args[0];
        final String outputname = args[1];
        final CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(inputname)), ',', '\'');
        final CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outputname)), '\t');
        String[] items = reader.readNext();
        final Map<String, Integer> column = new HashMap<>();
        for (int i = 0; i < items.length; i ++)
            column.put(items[i], i);
        writer.writeNext(resultColumns);
        while ((items = reader.readNext()) != null) {
            final String[] result = new String[resultColumns.length];
            for (int i = 0; i < resultColumns.length; i ++)
                result[i] = items[column.get(resultColumns[i])];
            writer.writeNext(result);
        }
        reader.close();
        writer.close();
    }
}
