import java.io.*;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Indexes {
    static int lengths_increment_size = 5;
    static String free_target;
    final static int entry_length = 4;
    final static int placeholder_per_category = 3000;
    final static int line_length = 7;
    HashMap<String, Category> categories = new HashMap<>();
    Indexes(){
        //must assume that lengths and indexes are set up
        try{
            initialize_categories();
        }
        catch (IOException e){
        }
        //categories are initializes, so i can set file value for all
        categories.forEach((name, category)->{
            category.length_from_file = category.get_length_from_file();
        });

    }
    Indexes(String code){
        //set up categories
        System.out.println("index constructor");
        StringBuilder temp = new StringBuilder();
        for (int i=0; i<entry_length; i++){
            temp.append("#");
        }
        free_target = temp.toString();//automates target for searching for free memory
        try{
            if (code.equalsIgnoreCase("CLEAR")){
                System.out.println("initializing categories");
                initialize_categories();
                System.out.println("deleting indexes");
                this.delete_file();
                System.out.println("setting up indexes");
                this.set_up_file();
                categories.forEach((name, category)->{
                    category.length_from_file = category.get_length_from_file();//use this if not setting up lengths
                    System.out.println("length: "+category.length_from_file);//RETURNING 0 WHEN IT SHOULDNT
                });
                System.out.println("importing database");
                this.import_database_relative_ordered();
                //System.out.println("deleting lengths");
                //this.delete_lengths();                         NEVER UNCOMMENT, lengths needs to be the same for it to work
                //System.out.println("setting up lengths");
                //this.set_up_lengths();//needs categories to be initialized
            }
        }
        catch (Exception e){
            System.out.println("ERROR IN CONSTRUCTOR");
            System.out.println(e.getMessage());
        }
    }

    public  void set_up_lengths()throws IOException{

            try (RandomAccessFile lengths = new RandomAccessFile(Paths.get("lengths.txt").toString(), "rw")) {
                int pointer = 0;
                for (int letter = 0; letter < 26; letter++) {
                    for (int number = 1; number <= 5; number++) {
                        try {
                            char real_letter = (char) (letter + 'A');
                            String category = String.valueOf(real_letter).concat(String.valueOf((char) (number + 48)));// should work, -48 because we want it to be A1, not A0r
                            lengths.seek(pointer);
                            lengths.write(category.getBytes());
                            pointer += 2;
                            lengths.seek(pointer);
                            lengths.writeShort(categories.get(category).length);//throws the error
                            categories.get(category).length_from_file = categories.get(category).get_length_from_file();//gives the category its file value THIS IS FAILING
                            pointer += 2;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(e.getMessage());
                        }

                    }
                }
            }

    }

    private void delete_lengths() throws IOException{
        try(RandomAccessFile lengths = new RandomAccessFile(Paths.get("lengths.txt").toString(), "rw")){
            lengths.setLength(0);
        }
    }


    private void initialize_categories() throws IOException {
        StringBuilder insertable = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            insertable.append((char) (i + 'A'));
            for (int j = 1; j < 6; j++) {
                insertable.append(j);
                this.categories.put(insertable.toString(), new Category(insertable.toString()));
                insertable.setLength(insertable.length() - 1);
            }
            insertable.setLength(0);
        }
    }


    public void make_indexes_math(String input, int word_index) throws IOException{//uses short,should use int
        StringBuilder char1 = new StringBuilder(String.valueOf(input.charAt(0)).toUpperCase());
        StringBuilder char2 = new StringBuilder(String.valueOf(input.charAt(1)).toUpperCase());
        StringBuilder char3 = new StringBuilder(String.valueOf(input.charAt(2)).toUpperCase());
        StringBuilder char4 = new StringBuilder(String.valueOf(input.charAt(3)).toUpperCase());
        StringBuilder char5 = new StringBuilder(String.valueOf(input.charAt(4)).toUpperCase());
        char1.append("1");
        char2.append("2");
        char3.append("3");
        char4.append("4");
        char5.append("5");
        this.categories.get(char1.toString()).insert_index_math(word_index);
        this.categories.get(char2.toString()).insert_index_math(word_index);
        this.categories.get(char3.toString()).insert_index_math(word_index);
        this.categories.get(char4.toString()).insert_index_math(word_index);
        this.categories.get(char5.toString()).insert_index_math(word_index);
    }

    // THIS IS ONLY TO BE USED TO HELP
    public void import_database_relative_ordered() throws IOException{
        // GIVES ITS EXACT INDEX to the file
        try(RandomAccessFile data  = new RandomAccessFile("C:\\Users\\alex\\IdeaProjects\\test\\database.txt","r")){
            final int line_length = 7;
            final int file_length = (int) data.length();
            byte[] placeholder = new byte[5];//word length
            PriorityQueue<ToAdd> to_make_indexes = new PriorityQueue<>(new Comparator<ToAdd>() {
                @Override
                public int compare(ToAdd o1, ToAdd o2) {
                    return Double.compare(o2.probability, o1.probability);//need to
                }
            });
            for (long i = 0; i < file_length; i += line_length) {
                data.seek(i);
                data.readFully(placeholder);
                String output = new String(placeholder).toUpperCase();
                to_make_indexes.offer(new ToAdd(output, (int)(i/line_length), this.get_probability(output)));
            }
            while (!to_make_indexes.isEmpty()){
                ToAdd to_add = to_make_indexes.poll();
                System.out.println(to_add.word+", index: "+ to_add.relative_index + ", probability: "+to_add.probability);
                try{
                    make_indexes_math(to_add.word, to_add.relative_index);
                }
                catch (IOException e){
                    System.out.println(e.getMessage());
                }

            }

        }
    }

    public int get_probability(String input){
        String char1 = String.valueOf(input.charAt(0)).concat("1");
        String char2 = String.valueOf(input.charAt(1)).concat("2");
        String char3 = String.valueOf(input.charAt(2)).concat("3");
        String char4 = String.valueOf(input.charAt(3)).concat("4");
        String char5 = String.valueOf(input.charAt(4)).concat("5");
        int sum = 0;
        sum+=categories.get(char1).length_from_file;
        sum+=categories.get(char2).length_from_file;
        sum+=categories.get(char3).length_from_file;
        sum+=categories.get(char4).length_from_file;
        sum+=categories.get(char5).length_from_file;
        return sum;
    }



    public void delete_file() throws IOException{
        try (RandomAccessFile file = new RandomAccessFile("C:\\Users\\alex\\IdeaProjects\\test\\indexes.txt", "rw")){
            file.setLength(0);
        }
    }
    public void set_up_file() throws IOException{
        try(RandomAccessFile file = new RandomAccessFile("C:\\Users\\alex\\IdeaProjects\\test\\indexes.txt", "rw")){
            int cur_index = 0;
            StringBuilder insertable = new StringBuilder();
            for (int i=0; i < 26; i++){
                insertable.delete(0,insertable.length());
                insertable.append((char)(i+(int)'A'));
                for (int j=1; j < 6; j++){
                    insertable.append(j);
                    try{
                        file.seek(file.length());
                        file.write(insertable.toString().getBytes());
                            for (int y = 0; y < placeholder_per_category; y++){
                                file.seek(file.length());
                                file.write("##########".getBytes());
                            }


                        file.seek(file.length());
                        file.write("ZZ".getBytes());//changed delimiter to ZZ
                    }
                    catch (IOException e){
                        System.out.println(e.getMessage());
                    }
                    insertable.deleteCharAt(insertable.length()-1);
                }
            }
        }
        }
}
