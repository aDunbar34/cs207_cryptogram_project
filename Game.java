
import java.io.*;
import java.util.*;

public class Game {
    //  private List<List<String>> encryption;
    int userInput;
    int KeyInputNumber = -1;

    String blankPhrase;
    Players players;

    String playerName;
    Player player;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Create a new game
        new Game();
    }

    public Game(boolean test){}
    public Game() throws IOException, ClassNotFoundException {
        // Create a new player
        players = new Players();
        players.loadPlayerList();
        System.out.println("Do you want to enter a name to store your stats? y/n");
        Scanner sc = new Scanner(System.in);
        if (sc.nextLine().charAt(0) == 'y') {
            System.out.print("Please enter here:   ");
            playerName = sc.nextLine();
            if (!players.isPlayer(playerName)) {
                players.addPlayer(new Player(playerName));
            }
        } else {
            playerName = "player";
        }
        player = players.getPlayer(playerName);
        players.savePlayersList();

        /**
         // if the save game gets fixed to properly save completeEncryption, this will be used
         */
     /*   // gets the user input for the game version
        if (new File(playerName + ".game_save").exists() && new File(playerName + ".input").exists()) {
           Cryptogram c = loadGame(player);
            userInput = fileInput();
            GameVersion(userInput, c, player);
        } else {
            userInput = getUserInput();
            GameVersion(userInput, null, player);
        } */

        userInput = getUserInput();
        GameVersion(userInput, null, player);
        // runs that game version;

    }

    public int fileInput() throws FileNotFoundException {
        File file = new File("user_files/" + playerName + ".input");
        Scanner sc = new Scanner(file);
        return sc.nextInt();
    }

    //checks if the user wants to replace that value
    private boolean inputCheck(Cryptogram crypt) throws FileNotFoundException {
        System.out.println("you have already allocated a value to this key, do you want to replace it? (y/n)");
        char replace = ' ';
        do {
            replace = enterLetter(crypt);
            replace = Character.toLowerCase(replace);
            if (replace != 'y' && replace != 'n') {
                System.out.println("please enter a valid input");
            }
        } while (replace != 'y' && replace != 'n');

        if (replace == 'y') {
            return true;
        } else {
            return false;
        }
    }



    public void playGame(Cryptogram crypt) throws IOException {

        ArrayList keySet;
        HashMap UserMap = null;
        boolean running = true;
        blankPhrase = crypt.getBlankPhrase();
        System.out.println(crypt.getCompleteEncryption());

        while (running) {

            System.out.println("keys: ");
            // get data depending on game type
            if (userInput == 1) {
                keySet = new ArrayList<Character>();
                for (char key : crypt.getLetterEncryptionMap().keySet()) {
                    keySet.add(key);
                    UserMap = crypt.getLetterUserMap();
                    System.out.print(": " + key);
                }
            } else {
                keySet = new ArrayList<Integer>();
                for (int key : crypt.getEncryptionMap().keySet()) {
                    keySet.add(key);
                    UserMap = crypt.getUserMap();
                    System.out.print(": " + key);
                }
            }


            // start of game
            System.out.println();
            System.out.println("phrase: " + crypt.getPhrase());
            System.out.println("current phrase: " + blankPhrase);
            System.out.println("enter a letter or enter - to remove a mapping");
            System.out.println("Enter % at any time to show the frequency of letters");
            System.out.println("Enter ? at any time to show the answer");

            char letterInput = enterLetter(crypt);
            char keyInputChar = '_';

            if (letterInput == '?'){
                showSolution(crypt,UserMap);
                running = false;
                continue;
            }
            if (letterInput == '#'){
                players.getTop10();
                continue;

            }
            if (letterInput == '%'){
                ArrayList<Integer> frequency = crypt.getFrequency();
                    for (int i = 0; i < frequency.size(); i++){
                        System.out.println("key: " + keySet.get(i) + " frequency: " + frequency.get(i));
                    }
                    System.out.println();
                    continue;
            }

            if (letterInput == '*'){
                getHints(crypt,UserMap);
                updateBlankPhrase(UserMap,crypt);
                continue;
            }
            // the undo button//
            if (letterInput == '-') {
                if (UserMap.isEmpty()) {
                    System.out.println("you have not allocated a value to this key");
                } else {
                    System.out.println("what key do you want to unmap");
                    UserMap = gameInput(UserMap, keySet, keyInputChar, letterInput, crypt, false);

                    int counter = 0;
                    for (int i = 0; i < blankPhrase.length(); i++) {

                        if (blankPhrase.charAt(i) != ' ') {
                            if (KeyInputNumber == crypt.getCompleteEncryption().get(counter)) {
                                blankPhrase = blankPhrase.substring(0, i) + '_' + blankPhrase.substring(i + 1);
                            }
                            counter++;
                        }
                    }
                }
            } else {

                System.out.println("what key do you want to allocate it to");
                UserMap = gameInput(UserMap, keySet, keyInputChar, letterInput, crypt, true);


                // prints out the keys and what's mapped to them
                int i = 0;
                for (Object values : UserMap.values()) {
                    System.out.print("key: " + keySet.get(i) + "-" + values + "| ");
                    i++;
                }
                // updates the blank phrase for the user
                updateBlankPhrase(UserMap, crypt);


                // checks if the user has won or lost
                if (blankPhrase.equals(crypt.getPhrase())) {
                    System.out.println("\n you have won");
                    player.addCryptogramsCompleted();
                    player.addCryptogramsPlayed();
                    running = false;
                } else if (!blankPhrase.contains("_")) {
                    System.out.println("\n you have lost");
                    player.addCryptogramsPlayed();
                }

            }
        }
            System.out.println();
            player.saveDetails(userInput);
            saveGame(player, crypt);
    }


    private void GameVersion(int userInput, Cryptogram c, Player p) throws IOException {
        if (userInput == 1) {
            if (c == null) {
                c = new LetterCryptogram();
            }
            playGame(c);
        }
        if (userInput == 2) {

            if (c == null) {
                c = new NumberCryptogram();
            }
            playGame(c);
        }
    }

    private int getUserInput() throws FileNotFoundException {
        int choice = 0;
        boolean correctInput = false;
        System.out.println("Enter / at any time to quit the game");
        System.out.println("Enter * at any time to get a hint");
        System.out.println("Enter % at any time to show the frequency of letters");
        System.out.println("Enter ? at any time to show the answer");
        System.out.println("Enter # at any time to show the top ten player scoreboard");
        do {
            System.out.println("Please enter 1 to play a letter cryptogram or 2 to play a number cryptogram");
            Scanner input = new Scanner(System.in);
            try {
                choice = input.nextInt();
            } catch (InputMismatchException e) {
                char c = input.nextLine().charAt(0);
                player.saveDetails(userInput);
                System.exit(0);
            }

            input.nextLine();

            if (choice == 1 || choice == 2){
                correctInput = true;
            } else {
                System.out.println("Please enter a valid input");
            }
        } while (!correctInput);

        return choice;
    }



    public int enterNumber(ArrayList<Integer> keySet) throws FileNotFoundException{

        try {
            int i = 0;
            do {
                Scanner sc = new Scanner(System.in);
                try {
                    i = sc.nextInt();
                } catch (InputMismatchException e) {
                    char c = sc.nextLine().charAt(0);
                    player.saveDetails(userInput);
                    System.exit(0);
                }


                if (!keySet.contains(i)){
                    System.out.println("key in not in the list, please enter a valid key");
                }
            }while(!keySet.contains(i));
            return i;
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid input");
            return enterNumber(keySet);
        }

    }
    public char enterLetter(ArrayList<Character> keySet, Cryptogram crypt) throws FileNotFoundException, InputMismatchException{
        // takes in user input
        try {
            char c = ' ';
            do {
                Scanner sc = new Scanner(System.in);
                c = sc.nextLine().charAt(0);

                if (c == '/') {
                    player.saveDetails(userInput);
                    if (crypt != null)
                        saveGame(players.getPlayer(playerName), crypt);
                    else
                        saveGame(players.getPlayer(playerName), null);
                    System.exit(0);
                }

                if (c == '?') {
                    System.out.println("showing solution ...");
                    return c;
                }

                if (c == '#') {
                    return c;
                }

                if (c == '*') {
                    return c;
                }

                if (c == '%') {
                    System.out.println("showing frequency ...");
                    return c;
                }

                if (!keySet.contains(c)) {
                    System.out.println("key in not in the list, please enter a valid key");
                }
                if (!Character.isLetter(c)) {
                    System.out.println("Please enter a letter");
                }
            } while (!Character.isLetter(c) || !keySet.contains(c));
            return c;
        } catch (IOException e) {
            System.out.println("Please enter a valid input");
            return enterLetter(keySet, crypt);
        }
    }

    public char enterLetter(Cryptogram crypt) throws FileNotFoundException, InputMismatchException{
        // takes in user input
        try {
            char c = ' ';
            do {
                Scanner sc = new Scanner(System.in);
                c = sc.nextLine().charAt(0);

                if (c == '/') {
                    player.saveDetails(userInput);
                    if (crypt != null)
                        saveGame(players.getPlayer(playerName), crypt);
                    else
                        saveGame(players.getPlayer(playerName), null);
                    System.exit(0);
                }

                if (c == '?') {
                    System.out.println("showing solution ...");
                    return c;
                }

                if (c == '*') {
                    return c;
                }

                if (c == '%') {
                    System.out.println("showing frequency ...");
                    return c;
                }

                if (c == '#') {
                    return c;
                }

                if (!Character.isLetter(c) && c != '-') {
                    System.out.println("Please enter a letter");
                }
            } while (!Character.isLetter(c) && c != '-');
            return c;
        } catch (IOException e) {
            System.out.println("Please enter a valid input");
            return enterLetter(crypt);
        }
    }

    private HashMap gameInput(HashMap UserMap, ArrayList keySet, char keyInputChar, char letterInput, Cryptogram crypt, boolean newInput) throws FileNotFoundException {
        if (userInput == 1) {
            keyInputChar = enterLetter(keySet, crypt);
            if (!UserMap.get(keyInputChar).equals('_')) {
                if (inputCheck(crypt)) {
                    UserMap.replace(keyInputChar, letterInput);
                }
            } else {
                UserMap.replace(keyInputChar, letterInput);
            }
            if(newInput){
                if (crypt.getLetterEncryptionMap().get(keyInputChar) == letterInput) {
                    player.addTotalGuesses();
                    player.addCorrectGuesses();
                } else {
                    player.addTotalGuesses();
                }
            }
            this.KeyInputNumber = crypt.getChatAt(keyInputChar);
        } else {
            KeyInputNumber = enterNumber(keySet);
            if (!UserMap.get(KeyInputNumber).equals('_')) {
                if (inputCheck(crypt)) {
                    UserMap.replace(KeyInputNumber, letterInput);
                }
            } else {
                UserMap.replace(KeyInputNumber, letterInput);
            }

            if (newInput){
                char l = crypt.getEncryptionMap().get(KeyInputNumber);
                if (crypt.getEncryptionMap().get(KeyInputNumber).equals(letterInput)) {
                    player.addTotalGuesses();
                    player.addCorrectGuesses();
                } else {
                    player.addTotalGuesses();
                }
            }

        }
        return UserMap;
    }

    private void updateBlankPhrase(HashMap UserMap, Cryptogram crypt){
        if (userInput == 1) {
            for (char key : crypt.getLetterEncryptionMap().keySet()) {
                int counter = 0;
                if (!UserMap.get(key).equals('_')) {
                    for (int i = 0; i < blankPhrase.length(); i++) {

                        if (blankPhrase.charAt(i) != ' ') {
                            if (key == crypt.getalphabet(crypt.getCompleteEncryption().get(counter))) {
                                blankPhrase = blankPhrase.substring(0, i) + UserMap.get(key) + blankPhrase.substring(i + 1);
                            }
                            counter++;
                        }
                    }
                }
            }
        } else {
            for (int key : crypt.encryptionMap.keySet()) {
                int counter = 0;
                if (!UserMap.get(key).equals('_')) {
                    for (int i = 0; i < blankPhrase.length(); i++) {

                        if (blankPhrase.charAt(i) != ' ') {
                            if (key == crypt.getCompleteEncryption().get(counter)) {
                                blankPhrase = blankPhrase.substring(0, i) + UserMap.get(key) + blankPhrase.substring(i + 1);
                            }
                            counter++;
                        }
                    }
                }
            }
        }
    }

    public void saveGame(Player p, Cryptogram c) throws IOException {
        String filename = "user_files/" + p.getUserName() + ".game_save";
        FileWriter writer = new FileWriter("user_files/" + p.getUserName() + ".input", false);
        writer.write(userInput + "\n");
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
        oos.writeObject(c);
        writer.close();

    }

    public Cryptogram loadGame(Player p) throws IOException, ClassNotFoundException {
        String filename = "user_files/" + p.getUserName() + ".game_save";
        userInput = fileInput();
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
        return (Cryptogram) ois.readObject();
    }


    public HashMap showSolution(Cryptogram crypt, HashMap UserMap){
        System.out.println("the phrase is: " + crypt.getPhrase());
        if (userInput == 1){
            UserMap = crypt.getLetterEncryptionMap();
        } else {
            UserMap = crypt.getEncryptionMap();
        }
        System.out.println("updating current phrase and exiting");
        updateBlankPhrase(UserMap, crypt);
        System.out.println("current phrase: " + blankPhrase);

        return UserMap;
    }

    public void getHints(Cryptogram crypt, HashMap UserMap) {
        if (crypt instanceof LetterCryptogram) {
            ArrayList<Character> keySet = new ArrayList<Character>(UserMap.keySet());
            Random random = new Random();
            char letterToReplace;
            char hintLetter;

            do {
                letterToReplace = keySet.get(random.nextInt(keySet.size()));
            } while (!crypt.getLetterUserMap().get(letterToReplace).equals('_'));

            hintLetter = crypt.getLetterEncryptionMap().get(letterToReplace);

            UserMap.replace(letterToReplace, hintLetter);

            System.out.println("Hint: allocated the key '" + letterToReplace + "' with '" + hintLetter + "'");
        } else if (crypt instanceof NumberCryptogram) {
            ArrayList<Integer> keySet = new ArrayList<Integer>(UserMap.keySet());
            Random random = new Random();
            int numberToReplace;
            char hintLetter;

            do {
                numberToReplace = keySet.get(random.nextInt(keySet.size()));
            } while (!UserMap.get(numberToReplace).equals('_'));

            hintLetter = crypt.getEncryptionMap().get(numberToReplace);

            UserMap.replace(numberToReplace, hintLetter);


            System.out.println("Hint: allocated the key '" + numberToReplace + "' with '" + hintLetter + "'");
        } else {
            System.out.println("Invalid cryptogram type");
        }
    }
}
