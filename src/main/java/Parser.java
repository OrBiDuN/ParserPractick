import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;


public class Parser {

    private static Document getPage() throws IOException{
        String url = "https://poezdato.net/raspisanie-po-stancyi/samara/elektrichki/";
        Document page = (Document) Jsoup.parse(new URL(url), 3000);
        return page;

    }

    public static void main(String[] args) throws IOException,SQLException,ClassNotFoundException {
        // БД
        String LoginBD = "root";
        String PassBD = "root";
        String connectionURL = "jdbc:mysql://localhost:3306/users?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
        Class.forName("com.mysql.cj.jdbc.Driver");
        //очистка бд
        try (Connection connection = DriverManager.getConnection(connectionURL, LoginBD, PassBD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE marhfull;");
            statement.executeUpdate("TRUNCATE TABLE marh;");
        }
        //Парсер основной страницы
        Document page = getPage();
        Element table = page.select("table[class=schedule_table stacktable desktop]").first();
        Elements td = table.select("td");


        //Чистка от не нужных столбцов
        int i = 0;
        int coll = td.size();
        try {
            while (i <= coll) {

                td.remove(i);
                i = i + 7;
                coll--;

            }
        } catch (Exception e) {

            i = 5;
            while (i <= coll) {
                td.remove(i);
                i = i + 6;
                coll--;
            }

            i = 5;
            while (i <= coll) {

                td.remove(i);
                i = i + 5;
                coll--;

            }


        }

        // запись её в основную таблицу
        String col1 = new String();
        String col2 = new String();
        String col3 = new String();
        String col4 = new String();
        String col5 = new String();

        i = 0;
        try {
            while (i <= coll) {
                int ln = i;
                i = i + 5;


                col1 = td.get(ln).text();
                col2 = td.get(ln + 1).text();
                col3 = td.get(ln + 2).text();
                col4 = td.get(ln + 3).text();
                col5 = td.get(ln + 4).text();

                try (Connection connection = DriverManager.getConnection(connectionURL, LoginBD, PassBD);
                     Statement statement = connection.createStatement()) {
                    statement.executeUpdate("INSERT INTO marh (Num,Staninout,Timein,Timepas,Timeout) VALUES ('" + col1 + "', '" + col2 + "', '" + col3 + "', '" + col4 + "', '" + col5 + "');");
                }


            }
        }catch (Exception e){
            int nurl = 0;
            i = 0;
            System.out.println("Парсинг в базы данных.");
            try{while (coll >= i) {
                URL url = null;
                page = null;
                //Парсер всех остальных
                String http = "https://poezdato.net";
                String urln = td.get(i).select("a").attr("href");
                i=i+5;
                urln = http+urln;
                int download = coll/5;
                int down = i/5;
                System.out.println("Прогресс: [" + down + "/" + download + "]");

                //System.out.println(urln);


                Document pagen = Jsoup.parse(new URL(urln), 3000);

                Element tablen = pagen.select("table[class=train_schedule_table stacktable desktop]").first();
                Elements linen = tablen.select("td");


                String NumSting = pagen.select("h1[class=electr_schedule]").first().text();
                //индекс и обозначение переменных
                int NumFr = NumSting.indexOf(" ");
                String NumFin = NumSting.substring(0, NumFr);
              //  int Num = Integer.parseInt(NumFin);

                int col = linen.size();
              //  System.out.println(linen);
                col= col-1;
                int ii = 0;
                while (ii <= col) {
                    int ln = ii;
                    ii = ii + 5;



                    col1 = linen.get(ln).text();
                    col2 = linen.get(ln + 1).text();
                    col3 = linen.get(ln + 2).text();
                    col4 = linen.get(ln + 3).text();
                    col5 = linen.get(ln + 4).text();

                    try (Connection connection = DriverManager.getConnection(connectionURL, LoginBD, PassBD);
                         Statement statement = connection.createStatement()) {
                        statement.executeUpdate("INSERT INTO marhfull (IDtrain,Stanin,Timein,Timew,Timeout,Timed) VALUES ('" + NumFin + "', '" + col1 + "', '" + col2 + "', '" + col3 + "', '" + col4 + "', '" + col5 + "');");
                    }


                }
            }
        }catch (Exception f){
            System.out.println("База данных обновлена, хотите узнать все маршруты? Yes/No");
            String approval = new String();
            Scanner in = new Scanner(System.in);
            approval = in.next();
            switch(approval){
                case ("No"):
                    System.out.println("Закрытие программы");
                    break;
                case ("Yes"):
                    System.out.println("Номер   Маршрут   Прибытие   Стоянка   Отправление");
                    try (Connection connection = DriverManager.getConnection(connectionURL, LoginBD, PassBD);
                         Statement statement = connection.createStatement()) {
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM marh;");
                        while (resultSet.next()){
                            int ID = resultSet.getInt(1);
                            String Num = resultSet.getString(2);
                            String Statinout = resultSet.getString(3);
                            String Timein = resultSet.getString(4);
                            String Timeout = resultSet.getString(5);
                            String Timepas = resultSet.getString(6);
                            System.out.println(Num + "  " + Statinout + "  " + Timein + "  " + Timeout + "  " + Timepas);
                        }
                    }
                    default:break;

            }
                System.out.println("Хотите узнать путь следование определённого поезда? Yes/No");
                //Scanner inn = new Scanner(System.in);
                approval = in.next();

                switch (approval){
                    case ("Yes"):
                        System.out.println("Введите номер поезда:");

                        //Scanner innn = new Scanner(System.in);
                        approval = in.next();
                        System.out.println("Номер   Станция   Прибытие   Стоянка   Отправление");

                        try (Connection connection = DriverManager.getConnection(connectionURL, LoginBD, PassBD);
                             Statement statement = connection.createStatement()) {
                            ResultSet resultSet = statement.executeQuery("SELECT * FROM marhfull WHERE IDtrain='" + approval + "';");
                            while (resultSet.next()){
                                int ID = resultSet.getInt(1);
                                String Num = resultSet.getString(2);
                                String Statinout = resultSet.getString(3);
                                String Timein = resultSet.getString(4);
                                String Timeout = resultSet.getString(5);
                                String Timepas = resultSet.getString(6);
                                System.out.println(Num + "  " + Statinout + "  " + Timein + "  " + Timeout + "  " + Timepas);
                            }

                        }


                }

            }
        }
    }
}

