package dev.ragnarok.fenrir.db.model.entity;


import com.google.gson.annotations.SerializedName;

public class PhotoSizeEntity {

    @SerializedName("s")
    private Size s;
    @SerializedName("m")
    private Size m;
    @SerializedName("x")
    private Size x;
    @SerializedName("o")
    private Size o;
    @SerializedName("p")
    private Size p;
    @SerializedName("q")
    private Size q;
    @SerializedName("r")
    private Size r;
    @SerializedName("y")
    private Size y;
    @SerializedName("z")
    private Size z;
    @SerializedName("w")
    private Size w;

    public Size getS() {
        return s;
    }

    public PhotoSizeEntity setS(Size s) {
        this.s = s;
        return this;
    }

    public Size getM() {
        return m;
    }

    public PhotoSizeEntity setM(Size m) {
        this.m = m;
        return this;
    }

    public Size getX() {
        return x;
    }

    public PhotoSizeEntity setX(Size x) {
        this.x = x;
        return this;
    }

    public Size getO() {
        return o;
    }

    public PhotoSizeEntity setO(Size o) {
        this.o = o;
        return this;
    }

    public Size getP() {
        return p;
    }

    public PhotoSizeEntity setP(Size p) {
        this.p = p;
        return this;
    }

    public Size getQ() {
        return q;
    }

    public PhotoSizeEntity setQ(Size q) {
        this.q = q;
        return this;
    }

    public Size getR() {
        return r;
    }

    public PhotoSizeEntity setR(Size r) {
        this.r = r;
        return this;
    }

    public Size getY() {
        return y;
    }

    public PhotoSizeEntity setY(Size y) {
        this.y = y;
        return this;
    }

    public Size getZ() {
        return z;
    }

    public PhotoSizeEntity setZ(Size z) {
        this.z = z;
        return this;
    }

    public Size getW() {
        return w;
    }

    public PhotoSizeEntity setW(Size w) {
        this.w = w;
        return this;
    }

    public static final class Size {
        @SerializedName("width")
        private int width;
        @SerializedName("height")
        private int height;
        @SerializedName("url")
        private String url;

        public int getH() {
            return height;
        }

        public Size setH(int height) {
            this.height = height;
            return this;
        }

        public int getW() {
            return width;
        }

        public Size setW(int width) {
            this.width = width;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Size setUrl(String url) {
            this.url = url;
            return this;
        }
    }
}