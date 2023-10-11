import server.uci.UCI;

import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
        UCI uci = new UCI();
        uci.toggle();
        Scanner sc = new Scanner(System.in);
        String input;
        while(true){
            input = sc.nextLine();
            if(input.equals("quit")){
                break;
            }
            uci.sendCommand(input);
        }

    }
}