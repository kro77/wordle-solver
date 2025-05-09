import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    //use linkedlist for O(1) addition
    static HashSet<Character> locked_letters = new HashSet<>();
    static String regex = " *([G|Y|*]\\w) *([G|Y|*]\\w) *([G|Y|*]\\w) *([G|Y|*]\\w) *([G|Y|*]\\w)";
    static HashMap<Character, Integer> letter_counts = new HashMap<>();
   static ArrayList<String> must_include = new ArrayList<>();
    static HashSet<String> must_exclude = new HashSet<>();//only this one is the hashset
  static ArrayList<String> yellow_list = new ArrayList<>();//make yellow_check method for when iterating through file, should use hashet of 1,2,3,4,5 and then remove the given one
    static Indexes indexes = new Indexes();
    static HashSet<Integer> working = new HashSet<>();
    static Scanner scanner = new Scanner(System.in);
    //I ALREADY HAVE THE LENGTHS FOR THE CATEGORIES< CAN USE THOSE AS PROABABILITIES
    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("(ENTER \"HELP\" FOR GUIDE)");
        get_input();
/*
TO DO:
MAKE SURE SKIPPING WORDS ONLY GIVES CORRECT ANSWERS
locking mechanism might not work, might have to do with s5?
green detection isnt working i fear
            * */
        }

    public static void get_input() {
        Scanner scanner = new Scanner(System.in);
        String input =  scanner.nextLine().trim().toUpperCase();
        if (input.equals("HELP")){
            System.out.println("AN INPUT WILL RESULT IN A GUESSED WORD\nYOU CAN SKIP A WORD YOU DO NOT LIKE\nYOU CAN RESET THE GAME BY TYPING IN \"RESET\"\nG = GREEN  Y = YELLOW  * = GREY\nENTER IN ORDER: COLOR+LETTER\nEXAMPLE: GH YE *L *L *O");
            get_input();//recursion is just easy here, not the most efficient
        }
        else if (input.equals("RESET")){
            must_include.clear();
            locked_letters.clear();
            yellow_list.clear();
            must_exclude.clear();
            letter_counts.clear();
            System.out.println("CONSTAINTS CLEARED");
            get_input();
        }
        else{
            if (validate_input(input)){
                process_input(input);
            }
            else {
                System.out.println("INVALID FORMAT");
                get_input();
            }
        }
    }


    public static String guess_list() throws IOException {
        if (!must_include.isEmpty()){//use smallest green category to iterate
            //should fetch highest probability word in the smallest green category(smallest = fastest searching)
            try {
                must_include.sort(Comparator.comparingInt(category -> indexes.categories.get(category).length_from_file));//works correctly
                Category category = indexes.categories.get(must_include.getFirst());//should be list with the lowest probability
                int length = category.length_from_file;
                for (int category_index=0; category_index<length; category_index++){
                    if (check_all(category.read_on_index(category_index))){//check all receives the data index, converts
                        System.out.println(Data.read_relative_index(category.read_on_index(category_index)));//we return the actual data by using the data index
                        System.out.println("KEEP WORD? (Y/N)");//works?
                        if (scanner.nextLine().trim().substring(0,1).equalsIgnoreCase("Y")){//pythonic while loop?
                            return Data.read_relative_index(category.read_on_index(category_index));
                        }
                    }
                }
                System.out.println("WORD NOT FOUND");
                return "WORD NOT FOUND";
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                return "ERROR";
            }
        }
        else if (!yellow_list.isEmpty()){//simultaneously go through all the yellows and return the first that is contained by all of them(arraylist/linkedlist of Hashsets?  iteratively retainall on them and see if it is empty)
            ArrayList<String> searching_categories = new ArrayList<String>();

            HashSet<Character> seen_letters = new HashSet<>();
            for (String category : yellow_list){//redo this, make sure to search only those not contained in yellow list
                char letter_name = category.charAt(0);
                int excluded_number = category.charAt(1) - 48;//-48 to convert it to its true number
                if (!seen_letters.contains(letter_name)){
                    seen_letters.add(letter_name);
                    for (int i = 1; i <= 5; i++){
                        if (!yellow_list.contains(String.valueOf(letter_name)+i)){//NOW ADD ADDITIONAL CHECKS TO EXCLUDE OTHER NUMBERS IN YELLOW LIST
                            searching_categories.add(String.valueOf(letter_name)+i);//should concatenate
                            System.out.println("added: "+String.valueOf(letter_name)+i+" to searching_categories");
                        }
                    }
                }

            }
            //find max file_length between all
            String max_length_category = searching_categories.stream().max(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {//should return greatest file length?
                    return Integer.compare(indexes.categories.get(o1).length_from_file, indexes.categories.get(o2).length_from_file);
                }
            }).get();
            int max_length = indexes.categories.get(max_length_category).length_from_file;

            for (int i =0; i< max_length; i++){
                //go through searching array, if i is in their scope (not out of bounds, check their ith element, if valid, return guess)
                for (String category : searching_categories){
                    if (i<indexes.categories.get(category).length_from_file){
                        int relative_index_in_data = indexes.categories.get(category).read_on_index(i);
                        if (check_all(relative_index_in_data)){
                            System.out.println(Data.read_relative_index(relative_index_in_data));
                            return Data.read_relative_index(relative_index_in_data);
                        }
                    }
                    else{
                        System.out.println("file length of "+indexes.categories.get(category).length_from_file+" to small for index "+i+" in category "+category);
                    }
                }
            }
            System.out.println("WORD NOT FOUND");
            return "WORD NOT FOUND";

        }
        else{//go from highest probability (valid) category to the next highest (valid) probability category, searching for a valid word
            System.out.println("ALL GREY GUESSING");
            String[] searching_categories_unsorted =  indexes.categories.keySet().toArray(new String[0]);//hopefully doesn't error
            String[] searching_categories_sorted = (String[]) Arrays.stream(searching_categories_unsorted)
                    .sorted(((o1, o2) -> {return Integer.compare(indexes.categories.get(o2).length_from_file,indexes.categories.get(o1).length_from_file);}))
                    .toArray(String[]::new);
            for (String category : searching_categories_sorted){
                boolean category_valid = true;
                for (String excluded : must_exclude){
                    if (category.charAt(0) == excluded.charAt(0)){
                        category_valid = false;
                    }
                }
                if (category_valid){
                    int category_length = indexes.categories.get(category).length_from_file;
                    for (int i = 0; i < category_length; i++){
                        int relative_index_in_data = indexes.categories.get(category).read_on_index(i);
                        if (check_all(relative_index_in_data)){
                            System.out.println(Data.read_relative_index(relative_index_in_data));
                            return Data.read_relative_index(relative_index_in_data);
                        }
                    }
                }
            }
            System.out.println("WORD NOT FOUND");
            return "WORD NOT FOUND";
        }
    }
    public static boolean check_counts(String word){
        System.out.println("CHECKING COUNTS: LOCKED ARE "+locked_letters+"\nLETTERS COUNTS ARE "+letter_counts);
        for (Map.Entry<Character, Integer> entry : letter_counts.entrySet()) {
            int observed_count = 0;

                for (int j = 0; j < word.length(); j++){
                    if (word.charAt(j)==entry.getKey()){
                            observed_count++;
                    }

            }//sets observed count
            if (locked_letters.contains(entry.getKey()) && observed_count!=entry.getValue()){
                return false;
            }
            else if (observed_count < entry.getValue()){
                return false;
            }
        }
        return true;
    }
    private static boolean validate_input(String input){
        return Pattern.matches(regex, input);
    }
    public static void process_input(String input){
        //guaranteed to receive valid input
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        String[] groups = new String[5];
        matcher.find();
        for (int i = 1; i < 6; i++){
            String group = matcher.group(i);
            group = group + i;//plus i because it is already 1 based indexing
            groups[i-1] = group;  //form of COLOR + LETTER + NUMBER
        }
        HashMap<Character, Integer> unique_letters  = new HashMap<>();
        for (String data : groups){
            if (unique_letters.containsKey(data.charAt(1))){
                unique_letters.put(data.charAt(1),unique_letters.get(data.charAt(1))+1);
            }
            else{
                unique_letters.put(data.charAt(1),1);
            }
        }//now we know the count of all letters in the input
        //first take care of those without duplicates
        for (Map.Entry<Character,Integer> entry : unique_letters.entrySet()){
            if (entry.getValue()==1){
                //search for the value in input array, there must be only one!
                for (String data : groups){
                    if (data.charAt(1) == entry.getKey()){//this and the loop above find the actual group and assign it to "data"
                        switch (data.charAt(0)){
                            case 'Y':
                                register_yellow_list(data.substring(1));
                                letter_counts.put(data.charAt(1), 1);
                                break;
                            case 'G':
                                register_green_list(data.substring(1));
                                letter_counts.put(data.charAt(1), 1);
                                break;
                            case '*':
                                register_grey_list(data.substring(1));
                                break;
                        }
                    }
                }

            }
            else{
                //consolidate all the letters into an array of size key's length
                String[] letter_instances = new String[entry.getValue()];
                int instances_pointer = 0;
                for (String data : groups){
                    if (data.charAt(1)==entry.getKey()){
                        letter_instances[instances_pointer] = data;
                        instances_pointer++;
                    }
                }//we should be left with the individual instances now
                int grey_counter = 0;
                letter_counts.put(letter_instances[0].charAt(1), 0);//reset to zero so that we can redo the count
                for (String instance : letter_instances){
                    System.out.print(instance);
                    if (instance.charAt(0)=='*'){
                        System.out.println(" WAS GREY");
                        grey_counter++;
                    }
                    else if (instance.charAt(0)=='G'){
                        System.out.println(" WAS GREEN");
                       letter_counts.put(letter_instances[0].charAt(1), letter_counts.get(letter_instances[0].charAt(1))+1);
                        register_green_list(instance.substring(1));
                    }
                    else if (instance.charAt(0)=='Y'){
                        System.out.println(" WAS YELLOW");
                        letter_counts.put(letter_instances[0].charAt(1), letter_counts.get(letter_instances[0].charAt(1))+1);
                        register_yellow_list(instance.substring(1));
                    }
                }
                if (grey_counter!=0&&grey_counter!=letter_instances.length){//means there must be greys and thus we must lock letter
                    System.out.println("LOCKING "+letter_instances[0].charAt(1));
                    locked_letters.add(letter_instances[0].charAt(1));
                }
                else if (grey_counter==letter_instances.length){
                    System.out.println("ALL DUPLICATES GREY, SO MUST EXCLUDE "+letter_instances[0].substring(1));
                    register_grey_list(letter_instances[0].substring(1));
                }
            }
        }
        try{
            guess_list();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            System.out.println("ERROR");
        }
        get_input();

    }
    public static void print_valid(String category){//prints all in a given category, unlike guess which should parse the entire file(maybe?)
       Category real_category = indexes.categories.get(category);
        int length = real_category.length_from_file;
        for (int i=0; i<length; i++){
            try{
                int relative_data_index = real_category.read_on_index(i);
                System.out.println(Data.read_relative_index(relative_data_index));
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                return;
            }


        }
    }
    public static void register_grey_list(String category){
        must_exclude.add(category);//adds to exclusion HASHSET
    }
    public static void register_green_list(String category){
        if (!must_include.contains(category)){
            must_include.add(category);//appends to end of list
        }
    }
    public static void register_yellow_list(String category){
        if (!yellow_list.contains(category)) {
            yellow_list.add(category);
        }
    }
    public static boolean check_all(int data_relative_index){//relative index is read from the indexes
        try{
            String word = Data.read_relative_index(data_relative_index);
           System.out.println(word);
            if (!check_green(word)||!check_grey(word)||!check_yellow(word)||!check_counts(word)){
                return false;
            }
            return true;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            throw new Error();
        }



    }
    public static boolean check_green(String word){
        for (String category : must_include){
            int number = (category.charAt(1))-49;//-49 to convert 1 based to 0 based
            if (word.charAt(number)!=category.charAt(0)){
                System.out.println("word did not contain "+category.charAt(0));
                return false;
            }
        }
        return true;
    }
    public static boolean  check_yellow(String word){
        for (String category : yellow_list){
            int exclude_index = category.charAt(1)-49;
            if (word.charAt(exclude_index) == category.charAt(0)){
                //System.out.println("contains "+category.charAt(0)+" at index "+exclude_index);
                return false;
            }
            else{//doesnt have it at that index, so if it has the number at all, it won't be at that index.  So, i can check if it has it
                if (!word.contains(category.substring(0,1))){
                   // System.out.println("word did not contain "+category.charAt(0));
                    return false;
                }
            }
        }
        return true;
    }
    public static boolean check_grey(String word){
        HashMap<Character, Integer> character_count = new HashMap<>();

        for (String category : must_exclude){
            if (word.contains(category.substring(0,1))){//make sure this is correct
                //System.out.println("WORD CONTAINED "+category.charAt(0));
                return false;
            }
        }
        return true;
    }
}