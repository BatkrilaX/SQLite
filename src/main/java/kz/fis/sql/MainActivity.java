package kz.fis.sql;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    static final String FILTER = "FILTER"; // Имя параметра для сохранения при переворачивании экрана
    static final String CONFIG_FILE_NAME = "Config";
    static final String FONT_SIZE = "FontSize";
    final MySQLite db = new MySQLite(this); // Класс работы с базой данных
    private final int LARGE_FONT = 16;
    private final int SMALL_FONT = 12;
    EditText editText;
    TextView textView;
    String filter = ""; // Фильтр поиска
    SharedPreferences sPref; // Класс для работы с настройками программы
    private int fontSize = SMALL_FONT;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(FILTER, filter);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);

        textView.setKeyListener(null);

        // Чтение сохраненной настройки размера шрифта из параметров приложения
        sPref = getSharedPreferences(CONFIG_FILE_NAME, MODE_PRIVATE);
        fontSize = sPref.getInt(FONT_SIZE, SMALL_FONT);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize); // Установка начально размера шрифта
        textView.requestFocus(); // Передача фокуса на комонент чтобы закрылось окно ввода у "editText"

        // Восстановление фильтра после переворота экрана
        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getString(FILTER));
        }

        textView.setText(R.string.Загрузка_данных);

        // Обработчик изменения текста в компоненте "editText"
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                // Сделаем поиск данных в другом потоке
                new Thread(() -> {
                    filter = editText.getText().toString().trim();
                    final String data = db.getData(filter);
                    // Сделаем вывод результата синхронно с основным потоком
                    textView.post(() -> textView.setText(data));
                }).start();

            }

        });

        // Инициализация начального поиска (показать все записи)
        editText.post(() -> editText.setText(filter));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Установка правильного отображения пункта выбора крупного шрифта
        menu.findItem(R.id.large_font).setChecked(fontSize == LARGE_FONT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.large_font) {
            item.setChecked(!item.isChecked());
            int size = item.isChecked() ? LARGE_FONT : SMALL_FONT;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            fontSize = size;
            return true;
        }
        if (id == R.id.exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Сохранение размера шрифта в настройках программы
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(FONT_SIZE, fontSize);
        ed.apply();
    }
}
