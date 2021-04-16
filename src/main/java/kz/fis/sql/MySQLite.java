package kz.fis.sql;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "vacuum";
    static final String TABLE_NAME = "emergency_service";
    static final String ID = "id";
    static final String VACUUMS = "vacuums";
    static final String VACUUMS_LC = "vacuums_lc";
    static final String PRICE = "price";
    static final String ASSETS_FILE_NAME = "vacuums.txt";
    static final String DATA_SEPARATOR = "|";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + VACUUMS + " TEXT,"
                + VACUUMS_LC + " TEXT,"
                + PRICE + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addData(SQLiteDatabase db, String vacuums, String price) {
        ContentValues values = new ContentValues();
        values.put(VACUUMS, vacuums);
        values.put(VACUUMS_LC, vacuums.toLowerCase());
        values.put(PRICE, price);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String vacuums = st.nextToken().trim(); // Извлекаем из строки название организации без пробелов на концах
                    String price = st.nextToken().trim(); // Извлекаем из строки номер организации без пробелов на концах
                    addData(db, vacuums, price); // Добавляем название и телефон в базу данных
                }
            }

            // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter) {

        String selectQuery; // Переменная для SQL-запроса

        if (filter.equals("")) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + VACUUMS;
        } else {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + VACUUMS_LC + " LIKE '%" +
                    filter.toLowerCase() + "%'" +
                    " OR " + PRICE + " LIKE '%" + filter + "%'" + ") ORDER BY " + VACUUMS;
        }
        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                String vacuums = cursor.getString(1);
                String price = cursor.getString(3);
                data.append(++num).append(") ").append(vacuums).append(".\nЦена: ").append(price).append(" тг").append("\n\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}