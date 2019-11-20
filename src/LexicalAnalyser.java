import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Arrays;


class LexicalAnalyser {
    Map<String, Integer>  keywords_map = this.get_keywords();
    Map<String, Integer> identifiers = new HashMap<String, Integer>();
    Map<String, Integer> custom_lexems = new HashMap<String, Integer>();

    Map<String, Integer> constants = new HashMap<String, Integer>();
    Map<String, Integer> multiple_delimeters = this.get_delimeters();

    Set<Integer> delimeters = new HashSet<>(Arrays.asList(59,60,46));
    Set<Integer> ascii_numbers = new HashSet<>(Arrays.asList(48, 49, 50, 51, 52, 53, 54, 55, 56, 57));
    Set<Integer> ascii_letters = new HashSet<>(Arrays.asList(65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
            79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90));


    Integer latest_identifier= 1001;
    Integer latest_constant= 2001;
    Integer latest_custom = 7001;


    Integer row_counter = 1;
    Integer column_counter = 0;

    ArrayList<ArrayList<Object>> listOfLexems = new ArrayList<ArrayList<Object>>();


    private Map<String, Integer> get_keywords(){
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("PROGRAM",401);
        map.put("BEGIN", 402);
        map.put("IF", 403);
        map.put("THEN",404);
        map.put("ELSE",405);
        map.put("ENDIF",406);
        map.put( "WHILE", 407);
        map.put( "DO", 409);
        map.put("ENDWHILE", 410);
        map.put("END", 411);
        return map;

    }
    private Map<String, Integer> get_delimeters(){
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("<",60);
        map.put("<=", 301);
        map.put("<>", 302);
        map.put(">",62);
        map.put(">=",303);
        return map;

    }

    public void parse_file(String filename) {
        File file = new File(filename);
        FileInputStream fis = null;

        try {


            fis = new FileInputStream(file);

            int character = 0;
            while (true) {

                if(character == 0){
                    character = fis.read();
                    column_counter += 1;
                }else if (character == -1){
                    break;
                }else if(ascii_numbers.contains(character)){
                    String token = Character.toString((char) character);
                    character = fis.read();
                    column_counter += 1;
                    if(ascii_letters.contains(character)){
                        throw new LexerException("Error (line " +Integer.toString(row_counter)+ ",column " + Integer.toString(column_counter) +"): Illegal symbol \'"+ Character.toString((char) character)+"\'" );
                    }
                    while (ascii_numbers.contains(character)){
                        token += (char) character;
                        character = fis.read();
                        column_counter += 1;
                    }

                    if (constants.containsKey(token)) {
                        listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter, column_counter - token.length(), constants.get(token), token)));

                    } else {

                        constants.put(token, latest_constant);
                        listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter, column_counter - token.length(), latest_constant, token)));
                        latest_constant += 1;
                    }


                }else if(ascii_letters.contains(character)) {
                    String token = Character.toString((char) character);

                    character = fis.read();
                    column_counter += 1;

                    while (ascii_letters.contains(character) || ascii_numbers.contains(character)){
                        token += (char) character;
                        character = fis.read();
                        column_counter += 1;
                    }


                    if(keywords_map.containsKey(token)){
                        listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter,column_counter - token.length(),keywords_map.get(token),token)));

                    }else {
                        if (identifiers.containsKey(token)) {
                            listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter, column_counter - token.length(), identifiers.get(token), token)));

                        } else {


                            identifiers.put(token, latest_identifier);
                            listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter, column_counter - token.length(), latest_identifier, token)));
                            latest_identifier += 1;
                        }
                    }



                }else if(character == 61){
                    String token = Character.toString((char) character);
                    listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter,column_counter - token.length() + 1,multiple_delimeters.get(token),token)));

                    character=0;

                }else if(character == 60) {
                    character = fis.read();
                    column_counter += 1;

                    String token = "";
                    if (character == 62) {
                        token = "<>";

                        character = 0;
                    } else if (character == 61) {
                        token = "<=";
                        character = 0;
                    } else {
                        token = "<";
                    }
                    listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter,column_counter - token.length() + 1 ,multiple_delimeters.get(token),token)));

                }else if(character == 62) {
                    character = fis.read();
                    column_counter += 1;

                    String token = "";
                    if (character == 61) {
                        token = ">=";

                        character = 0;
                    } else {
                        token = ">";
                    }
                    listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter, column_counter - token.length() + 1, multiple_delimeters.get(token), token)));


                }else if(character == 40){
//                    comments extraction
                    character = fis.read();
                    column_counter += 1;

                    if (character==42){
                        String comment = "";

                        while(true){
                            character = fis.read();
                            column_counter += 1;

                            if ((character == 42)){
                                int next_character = fis.read();
                                column_counter += 1;

                                if (next_character == -1){
                                    throw new LexerException("Error: unexpected end of file" );

                                }else if (next_character == 41){
                                    break;
                                }else{
                                    comment += (char) character;
                                    character = next_character;
                                }

                            }else {
                                throw new LexerException("Error: unexpected end of file" );

                            }

                            comment += (char) character;
                        }
                        character = 0;


                    }
                    else{

                        throw new LexerException("Error (line " +Integer.toString(row_counter)+ ",column " + Integer.toString(column_counter-1) +"): Illegal symbol \'(\'" );

                    }


                }

                else if(delimeters.contains(character)){
                    listOfLexems.add(new ArrayList<Object>(Arrays.asList(row_counter,column_counter, character, (char) character)));
                    character = 0;

                }else if(character == 32) {
                    character = 0;
                }else if (character == 10) {
                    character = 0;
                    column_counter = 0;
                    row_counter += 1;
                }else{
                    throw new LexerException("Error (line " +Integer.toString(row_counter)+ ",column " + Integer.toString(column_counter) +"): Illegal symbol \'"+ Character.toString((char) character)+"\'" );
                }

            }


        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public void print_results() {
        if (!identifiers.isEmpty()) {
            System.out.println("Identifiers:");

            for (Map.Entry<String, Integer> entry : identifiers.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue().toString());
            }
        }
        System.out.println();

        if (!constants.isEmpty()) {
            System.out.println("Constants:");

            for (Map.Entry<String, Integer> entry : constants.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue().toString());
            }
        }
        System.out.println();

        for (ArrayList<Object> l1 : listOfLexems) {
            for (Object n : l1) {
                System.out.print(n + " ");
            }

            System.out.println();
        }
    }


    public static void main(String[] args) {
        LexicalAnalyser lexicalAnalyser = new LexicalAnalyser();
        lexicalAnalyser.parse_file("tests/test1");
        lexicalAnalyser.print_results();

    }
}