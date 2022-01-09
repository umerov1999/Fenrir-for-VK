package dev.ragnarok.fenrir.view.mozaik;

public class MatrixCalculator {

    private final Libra libra;
    private final int count;

    public MatrixCalculator(int count, Libra libra) {
        this.count = count;
        this.libra = libra;
    }

    private static float getMaxDiff(Libra libra, int[][] variant) {
        //float[][] realRows = new float[variant.length][variant[0].length];

        //for (int i = 0; i < variant.length; i++) {
        //    for (int a = 0; a < variant[i].length; a++) {
        //        int v = variant[i][a];
//
        //       if (v == -1) {
        //            realRows[i][a] = 0;
        //        } else {
        //            realRows[i][a] = libra.getWeight(v);
        //        }
        //    }
        //}

        float[] sums = new float[variant.length];

        for (int i = 0; i < variant.length; i++) {
            sums[i] = getWeightSumm(libra, variant[i]);
        }

        //for (int i = 0; i < realRows.length; i++) {
        //    float[] rowArray = realRows[i];
        //    float sum = getSum(rowArray);
        //    sums[i] = sum;
        //}

        float average = getAverage(sums);
        float maxDiff = 0;

        for (float sum : sums) {
            float diff = Math.abs(sum - average);

            if (diff > maxDiff) {
                maxDiff = diff;
            }
        }

        return maxDiff;
    }

    private static float getWeightSumm(Libra libra, int... positions) {
        float s = 0;
        for (int position : positions) {
            if (position == -1) {
                continue;
            }

            s = s + libra.getWeight(position);
        }

        return s;
    }

    private static float getAverage(float... values) {
        float sum = 0;
        int nonZeroValuesCount = 0;

        for (float value : values) {
            sum = sum + value;

            if (value != 0) {
                nonZeroValuesCount++;
            }
        }

        return sum / (float) nonZeroValuesCount;
    }

    /**
     * Clones the provided array
     *
     * @param src
     * @return a new clone of the provided array
     */
    private static int[][] cloneArray(int[][] src) {
        int length = src.length;
        int[][] target = new int[length][src[0].length];

        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }

        return target;
    }

    /**
     * Можно ли переместить последний елемент субмассива data по индексу row на следующую строку
     *
     * @param row
     * @param data
     * @return
     */
    private static boolean canMoveToNext(int row, int[][] data) {
        // можно только в том случае, если в строке есть хотябы 2 валидных значения
        // и с главном массиве есть следующая строка после row
        return data[row][1] != -1 && data.length > row + 1;
    }

    /**
     * Переместить последний елемент из строки с индексом row на следующую
     */
    private static void move(int row, int[][] data) {
        //if(data.length < row){
        //    throw new IllegalArgumentException();
        //}

        int[] rowArray = data[row];

        int[] nextRowArray = data[row + 1];

        if (nextRowArray[nextRowArray.length - 1] != -1) {
            move(row + 1, data);
        }

        int moveIndex = getLastNoNegativeIndex(rowArray);
        //if(moveIndex == -1){
        //    throw new IllegalStateException();
        //}

        int value = rowArray[moveIndex];

        shiftByOneToRight(nextRowArray);

        nextRowArray[0] = value;
        rowArray[moveIndex] = -1;
    }

    /*private static float getSum(float... values) {
        float s = 0;
        for (float f : values) {
            s = s + f;
        }

        return s;
    }*/

    /**
     * Сдвинуть все значение на 1 вправо, значение первого елемента будет заменено на -1
     *
     * @param array
     */
    private static void shiftByOneToRight(int[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (i == 0) {
                array[i] = -1;
            } else {
                array[i] = array[i - 1];
            }
        }
    }

    /**
     * Получить индекс последнего елемента, чье значение не равно -1
     *
     * @param array
     * @return
     */
    private static int getLastNoNegativeIndex(int[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] != -1) {
                return i;
            }
        }

        return -1;
    }

    private void analize(int[][] matrix, Result target) {
        float maxDiff = getMaxDiff(libra, matrix);

        if (maxDiff < target.minDiff || target.matrix == null) {
            target.minDiff = maxDiff;
            target.matrix = cloneArray(matrix);
        }
    }

    public int[][] calculate(int rows) {
        Result result = checkAllVariants(rows);
        return result.matrix;
    }

    private Result checkAllVariants(int rowsCount) {
        Result result = new Result();

        int[][] rows = new int[rowsCount][count];

        for (int i = rowsCount - 1; i >= 0; i--) {
            int[] array = new int[count];

            for (int a = 0; a < count; a++) {
                array[a] = -1;
            }

            rows[i] = array;
        }

        int forFirst = count - rowsCount;

        for (int i = 0; i < count; i++) {
            boolean toFirst = i < forFirst + 1;
            rows[toFirst ? 0 : i - forFirst][toFirst ? i : 0] = i;
        }

        doShuffle(rows, result);
        return result;
    }

    private void doShuffle(int[][] data, Result result) {
        analize(data, result);

        moveAll(data, 0, result);
    }

    private void moveAll(int[][] data, int startFromIndex, Result result) {
        while (canMoveToNext(startFromIndex, data)) {
            move(startFromIndex, data);

            analize(data, result);

            if (startFromIndex + 1 < data.length - 1) {
                moveAll(cloneArray(data), startFromIndex + 1, result);
            }
        }
    }

    public interface Libra {
        float getWeight(int index);
    }

    private static class Result {

        float minDiff = Float.MAX_VALUE;

        int[][] matrix;

    }
}
