package pers.lonestar.pixelcanvas.utils;

public class CanvasInfo {
    private int pixelCount;
    private String canvasName;
    private int[][] pixelColor;

    public CanvasInfo(String canvasName, int pixelCount, int[][] pixelColor) {
        this.canvasName = canvasName;
        this.pixelCount = pixelCount;
        this.pixelColor = pixelColor;
    }
}
