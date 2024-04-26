package networking;

import java.util.ArrayList;

/**
 * Is used for easy communication between Server and Client.
 * <p>
 * A NetworkManager is constructed with a message-type and a list of packages.
 * <br>
 * 
 * @see #messageType
 * @see #packages
 */
public class NetworkManager {

    /**
     * The messageType is used for identification of a message once received by
     * Server/Client.
     * <p>
     * Best practice is to use all-caps and underscores for spaces.
     * <p>
     * Example: <code>PLAYER</code>
     */
    public String messageType;
    /**
     * A package is a String[2] containing <code>String[0] = name </code> and
     * <code>String[1] = message</code>.
     * <p>
     * The <code>message</code> is a String containing all fields of the package
     * separated by ",".
     */
    private ArrayList<String[]> packages = new ArrayList<>();

    /**
     * Constructs an empty NetworkManager.
     * <p>
     * Usefull for constructing messages.
     * <p>
     */
    public NetworkManager() {
    }

    /**
     * Constructs a message with the current state of <code>this</code>
     * NetworkManager
     * 
     * @return String with the message
     */
    public String constructMessage() {
        String message = "";
        message += messageType + "%";
        for (String[] strings : packages) {
            message += strings[0] + ":" + strings[1] + ";";
        }
        return message;
    }

    /**
     * Deconstructs a message and stores it into a <code>NetworkManager</code>
     * <p>
     * Usefull for processing messages.
     * 
     * @param message String to deconstruct
     */
    public static NetworkManager deconstructMessage(String message) {
        NetworkManager manager = new NetworkManager();

        String[] type_data = message.split("%");
        manager.messageType = type_data[0];
        String[] packages = type_data[1].split(";");
        for (String string : packages) {
            String[] name_message = string.split(":");
            manager.addPackage(name_message[0], name_message[1]);
        }

        return manager;
    }

    /**
     * Returns all <code>packages</code> with a certain name.
     * <p>
     * Example: <code>getPackagesByName("PLAYER")</code> could give you the data of
     * all players.
     * 
     * @param name name of the packages
     * @return String[] with data from all packages with the name
     */
    public String[] getPackagesByName(String name) {
        ArrayList<String> result = new ArrayList<>();
        for (String[] strings : packages) {
            if (strings[0].equals(name))
                result.add(strings[1]);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returning split-up fields of packages with a certain name.
     * <p>
     * (Splits package-message at ",")
     * 
     * @return String[][] with fields of packages.
     */
    public String[][] getFieldsByPackageName(String name) {
        String[] packages = getPackagesByName(name);
        String[][] result = new String[packages.length][];
        for (int i = 0; i < packages.length; i++) {
            result[i] = packages[i].split(",");
        }
        return result;
    }

    /**
     * Adds a package to the manager.
     * <p>
     * Usefull if message is already constructed or custom.
     * 
     * @param name    name of the package
     * @param message message of the package
     */
    public void addPackage(String name, String message) {
        packages.add(new String[] { name, message });
    }

    /**
     * Adds a package to the manager.
     * 
     * @param name   name of the package
     * @param fields fields of the package. Will be converted to a message-string.
     */
    public void addPackage(String name, int[] fields) {
        String message = "";
        for (Object o : fields) {
            message += o + ",";
        }
        message = message.substring(0, message.length() - 1);
        addPackage(name, message);
    }

    /**
     * Adds a package to the manager
     * 
     * @param name   name of the package
     * @param fields fields of the package. Will be converted to a message-string.
     */
    public void addPackage(String name, double[] fields) {
        String message = "";
        for (Object o : fields) {
            message += o + ",";
        }
        message = message.substring(0, message.length() - 1);
        addPackage(name, message);
    }

    /**
     * Adds a package to the manager
     * 
     * @param name   name of the package
     * @param fields fields of the package. Will be converted to a message-string.
     */
    public void addPackage(String name, String[] fields) {
        String message = "";
        for (Object o : fields) {
            message += o + ",";
        }
        message = message.substring(0, message.length() - 1);
        addPackage(name, message);
    }

}
