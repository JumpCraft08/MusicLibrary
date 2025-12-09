import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLogger implements AutoCloseable {
    private final File logFile;
    private final BufferedWriter writer;

    public SimpleLogger(File baseFolder, String prefix) throws IOException {
        // Log diario: YYYY-MM-DD-prefix.log
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.logFile = new File(baseFolder, date + "-" + prefix + ".log");
        // true para append, así no se sobrescribe el log del día
        this.writer = new BufferedWriter(new FileWriter(logFile, true));
    }

    public synchronized void log(String message) {
        try {
            // Cada mensaje lleva la hora exacta
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            writer.write(timestamp + " - " + message);
            writer.newLine();
            writer.flush();
            // También mostramos por consola
            System.out.println(timestamp + " - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getLogFile() {
        return logFile;
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
