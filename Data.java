import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.HashSet;

public class Data {


static final String file_path = "database.txt";
static byte[] holder = new byte[5];

public static String read_relative_index(int relative_index) throws IOException {
    try (RandomAccessFile data = new RandomAccessFile(Paths.get(file_path).toString(), "r")){
        //System.out.println("relative address in data "+relative_index);
        data.seek(relative_index*Indexes.line_length);
        data.read(holder);
        return new String(holder).toUpperCase();
    }
}
}
