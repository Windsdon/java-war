package windsdon.war.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author Windsdon
 */
public class WarServer extends Thread implements ActionListener, WindowListener {

    private String decodedPath;
    private boolean showConsole;
    private boolean autoStart;
    private boolean serverRunning;
    private WarServerConsole console;
    /**
     *
     */
    private final double tickInterval = 100 / 3;
    private ArrayList<Player> players;
    private SocketListener loginListener;
    private ServerConfig config;

    public boolean init(String[] args) {
        boolean status = true;
        showConsole = true;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("nc".equals(arg)) {
                showConsole = false;
            }
        }

        if (showConsole) {
            console = new WarServerConsole();
            console.setVisible(true);
            console.text.addActionListener(this);
            console.text.setText("");
            console.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            console.addWindowListener(this);
        }

        createServerStructure();

        loadServerConfig();

        start();

        return status;
    }

    private void createServerStructure() {
        String path = WarServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return;
        }

        console.display.addLine(decodedPath);

        File serverDir = new File(decodedPath + "serverconfig");
        File serverConfig = new File(decodedPath + "serverconfig/server.properties");
        File serverOps = new File(decodedPath + "serverconfig/ops.txt");
        File serverSaves = new File(decodedPath + "serverconfig/saves");

        if (!serverDir.exists()) {
            console.display.addLine("§1Criando diretório§b serverconfig");
            if (serverDir.mkdir()) {
                console.display.addLine("§6Criado");
            } else {
                console.display.addLine("§2Erro");
            }
        }

        if (!serverConfig.exists()) {
            console.display.addLine("§1Criando arquivo§b server.properties");
            try {
                if (serverConfig.createNewFile()) {
                    console.display.addLine("§6Criado");
                } else {
                    console.display.addLine("§2Erro");
                }
            } catch (IOException ex) {
                console.display.addLine("§2Erro");
            }
        }

        if (!serverOps.exists()) {
            console.display.addLine("§1Criando arquivo§b ops.txt");
            try {
                if (serverOps.createNewFile()) {
                    console.display.addLine("§6Criado");
                } else {
                    console.display.addLine("§2Erro");
                }
            } catch (IOException ex) {
                console.display.addLine("§2Erro");
            }
        }

        if (!serverSaves.exists()) {
            console.display.addLine("§1Criando diretório§b serverconfig/saves");
            if (serverSaves.mkdir()) {
                console.display.addLine("§6Criado");
            } else {
                console.display.addLine("§2Erro");
            }
        }
    }

    private void exit() {
        addLine("§2§bSaving config...");
        try {
            saveConfigToFile();
        } catch (Exception ex) {
            return;
        }
        addLine("§2§bServer closing...");
        if (showConsole) {
            console.dispose();
        }
        System.exit(0);
        //interrupt();
    }

    @Override
    public void run() {
        serverRunning = true;
        while (serverRunning) {
            doServerRunTick();
        }
    }

    public static void main(String[] args) {
        new WarServer().init(args);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parseCommand(console.text.getText());
        synchronized (this) {
            notify();
        }
        console.text.setText("");
    }

    private void addLine(String text) {
        if (showConsole) {
            console.display.addLine(text);
        }
    }

    private void parseCommand(String command) {
        String[] pieces = command.split(" ");
        switch (pieces[0]) {
            case Commands.COMMAND_EXIT:
                exit();
                break;
            case Commands.COMMAND_CONFIG:
                displayServerConfig();
                break;
            case Commands.COMMAND_SET: {
                String joinedPiece = "";
                for (int i = 2; i < pieces.length; i++) {
                    String string = pieces[i];
                    joinedPiece += " " + string;
                }
                if (pieces.length > 2 && config.exists(pieces[1]) && config.isValueValid(pieces[1], joinedPiece)) {
                    config.setOption(pieces[1], joinedPiece);
                    console.display.addLine("§rChanged §[fcad00]" + pieces[1] + "§r to §[90ebff]" + config.getOption(pieces[1]));
                } else {
                    console.display.addLine("§2" + Commands.COMMAND_SET_FORMAT);
                }
            }
            break;
            default:
                console.display.addLine("§2Comando desconhecido:§r §b§7" + pieces[0]);
        }
    }

    private FileInputStream getResource(String txt) {
        try {
            return new FileInputStream(new File(decodedPath + txt));
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    private void loadServerConfig() {
        try {
            config = new ServerConfig(FileLoader.loadLines(getResource("serverconfig/server.properties"), console.display));
        } catch (IOException ex) {
            console.display.addLine("§2Erro ao carregar arquivo §bserver.properties");
        } finally {
            console.display.addLine("§6Arquivo carregado: §bserver.properties");
        }
        displayServerConfig();
    }

    private void displayServerConfig() {
        String[] configString = config.getConfigLines();
        for (int i = 0; i < configString.length; i++) {
            String string = configString[i];
            console.display.addLine(string);
        }
    }

    private void doServerRunTick() {
        synchronized (this) {
            try {
                wait(100);
            } catch (InterruptedException ex) {
            }
        }
    }

    private void saveConfigToFile() throws FileNotFoundException, Exception {
        File serverConfig = new File(decodedPath + "serverconfig/server.properties");
        String[] configLines = config.getFile();
        if (serverConfig.canWrite()) {
            try (PrintStream pConfig = new PrintStream(serverConfig)) {
                for (int i = 0; i < configLines.length; i++) {
                    String string = configLines[i];
                    pConfig.println(string);
                }
                console.display.addLine("§6Salvo!");
                pConfig.close();
            } catch (FileNotFoundException ex) {
                console.display.addLine("§2Erro ao salvar!");
                throw ex;
            }
        } else {
            console.display.addLine("§2Erro ao salvar!");
            throw new Exception();
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exit();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private static final class Commands {

        public static final String COMMAND_EXIT = "exit";
        public static final String COMMAND_SET = "set";
        public static final String COMMAND_RELOAD = "reload";
        public static final String COMMAND_OP = "op";
        public static final String COMMAND_DEOP = "deop";
        public static final String COMMAND_START = "start";
        public static final String COMMAND_PAUSE = "pause";
        public static final String COMMAND_SAVE = "save";
        public static final String COMMAND_KICK = "kick";
        public static final String COMMAND_PLAYER = "player";
        public static final String COMMAND_CONFIG = "config";
        ///
        public static final String COMMAND_EXIT_FORMAT = "Uso: §bexit";
        public static final String COMMAND_SET_FORMAT = "Uso: §bset <string> <value>";
        public static final String COMMAND_RELOAD_FORMAT = "Uso: §breload";
        public static final String COMMAND_OP_FORMAT = "Uso: §bop <player_name>";
        public static final String COMMAND_DEOP_FORMAT = "Uso: §bdeop <player_name>";
        public static final String COMMAND_START_FORMAT = "Uso: §bstart";
        public static final String COMMAND_PAUSE_FORMAT = "Uso: §bpause";
        public static final String COMMAND_SAVE_FORMAT = "Uso: §bsave";
        public static final String COMMAND_KICK_FORMAT = "Uso: §bkick <player_name>";
        public static final String COMMAND_PLAYER_FORMAT = "Uso: §bplayer <option> <value>";
        public static final String COMMAND_CONFIG_FORMAT = "config";
    }
}
