import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import org.apache.commons.io.FilenameUtils;

public class JSONConverter {

    public static void HTMLtoJSON(String folderPath, String resultPath, boolean inDepth) {
        
        File folder = new File(folderPath);
        
        if(folder.listFiles().length > 0){

            File JSONFolder = new File(resultPath);
            
            //CREAR UNA CARPETA DE DESTINO PARA LOS JSONES
            if(!JSONFolder.exists()){
                
                System.out.println("Creando carpeta" + JSONFolder.getName() + "...");
                if(JSONFolder.mkdir()){
                    System.out.println("Carpeta json creada correctamente");
                }else{
                    System.out.println("Error al crear la carpeta json");
                }
            }
            
            for (final File f : folder.listFiles()) {
        
                if (!f.isDirectory()) {
                   
                    String extension = FilenameUtils.getExtension(f.getName());

                    if(extension.equals("html")){
                        try{
                            String name = FilenameUtils.removeExtension(f.getName());

                            String fileContent = readHTML(f);
                            String firstCharacter = name.substring(0,1);
                            if(firstCharacter.equals("_")){
                                name = name.substring(1, name.length());
                            }

                            File jsonFromHTML = new File(JSONFolder.getPath() + "/" + name + ".json");

                            if(jsonFromHTML.exists()){
                                //TODO: LEER EL FICHERO E INSERTAR EL HTML EN EL VALOR "data" DEL JSON
                                System.out.println("Modificando el archivo " + jsonFromHTML.getName());

                                writeInJSON(jsonFromHTML, fileContent);
                            }else{

                                try{

                                    System.out.println("Creando el archivo " + jsonFromHTML.getName());
                                    jsonFromHTML.createNewFile();
                                    writeInJSON(jsonFromHTML, fileContent);

                                }catch(IOException e){
                                    System.out.println("Error creando el archivo" + jsonFromHTML.getName());
                                }
                            }
                        }catch(IOException e){
                            System.out.println("Error IO");
                        }
                    }
                }else{
                   if(inDepth && !f.getPath().equals(resultPath)){
                        HTMLtoJSON(f.getPath(), resultPath + "/" + f.getName(), inDepth);
                   }
                }
            }
        }else{
            System.out.println("No se han encontrado archivos .html");
        }
    }
    
    /**
     *  Devuelve el contenido de un fichero como String
     *  @params{File} file - Archivo a leer.
     *  @return - String con el contenido del fichero.
     * 
    */
    public static String readHTML(File file) throws IOException {

        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        String fileContent = new String(encoded);

        return new String(encoded);
    }

    /** Comprueba si el fichero existente contiene una estructura de JSON para sobreescribirla o crear una nueva
     *  @param {File} file - Fichero a comprobar
     *  @param {String} text - String con el valor del campo data del JSON
     */
    public static void writeInJSON(File file, String text){
        try{
            
            String fileContent = readHTML(file);
            String aux = fileContent;
           
            // COMPROBAMOS SI EL FICHERO ESTÁ VACÍO
            if(fileContent.length() > 0){
                writeFile(file, text,  true);
            }else{
                writeFile(file, text, false);
            }
            
        }catch(FileNotFoundException e){
            System.out.println("File " + file.getName() + "not found");
        }catch(IOException e){
            System.out.println("Error abriendo el archivo " + file.getName());
        }
    }
    
    /** Escribe un JSON en el fichero.
     *  @param {File} file - Fichero donde se va a escribir.
     *  @param {String} text - String con el valor del campo data del JSON
     *  @param {boolean} isJONS - Indica si el fichero contiene una estructura de JSON
     */
    private static void writeFile(File file, String text, boolean isJSON) throws IOException{
       

        String result = readHTML(file);
        
        FileWriter writer = null;
        
        try{
            
            if(isJSON){
                
                if(result.contains("data")){
                    text = escapeHTML(text);
                    String a = "\"data\" : \"" + text + "\"";
                    result = result.replaceAll("\"data\"\\s*:\\s*\".*\"", Matcher.quoteReplacement(a));
                }else{
                    text = escapeHTML(text);
                    result = result.replaceAll("\\s*}$", "");
                    result += ",\n\"data\" : \"" + text + "\"\n}";
                }
                
            }else{
                result = "{\n"
                        + "\"success\" : true,\n"
                        + "\"data\" : \"" + escapeHTML(text) + "\" \n"
                        + "}";
            }
            
            writer = new FileWriter(file.getPath());

            writer.write(result);
        
        }catch(IOException e){
            System.out.println("Error de escritura en " + file.getName());
        }finally{
            if(writer!=null){
                writer.flush();
                writer.close();
            }
        }
    }
    
      
    /**
     *  Escapa un html y lo reduce a una única línea.
     *  @params{String} html - String con el texto a escapar.
     *  @return - String con el texto escapado.
     * 
    */
    public static String escapeHTML(String html){
        
        String text = html;
        
        // Eliminar saltos de línea
        text = text.replace("\n", "").replace("\r", "");
        // Eliminar tabulaciones
        text = text.replace("\t", "");
        
        //Sustituir " por \"
        text = text.replace("\"", "\\\"");
        
        //Sustituir / por \/
        text = text.replace("/", "\\/");
        
        //Elimina los espacios en blanco entre los finales de una etiqueta y el inicio de la siguiente
        text = text.replaceAll(">\\s+<", "><");
        
        return text;
    }
}
