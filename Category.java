import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

public class Category {
    int length_from_file = -1;//negative one default value
    HashSet<Integer> set = new HashSet<>();
    boolean set_filled = false;
    int length = 0;
    String category;
    static RandomAccessFile indexes;
    int category_pointer;
    int data_pointer = 0;//zero because it is added to the offset which is already one
    HashSet<Integer> found_indexes = new HashSet<>();
    Category(String category) {
        this.category = category;
           //this.length_from_file = get_file_length();   CANT DO THIS, INITIALIZED BEFORE LENGTH SETUP
        try {
            this.category_pointer = this.find_pointer_math();
        } catch (IOException e) {
            System.out.println("COULD NOT CREATE CATEGORY POINTER");
        }
    }
    public int get_length_from_file(){
        try{
            try(RandomAccessFile lengths = new RandomAccessFile(Paths.get("lengths.txt").toString(), "rw")){
                int relative_position = (this.category.charAt(0) - 'A')*5 + (this.category.charAt(1)-49);//I truly forget what this does but it is necessary
                int real_position = relative_position*(Indexes.lengths_increment_size-1);
                               real_position+=2;
                lengths.seek(real_position);
                return lengths.readShort();
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            throw new Error();        }
    }

    public void insert_index_math(int to_insert) throws IOException {

        try (RandomAccessFile indexes = new RandomAccessFile(Paths.get("indexes.txt").toString(), "rw")) {
            int pointer = this.category_pointer + (1+length)*Indexes.entry_length;
            indexes.seek(pointer);
            indexes.writeInt(to_insert);
            length++;
        }
    }


    public int find_pointer_math() throws IOException {
        try (RandomAccessFile indexes = new RandomAccessFile(Paths.get("indexes.txt").toString(), "r")) {
            int position;
            position = (this.category.charAt(0) - 'A')*5 + (this.category.charAt(1)-49);
            position*=((Indexes.placeholder_per_category*10)+4);
            return position;
        }
    }

    public int read_on_index(int relative_index) throws IOException {//returns number(what is stored)
        try (RandomAccessFile file = new RandomAccessFile("C:\\Users\\alex\\IdeaProjects\\test\\indexes.txt", "rw")) {
            int offset = 1+relative_index;
            int pointer = this.category_pointer+(offset*Indexes.entry_length);
            file.seek(pointer);
            return file.readInt();
        }
    }
}
