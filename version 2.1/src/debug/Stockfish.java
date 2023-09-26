package debug;

import java.io.*;


//code from https://chess.stackexchange.com/questions/34687/unable-to-communicate-with-uci-through-java-program
public class Stockfish {
    private Process engineProcess;
    private BufferedReader processReader;
    private OutputStreamWriter processWriter;
    private String path = "fish";

    private boolean foundExecutable = false;
    public Stockfish(){
        File dir = new File(path);
        if(dir.isDirectory()){
            for(File file:dir.listFiles()){
                if (!file.isDirectory() && file.getName().contains("stockfish")) {
                    path = file.getPath();
                    foundExecutable = true;
                    break;
                }
            }
        }
    }

    public boolean startEngine() {
        if(!foundExecutable){
            System.out.println("Could not find executable");
        }
        try {
            engineProcess = Runtime.getRuntime().exec(path);
            processReader = new BufferedReader(new InputStreamReader(
                    engineProcess.getInputStream()));
            processWriter = new OutputStreamWriter(
                    engineProcess.getOutputStream());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void sendCommand(String command) {
        try {
            processWriter.write(command + "\n");
            processWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOutput() throws IOException, InterruptedException {

        return getOutput(1000);
    }

    public String getOutput(int waitTime) throws IOException, InterruptedException {
        StringBuffer buffer = new StringBuffer();

        Thread.sleep(waitTime);
        sendCommand("isready");
        while (true) {
            String text = processReader.readLine();
            if (text.equals("readyok")) {
                break;
            }
            else if(!text.isEmpty()){
                buffer.append(text + "\n");
            }
        }
        return buffer.toString().trim();
    }



        public void stopEngine() {
            try {
                sendCommand("quit");
                processReader.close();
                processWriter.close();
            } catch (IOException e) {
            }
        }

    }
