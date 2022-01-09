package dev.ragnarok.fenrir.util;

import static java.lang.Character.toUpperCase;

public class Translit {

    public static String lat2cyr(String s) {
        if (Utils.isEmpty(s)) {
            return s;
        }

        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {// Идем по строке слева направо. В принципе, подходит для обработки потока
            char ch = s.charAt(i);
            boolean lc = Character.isLowerCase(ch); // для сохранения регистра
            ch = toUpperCase(ch);
            if (ch == 'J') { // Префиксная нотация вначале
                i++; // преходим ко второму символу сочетания
                ch = toUpperCase(s.charAt(i));
                switch (ch) {
                    case 'O':
                        sb.append(ch('Ё', lc));
                        break;
                    case 'H':
                        if (i + 1 < s.length() && toUpperCase(s.charAt(i + 1)) == 'H') { // проверка на постфикс (вариант JHH)
                            sb.append(ch('Ъ', lc));
                            i++; // пропускаем постфикс
                        } else {
                            sb.append(ch('Ь', lc));
                        }
                        break;
                    case 'U':
                        sb.append(ch('Ю', lc));
                        break;
                    case 'A':
                        sb.append(ch('Я', lc));
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal transliterated symbol '" + ch + "' at position " + i);
                }
            } else if (i + 1 < s.length() && toUpperCase(s.charAt(i + 1)) == 'H') {// Постфиксная нотация, требует информации о двух следующих символах. Для потока придется сделать обертку с очередью из трех символов.
                switch (ch) {
                    case 'Z':
                        sb.append(ch('Ж', lc));
                        break;
                    case 'K':
                        sb.append(ch('Х', lc));
                        break;
                    case 'C':
                        sb.append(ch('Ч', lc));
                        break;
                    case 'S':
                        if (i + 2 < s.length() && toUpperCase(s.charAt(i + 2)) == 'H') { // проверка на двойной постфикс
                            sb.append(ch('Щ', lc));
                            i++; // пропускаем первый постфикс
                        } else {
                            sb.append(ch('Ш', lc));
                        }
                        break;
                    case 'E':
                        sb.append(ch('Э', lc));
                        break;
                    case 'I':
                        sb.append(ch('Ы', lc));
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal transliterated symbol '" + ch + "' at position " + i);
                }
                i++; // пропускаем постфикс
            } else {// одиночные символы
                switch (ch) {
                    case 'A':
                        sb.append(ch('А', lc));
                        break;
                    case 'B':
                        sb.append(ch('Б', lc));
                        break;
                    case 'V':
                        sb.append(ch('В', lc));
                        break;
                    case 'G':
                        sb.append(ch('Г', lc));
                        break;
                    case 'D':
                        sb.append(ch('Д', lc));
                        break;
                    case 'E':
                        sb.append(ch('Е', lc));
                        break;
                    case 'Z':
                        sb.append(ch('З', lc));
                        break;
                    case 'I':
                        sb.append(ch('И', lc));
                        break;
                    case 'Y':
                        sb.append(ch('Й', lc));
                        break;
                    case 'K':
                        sb.append(ch('К', lc));
                        break;
                    case 'L':
                        sb.append(ch('Л', lc));
                        break;
                    case 'M':
                        sb.append(ch('М', lc));
                        break;
                    case 'N':
                        sb.append(ch('Н', lc));
                        break;
                    case 'O':
                        sb.append(ch('О', lc));
                        break;
                    case 'P':
                        sb.append(ch('П', lc));
                        break;
                    case 'R':
                        sb.append(ch('Р', lc));
                        break;
                    case 'S':
                        sb.append(ch('С', lc));
                        break;
                    case 'T':
                        sb.append(ch('Т', lc));
                        break;
                    case 'U':
                        sb.append(ch('У', lc));
                        break;
                    case 'F':
                        sb.append(ch('Ф', lc));
                        break;
                    case 'C':
                        sb.append(ch('Ц', lc));
                        break;
                    default:
                        sb.append(ch(ch, lc));
                }
            }

            i++; // переходим к следующему символу
        }
        return sb.toString();
    }

    private static String cyr2lat(char ch) {
        switch (ch) {
            case 'А':
                return "A";
            case 'Б':
                return "B";
            case 'В':
                return "V";
            case 'Г':
                return "G";
            case 'Д':
                return "D";
            case 'Е':
                return "E";
            case 'Ё':
                return "JO";
            case 'Ж':
                return "ZH";
            case 'З':
                return "Z";
            case 'И':
                return "I";
            case 'Й':
                return "Y";
            case 'К':
                return "K";
            case 'Л':
                return "L";
            case 'М':
                return "M";
            case 'Н':
                return "N";
            case 'О':
                return "O";
            case 'П':
                return "P";
            case 'Р':
                return "R";
            case 'С':
                return "S";
            case 'Т':
                return "T";
            case 'У':
                return "U";
            case 'Ф':
                return "F";
            case 'Х':
                return "KH";
            case 'Ц':
                return "C";
            case 'Ч':
                return "CH";
            case 'Ш':
                return "SH";
            case 'Щ':
                return "SHH";
            case 'Ъ':
                return "JHH";
            case 'Ы':
                return "IH";
            case 'Ь':
                return "JH";
            case 'Э':
                return "EH";
            case 'Ю':
                return "JU";
            case 'Я':
                return "JA";
            default:
                return String.valueOf(ch);
        }
    }

    public static String cyr2lat(String s) {
        if (Utils.isEmpty(s)) {
            return s;
        }

        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (char ch : s.toCharArray()) {
            char upCh = toUpperCase(ch);
            String lat = cyr2lat(upCh);
            if (ch != upCh) {
                lat = lat.toLowerCase();
            }

            sb.append(lat);
        }

        return sb.toString();
    }

    /**
     * Вспомогательная функция для восстановления регистра
     */
    private static char ch(char ch, boolean toLowerCase) {
        return toLowerCase ? Character.toLowerCase(ch) : ch;
    }

    /**
     * Пробы
     */
    public static void main(String[] args) {
        String s1 = cyr2lat("Александр Иванович Лебедь");
        String s2 = cyr2lat("Веснушчатый Щавелевый");
        String s3 = cyr2lat("Широкая электрификация южных губерний даст мощный толчок подъёму сельского хозяйства");
        String s4 = cyr2lat("Съешь же ещё этих мягких французских булок да выпей чаю.");
        String s5 = cyr2lat("А Б В Г Д Е Ё Ж З И Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ Ъ Ы Ь Э Ю Я");

        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s4);
        System.out.println(s5);
        System.out.println();
        System.out.println(lat2cyr(s1));
        System.out.println(lat2cyr(s2));
        System.out.println(lat2cyr(s3));
        System.out.println(lat2cyr(s4));
        System.out.println(lat2cyr(s5));
    }
}