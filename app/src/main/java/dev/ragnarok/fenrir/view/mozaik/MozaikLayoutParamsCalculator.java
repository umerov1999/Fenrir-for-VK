package dev.ragnarok.fenrir.view.mozaik;

import android.util.SparseIntArray;

import java.util.List;

import dev.ragnarok.fenrir.adapter.PostImage;

public class MozaikLayoutParamsCalculator {

    private final int maxWidth;
    private final int spacing;
    private final int[][] matrix;

    private final SparseIntArray rowHeight;
    private final SparseIntArray photoWidth;
    private final List<PostImage> images;

    public MozaikLayoutParamsCalculator(int[][] matrix, List<PostImage> images, int maxWidth, int spacing) {
        this.maxWidth = maxWidth;
        this.spacing = spacing;
        this.matrix = matrix;
        this.images = images;
        rowHeight = new SparseIntArray(1);
        photoWidth = new SparseIntArray(1);
    }

    private static int getRowNumberForIndex(int[][] array, int index) {
        for (int i = 0; i < array.length; i++) {
            int[] inner = array[i];

            for (int a : inner) {
                if (a == index) {
                    return i;
                }
            }
        }

        throw new IllegalStateException("Value does not exist");
    }

    private static int getColumnNumberForIndex(int[][] array, int index) {
        for (int[] inner : array) {
            for (int i = 0; i < inner.length; i++) {
                if (inner[i] == index) {
                    return i;
                }
            }
        }

        throw new IllegalStateException("Value does not exist");
    }

    private float getAspectRatioSumForRow(int row) {
        float sum = 0;
        int[] rowArray = matrix[row];

        for (int index : rowArray) {
            if (index == -1) {
                break;
            }

            sum = sum + images.get(index).getAspectRatio();
        }

        return sum;
    }

    public PostImagePosition getPostImagePosition(int index) {
        PostImage photo = images.get(index);

        int rowNumber = getRowNumberForIndex(matrix, index);
        int numberInrow = getColumnNumberForIndex(matrix, index);

        double propotrionRowSum = getAspectRatioSumForRow(rowNumber);

        double currentPhotoProportion = photo.getAspectRatio();

        double coeficien = currentPhotoProportion / propotrionRowSum;

        int width = (int) ((double) maxWidth * coeficien);
        int height = (int) ((double) photo.getHeight() * ((double) width / (double) photo.getWidth()));

        int marginLeft = 0;

        int firstIndexInRow = index - numberInrow;

        for (int i = firstIndexInRow; i < index; i++) {
            marginLeft = marginLeft + photoWidth.get(i) + spacing;
        }

        int marginTop = 0;
        for (int i = 0; i < rowNumber; i++) {
            marginTop = marginTop + rowHeight.get(i) + spacing;
        }

        PostImagePosition position = new PostImagePosition();
        position.sizeY = height;
        position.sizeX = width;
        position.marginX = marginLeft;
        position.marginY = marginTop;

        photoWidth.put(index, width);
        rowHeight.put(rowNumber, height);
        return position;
    }
}
