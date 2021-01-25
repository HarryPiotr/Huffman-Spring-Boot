package springboot;


import springboot.formobjects.HuffmanFileDecodingForm;
import springboot.formobjects.HuffmanFileEncodingForm;
import springboot.formobjects.HuffmanTextDecodingForm;
import springboot.formobjects.HuffmanTextEncodingForm;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;


public class Main {

    private static void extractSampleDataLoremIpsum(int sampleSize) throws IOException {
        for (int i = 0; i < 1000; i++) {
            System.out.println(i + "/1000");
            URL lorem = new URL("https://pl.lipsum.com/feed/html?what=bytes&amount=" + sampleSize + "&start=no");
            URLConnection lc = lorem.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()));
            while (true) {
                String line = in.readLine();
                if (line.trim().contains("<div id=\"lipsum\">")) {
                    in.readLine();
                    File of = new File("src/main/resources/testing-data/loremipsum/size" + sampleSize + "/sample" + i + ".txt");
                    OutputStream os = new FileOutputStream(of);
                    while (true) {
                        String loremString = in.readLine();
                        if (loremString.contains("</p>")) {
                            if (in.readLine().equals("<p>")) continue;
                            else break;
                        } else os.write(loremString.getBytes());
                    }
                    os.close();
                    in.close();
                    break;
                }
            }
        }
    }

    private static void extractSampleDataPanTadeusz(int sampleSize) throws IOException {
        for (int i = 713; i < 1000; i++) {
            System.out.println(i + "/1000");
            StringBuilder sb = new StringBuilder();
            while (sb.length() < sampleSize) {
                URL lorem = new URL("http://lipsum.pl/index.php?tekst=2&ile=1&typ=1&dlugosc=1&slow=" + sampleSize / 2);
                URLConnection lc = lorem.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(lc.getInputStream()));
                while (true) {
                    String line = in.readLine();
                    if (line.trim().contains("<textarea class=\"codebox\" id=\"codebox\" rows=\"10\" cols=\"50\" lang=\"latin\">")) {
                        String sample = line.substring(75, line.indexOf("</textarea"));
                        sb.append(sample);
                        in.close();
                        break;
                    }
                }
            }
            sb.setLength(sampleSize);
            File of = new File("src/main/resources/testing-data/pantadeusz/size" + sampleSize + "/sample" + i + ".txt");
            OutputStream os = new FileOutputStream(of);
            os.write(sb.toString().getBytes());
            os.close();
        }
    }

    private static double[] testTextData(int l, int dataSize, String dirName) throws IOException {

        double avgEntropy = 0.0;
        double avgCodewordLength = 0.0;
        double avgCompressionRatio = 0.0;
        double successfullyDecoded = 1000.0;

        for (int i = 0; i < 1000; i++) {

            File sampleDataFile = new File("src/main/resources/testing-data/" + dirName + "/sample" + i + ".txt");
            InputStream is = new FileInputStream(sampleDataFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            HuffmanTextEncodingForm hf = new HuffmanTextEncodingForm();
            hf.setWordLength(l);
            hf.setInputText(sb.toString());
            hf.calculateAverageCodewordLenght();

            avgCompressionRatio += hf.getCompressionRatioDouble();
            avgEntropy += hf.entropy;
            avgCodewordLength += hf.averageCWLngth;

            HuffmanTextDecodingForm hdf = new HuffmanTextDecodingForm();
            hdf.setInputText(hf.getCompressedText());
            hdf.splitInput();
            hdf.decodeText();

            if (!sb.toString().equals(hdf.getOutputText())) successfullyDecoded -= 1.0;
        }
        avgEntropy /= 1000;
        avgCodewordLength /= 1000;
        successfullyDecoded /= 10;
        avgCompressionRatio /= 1000;

        return new double[]{avgEntropy, avgCodewordLength, successfullyDecoded, avgCompressionRatio};
    }

    private static double[] testFileData(int dataSize, int sampleCount, String dirName) throws IOException {
        double avgEntropy = 0.0;
        double avgCodewordLength = 0.0;
        double avgCompressionRatio = 0.0;
        double successfullyDecoded = (double) sampleCount;

        final File folder = new File("src/main/resources/testing-data/" + dirName + "/size" + dataSize);

        int i = 0;

        for (final File fileEntry : folder.listFiles()) {
            System.out.println(i++ + "/" + sampleCount);
            if (fileEntry.isDirectory()) continue;
            else {
                InputStream is = new FileInputStream(fileEntry);
                ByteArrayOutputStream inter = new ByteArrayOutputStream();
                File outputFile = new File("src/main/resources/testing-data/" + dirName + "/size" + dataSize + "/output/" + fileEntry.getName());
                OutputStream os = new FileOutputStream(outputFile);
                HuffmanFileEncodingForm hencf = new HuffmanFileEncodingForm();
                hencf.setInput(is);
                hencf.setOutput(inter);
                hencf.fileSize = fileEntry.length();
                hencf.calculateExpectedLength();
                hencf.compressFile();
                hencf.calculateTestedData();
                if (hencf.getOutputFileSize() != inter.size())
                    System.out.println("NO I CHUJ!: " + hencf.getOutputFileSize() + "~" + inter.size());

                avgEntropy += hencf.entropy;
                avgCodewordLength += hencf.avgCodeWordLength;
                avgCompressionRatio += hencf.compressionRatio;

                HuffmanFileDecodingForm hdecf = new HuffmanFileDecodingForm();
                ByteArrayInputStream inter2 = new ByteArrayInputStream(inter.toByteArray());
                hdecf.setInput(inter2);
                hdecf.setOutput(os);
                hdecf.rebuildTree();
                hdecf.decompressFile();

                if (!FileUtils.contentEquals(fileEntry, outputFile)) {
                    System.out.println("Pliki się nie zgadzają - " + fileEntry.getName());
                    successfullyDecoded -= 1.0;
                }
            }
        }
        avgEntropy /= sampleCount;
        avgCodewordLength /= sampleCount;
        avgCompressionRatio /= sampleCount;
        successfullyDecoded /= sampleCount;
        successfullyDecoded *= 100.0;

        return new double[]{avgEntropy, avgCodewordLength, successfullyDecoded, avgCompressionRatio};
    }

    private static void testXtraLargeTextData() throws IOException {

        double avgEntropy = 0.0;
        double avgCodewordLength = 0.0;
        double avgCompressionRatio = 0.0;
        double successfullyDecoded = 1000.0;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 1000; i++) {
            File sampleDataFile = new File("src/main/resources/testing-data/loremipsum/size10000/sample" + i + ".txt");
            InputStream is = new FileInputStream(sampleDataFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            is.close();
        }
        HuffmanTextEncodingForm hf = new HuffmanTextEncodingForm();
        for(int i = 1; i < 5; i++) {
            hf.setWordLength(i);
            hf.setInputText(sb.toString());
            hf.calculateAverageCodewordLenght();
            System.out.println("--------------------");
            System.out.println("L = " + i);
            System.out.println("Wsp. Kompresji = " + hf.getCompressionRatioDouble());
            System.out.println("Entropia = " + hf.entropy);
            System.out.println("Śr. dł. słowa kodowego = " + hf.averageCWLngth);
            System.out.println("--------------------");
        }
    }

    public static void main(String[] args) throws IOException {

//        testXtraLargeTextData();

        StringBuilder sb = new StringBuilder();

        int[] arr1 = new int[]{1000, 10000, 1000000};
        for (int i : arr1) {
            double[] res = testFileData(i, 100, "files");
            sb.append("---------------------------------" + "\r\n");
            sb.append("PLIKI: Fastest Fish" + "\r\n");
            sb.append("WIELKOŚĆ PRÓBKI: " + i + "\r\n");
            sb.append("Średnia Entropia: " + res[0] + "\r\n");
            sb.append("Średnia długość słowa kodowego: " + res[1] + "\r\n");
            sb.append("Średnia wartość współczynnika kompresji: " + res[3] + "\r\n");
            sb.append("Poprawne wyniki kompresji i ponownej dekompresji: " + res[2] + "%" + "\r\n");
            sb.append("---------------------------------" + "\r\n");
        }

        int[] arr2 = new int[]{10, 100, 1000, 10000};
        for (int i : arr2) {
            double[] res = testFileData(i, 1000, "loremipsum");
            sb.append("---------------------------------" + "\r\n");
            sb.append("PLIKI: Lorem Ipsum" + "\r\n");
            sb.append("WIELKOŚĆ PRÓBKI: " + i + "\r\n");
            sb.append("Średnia Entropia: " + res[0] + "\r\n");
            sb.append("Średnia długość słowa kodowego: " + res[1] + "\r\n");
            sb.append("Średnia wartość współczynnika kompresji: " + res[3] + "\r\n");
            sb.append("Poprawne wyniki kompresji i ponownej dekompresji: " + res[2] + "%" + "\r\n");
            sb.append("---------------------------------" + "\r\n");
        }

        for (int i = 10; i < 10001; i *= 10) {
            for (int j = 1; j < 5; j++) {
                double[] res = testTextData(j, i, "pantadeusz/size" + i);
                sb.append("---------------------------------" + "\r\n");
                sb.append("TEKST: Pan Tadeusz" + "\r\n");
                sb.append("DŁUGOŚĆ SYMBOLU: " + j + "\r\n");
                sb.append("WIELKOŚĆ PRÓBKI: " + i + "\r\n");
                sb.append("Średnia Entropia: " + res[0] + "\r\n");
                sb.append("Średnia długość słowa kodowego: " + res[1] + "\r\n");
                sb.append("Średnia wartość współczynnika kompresji: " + res[3] + "\r\n");
                sb.append("Poprawne wyniki kompresji i ponownej dekompresji: " + res[2] + "%" + "\r\n");
                sb.append("---------------------------------" + "\r\n");
            }
        }

        for (int i = 10; i < 10001; i *= 10) {
            for (int j = 1; j < 5; j++) {
                double[] res = testTextData(j, i, "loremipsum/size" + i);
                sb.append("---------------------------------" + "\r\n");
                sb.append("TEKST: Lorem Ipsum" + "\r\n");
                sb.append("DŁUGOŚĆ SYMBOLU: " + j + "\r\n");
                sb.append("WIELKOŚĆ PRÓBKI: " + i + "\r\n");
                sb.append("Średnia Entropia: " + res[0] + "\r\n");
                sb.append("Średnia długość słowa kodowego: " + res[1] + "\r\n");
                sb.append("Średnia wartość współczynnika kompresji: " + res[3] + "\r\n");
                sb.append("Poprawne wyniki kompresji i ponownej dekompresji: " + res[2] + "%" + "\r\n");
                sb.append("---------------------------------" + "\r\n");
            }
        }

        File reportFile = new File("src/main/resources/testing-data/test-results.txt");
        OutputStream os = new FileOutputStream(reportFile);
        os.write(sb.toString().getBytes());
    }
}
