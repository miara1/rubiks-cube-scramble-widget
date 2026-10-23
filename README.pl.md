# Rubik's Cube Scramble Widget — instrukcja po polsku

Prosty widget na Windows: wąski biały pasek w prawym dolnym rogu, jedna linia scrambla 3×3 i mały przycisk **↻**. Pasek ma 440 × 28 pikseli logicznych, czcionkę 12 pkt, która zmniejsza się w razie potrzeby, żeby nie ucinać tekstu. Scramble typu random-state generuje **TNoodle-lib**. Gotowa aplikacja działa całkowicie lokalnie i zawiera własne środowisko Java.

![Wygląd widgetu](docs/screenshot.png)

## Uruchomienie

1. Pobierz paczkę `windows-x64.zip` z sekcji **Releases** repozytorium.
2. Rozpakuj całą paczkę do wybranego folderu.
3. Uruchom **RubiksCubeScrambleWidget.exe**.

Wymagany Windows 10 lub 11 w wersji 64-bitowej. Zachowaj obok programu foldery `app` i `runtime`. Nie musisz instalować Javy ani uruchamiać aplikacji jako administrator. Wersja nie ma podpisu cyfrowego; Windows może pokazać ostrzeżenie o nieznanym wydawcy.

## Obsługa

| Działanie | Skrót / przycisk |
| --- | --- |
| Pokaż / schowaj widget | **Ctrl + Shift + Spacja** |
| Nowy scramble | **Ctrl + Shift + F9** lub **↻** |
| Menu programu | Prawy przycisk na ikonie kostki w zasobniku |
| Pokaż / schowaj przez zasobnik | Podwójne kliknięcie ikony kostki |
| Autostart | **Start with Windows** w menu zasobnika |
| Zakończ program | **Quit** w menu zasobnika |

Panel pojawia się nad zwykłymi oknami, nad paskiem zadań, na monitorze z kursorem myszy. Nie przejmuje klawiatury. Schowanie zachowuje aktualny scramble. Skrót nowego scrambla działa również przy schowanym panelu, bez pokazywania go.

Autostart jest domyślnie wyłączony. Po włączeniu aplikacja startuje schowana przy logowaniu. Wyłącz autostart przed przeniesieniem lub usunięciem folderu programu. Po przeniesieniu możesz włączyć go ponownie.

## Zmiana skrótów

Wybierz **Edit shortcuts (restart to apply)** w menu zasobnika. Ustawienia znajdują się w:

```text
%APPDATA%\RubiksCubeScrambleWidget\settings.properties
```

```properties
toggle=Ctrl+Shift+Space
next=Ctrl+Shift+F9
```

Po zmianie zakończ program przez **Quit** i uruchom go ponownie. Dostępne klawisze i ograniczenia opisano w [angielskim README](README.md#change-shortcuts). Unikaj `Ctrl+Alt` z literami, bo może kolidować z polskimi znakami. Jeśli skrót jest zajęty, program pokaże komunikat; menu zasobnika nadal działa.

Log `widget.log` znajduje się obok ustawień i pozostaje na komputerze. Usunięcie pliku ustawień przywróci domyślne skróty przy kolejnym starcie.

## Budowanie projektu

Zainstaluj 64-bitowy JDK 21 i uruchom **build.cmd**. Skrypt wykona testy, zbuduje program i przygotuje paczki w `dist`. Pierwsze budowanie wymaga internetu; gotowa aplikacja go nie potrzebuje. **run.cmd** uruchamia lokalnie zbudowany program.

To narzędzie do treningu. Do oficjalnych zawodów WCA należy używać oficjalnego programu i procedur WCA.

Kod projektu: **GPL-3.0-only**. Szczegóły budowania, zależności i publikowania: [README.md](README.md).
