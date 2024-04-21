package client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

@Service
public class ConfigService {
    private final String configFilePath = "src/main/java/client/config/user_configs.properties";

    /**
     * Reads the server url from the config file
     *
     * @return - the server url in the config file
     */
    public String getServer() {
        try {
            return getProperties().getProperty("server");
        } catch (IOException e) {
            // ToDo show Error. Switching to default server: localhost
            return "http://localhost:8080/";
        }
    }

    /**
     * Writes the server url to the config file
     *
     * @param url - the url to be written to the config file
     */
    public void setServer(String url) {
        try {
            //load config file
            Properties p = getProperties();
            p.setProperty("server", url);

            //write the new server url to the config file
            FileOutputStream outputStream = new FileOutputStream(configFilePath);
            p.store(outputStream, "Updated server");
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * checks what language is in the config file
     *
     * @return - the language in the config file ("english" if the file is not found)
     */
    public String getConfigLanguage() {
        try {
            final String language = getProperties().getProperty("language");
            if (language == null || language.isEmpty()) return "english";
            return language;
        } catch (IOException e) {
            // ToDO show Error. Switching to default language: English
            return "english";
        }
    }

    /**
     * Changes the configuration language based on the chosen language in the settings
     *
     * @param language - the language to change the config to
     */
    public void setConfigLanguage(String language) {
        try {
            //load config file
            Properties p = getProperties();
            p.setProperty("language", language);

            //write the new server url to the config file
            FileOutputStream outputStream = new FileOutputStream(configFilePath);
            p.store(outputStream, "Updated language");
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the language from the config file
     *
     * @return - hashmap with the properties and their translation of the language
     */
    public HashMap<String, Object> getLanguage() {
        String language = getConfigLanguage();

        return readJsonToMap("src/main/java/client/languages/" + language + ".json");
    }

    /**
     * checks what currency is in the config file
     *
     * @return - the currency code in the config file ("EUR" if the file is not found)
     */
    public String getConfigCurrency() {
        try {
            Properties p = getProperties();
            String language = p.getProperty("currency");
            if (language == null || language.isEmpty()) return "EUR";
            return language;
        } catch (Exception e) {
            // ToDO show Error. Switching to default currency: EUR
            return "EUR";
        }
    }

    /**
     * Reads the email from the config file
     *
     * @return - the email in the config file
     */
    public String getEmail() {
        try {
            return getProperties().getProperty("email");
        } catch (IOException e) {
            // ToDo show Error. No email available
            return "";
        }
    }

    /**
     * Reads the password from the config file
     *
     * @return - the password in the config file
     */
    public String getPassword() {
        try {
            return getProperties().getProperty("password");
        } catch (IOException e) {
            // ToDo show Error. No password available
            return "";
        }
    }

    /**
     * Saves the currency to the config file
     *
     * @param currency - the currency to be saved
     */
    public void setConfigCurrency(String currency) {
        try {
            // load config file
            Properties p = getProperties();
            p.setProperty("currency", currency);

            // write the new currency to the config file
            FileOutputStream outputStream = new FileOutputStream(configFilePath);
            p.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            // ToDo show Error saving the currency.
        }
    }

    /**
     * Reads the properties from the config file
     *
     * @return - the properties from the config file
     */
    private Properties getProperties() throws IOException {
        Properties p = new Properties();
        FileInputStream inputStream = new FileInputStream(configFilePath);
        p.load(inputStream);
        inputStream.close();
        return p;
    }


    /**
     * Reads a json file and returns a hashmap with the properties and their translations
     *
     * @param filePath - the path to the json file
     * @return - the hashmap with the properties and their translations
     */
    public HashMap<String, Object> readJsonToMap(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<>();
        try {
            map = mapper.readValue(Paths.get(filePath).toFile(), HashMap.class);
            return map;
        } catch (Exception e) {
            return map;
        }
    }
}
