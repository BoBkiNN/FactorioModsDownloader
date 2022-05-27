package xyz.bobkinn.factoriomodsdownloader;

import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;
import net.lingala.zip4j.ZipFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static String ostype = null;
    static boolean unzip = false;
    public static void getOstype() {
        ostype= System.getProperty("os.name").toLowerCase();
    }

    public static void printTextL(String text){
        System.out.println(text);
    }

    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    /**
     * Stops the app
     */
    public static void stop(){
        String dirpath = "tmp";
        File dir = new File(dirpath);
        String mfilepath = "tmp"+File.separator+"mods.yml";

        File mfile = new File(mfilepath);
//        printTextL(mfile.getAbsolutePath());
        try {
            Files.deleteIfExists(Paths.get(mfilepath));
            if (dir.exists()){
                if (dir.listFiles().length == 0){
                    Files.deleteIfExists(Paths.get(dirpath));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

    /**
     * Downloads file
     * @param url URL string to download
     * @param dest File destination to save
     * @throws IOException when file can`t be downloaded or other IO errors
     * @throws MalformedURLException if url is incorrect
     */
    public static void download(String url, File dest) throws IOException {
        URL site = null;
        try {
            site = new URL(url);
        } catch (MalformedURLException e) {

            printTextL("Неверная ссылка.");
            e.printStackTrace();
            System.exit(0);
        }
            FileUtils.copyURLToFile(site, dest);
//        printTextL("Загружено в: "+dest.toString());
    }

    /**
     * @param list1 список содержит все
     * @param list2 список содержит не все из list1
     * @return Список элементов которых нет в list1
     */
    public static ArrayList<String> strArrayDiff(ArrayList<String> list1, ArrayList<String> list2){
        ArrayList<String> diffList = new ArrayList<String>();
        for (String str : list2){
            if (!list1.contains(str)){
                diffList.add(str);
            }
        }
        return diffList;
    }

    public static void main(String[] args) {
        String mfile = "https://raw.githubusercontent.com/ADAMADA8/factorio/main/mods.yml";
        String moddownloadurl = "https://github.com/ADAMADA8/factorio/raw/main/";

        String arg1 = "-unzip";
        String arg2 = "";
        boolean arg2Ex = false;
        boolean pathValid = false;
        File customModdir;

        if (args.length == 1){
            arg1 = args[0];
        }
        if (args.length == 2){
            arg2 = args[1];
            arg2Ex = true;
        }

        if (arg2Ex){
            if (!isValidPath(arg2)) {
                printTextL("Путь указан неправильно, завершение");
                stop();
            }

            customModdir = new File(arg2);
            if (!customModdir.exists()){
                printTextL("Указанный путь не существует, создайте его.");
                stop();
            }
            if (!customModdir.isDirectory()){
                printTextL("Указанный путь не является папкой, завершение");
                stop();
            } else {pathValid = true;}

        }

        unzip = Objects.equals(arg1, "+unzip");
        if (unzip){printTextL("Распаковка включена");} else {printTextL("Распаковка выключена");}

        ArrayList<String> dmodlist = new ArrayList<String>();
        String moddir = null;
        Yaml yaml = new Yaml();
        getOstype();
        printTextL("Система: " + ostype);

        if (ostype.startsWith("win")) {
            moddir = System.getenv("APPDATA") + File.separator+"Factorio"+File.separator+"Mods";
//            printTextL(moddir);
        } else if (ostype.contains("nix") || ostype.contains("nux")) {
            moddir = System.getProperty("user.home")+ File.separator+".factorio"+File.separator+"mods";
            new File(moddir).mkdirs();
        }  else{
            printTextL("Ваша система не поддерживатеся 0-0");
            stop();
        }
        if (pathValid){
            printTextL("Кастомный путь к папке с модами: "+arg2);
            moddir = arg2;
        }

        File tmpdir = new File("tmp");
        if (!tmpdir.exists()) {
            tmpdir.mkdir();
        }
        File modlistfile = new File("tmp" + File.separator + "mods.yml").getAbsoluteFile();
        if (!modlistfile.exists()){
            try {
                modlistfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        printTextL(modlistfile.toString());
        printTextL("Загрузка списка модов в "+modlistfile.getAbsolutePath());
        try {
            download(mfile, modlistfile);
        } catch (IOException e) {
            printTextL("Список модов не найден, завершение..");
            stop();
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(modlistfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        HashMap yamlMap = yaml.load(inputStream);
//        printTextL(yamlMap.toString());
        ArrayList<String> modlist = (ArrayList) yamlMap.get("download");
        ArrayList<String> needunzip = (ArrayList<String>) yamlMap.get("needunzip");
        printTextL("Будут установлены моды ("+ modlist.size() +") в "+moddir+" :");
        File moddirFile = new File(moddir);
        if (!moddirFile.exists()){
            moddirFile.mkdirs();
        }

        for (Object mod : modlist){
            printTextL(mod.toString());
        }
        printTextL("=========================");
//        printTextL(download.toString());
        Scanner in = new Scanner(System.in);
        printTextL("Продолжить? y/n");
        String choose = in.nextLine();
        in.close();
//        printTextL(choose);
        if (choose.contains("n")){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stop();
//            printTextL("Stopping!");
        }
        printTextL("Подождите..");
        long startTime = System.currentTimeMillis();
        long fullSize = 0L;

        for (Object mod : modlist){
            printTextL("--------------------------");
            File dest = new File(moddir+File.separator+mod.toString());
            String url = moddownloadurl+ mod;
            int modIndex = modlist.indexOf(mod)+1;
            printTextL("["+modIndex+"/"+modlist.size()+"] "+ mod +" ("+url.replace(" ","%20")+")..");
            File modfile = new File(moddir+File.separator+ mod);
            try {
                System.out.print("Скачивание .. ");

                download(url.replace(" ","%20"),dest);
                printTextL("("+ modfile.length() / 1024 / 1024+"."+modfile.length()%1024 +")MB");
                dmodlist.add(mod.toString());
            } catch (IOException e) {
                printTextL(System.lineSeparator()+"Мод "+ mod +" не найден ("+modfile+"), запустите jar с sudo или возможностями админа");
                e.printStackTrace();
                continue;
            }

            fullSize+= modfile.length();
            if (unzip) {
                try {
                    printTextL("Распаковка ..");
                    new ZipFile(modfile).extractAll(moddir);
                } catch (ZipException e) {
                    printTextL("При распаковке произошла ошибка:");
                    e.printStackTrace();
                }
                dest.delete();
            }


            if (!unzip) {
                if (needunzip.contains(mod)){
                    try {
                        printTextL("Обязательная распаковка ..");
                        new ZipFile(modfile).extractAll(moddir);
                    } catch (ZipException e) {
                        printTextL("При распаковке произошла ошибка:");
                        e.printStackTrace();
                    }
                    dest.delete();
                }
            }


        }
        printTextL("=========================");

        long endTime = System.currentTimeMillis();
        long elapsedMillis = endTime-startTime;
        long elapsedSeconds= elapsedMillis / 1000;
        long elapsedMinutes = elapsedSeconds / 60;
        long elapsedMinutesSeconds= elapsedMinutes % 60;

        fullSize=fullSize/1024; //kb
        long fullSizeMB=fullSize/1024; //mb
        String elapsedTime = elapsedMinutes+":"+elapsedMinutesSeconds;
        printTextL("Скачано ("+fullSizeMB+"MB) и установлено "+ dmodlist.size() +" из "+modlist.size());
        ArrayList<String> notdownloaded = strArrayDiff(modlist,dmodlist);
//        printTextL(Arrays.toString(notdownloaded.toArray()));
        if (notdownloaded.size()>0){
            printTextL("Неустановлено:");
            for (String mod : notdownloaded){
                printTextL(mod);
            }
        }
        try {
            assert inputStream != null;
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }
}
